package com.example.quickchat.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quickchat.ui.theme.QuickChatTheme
import com.parse.ParseUser


@Composable
fun SignUpScreen(
    navController: NavController
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(value = false) }
    var isLoading by remember { mutableStateOf(false) }

    QuickChatTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFFAFAFA)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)

            ) {
                val context = LocalContext.current
                Text(
                    text = "Create an Account",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold, color = Color(0xFF333333)
                )
                Spacer(modifier = Modifier.height(24.dp))

                BasicTextField(
                    value = username,
                    onValueChange = { username = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE), shape = MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF333333),
                        fontSize = 16.sp
                    ),
                    decorationBox = { innerTextField ->
                        if (username.isEmpty()) {
                            Text(text = "Username", color = Color.Gray)
                        }
                        innerTextField()
                    }
                )
                Spacer(modifier = Modifier.height(16.dp))

                BasicTextField(
                    value = email,
                    onValueChange = { email = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE), shape = MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF333333),
                        fontSize = 16.sp
                    ),
                    decorationBox = { innerTextField ->
                        if (email.isEmpty()) {
                            Text(text = "Email", color = Color.Gray)
                        }
                        innerTextField()

                    }

                )
                Spacer(modifier = Modifier.height(16.dp))

                BasicTextField(
                    value = password,
                    onValueChange = { password = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE), shape = MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF333333), fontSize = 16.sp

                    ),
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    decorationBox = { innerTextField ->
                        if (password.isEmpty()) {
                            Text(text = "Password", color = Color.Gray)
                        }
                        innerTextField()
                    }

                )
                Spacer(modifier = Modifier.height(16.dp))
                BasicTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFEEEEEE), shape = MaterialTheme.shapes.medium)
                        .padding(16.dp),
                    textStyle = LocalTextStyle.current.copy(
                        color = Color(0xFF333333), fontSize = 16.sp

                    ),
                    visualTransformation =
                        if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    decorationBox = { innerTextField ->
                        if (confirmPassword.isEmpty()) {
                            Text(text = "Confirm Password", color = Color.Gray)
                        }
                        innerTextField()
                    }

                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        } else if (password != confirmPassword) {
                            Toast.makeText(context, "Passwords do not match", Toast.LENGTH_SHORT).show()
                        } else {
                            isLoading = true
                            signUp(username, email, password, context, navController) {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xBF009688)),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                    } else {
                        Text(
                            text = "Sign Up",
                            color = Color.White, fontSize = 16.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(
                    onClick = {
                        navController.navigate("login")
                    }
                ) {
                    Text(
                        text = "Already have an account ? Login",
                        color = Color(0x740A0404)
                    )

                }


            }

        }
    }

}
fun signUp(username: String, email: String, password: String, context: Context, navController: NavController, onComplete: () -> Unit) {
    // Ensure no user is logged in before signing up a new one
    ParseUser.logOut()
    
    val user = ParseUser()
    user.username = username
    user.email = email
    user.setPassword(password)

    user.signUpInBackground { e ->
        onComplete()
        if (e == null) {
            navController.navigate("users") {
                popUpTo("signup") { inclusive = true }
            }
            Toast.makeText(context, "Sign Up Successful", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(context, e.message, Toast.LENGTH_LONG).show()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    val navController = rememberNavController()
    SignUpScreen(navController = navController)
}
