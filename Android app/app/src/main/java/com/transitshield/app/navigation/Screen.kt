package com.transitshield.app.navigation

sealed class Screen(val route: String) {
    // Auth
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")

    // Passenger
    object PassengerHome : Screen("passenger_home")
    object QrScan : Screen("qr_scan")
    object TripDetails : Screen("trip_details")
    object StopSelection : Screen("stop_selection")
    object Payment : Screen("payment")
    object DigitalReceipt : Screen("digital_receipt")
    object ActiveTrip : Screen("active_trip")
    object LiveTracker : Screen("live_tracker")
    object Rewards : Screen("rewards")
    object ComplaintSubmission : Screen("complaint_submission")
    object LostItemReport : Screen("lost_item_report")
    object PassengerTasks : Screen("passenger_tasks")
    object RecentTrips : Screen("recent_trips")
    object PassengerProfile : Screen("passenger_profile")

    // Driver
    object DriverHome : Screen("driver_home")
    object DriverProfile : Screen("driver_profile")

    // Conductor
    object ConductorVerification : Screen("conductor_verification")
}
