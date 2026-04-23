package com.example.quickchat.screens


import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.example.quickchat.models.User
import com.parse.ParseQuery
import com.parse.ParseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


@Composable
fun UserListScreen(navController: NavController) {

    var isLoading by remember { mutableStateOf(value = true) }
    var users by remember { mutableStateOf(listOf<User>()) }

    LaunchedEffect(Unit) {
        try {
            users = fetchUsers()
            isLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            isLoading = false
        }
    }
    if (isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        if (users.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "No other users yet.", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                item {
                    Text(
                        text = "Momento",
                        fontSize = 28.sp,
                        style = MaterialTheme.typography.headlineSmall.copy(color = Color(0xC623A497)),
                        modifier = Modifier.padding(start = 12.dp, top = 4.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                }
                items(users) { user ->
                    UserItem(user = user) {
                        navController.navigate("chat/${user.userId}")
                    }
                }
            }
        }
    }
}


suspend fun fetchUsers(): List<User> {
    return withContext(Dispatchers.IO) { // Moves the work to a background thread
        try {
            val query = ParseQuery.getQuery<ParseUser>("_User")
            // Exclude current user
            val currentUser = ParseUser.getCurrentUser()
            if (currentUser != null) {
                query.whereNotEqualTo("objectId", currentUser.objectId)
            }

            val parseUsers = query.find() // Blocking call now safe on IO thread

            parseUsers.map {
                val profileFile = it.getParseFile("profilePic")
                User(
                    userId = it.objectId ?: "",
                    username = it.username ?: "Unknown",
                    email = it.email ?: "No email",
                    profilePicUrl = profileFile?.url

                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}

@Composable
fun UserItem(
    user: User,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable {
                onClick(

                )
            },
        shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically



        ) {
            AsyncImage(
                model = user.profilePicUrl,
                contentDescription = "User Avatar",
                contentScale = ContentScale.Crop,
                // If the URL is null, show this icon
                fallback = rememberVectorPainter(Icons.Default.AccountCircle),
                // While loading, show this icon
                placeholder = rememberVectorPainter(Icons.Default.AccountCircle),
                // If loading fails, show this icon
                error = rememberVectorPainter(Icons.Default.AccountCircle),
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            )
            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f)

            ) {
                Text(
                    text = user.username,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface

                )
                Text(
                    text = user.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Go to Chat",
                tint = Color.Gray
            )

        }

    }

}