package com.example.quickchat.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.quickchat.R
import com.parse.ParseUser

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(navController: NavController) {
    var searchQuery by remember { mutableStateOf("") }
    var userList by remember { mutableStateOf<List<ParseUser>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Function to perform the Parse Query
    fun searchUsers(queryText: String) {
        if (queryText.isBlank()) {
            userList = emptyList()
            return
        }

        isLoading = true
        val query = ParseUser.getQuery()

        // Find users whose username starts with the query (case-insensitive-ish)
        // Note: 'whereContains' or 'whereMatches' can be used for partial matches
        query.whereStartsWith("username", queryText)

        // Exclude the current user from search results
        ParseUser.getCurrentUser()?.let {
            query.whereNotEqualTo("objectId", it.objectId)
        }

        query.findInBackground { objects, e ->
            isLoading = false
            if (e == null) {
                userList = objects ?: emptyList()
            } else {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.LightGrey))
    ) {
        // Search Bar Implementation
        SearchBar(
            query = searchQuery,
            onQueryChange = {
                searchQuery = it
                searchUsers(it) // Real-time search
            },
            onSearch = { searchUsers(it) },
            active = false,
            onActiveChange = {},
            placeholder = { Text("Search by username...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {}

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(userList) { user ->
                UserItem(user = user) {
                    // Navigate to Chat Screen or Profile
                    // navController.navigate("chat/${user.objectId}")
                }
            }
        }
    }
}

@Composable
fun UserItem(user: ParseUser, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Placeholder for profile picture
            Surface(
                modifier = Modifier.size(40.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = user.username?.firstOrNull()?.uppercase() ?: "?",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = user.username ?: "Unknown User",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = user.email ?: "",
                    fontSize = 12.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
@Preview
@Composable
fun SearchScreenPreview() {
    val navController = rememberNavController()
    SearchScreen(navController = navController)


}