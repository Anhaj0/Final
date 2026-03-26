package com.transitshield.backend.service;

import com.transitshield.backend.dto.FarePreviewRequest;
import com.transitshield.backend.dto.PassengerTripDto;
import com.transitshield.backend.dto.TripEndRequest;
import com.transitshield.backend.dto.TripExtendRequest;
import com.transitshield.backend.dto.TripStartRequest;
import com.transitshield.backend.entity.BusAssignment;
import com.transitshield.backend.entity.PassengerProfile;
import com.transitshield.backend.entity.PassengerTrip;
import com.transitshield.backend.entity.RouteVariantStop;
import com.transitshield.backend.entity.Stop;
import com.transitshield.backend.entity.User;
import com.transitshield.backend.entity.enums.BoardingDetectMethod;
import com.transitshield.backend.entity.enums.ExtensionStatus;
import com.transitshield.backend.entity.enums.PaymentStatus;
import com.transitshield.backend.entity.enums.TripStatus;
import com.transitshield.backend.exception.BadRequestException;
import com.transitshield.backend.exception.ResourceNotFoundException;
import com.transitshield.backend.repository.BusAssignmentRepository;
import com.transitshield.backend.repository.BusQrCodeRepository;
import com.transitshield.backend.repository.FareRuleRepository;
import com.transitshield.backend.repository.PassengerProfileRepository;
import com.transitshield.backend.repository.PassengerTripRepository;
import com.transitshield.backend.repository.RouteVariantStopRepository;
import com.transitshield.backend.repository.StopRepository;
import com.transitshield.backend.repository.TripExtensionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PassengerTripService {

    private static final String TRIP_QR_PREFIX = "TS_TRIP:";

    private final PassengerTripRepository tripRepository;
    private final FareRuleRepository fareRuleRepository;
    private final PassengerProfileRepository passengerProfileRepository;
    private final BusAssignmentRepository busAssignmentRepository;
    private final BusQrCodeRepository busQrCodeRepository;
    private final StopRepository stopRepository;
    private final TripExtensionRepository tripExtensionRepository;
    private final RouteVariantStopRepository routeVariantStopRepository;
    private final RewardService rewardService;

    public Double previewFare(FarePreviewRequest request) {
        return calculateFare(
                request.getRouteVariantId(),
                request.getBoardingStopId(),
                request.getDestinationStopId()
        );
    }

    public PassengerTripDto startTrip(User user, TripStartRequest request) {
        PassengerProfile profile = resolvePassengerProfile(user);

        if (tripRepository.findByPassengerProfileIdAndTripStatus(profile.getId(), TripStatus.ACTIVE).isPresent()) {
            throw new BadRequestException("You already have an active trip");
        }

        BusAssignment assignment = busAssignmentRepository.findById(request.getBusAssignmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Bus assignment not found"));

        if (request.getQrTokenUsed() == null || request.getQrTokenUsed().isBlank()) {
            throw new BadRequestException("A scanned bus QR token is required to start a trip");
        }

        busQrCodeRepository.findByQrTokenAndIsActiveTrue(request.getQrTokenUsed().trim())
                .filter(qr -> qr.getBus() != null
                        && assignment.getBus() != null
                        && qr.getBus().getId().equals(assignment.getBus().getId()))
                .orElseThrow(() -> new BadRequestException("Scanned QR is invalid for this bus assignment"));

        Stop boardingStop = stopRepository.findById(request.getBoardingStopId())
                .orElseThrow(() -> new ResourceNotFoundException("Boarding stop not found"));

        Stop destinationStop = request.getSelectedDestinationStopId() != null
                ? stopRepository.findById(request.getSelectedDestinationStopId())
                    .orElseThrow(() -> new ResourceNotFoundException("Destination stop not found"))
                : null;

        validateStopBelongsToAssignment(assignment.getRouteVariant().getId(), boardingStop.getId(), "Boarding stop");
        if (destinationStop != null) {
            validateStopBelongsToAssignment(assignment.getRouteVariant().getId(), destinationStop.getId(), "Destination stop");
        }

        Double baseFare = 0.0;
        if (destinationStop != null) {
            baseFare = calculateFare(
                    assignment.getRouteVariant().getId(),
                    boardingStop.getId(),
                    destinationStop.getId()
            );
        }

        PassengerTrip trip = new PassengerTrip();
        trip.setTripRef(UUID.randomUUID().toString());
        trip.setPassengerProfile(profile);
        trip.setBusAssignment(assignment);
        trip.setQrTokenUsed(request.getQrTokenUsed());
        trip.setBoardingStop(boardingStop);
        trip.setSelectedDestinationStop(destinationStop);
        trip.setBoardingDetectMethod(BoardingDetectMethod.MANUAL);
        trip.setBaseFareLkr(baseFare);
        trip.setExtraFareLkr(0.0);
        trip.setTotalFareLkr(baseFare);
        trip.setPaymentStatus(PaymentStatus.PAID);
        trip.setTripStatus(TripStatus.ACTIVE);
        trip.setCreatedAt(LocalDateTime.now());
        trip.setDriverValidatedAt(null);
        trip.setIsVerified(false);
        trip.setDestinationStop(destinationStop != null ? destinationStop.getStopName() : null);

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

        validateStopBelongsToAssignment(
                trip.getBusAssignment().getRouteVariant().getId(),
                newDestination.getId(),
                "New destination stop"
        );

        List<RouteVariantStop> rvsList = routeVariantStopRepository
                .findByRouteVariantIdOrderByStopOrderAsc(trip.getBusAssignment().getRouteVariant().getId());

        Integer previousOrder = -1;
        Integer newOrder = -1;
        Integer boardingOrder = -1;

        for (RouteVariantStop rvs : rvsList) {
            if (trip.getBoardingStop() != null && rvs.getStop().getId().equals(trip.getBoardingStop().getId())) {
                boardingOrder = rvs.getStopOrder();
            }
            if (trip.getSelectedDestinationStop() != null && rvs.getStop().getId().equals(trip.getSelectedDestinationStop().getId())) {
                previousOrder = rvs.getStopOrder();
            }
            if (rvs.getStop().getId().equals(newDestination.getId())) {
                newOrder = rvs.getStopOrder();
            }
        }

        if (boardingOrder != -1 && newOrder <= boardingOrder) {
            throw new BadRequestException("New destination must be after the boarding stop");
        }

        if (previousOrder != -1 && newOrder <= previousOrder) {
            throw new BadRequestException("New destination must be further along the route than the original destination");
        }

        Double newFare = calculateFare(
                trip.getBusAssignment().getRouteVariant().getId(),
                trip.getBoardingStop().getId(),
                newDestination.getId()
        );
        Double additionalFare = newFare > trip.getTotalFareLkr() ? newFare - trip.getTotalFareLkr() : 0.0;

        com.transitshield.backend.entity.TripExtension extension = new com.transitshield.backend.entity.TripExtension();
        extension.setPassengerTrip(trip);
        extension.setPreviousDestinationStop(trip.getSelectedDestinationStop());
        extension.setNewDestinationStop(newDestination);
        extension.setAdditionalFareLkr(additionalFare);
        extension.setExtensionStatus(ExtensionStatus.APPROVED);
        extension.setCreatedAt(LocalDateTime.now());
        tripExtensionRepository.save(extension);

        trip.setSelectedDestinationStop(newDestination);
        trip.setDestinationStop(newDestination.getStopName());
        trip.setExtraFareLkr((trip.getExtraFareLkr() != null ? trip.getExtraFareLkr() : 0.0) + additionalFare);
        trip.setTotalFareLkr((trip.getBaseFareLkr() != null ? trip.getBaseFareLkr() : 0.0) + trip.getExtraFareLkr());
        trip = tripRepository.save(trip);

        return mapToDto(trip);
    }

    public PassengerTripDto endTrip(User user, TripEndRequest request) {
        PassengerProfile profile = resolvePassengerProfile(user);

        PassengerTrip trip = tripRepository.findById(request.getTripId())
                .orElseThrow(() -> new ResourceNotFoundException("Trip not found"));

        if (!trip.getPassengerProfile().getId().equals(profile.getId())) {
            throw new BadRequestException("You can only end your own trip");
        }

        if (trip.getTripStatus() == TripStatus.COMPLETED) {
            throw new BadRequestException("Trip already completed");
        }

        Stop actualExit = null;
        int stopsTravelled = 0;
        if (request.getActualExitStopId() != null) {
            actualExit = stopRepository.findById(request.getActualExitStopId())
                    .orElseThrow(() -> new ResourceNotFoundException("Exit stop not found"));

            validateStopBelongsToAssignment(
                    trip.getBusAssignment().getRouteVariant().getId(),
                    actualExit.getId(),
                    "Exit stop"
            );
        }

        if (trip.getBoardingStop() != null && actualExit != null && trip.getBusAssignment() != null
                && trip.getBusAssignment().getRouteVariant() != null) {
            List<RouteVariantStop> routeStops = routeVariantStopRepository
                    .findByRouteVariantIdOrderByStopOrderAsc(trip.getBusAssignment().getRouteVariant().getId());
            Integer boardingOrder = routeStops.stream()
                    .filter(rvs -> rvs.getStop() != null && rvs.getStop().getId().equals(trip.getBoardingStop().getId()))
                    .map(RouteVariantStop::getStopOrder)
                    .findFirst()
                    .orElse(null);
            Integer exitOrder = routeStops.stream()
                    .filter(rvs -> rvs.getStop() != null && rvs.getStop().getId().equals(actualExit.getId()))
                    .map(RouteVariantStop::getStopOrder)
                    .findFirst()
                    .orElse(null);
            if (boardingOrder != null && exitOrder != null) {
                stopsTravelled = Math.max(0, Math.abs(exitOrder - boardingOrder));
            }
        }

        if (stopsTravelled == 0) {
            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = trip.getCreatedAt() != null ? trip.getCreatedAt() : endTime.minusMinutes(10);
            long minutesTravelled = Math.max(1, java.time.Duration.between(startTime, endTime).toMinutes());
            stopsTravelled = Math.max(1, (int) Math.ceil(minutesTravelled / 8.0));
        }

        double baseFare = 20.0;
        double perStopRate = 15.0;
        Double finalFare = baseFare + (stopsTravelled * perStopRate);

        PassengerProfile passengerProfile = trip.getPassengerProfile();
        double walletBalance = passengerProfile.getWalletBalance() != null ? passengerProfile.getWalletBalance() : 0.0;
        if (walletBalance < finalFare) {
            throw new BadRequestException("Insufficient Funds");
        }
        passengerProfile.setWalletBalance(walletBalance - finalFare);
        passengerProfileRepository.save(passengerProfile);

        trip.setBaseFareLkr(finalFare);
        trip.setExtraFareLkr(0.0);
        trip.setTotalFareLkr(finalFare);
        trip.setActualExitStop(actualExit);
        trip.setDestinationStop(actualExit != null ? actualExit.getStopName() : trip.getDestinationStop());
        trip.setTripStatus(TripStatus.COMPLETED);
        trip.setEndedAt(LocalDateTime.now());
        trip.setPaymentStatus(PaymentStatus.PAID);

        trip = tripRepository.save(trip);

        if (trip.getTotalFareLkr() != null && trip.getTotalFareLkr() > 0) {
            Double pointsEarned = trip.getTotalFareLkr() / 100.0;
            rewardService.earnPointsFromTrip(trip.getPassengerProfile(), trip, pointsEarned);
        }

        return mapToDto(trip);
    }

    public PassengerTripDto getActiveTrip(Long passengerProfileId) {
        return tripRepository.findByPassengerProfileIdAndTripStatus(passengerProfileId, TripStatus.ACTIVE)
                .map(this::mapToDto)
                .orElseThrow(() -> new ResourceNotFoundException("No active trip found"));
    }

    public PassengerTripDto getActiveTripForUser(User user) {
        PassengerProfile profile = resolvePassengerProfile(user);
        return getActiveTrip(profile.getId());
    }

    public List<PassengerTripDto> getTripHistory(Long passengerProfileId) {
        return tripRepository.findByPassengerProfileId(passengerProfileId).stream()
                .filter(t -> t.getTripStatus() == TripStatus.COMPLETED)
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public List<PassengerTripDto> getTripHistoryForUser(User user) {
        PassengerProfile profile = resolvePassengerProfile(user);
        return getTripHistory(profile.getId());
    }

    public List<PassengerTripDto> getActiveTripsByBusAssignment(Long busAssignmentId) {
        return tripRepository.findByBusAssignmentIdAndTripStatus(busAssignmentId, TripStatus.ACTIVE)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private PassengerProfile resolvePassengerProfile(User user) {
        if (user == null || user.getId() == null) {
            throw new BadRequestException("Authenticated passenger user is required");
        }

        return passengerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Passenger profile not found for user ID: " + user.getId()));
    }

    private void validateStopBelongsToAssignment(Long routeVariantId, Long stopId, String label) {
        boolean exists = routeVariantStopRepository.findByRouteVariantIdOrderByStopOrderAsc(routeVariantId).stream()
                .anyMatch(rvs -> rvs.getStop() != null && rvs.getStop().getId().equals(stopId));

        if (!exists) {
            throw new BadRequestException(label + " does not belong to the selected route");
        }
    }

    private Double calculateFare(Long variantId, Long originId, Long destId) {
        com.transitshield.backend.entity.FareRule rule = fareRuleRepository
                .findByRouteVariantIdAndBoardingStopIdAndDestinationStopId(variantId, originId, destId)
                .orElseThrow(() -> new BadRequestException("No fare rule exists for the specified route and stops"));
        return rule.getFareLkr();
    }

    private PassengerTripDto mapToDto(PassengerTrip trip) {
        PassengerTripDto dto = new PassengerTripDto();
        dto.setId(trip.getId());
        dto.setTripRef(trip.getTripRef());

        if (trip.getPassengerProfile() != null) {
            dto.setPassengerProfileId(trip.getPassengerProfile().getId());
            dto.setPassengerPublicUserId(trip.getPassengerProfile().getPublicUserId());
            if (trip.getPassengerProfile().getUser() != null) {
                dto.setPassengerName(trip.getPassengerProfile().getUser().getFullName());
            }
        }

        if (trip.getBusAssignment() != null) {
            dto.setBusAssignmentId(trip.getBusAssignment().getId());

            if (trip.getBusAssignment().getBus() != null) {
                dto.setBusId(trip.getBusAssignment().getBus().getId());
                dto.setBusCode(trip.getBusAssignment().getBus().getBusCode());
                dto.setBusDisplayName(trip.getBusAssignment().getBus().getBusDisplayName());
                dto.setRegistrationNumber(trip.getBusAssignment().getBus().getRegistrationNumber());
            }

            if (trip.getBusAssignment().getDriverProfile() != null) {
                dto.setDriverProfileId(trip.getBusAssignment().getDriverProfile().getId());
                dto.setDriverCode(trip.getBusAssignment().getDriverProfile().getDriverCode());

                if (trip.getBusAssignment().getDriverProfile().getUser() != null) {
                    dto.setDriverUserId(trip.getBusAssignment().getDriverProfile().getUser().getId());
                    dto.setDriverName(trip.getBusAssignment().getDriverProfile().getUser().getFullName());
                }
            }

            if (trip.getBusAssignment().getRouteVariant() != null) {
                dto.setRouteVariantId(trip.getBusAssignment().getRouteVariant().getId());
                dto.setRouteVariantCode(trip.getBusAssignment().getRouteVariant().getVariantCode());
                dto.setOriginName(trip.getBusAssignment().getRouteVariant().getOriginName());
                dto.setDestinationName(trip.getBusAssignment().getRouteVariant().getDestinationName());
                dto.setDirectionLabel(trip.getBusAssignment().getRouteVariant().getDirectionLabel());

                if (trip.getBusAssignment().getRouteVariant().getRoute() != null) {
                    dto.setRouteNumber(trip.getBusAssignment().getRouteVariant().getRoute().getRouteNumber());
                    dto.setRouteName(trip.getBusAssignment().getRouteVariant().getRoute().getDisplayName());
                }
            }
        }

        dto.setQrTokenUsed(trip.getQrTokenUsed());
        dto.setTicketQrPayload(buildTicketPayload(trip));
        dto.setDriverValidated(trip.getDriverValidatedAt() != null);
        dto.setDriverValidatedAt(trip.getDriverValidatedAt());
        dto.setIsVerified(Boolean.TRUE.equals(trip.getIsVerified()));
        dto.setDestinationStop(trip.getDestinationStop());

        if (trip.getBoardingStop() != null) {
            dto.setBoardingStopId(trip.getBoardingStop().getId());
            dto.setBoardingStopName(trip.getBoardingStop().getStopName());
        }

        dto.setBoardingDetectMethod(trip.getBoardingDetectMethod());

        if (trip.getSelectedDestinationStop() != null) {
            dto.setSelectedDestinationStopId(trip.getSelectedDestinationStop().getId());
            dto.setSelectedDestinationStopName(trip.getSelectedDestinationStop().getStopName());
        }

        if (trip.getActualExitStop() != null) {
            dto.setActualExitStopId(trip.getActualExitStop().getId());
            dto.setActualExitStopName(trip.getActualExitStop().getStopName());
        }

        dto.setBaseFareLkr(trip.getBaseFareLkr());
        dto.setExtraFareLkr(trip.getExtraFareLkr());
        dto.setTotalFareLkr(trip.getTotalFareLkr());
        dto.setPaymentStatus(trip.getPaymentStatus());
        dto.setTripStatus(trip.getTripStatus());
        dto.setCreatedAt(trip.getCreatedAt());
        dto.setEndedAt(trip.getEndedAt());

        return dto;
    }

    public String buildTicketPayload(PassengerTrip trip) {
        if (trip == null || trip.getTripRef() == null || trip.getTripRef().isBlank()) {
            throw new BadRequestException("Trip reference is not available for ticket generation");
        }
        return TRIP_QR_PREFIX + trip.getTripRef();
    }
}
//bugs
