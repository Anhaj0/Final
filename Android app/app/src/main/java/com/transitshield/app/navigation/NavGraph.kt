package com.transitshield.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.transitshield.app.ui.screens.auth.LoginScreen
import com.transitshield.app.ui.screens.auth.RegisterScreen
import com.transitshield.app.ui.screens.auth.SplashScreen
import com.transitshield.app.ui.screens.conductor.ConductorVerificationScreen
import com.transitshield.app.ui.screens.driver.DriverHomeScreen
import com.transitshield.app.ui.screens.driver.DriverProfileScreen
import com.transitshield.app.ui.screens.passenger.*

@Composable
fun TransitShieldNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        // Auth
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }

        // Passenger
        composable(Screen.PassengerHome.route) { PassengerHomeScreen(navController) }
        composable(Screen.QrScan.route) { QrScanScreen(navController) }
        composable(Screen.TripDetails.route) { TripDetailsScreen(navController) }
        composable(Screen.Payment.route) { PaymentScreen(navController) }
        composable(Screen.DigitalReceipt.route) { DigitalReceiptScreen(navController) }
        composable(Screen.ActiveTrip.route) { ActiveTripScreen(navController) }
        composable(Screen.LiveTracker.route) { LiveTrackerScreen(navController) }
        composable(Screen.Rewards.route) { RewardsScreen(navController) }
        composable(Screen.ComplaintSubmission.route) { ComplaintSubmissionScreen(navController) }
        composable(Screen.LostItemReport.route) { LostItemReportScreen(navController) }
        composable(Screen.RecentTrips.route) { RecentTripsScreen(navController) }
        composable(Screen.PassengerProfile.route) { PassengerProfileScreen(navController) }

        // Driver
        composable(Screen.DriverHome.route) { DriverHomeScreen(navController) }
        composable(Screen.DriverProfile.route) { DriverProfileScreen(navController) }

        // Conductor
        composable(Screen.ConductorVerification.route) { ConductorVerificationScreen(navController) }
    }
}
