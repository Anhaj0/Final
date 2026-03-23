package com.transitshield.app.data.network.dto

/**
 * Network DTOs for the TransitShield Android app.
 * These mirror the Spring Boot backend's DTO classes.
 */

// ─── Auth ────────────────────────────────────────────────
data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val password: String
)

data class AuthResponse(
    val userId: Long?,
    val fullName: String?,
    val email: String?,
    val role: String?,
    val token: String?,
    val message: String?
)

data class UserDto(
    val id: Long?,
    val fullName: String?,
    val age: Int?,
    val email: String?,
    val phoneNumber: String?,
    val role: String?,
    val isActive: Boolean?,
    val walletBalance: Double? = null
)

// ─── QR ──────────────────────────────────────────────────
data class QrScanRequest(
    val passengerId: Long,
    val qrToken: String,
    val latitude: Double?,
    val longitude: Double?
)

data class QrScanResponse(
    val message: String?,
    val busId: Long?,
    val busDisplayName: String?,
    val driverProfileId: Long?,
    val routeVariantId: Long?,
    val busAssignmentId: Long?,
    val nearestBoardingStopId: Long?,
    val orderedStops: List<RouteVariantStopDto>?
)

data class BusQrCodeDto(
    val id: Long?,
    val busId: Long?,
    val qrToken: String?,
    val qrLabel: String?,
    val isActive: Boolean?
)

data class RouteVariantStopDto(
    val id: Long?,
    val routeVariantId: Long?,
    val stopId: Long?,
    val stopOrder: Int?,
    val distanceFromStartKm: Double?,
    val isMajorStop: Boolean?
)

// ─── Trips ───────────────────────────────────────────────
data class TripStartRequest(
    val passengerProfileId: Long,
    val busAssignmentId: Long,
    val boardingStopId: Long,
    val selectedDestinationStopId: Long?,
    val qrTokenUsed: String
)

data class TripEndRequest(
    val tripId: Long,
    val actualExitStopId: Long?
)

data class PassengerTripDto(
    val id: Long?,
    val tripRef: String?,
    val passengerProfileId: Long?,
    val busAssignmentId: Long?,
    val qrTokenUsed: String?,
    val boardingStopId: Long?,
    val boardingDetectMethod: String?,
    val selectedDestinationStopId: Long?,
    val actualExitStopId: Long?,
    val baseFareLkr: Double?,
    val extraFareLkr: Double?,
    val totalFareLkr: Double?,
    val paymentStatus: String?,
    val tripStatus: String?,
    val createdAt: String?,
    val endedAt: String?
)

data class BusAssignmentDto(
    val id: Long?,
    val busId: Long?,
    val busCode: String?,
    val registrationNumber: String?,
    val busDisplayName: String?,
    val driverProfileId: Long?,
    val driverCode: String?,
    val driverUserId: Long?,
    val driverName: String?,
    val routeVariantId: Long?,
    val routeVariantCode: String?,
    val routeNumber: String?,
    val routeName: String?,
    val originName: String?,
    val destinationName: String?,
    val directionLabel: String?,
    val assignmentStatus: String?,
    val startedAt: String?
)

// ─── Ticket Validation ───────────────────────────────────
data class TicketValidationRequest(
    val scannedValue: String
)

data class TicketValidationResponse(
    val valid: Boolean?,
    val status: String?,
    val message: String?,
    val tripId: Long?,
    val tripRef: String?,
    val passengerName: String?,
    val busCode: String?,
    val routeNumber: String?,
    val originName: String?,
    val destinationName: String?,
    val paymentStatus: String?,
    val totalFareLkr: Double?,
    val issuedAt: String?,
    val validatedAt: String?
)

// ─── Location ────────────────────────────────────────────
data class LocationUpdateRequest(
    val busId: Long,
    val driverProfileId: Long,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double?,
    val heading: Double?,
    val occupancyStatus: String?,
    val sourceType: String?
)

data class BusLocationDto(
    val id: Long?,
    val busId: Long?,
    val routeVariantId: Long?,
    val driverProfileId: Long?,
    val latitude: Double?,
    val longitude: Double?,
    val speedKmh: Double?,
    val heading: Double?,
    val occupancyStatus: String?,
    val recordedAt: String?,
    val sourceType: String?
)

// ─── Buses ───────────────────────────────────────────────
data class BusDto(
    val id: Long?,
    val busCode: String?,
    val registrationNumber: String?,
    val busDisplayName: String?,
    val capacity: Int?,
    val operatorName: String?,
    val status: String?
)

// ─── Rewards ─────────────────────────────────────────────
data class RewardTransactionDto(
    val id: Long?,
    val type: String?,
    val points: Double?,
    val description: String?,
    val createdAt: String?
)

data class TransferRequest(
    val recipientPublicId: String,
    val amount: Double
)

// ─── Driver Dashboard ────────────────────────────────────
data class DriverDashboardDto(
    val name: String?,
    val profileInitial: String?,
    val id: Long?,
    val depot: String?,
    val isOnline: Boolean?,
    val demerits: Int?,
    val maxDemerits: Int?,
    val currentRoute: String?,
    val tripsToday: Int?,
    val onTimePercentage: Int?,
    val complaintsToday: Int?,
    val alerts: List<DriverAlertDto>?,
    val lostItems: List<LostItemDto>?
)

data class DriverScheduleDto(
    val assignmentId: Long?,
    val driverProfileId: Long?,
    val driverUserId: Long?,
    val driverName: String?,
    val driverCode: String?,
    val depot: String?,
    val busId: Long?,
    val busCode: String?,
    val busDisplayName: String?,
    val registrationNumber: String?,
    val capacity: Int?,
    val operatorName: String?,
    val busStatus: String?,
    val routeVariantId: Long?,
    val routeVariantCode: String?,
    val routeNumber: String?,
    val routeName: String?,
    val originName: String?,
    val destinationName: String?,
    val directionLabel: String?,
    val assignmentStatus: String?,
    val startedAt: String?,
    val endedAt: String?,
    val hasActiveQr: Boolean?,
    val activeQrId: Long?,
    val activeQrLabel: String?,
    val activeQrToken: String?
)

data class DriverAlertDto(
    val type: String?,
    val title: String?,
    val message: String?,
    val timestamp: String?
)

data class LostItemDto(
    val item: String?,
    val passengerName: String?,
    val route: String?,
    val time: String?,
    val status: String?
)


data class LostItemReportCreateRequest(
    val itemTitle: String,
    val description: String,
    val category: String?,
    val routeInfo: String?,
    val busInfo: String?,
    val lostAt: String?,
    val contactDetails: String?
)

data class LostItemReportDto(
    val id: Long?,
    val reporterUserId: Long?,
    val reporterName: String?,
    val reporterPhoneNumber: String?,
    val reporterRole: String?,
    val itemTitle: String?,
    val description: String?,
    val category: String?,
    val routeInfo: String?,
    val busInfo: String?,
    val lostAt: String?,
    val contactDetails: String?,
    val status: String?,
    val adminNotes: String?,
    val resolutionNotes: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class PassengerTaskLocationRequest(
    val busId: Long,
    val latitude: Double,
    val longitude: Double
)

data class PassengerTaskCapacityRequest(
    val busId: Long,
    val passengerCount: Int
)

data class PassengerTaskResponse(
    val message: String?,
    val awardedPoints: Double?,
    val totalPoints: Double?,
    val averageCapacity: Double?
)
