package com.transitshield.app.data.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.transitshield.app.data.network.RetrofitClient
import com.transitshield.app.data.network.dto.AuthResponse
import com.transitshield.app.data.network.dto.LoginRequest
import com.transitshield.app.data.network.dto.RegisterRequest
import kotlinx.coroutines.launch

/**
 * ViewModel for authentication state.
 * Manages login, registration, and session data.
 */
class AuthViewModel : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var loggedInUser by mutableStateOf<AuthResponse?>(null)
        private set

    var isLoggedIn by mutableStateOf(false)
        private set

    fun login(email: String, password: String, onSuccess: (AuthResponse) -> Unit) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.login(LoginRequest(email, password))
                loggedInUser = response
                isLoggedIn = true
                RetrofitClient.authToken = response.token.trim()
                onSuccess(response)
            } catch (e: Exception) {
                errorMessage = parseError(e)
            } finally {
                isLoading = false
            }
        }
    }

    fun registerPassenger(
        fullName: String, email: String, phone: String, password: String,
        onSuccess: (AuthResponse) -> Unit
    ) {
        isLoading = true
        errorMessage = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.registerPassenger(
                    RegisterRequest(fullName, email, phone, password)
                )
                loggedInUser = response
                isLoggedIn = true
                RetrofitClient.authToken = response.token.trim()
                onSuccess(response)
            } catch (e: Exception) {
                errorMessage = parseError(e)
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        loggedInUser = null
        isLoggedIn = false
        RetrofitClient.authToken = null
    }

    fun clearError() {
        errorMessage = null
    }

    private fun parseError(e: Exception): String {
        return when {
            e.message?.contains("Unable to resolve host") == true -> "Cannot reach the server. Make sure the backend is running."
            e.message?.contains("401") == true || e.message?.contains("Unauthorized") == true -> "Invalid email or password."
            e.message?.contains("409") == true -> "An account with this email already exists."
            else -> e.message ?: "An unexpected error occurred."
        }
    }
}
