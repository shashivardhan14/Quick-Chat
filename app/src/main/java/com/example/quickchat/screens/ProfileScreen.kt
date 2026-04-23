package com.example.quickchat.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage // Ensure you have coil dependency
import com.example.quickchat.R
import com.example.quickchat.components.OptionRow
import com.example.quickchat.components.TopBar
import com.parse.ParseFile
import com.parse.ParseUser
import com.parse.SaveCallback
import java.io.InputStream

@Composable
fun ProfileScreen(navController: NavController) {
    // State to hold the selected image URI
    val context = LocalContext.current
    // Fetch existing profile picture URL from Parse
    // Added a check for LocalInspectionMode to prevent NullPointerException in Compose Previews
    // since the Parse SDK is not initialized in the preview environment.
    val currentUser = if (LocalInspectionMode.current) null else ParseUser.getCurrentUser()
    // State for the image source (can be Uri for local or String for remote URL)
    var imageSource by remember {
        mutableStateOf<Any?>(currentUser?.getParseFile("profilePic")?.url ?: R.drawable.deadpool)
    }
    var isUploading by remember { mutableStateOf(false) }




    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            isUploading = true
            // Immediately show the selected image locally for better UX
            imageSource = it
            uploadProfilePicture(context, it) { success ->
                isUploading = false
                if (success) {
                    Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                    // Refresh the URL from Parse to ensure we have the live link
                    imageSource = ParseUser.getCurrentUser()?.getParseFile("profilePic")?.url
                } else {
                    Toast.makeText(context, "Upload failed", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colorResource(R.color.LightGrey)),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item { TopBar(onBack = { navController.navigateUp() }) }
        item { Spacer(Modifier.height(16.dp)) }

        // Profile Image Section
        item {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(contentAlignment = Alignment.BottomEnd) {

                    AsyncImage(
                        model = imageSource,
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(120.dp)
                            .clip(RoundedCornerShape(30.dp)),
                        contentScale = ContentScale.Crop
                    )

                    if (isUploading) {
                        CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            color = colorResource(id = R.color.black)
                        )
                    }

                    // Edit Button / Upload Icon
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .offset(x = 8.dp, y = 8.dp)
                            .clip(CircleShape)
                            .background(colorResource(id = R.color.black)) // Change to your primary color
                            .clickable { launcher.launch("image/*") },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile Picture",
                            tint = colorResource(id = R.color.white),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }

        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = currentUser?.username ?: "User",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = colorResource(id = R.color.black)
                )
                Text(
                    text = currentUser?.email ?: "email@example.com",
                    fontSize = 16.sp,
                    color = colorResource(id = R.color.black)
                )
            }
        }
        item { Spacer(Modifier.height(24.dp)) }
        item { OptionRow(title = "Account ", icon = R.drawable.key) }
        item { OptionRow(title = "Chats", icon = R.drawable.chat) }
        item { OptionRow(title = "Privacy", icon = R.drawable.privacy) }
        item { OptionRow(title = "Notifications", icon = R.drawable.notification) }
        item { OptionRow(title = "Storage & Data", icon = R.drawable.storage) }
        item { OptionRow(title = "App Language", icon = R.drawable.language) }
        item { OptionRow(title = "Invite a friend", icon = R.drawable.invite) }
        item { Spacer(Modifier.height(24.dp)) }


    }
}
fun uploadProfilePicture(context: Context, uri: Uri, onResult: (Boolean) -> Unit) {
    val user = ParseUser.getCurrentUser() ?: return

    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        val bytes = inputStream?.readBytes()
        inputStream?.close()

        if (bytes != null) {
            val file = ParseFile("profile_pic.jpg", bytes)

            // Step 1: Save the file to Parse
            file.saveInBackground(SaveCallback { e ->
                if (e == null) {
                    // Step 2: Link the file to the user object
                    user.put("profilePic", file)
                    user.saveInBackground { saveError ->
                        onResult(saveError == null)
                    }
                } else {
                    onResult(false)
                }
            })
        }
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(false)
    }
}


@Preview
@Composable
fun ProfileScreenPreview() {
    val navController = rememberNavController()
   ProfileScreen(navController = navController)
}