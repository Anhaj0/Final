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

    @POST("qr/verify-ticket/{tripRef}")
    suspend fun verifyTicket(@Path("tripRef") tripRef: String): PassengerTripDto

    @GET("buses/{busId}/active-qr")
    suspend fun getActiveQr(@Path("busId") busId: Long): BusQrCodeDto

    // ─── Trips ───────────────────────────────────────────────
    @POST("trips/preview-fare")
    suspend fun previewFare(@Body request: FarePreviewRequest): Double

    @POST("trips/start")
    suspend fun startTrip(@Body request: TripStartRequest): PassengerTripDto

    @POST("trips/end")
    suspend fun endTrip(@Body request: TripEndRequest): PassengerTripDto

    @GET("trips/passenger/{passengerId}/balance")
    suspend fun getPassengerBalance(@Path("passengerId") passengerId: Long): PassengerBalanceDto

    @GET("trips/passenger/{passengerId}/active")
    suspend fun getActiveTrip(@Path("passengerId") passengerId: Long): PassengerTripDto

    @GET("trips/passenger/{passengerId}/history")
    suspend fun getTripHistory(@Path("passengerId") passengerId: Long): List<PassengerTripDto>

    // ─── Location ────────────────────────────────────────────
    @POST("location/update")
    suspend fun updateLocation(@Body request: LocationUpdateRequest): Unit

    @GET("location/live")
    suspend fun getLiveLocations(): List<BusLocationDto>

    @GET("location/bus/{busId}")
    suspend fun getBusLocation(@Path("busId") busId: Long): BusLocationDto

    @GET("route-variant-stops")
    suspend fun getRouteVariantStops(): List<RouteVariantStopDto>

    // ─── Buses ───────────────────────────────────────────────
    @GET("buses")
    suspend fun getBuses(): List<BusDto>

    // ─── Rewards ─────────────────────────────────────────────
    @GET("rewards/me/public-id")
    suspend fun getMyPublicId(): Map<String, String>

    @GET("rewards/me/balance")
    suspend fun getMyBalance(): Map<String, Double>

    @GET("rewards/me/summary")
    suspend fun getMyBalanceSummary(): PassengerBalanceDto

    @GET("rewards/me/history")
    suspend fun getMyRewardHistory(): List<RewardTransactionDto>

    @POST("rewards/transfer")
    suspend fun transferPoints(@Body request: TransferRequest): RewardTransactionDto

    // ─── Driver ──────────────────────────────────────────────
    @GET("driver/dashboard")
    suspend fun getDriverDashboard(): DriverDashboardDto
}
