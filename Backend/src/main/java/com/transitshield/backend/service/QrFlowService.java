package com.transitshield.backend.service;

import com.transitshield.backend.dto.BusQrCodeDto;
import com.transitshield.backend.dto.QrScanRequest;
import com.transitshield.backend.dto.QrScanResponse;
import com.transitshield.backend.dto.StopDto;
import com.transitshield.backend.entity.*;
import com.transitshield.backend.entity.enums.AssignmentStatus;
import com.transitshield.backend.entity.enums.PaymentStatus;
import com.transitshield.backend.entity.enums.TripStatus;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QrFlowService {

    private final BusQrCodeRepository qrCodeRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final RouteVariantStopRepository routeVariantStopRepository;
    private final PassengerTripRepository passengerTripRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final BusRepository busRepository;

    /**
     * Admin generates a QR code for a specific bus.
     * Deactivates any existing active QR for that bus first (one-active-per-bus).
     */
    @Transactional
    public BusQrCodeDto generateQrForBus(Long busId) {
        Bus bus = busRepository.findById(busId)
                .orElseThrow(() -> new ResourceNotFoundException("Bus not found with ID: " + busId));

        // Deactivate all existing active QRs for this bus
        List<BusQrCode> existingActive = qrCodeRepository.findAllByBusIdAndIsActiveTrue(busId);
        for (BusQrCode existing : existingActive) {
            existing.setIsActive(false);
            qrCodeRepository.save(existing);
        }

        // Create new QR code
        BusQrCode newQr = new BusQrCode();
        newQr.setBus(bus);
        newQr.setQrToken(UUID.randomUUID().toString());
        newQr.setQrLabel("QR-" + bus.getBusCode());
        newQr.setIsActive(true);
        newQr = qrCodeRepository.save(newQr);

        return mapToDto(newQr);
    }

    /**
     * Get the current active QR for a specific bus.
     */
    public BusQrCodeDto getActiveQrForBus(Long busId) {
        BusQrCode qr = qrCodeRepository.findByBusIdAndIsActiveTrue(busId)
                .orElse(null);
        return qr != null ? mapToDto(qr) : null;
    }

    public QrScanResponse scanQr(QrScanRequest request) {
        // 1. Find active QrCode
        BusQrCode qrCode = qrCodeRepository.findByQrTokenAndIsActiveTrue(request.getQrToken())
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or inactive QR code"));

        // 2 & 3. Find the latest active assignment for this bus.
        // Using the most recent assignment avoids stale/duplicate ACTIVE rows breaking QR boarding.
        BusAssignment activeAssignment = busAssignmentRepository
                .findFirstByBusIdAndAssignmentStatusOrderByStartedAtDesc(
                        qrCode.getBus().getId(),
                        AssignmentStatus.ACTIVE
                )
                .orElseThrow(() -> new ResourceNotFoundException("No active assignment found for this bus"));

        if (activeAssignment.getRouteVariant() == null) {
            throw new ResourceNotFoundException("No route is assigned to the current bus");
        }

        // 4 & 5. Load RouteVariantStops
        List<RouteVariantStop> stops = routeVariantStopRepository
                .findByRouteVariantIdOrderByStopOrderAsc(activeAssignment.getRouteVariant().getId());
        if (stops.isEmpty()) {
            throw new ResourceNotFoundException("No route stops configured for the current bus");
        }

        // 6. Check if passenger has active trip
        Long passengerProfileId = resolvePassengerProfileId(request.getPassengerId());
        Optional<PassengerTrip> activeTrip = passengerTripRepository
                .findByPassengerProfileIdAndTripStatus(passengerProfileId, TripStatus.ACTIVE);
        if (activeTrip.isPresent() && activeTrip.get().getBusAssignment().getId().equals(activeAssignment.getId())) {
            QrScanResponse response = new QrScanResponse();
            response.setMessage("Active trip already exists for this assignment");
            response.setBusAssignmentId(activeAssignment.getId());
            return response;
        }

        // GPS is not required for QR boarding. Use the first route stop when present,
        // otherwise fall back to a known stop ID so the trip can still start.
        Long nearestStopId = stops.isEmpty() ? 1L : stops.get(0).getStop().getId();

        QrScanResponse response = new QrScanResponse();
        response.setMessage("Ready to start trip");
        response.setBusId(qrCode.getBus().getId());
        response.setBusDisplayName(qrCode.getBus().getBusDisplayName());
        response.setDriverProfileId(activeAssignment.getDriverProfile().getId());
        response.setRouteVariantId(activeAssignment.getRouteVariant().getId());
        response.setBusAssignmentId(activeAssignment.getId());
        response.setNearestBoardingStopId(nearestStopId);
        
        response.setOrderedStops(stops.stream().map(this::mapStopToDto).collect(Collectors.toList()));
        return response;
    }

    @Transactional
    public PassengerTripDto verifyTicket(String tripRef) {
        PassengerTrip trip = passengerTripRepository.findByTripRef(tripRef)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        if (trip.getPaymentStatus() != PaymentStatus.PAID) {
            throw new ResourceNotFoundException("Passenger ticket has not been paid");
        }

        if (trip.getTripStatus() != TripStatus.ACTIVE) {
            throw new ResourceNotFoundException("Passenger trip is not active");
        }

        trip.setTicketVerified(true);
        if (trip.getTicketVerifiedAt() == null) {
            trip.setTicketVerifiedAt(LocalDateTime.now());
        }
        trip = passengerTripRepository.save(trip);
        return mapTripToDto(trip);
    }

    private Long resolvePassengerProfileId(Long passengerId) {
        if (passengerId == null) {
            throw new ResourceNotFoundException("Passenger ID is required");
        }

        return passengerProfileRepository.findByUserId(passengerId)
                .or(() -> passengerProfileRepository.findById(passengerId))
                .map(PassengerProfile::getId)
                .orElseThrow(() -> new ResourceNotFoundException("Passenger profile not found for ID: " + passengerId));
    }

    private StopDto mapStopToDto(RouteVariantStop rvs) {
        StopDto dto = new StopDto();
        dto.setId(rvs.getStop().getId());
        dto.setStopName(rvs.getStop().getStopName());
        dto.setStopCode(rvs.getStop().getStopCode());
        dto.setLatitude(rvs.getStop().getLatitude());
        dto.setLongitude(rvs.getStop().getLongitude());
        dto.setStopOrder(rvs.getStopOrder());
        dto.setDistanceFromStartKm(rvs.getDistanceFromStartKm());
        dto.setIsMajorStop(rvs.getIsMajorStop());
        return dto;
    }

    private BusQrCodeDto mapToDto(BusQrCode qr) {
        BusQrCodeDto dto = new BusQrCodeDto();
        dto.setId(qr.getId());
        dto.setBusId(qr.getBus().getId());
        dto.setQrToken(qr.getQrToken());
        dto.setQrLabel(qr.getQrLabel());
        dto.setIsActive(qr.getIsActive());
        return dto;
    }

    private PassengerTripDto mapTripToDto(PassengerTrip t) {
        PassengerTripDto dto = new PassengerTripDto();
        dto.setId(t.getId());
        dto.setTripRef(t.getTripRef());
        dto.setPassengerProfileId(t.getPassengerProfile().getId());
        dto.setBusAssignmentId(t.getBusAssignment().getId());
        dto.setBusId(t.getBusAssignment().getBus() != null ? t.getBusAssignment().getBus().getId() : null);
        dto.setBusDisplayName(t.getBusAssignment().getBus() != null ? t.getBusAssignment().getBus().getBusDisplayName() : null);
        dto.setRouteVariantId(t.getBusAssignment().getRouteVariant() != null ? t.getBusAssignment().getRouteVariant().getId() : null);
        dto.setRouteName(t.getBusAssignment().getRouteVariant() != null && t.getBusAssignment().getRouteVariant().getRoute() != null
                ? t.getBusAssignment().getRouteVariant().getRoute().getRouteNumber() + " - " + t.getBusAssignment().getRouteVariant().getRoute().getDisplayName()
                : null);
        dto.setDriverName(t.getBusAssignment().getDriverProfile() != null && t.getBusAssignment().getDriverProfile().getUser() != null
                ? t.getBusAssignment().getDriverProfile().getUser().getFullName()
                : null);
        dto.setQrTokenUsed(t.getQrTokenUsed());
        if (t.getBoardingStop() != null) {
            dto.setBoardingStopId(t.getBoardingStop().getId());
            dto.setBoardingStopName(t.getBoardingStop().getStopName());
        }
        dto.setBoardingDetectMethod(t.getBoardingDetectMethod());
        if (t.getSelectedDestinationStop() != null) {
            dto.setSelectedDestinationStopId(t.getSelectedDestinationStop().getId());
            dto.setSelectedDestinationStopName(t.getSelectedDestinationStop().getStopName());
        }
        if (t.getActualExitStop() != null) {
            dto.setActualExitStopId(t.getActualExitStop().getId());
        }
        dto.setBaseFareLkr(t.getBaseFareLkr());
        dto.setExtraFareLkr(t.getExtraFareLkr());
        dto.setTotalFareLkr(t.getTotalFareLkr());
        dto.setWalletBalanceAfterPayment(t.getPassengerProfile() != null ? t.getPassengerProfile().getWalletBalance() : null);
        dto.setPaymentStatus(t.getPaymentStatus());
        dto.setTripStatus(t.getTripStatus());
        dto.setTicketVerified(t.getTicketVerified());
        dto.setTicketVerifiedAt(t.getTicketVerifiedAt());
        dto.setCreatedAt(t.getCreatedAt());
        dto.setEndedAt(t.getEndedAt());
        return dto;
    }
}
