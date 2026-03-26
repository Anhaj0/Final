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
    val createdAt: String? = null
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
    val orderedStops: List<StopDto>?
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
    val stopName: String?,
    val stopLatitude: Double?,
    val stopLongitude: Double?,
    val stopOrder: Int?,
    val distanceFromStartKm: Double?,
    val isMajorStop: Boolean?
)

data class StopDto(
    val id: Long?,
    val stopCode: String?,
    val stopName: String?,
    val latitude: Double?,
    val longitude: Double?,
    val isActive: Boolean?,
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

data class FarePreviewRequest(
    val routeVariantId: Long,
    val boardingStopId: Long,
    val destinationStopId: Long
)

data class PassengerBalanceDto(
    val fullName: String?,
    val publicUserId: String?,
    val walletBalance: Double?,
    val totalPoints: Double?
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
    val busId: Long?,
    val busDisplayName: String?,
    val routeVariantId: Long?,
    val routeName: String?,
    val driverName: String?,
    val qrTokenUsed: String?,
    val boardingStopId: Long?,
    val boardingStopName: String?,
    val boardingDetectMethod: String?,
    val selectedDestinationStopId: Long?,
    val selectedDestinationStopName: String?,
    val actualExitStopId: Long?,
    val baseFareLkr: Double?,
    val extraFareLkr: Double?,
    val totalFareLkr: Double?,
    val walletBalanceAfterPayment: Double?,
    val paymentStatus: String?,
    val tripStatus: String?,
    val ticketVerified: Boolean?,
    val ticketVerifiedAt: String?,
    val createdAt: String?,
    val endedAt: String?
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
    val driverProfileId: Long?,
    val activeAssignmentId: Long?,
    val activeBusId: Long?,
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

object PassengerTripFlowStore {
    var passengerId: Long? = null
    var qrToken: String? = null
    var qrScanResponse: QrScanResponse? = null
    var passengerBalance: PassengerBalanceDto? = null
    var selectedDestinationStop: StopDto? = null
    var farePreview: Double? = null
    var activeTrip: PassengerTripDto? = null
    var lastCompletedTrip: PassengerTripDto? = null

    fun markTripCompleted(completedTrip: PassengerTripDto?, updatedBalance: PassengerBalanceDto?) {
        passengerBalance = updatedBalance
        lastCompletedTrip = completedTrip
        qrToken = null
        qrScanResponse = null
        selectedDestinationStop = null
        farePreview = null
        activeTrip = null
    }

    fun resetForNewScan() {
        qrToken = null
        qrScanResponse = null
        selectedDestinationStop = null
        farePreview = null
        activeTrip = null
        lastCompletedTrip = null
    }
}
