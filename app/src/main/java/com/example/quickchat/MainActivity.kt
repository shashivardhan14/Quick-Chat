package com.example.quickchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.quickchat.navigation.BottomBar
import com.example.quickchat.screens.CallScreen
import com.example.quickchat.screens.ChatScreen
import com.example.quickchat.screens.LoginScreen
import com.example.quickchat.screens.PhoneAuthScreen
import com.example.quickchat.screens.ProfileScreen
import com.example.quickchat.screens.SearchScreen
import com.example.quickchat.screens.SignUpScreen
import com.example.quickchat.screens.UserListScreen
import com.parse.ParseUser


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChatApp()

        }
    }
}


@Composable
fun ChatApp() {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    LaunchedEffect(Unit) {
        if (ParseUser.getCurrentUser() != null) {
            navController.navigate("users") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        NavHost(navController = navController, startDestination = "splash") {
            composable("splash") { }
            composable("login") { LoginScreen(navController) }
            composable("signup") { SignUpScreen(navController) }
            composable("users") { UserListScreen(navController) }
            composable("profile") { ProfileScreen(navController) }
            composable("search") { SearchScreen(navController) }
            composable("call_screen") { CallScreen(navController) }
            composable("phone_auth") { PhoneAuthScreen(navController) }

            composable("chat/{userId}") { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId")
                userId?.let {
                    ChatScreen(
                        userId = userId,
                        navController = navController
                    )
                }
            }
        }

        val showBottomBar = currentRoute in bottomDestinations.map { it.route }
        if (showBottomBar) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(30.dp)
                    .navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                BottomBar(navController = navController)
            }
        }
    }
}

sealed class Screen(val route: String, val icon: Int) {
    data object Users :
        Screen("users", R.drawable.bottom_btn1)

    data object Search :
        Screen("search", R.drawable.search)

    data object Calls :
        Screen("call_screen", R.drawable.calls)

    data object Profile :
        Screen("profile", R.drawable.bottom_btn4)
}

val bottomDestinations = listOf(
    Screen.Users,
    Screen.Search,
    Screen.Calls,
    Screen.Profile
)

