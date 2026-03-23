package com.transitshield.app.data.network

import com.transitshield.app.data.network.dto.*
import retrofit2.http.*

/**
 * TransitShield API service interface.
 * All endpoints connect to the Spring Boot backend.
 */
interface ApiService {

    // ─── Auth ────────────────────────────────────────────────
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @GET("users/me")
    suspend fun getMe(): UserDto

    @PUT("users/me")
    suspend fun updateMe(@Body user: UserDto): UserDto

    @POST("auth/register/passenger")
    suspend fun registerPassenger(@Body request: RegisterRequest): AuthResponse

    // ─── QR ──────────────────────────────────────────────────
    @POST("qr/scan")
    suspend fun scanQr(@Body request: QrScanRequest): QrScanResponse

    @GET("buses/{busId}/active-qr")
    suspend fun getActiveQr(@Path("busId") busId: Long): BusQrCodeDto

    // ─── Trips ───────────────────────────────────────────────
    @POST("trips/start")
    suspend fun startTrip(@Body request: TripStartRequest): PassengerTripDto

    @POST("trips/end")
    suspend fun endTrip(@Body request: TripEndRequest): PassengerTripDto

    @GET("trips/passenger/{passengerId}/active")
    suspend fun getActiveTrip(@Path("passengerId") passengerId: Long): PassengerTripDto

    @GET("trips/me/active")
    suspend fun getMyActiveTrip(): PassengerTripDto

    @GET("trips/passenger/{passengerId}/history")
    suspend fun getTripHistory(@Path("passengerId") passengerId: Long): List<PassengerTripDto>

    // ─── Location ────────────────────────────────────────────
    @POST("location/update")
    suspend fun updateLocation(@Body request: LocationUpdateRequest): Unit

    @GET("location/live")
    suspend fun getLiveLocations(): List<BusLocationDto>

    // ─── Buses ───────────────────────────────────────────────
    @GET("buses")
    suspend fun getBuses(): List<BusDto>

    // ─── Rewards ─────────────────────────────────────────────
    @GET("rewards/me/public-id")
    suspend fun getMyPublicId(): Map<String, String>

    @GET("rewards/me/balance")
    suspend fun getMyBalance(): Map<String, Double>

    @GET("rewards/me/history")
    suspend fun getMyRewardHistory(): List<RewardTransactionDto>

    @POST("rewards/transfer")
    suspend fun transferPoints(@Body request: TransferRequest): RewardTransactionDto

    // ─── Driver ──────────────────────────────────────────────
    @GET("driver/dashboard")
    suspend fun getDriverDashboard(): DriverDashboardDto

    @GET("driver/schedule")
    suspend fun getDriverSchedule(): DriverScheduleDto

    @GET("driver/alerts")
    suspend fun getDriverAlerts(): List<DriverAlertDto>

    @GET("driver/assigned-bus/qr")
    suspend fun getAssignedBusQr(): BusQrCodeDto

    @GET("driver/lost-items")
    suspend fun getDriverLostItems(): List<LostItemReportDto>

    @GET("tickets/assignments/active")
    suspend fun getActiveAssignments(): List<BusAssignmentDto>

    @POST("driver/tickets/validate")
    suspend fun validatePassengerTicket(@Body request: TicketValidationRequest): TicketValidationResponse


    // ─── Lost & Found ──────────────────────────────────────
    @POST("lost-items")
    suspend fun createLostItemReport(@Body request: LostItemReportCreateRequest): LostItemReportDto

    @GET("lost-items/me")
    suspend fun getMyLostItemReports(): List<LostItemReportDto>

    // ─── Passenger Tasks ───────────────────────────────────
    @POST("tasks/location")
    suspend fun submitTaskLocation(@Body request: PassengerTaskLocationRequest): PassengerTaskResponse

    @POST("tasks/capacity")
    suspend fun submitTaskCapacity(@Body request: PassengerTaskCapacityRequest): PassengerTaskResponse
}
