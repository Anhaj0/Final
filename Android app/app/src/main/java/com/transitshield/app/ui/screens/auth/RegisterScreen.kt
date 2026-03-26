package com.transitshield.app.ui.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.transitshield.app.data.viewmodel.AuthViewModel
import com.transitshield.app.navigation.Screen
import com.transitshield.app.ui.components.PrimaryButton
import com.transitshield.app.ui.theme.*

@Composable
fun RegisterScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel()
) {
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(BgDeep, BgSurface)))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Create Account",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Text(
                text = "Join TransitShield as a Passenger",
                color = TextSecondary,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(8.dp))

            // Notice: Driver accounts are admin-created
            Card(
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = OrangeWarning.copy(alpha = 0.08f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, OrangeWarning.copy(alpha = 0.25f))
            ) {
                Text(
                    text = "Driver accounts are created by the admin. Only passengers can self-register.",
                    color = OrangeWarning,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(12.dp),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(24.dp))

            // Passenger Registration Form
            RegisterField(label = "Full Name", value = fullName, onValueChange = { fullName = it }, placeholder = "e.g. Ashan Perera")
            Spacer(Modifier.height(14.dp))
            RegisterField(label = "Email Address", value = email, onValueChange = { email = it }, placeholder = "you@example.com", keyboardType = KeyboardType.Email)
            Spacer(Modifier.height(14.dp))
            RegisterField(label = "Phone Number", value = phone, onValueChange = { phone = it }, placeholder = "+94 77 XXX XXXX", keyboardType = KeyboardType.Phone)
            Spacer(Modifier.height(14.dp))
            RegisterField(label = "Password", value = password, onValueChange = { password = it }, placeholder = "Min. 6 characters", isPassword = true)

            // Error message
            authViewModel.errorMessage?.let { error ->
                Spacer(Modifier.height(12.dp))
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = RedError.copy(alpha = 0.12f))
                ) {
                    Text(
                        text = error,
                        color = RedError,
                        fontSize = 13.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(Modifier.height(28.dp))

            PrimaryButton(
                text = if (authViewModel.isLoading) "Creating Account..." else "Create Passenger Account",
                enabled = !authViewModel.isLoading && fullName.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                onClick = {
                    authViewModel.registerPassenger(fullName, email, phone, password) { _ ->
                        navController.navigate(Screen.PassengerHome.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "Already have an account? ", color = TextSecondary, fontSize = 14.sp)
                Text(
                    text = "Sign In",
                    color = BlueElectric,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun RegisterField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false
) {
    Column {
        Text(text = label, color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = TextMuted, fontSize = 14.sp) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = BlueElectric,
                unfocusedBorderColor = BorderSubtle,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = BlueElectric,
                focusedContainerColor = BgCard,
                unfocusedContainerColor = BgCard
            )
        )
    }
}
