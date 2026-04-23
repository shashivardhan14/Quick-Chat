package com.example.quickchat.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.parse.ParseUser

@Composable
fun PhoneAuthScreen(navController: NavController) {
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(value = false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (!isOtpSent) "Link Phone Number" else "Verify OTP",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (!isOtpSent) {
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { phoneNumber = it },
                label = { Text("Phone Number (e.g. +91...)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        } else {
            OutlinedTextField(
                value = otpCode,
                onValueChange = { otpCode = it },
                label = { Text("Enter 6-digit OTP") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (!isOtpSent) {
                    // Step 1: Send OTP logic (Integration with Twilio/Firebase needed here)
                    isOtpSent = true
                } else {
                    // Step 2: Verify OTP and Update Parse User
                    linkPhoneToParseUser(phoneNumber, navController)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            Text(if (!isOtpSent) "Send OTP" else "Verify & Finish")
        }
    }
}

fun linkPhoneToParseUser(phone: String, navController: NavController) {
    val currentUser = ParseUser.getCurrentUser()
    if (currentUser != null) {
        currentUser.put("phone", phone)
        currentUser.put("phoneVerified", true)
        currentUser.saveInBackground { e ->
            if (e == null) {
                navController.navigate("call_screen") {
                    popUpTo("phone_auth") { inclusive = true }
                }
            }else {
                // Log the error to see why saving failed
                android.util.Log.e("ParseError", "Save failed: ${e.message}")
            }
        }
    }
}
@Composable
@Preview(showBackground = true)
fun PhoneAuthScreenPreview() {
    val navController = rememberNavController()
    PhoneAuthScreen(navController = navController)
}

