package com.transitshield.backend.service;

import com.transitshield.backend.dto.*;
import com.transitshield.backend.entity.*;
import com.transitshield.backend.entity.enums.BoardingDetectMethod;
import com.transitshield.backend.entity.enums.ExtensionStatus;
import com.transitshield.backend.entity.enums.PaymentStatus;
import com.transitshield.backend.entity.enums.TripStatus;
import com.transitshield.backend.exception.BadRequestException;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassengerTripService {

    private final PassengerTripRepository tripRepository;
    private final FareRuleRepository fareRuleRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final StopRepository stopRepository;
    private final TripExtensionRepository tripExtensionRepository;
    private final RouteVariantStopRepository routeVariantStopRepository;
    private final RewardService rewardService;

    public Double previewFare(FarePreviewRequest request) {
        return calculateFare(request.getRouteVariantId(), request.getBoardingStopId(), request.getDestinationStopId());
    }

    @Transactional
    public PassengerTripDto startTrip(TripStartRequest request) {
        PassengerProfile profile = resolvePassengerProfile(request.getPassengerProfileId());
        BusAssignment assignment = busAssignmentRepository.findById(request.getBusAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus assignment not found"));
        Stop boardingStop = stopRepository.findById(request.getBoardingStopId())
                .orElseThrow(() -> new ResourceNotFoundException("Boarding stop not found"));
        if (request.getSelectedDestinationStopId() == null) {
            throw new BadRequestException("Destination stop is required");
        }

        Stop destinationStop = stopRepository.findById(request.getSelectedDestinationStopId())
                .orElseThrow(() -> new ResourceNotFoundException("Destination stop not found"));

        tripRepository.findByPassengerProfileIdAndTripStatus(profile.getId(), TripStatus.ACTIVE)
                .ifPresent(existingTrip -> {
                    throw new BadRequestException("Passenger already has an active trip");
                });

        Double baseFare = calculateFare(assignment.getRouteVariant().getId(), boardingStop.getId(), destinationStop.getId());
        Double walletBalance = profile.getWalletBalance() != null ? profile.getWalletBalance() : 0.0;
        if (walletBalance < baseFare) {
            throw new BadRequestException("Insufficient wallet balance");
        }
        profile.setWalletBalance(walletBalance - baseFare);
        passengerProfileRepository.save(profile);

        PassengerTrip trip = new PassengerTrip();
        trip.setTripRef(UUID.randomUUID().toString());
        trip.setPassengerProfile(profile);
        trip.setBusAssignment(assignment);
        trip.setQrTokenUsed(request.getQrTokenUsed());
        trip.setBoardingStop(boardingStop);
        trip.setSelectedDestinationStop(destinationStop);
        trip.setBoardingDetectMethod(BoardingDetectMethod.QR_SCAN);
        trip.setBaseFareLkr(baseFare);
        trip.setTotalFareLkr(baseFare);
        trip.setPaymentStatus(PaymentStatus.PAID);
        trip.setTripStatus(TripStatus.ACTIVE);
        trip.setTicketVerified(false);
        trip.setCreatedAt(LocalDateTime.now());
        
        trip = tripRepository.save(trip);
        return mapToDto(trip);
    }

    public PassengerTripDto extendTrip(TripExtendRequest request) {
        PassengerTrip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));
        
        if (trip.getTripStatus() != TripStatus.ACTIVE) {
            throw new BadRequestException("Only active trips can be extended");
        }

        Stop newDestination = stopRepository.findById(request.getNewDestinationStopId())
                .orElseThrow(() -> new ResourceNotFoundException("New stop not found"));

        // Reject if new destination is same or earlier stop order
        List<RouteVariantStop> rvsList = routeVariantStopRepository.findByRouteVariantIdOrderByStopOrderAsc(trip.getBusAssignment().getRouteVariant().getId());
        Integer previousOrder = -1;
        Integer newOrder = -1;

        for (RouteVariantStop rvs : rvsList) {
            if (trip.getSelectedDestinationStop() != null && rvs.getStop().getId().equals(trip.getSelectedDestinationStop().getId())) {
                previousOrder = rvs.getStopOrder();
            }
            if (rvs.getStop().getId().equals(newDestination.getId())) {
                newOrder = rvs.getStopOrder();
            }
        }

        if (previousOrder != -1 && newOrder <= previousOrder) {
            throw new BadRequestException("New destination must be further along the route than the original destination");
        }

        Double newFare = calculateFare(trip.getBusAssignment().getRouteVariant().getId(), trip.getBoardingStop().getId(), newDestination.getId());
        Double additionalFare = newFare > trip.getTotalFareLkr() ? newFare - trip.getTotalFareLkr() : 0.0;

        TripExtension extension = new TripExtension();
        extension.setPassengerTrip(trip);
        extension.setPreviousDestinationStop(trip.getSelectedDestinationStop());
        extension.setNewDestinationStop(newDestination);
        extension.setAdditionalFareLkr(additionalFare);
        extension.setExtensionStatus(ExtensionStatus.APPROVED);
        extension.setCreatedAt(LocalDateTime.now());
        tripExtensionRepository.save(extension);

        trip.setSelectedDestinationStop(newDestination);
        trip.setExtraFareLkr(trip.getExtraFareLkr() + additionalFare);
        trip.setTotalFareLkr(trip.getBaseFareLkr() + trip.getExtraFareLkr());
        trip = tripRepository.save(trip);
        
        return mapToDto(trip);
    }

    public PassengerTripDto endTrip(TripEndRequest request) {
        PassengerTrip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (trip.getTripStatus() == TripStatus.COMPLETED) {
            throw new BadRequestException("Trip already completed");
        }

        Stop actualExit = null;
        if(request.getActualExitStopId() != null) {
            actualExit = stopRepository.findById(request.getActualExitStopId())
                    .orElseThrow(() -> new ResourceNotFoundException("Exit stop not found"));
        }

        trip.setActualExitStop(actualExit);
        trip.setTripStatus(TripStatus.COMPLETED);
        trip.setEndedAt(LocalDateTime.now());
        
        // No recalculation of fare here per requirements. Only through /extend.
        
        // Handle payment logic normally here (deduct from wallet if implemented)
        trip.setPaymentStatus(PaymentStatus.PAID);
        
        trip = tripRepository.save(trip);

        // Award points based on base fare safely (e.g. 100 LKR = 1 point)
        if (trip.getTotalFareLkr() != null && trip.getTotalFareLkr() > 0) {
            Double pointsEarned = trip.getTotalFareLkr() / 100.0;
            rewardService.earnPointsFromTrip(trip.getPassengerProfile(), trip, pointsEarned);
        }

        return mapToDto(trip);
    }

    public PassengerTripDto getActiveTrip(Long passengerId) {
        Long passengerProfileId = resolvePassengerProfileId(passengerId);
        return tripRepository.findByPassengerProfileIdAndTripStatus(passengerProfileId, TripStatus.ACTIVE)
                .map(this::mapToDto).orElseThrow(() -> new ResourceNotFoundException("No active trip found"));
    }

    public List<PassengerTripDto> getTripHistory(Long passengerId) {
        Long passengerProfileId = resolvePassengerProfileId(passengerId);
        return tripRepository.findByPassengerProfileId(passengerProfileId).stream()
                .filter(t -> t.getTripStatus() == TripStatus.COMPLETED)
                .map(this::mapToDto).collect(Collectors.toList());
    }

    public PassengerBalanceDto getPassengerBalance(Long passengerId) {
        PassengerProfile profile = resolvePassengerProfile(passengerId);
        PassengerBalanceDto dto = new PassengerBalanceDto();
        dto.setFullName(profile.getUser() != null ? profile.getUser().getFullName() : null);
        dto.setPublicUserId(profile.getPublicUserId());
        dto.setWalletBalance(profile.getWalletBalance());
        dto.setTotalPoints(profile.getTotalPoints());
        return dto;
    }

    private Double calculateFare(Long variantId, Long originId, Long destId) {
        FareRule rule = fareRuleRepository.findByRouteVariantIdAndBoardingStopIdAndDestinationStopId(variantId, originId, destId)
                .orElseThrow(() -> new BadRequestException("No fare rule exists for the specified route and stops"));
        return rule.getFareLkr();
    }

    private PassengerTripDto mapToDto(PassengerTrip t) {
        PassengerTripDto dto = new PassengerTripDto();
        dto.setId(t.getId());
        dto.setTripRef(t.getTripRef());
        dto.setPassengerProfileId(t.getPassengerProfile().getId());
        dto.setBusAssignmentId(t.getBusAssignment().getId());
        dto.setBusId(t.getBusAssignment().getBus() != null ? t.getBusAssignment().getBus().getId() : null);
        dto.setBusDisplayName(t.getBusAssignment().getBus() != null ? t.getBusAssignment().getBus().getBusDisplayName() : null);
        dto.setRouteVariantId(t.getBusAssignment().getRouteVariant() != null ? t.getBusAssignment().getRouteVariant().getId() : null);
        dto.setRouteName(buildRouteName(t.getBusAssignment()));
        dto.setDriverName(t.getBusAssignment().getDriverProfile() != null && t.getBusAssignment().getDriverProfile().getUser() != null
                ? t.getBusAssignment().getDriverProfile().getUser().getFullName()
                : null);
        dto.setQrTokenUsed(t.getQrTokenUsed());
        if(t.getBoardingStop() != null) {
            dto.setBoardingStopId(t.getBoardingStop().getId());
            dto.setBoardingStopName(t.getBoardingStop().getStopName());
        }
        dto.setBoardingDetectMethod(t.getBoardingDetectMethod());
        if(t.getSelectedDestinationStop() != null) {
            dto.setSelectedDestinationStopId(t.getSelectedDestinationStop().getId());
            dto.setSelectedDestinationStopName(t.getSelectedDestinationStop().getStopName());
        }
        if(t.getActualExitStop() != null) dto.setActualExitStopId(t.getActualExitStop().getId());
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

    private PassengerProfile resolvePassengerProfile(Long passengerId) {
        return passengerProfileRepository.findByUserId(passengerId)
                .or(() -> passengerProfileRepository.findById(passengerId))
                .orElseThrow(() -> new ResourceNotFoundException("Passenger not found"));
    }

    private Long resolvePassengerProfileId(Long passengerId) {
        return resolvePassengerProfile(passengerId).getId();
    }

    private String buildRouteName(BusAssignment assignment) {
        if (assignment == null || assignment.getRouteVariant() == null) {
            return null;
        }

        RouteVariant routeVariant = assignment.getRouteVariant();
        if (routeVariant.getRoute() == null) {
            return routeVariant.getVariantCode();
        }

        String routeNumber = routeVariant.getRoute().getRouteNumber();
        String displayName = routeVariant.getRoute().getDisplayName();
        if (routeNumber == null || routeNumber.isBlank()) {
            return displayName;
        }
        return routeNumber + " - " + displayName;
    }
}
