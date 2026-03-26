package com.transitshield.app.data.model

// ──────────────────────────────────────────────
// User / Auth Models
// ──────────────────────────────────────────────

enum class UserRole { PASSENGER, DRIVER }

data class PassengerProfile(
    val id: String = "P-20241",
    val name: String = "Ashan Perera",
    val phone: String = "+94 77 123 4567",
    val email: String = "ashan.perera@email.com",
    val nicNumber: String = "199812345678V",
    val walletBalance: Double = 1245.50,
    val rewardPoints: Int = 3840,
    val profileInitial: String = "A"
)

data class DriverProfile(
    val id: String = "D-5521",
    val name: String = "Kamal Jayasinghe",
    val phone: String = "+94 71 987 6543",
    val licenseNumber: String = "B1234567",
    val depot: String = "Maharagama Depot",
    val currentRoute: String = "138 – Colombo Fort to Maharagama",
    val isOnline: Boolean = true,
    val demerits: Int = 2,
    val maxDemerits: Int = 10,
    val onTimePercentage: Int = 87,
    val complaintsToday: Int = 1,
    val tripsToday: Int = 14,
    val profileInitial: String = "K"
)

// ──────────────────────────────────────────────
// Route / Trip Models
// ──────────────────────────────────────────────

data class BusStop(
    val id: String,
    val name: String,
    val order: Int
)

data class RouteInfo(
    val busId: String = "NC-3421",
    val routeNumber: String = "138",
    val routeName: String = "Colombo Fort – Maharagama",
    val driverName: String = "Kamal Jayasinghe",
    val driverId: String = "D-5521",
    val direction: String = "Inbound",
    val stops: List<BusStop> = emptyList()
)

data class TripDetails(
    val tripRef: String,
    val busId: String,
    val routeNumber: String,
    val routeName: String,
    val driverName: String,
    val boardingStop: String,
    val destinationStop: String,
    val fare: Double,
    val paymentStatus: String = "PAID",
    val receiptStatus: String = "ISSUED",
    val dateTime: String,
    val status: String = "Active"
)

// ──────────────────────────────────────────────
// Reward Model
// ──────────────────────────────────────────────

data class RewardItem(
    val id: String,
    val title: String,
    val pointsNeeded: Int,
    val category: String,
    val description: String,
    val isAvailable: Boolean = true
)

// ──────────────────────────────────────────────
// Alert / Complaint Models
// ──────────────────────────────────────────────

data class DriverAlert(
    val id: String,
    val title: String,
    val message: String,
    val type: AlertType,
    val timestamp: String
)

enum class AlertType { COMPLAINT, LOST_ITEM, SYSTEM }

data class LostItemAlert(
    val id: String,
    val item: String,
    val route: String,
    val time: String,
    val passengerName: String,
    val status: String = "Open"
)

// ──────────────────────────────────────────────
// Recent Trips
// ──────────────────────────────────────────────

data class RecentTrip(
    val id: String,
    val routeNumber: String,
    val routeName: String,
    val date: String,
    val fare: Double,
    val status: String,
    val from: String,
    val to: String
)
