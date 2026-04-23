package com.example.quickchat.screens

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.quickchat.R
import com.example.quickchat.components.CallTopBar
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.parse.ParseUser

// Data model for Call History
data class CallLog(
    val id: String,
    val name: String,
    val time: String,
    val isVideo: Boolean,
    val isMissed: Boolean,
    val imageUrl: String = "https://via.placeholder.com/150"
)

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CallScreen(navController: NavController) {

    val context = LocalContext.current
    val currentUser = ParseUser.getCurrentUser()
    val isPhoneVerified = currentUser?.getBoolean("phoneVerified") ?: false
    var showCallDialog by remember { mutableStateOf(false) }
    var selectedUserForCall by remember { mutableStateOf<CallLog?>(null) }

    // Permissions state
    val permissionState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA
        )
    )

    // Mock Data - In a real app, this would come from a ViewModel/Database
    val callHistory = listOf(
        CallLog("1", "eve", "Today, 10:30 AM", isVideo = false, isMissed = false),
        CallLog("2", "bob", "Yesterday, 8:45 PM", isVideo = true, isMissed = true),
        CallLog("3", "ram", "Monday, 11:20 AM", isVideo = false, isMissed = false)
    )
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(color = colorResource(R.color.LightGrey)),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            item { CallTopBar(onBack = { navController.navigateUp() }) }

            item {
                Text(
                    text = "Recent",
                    modifier = Modifier.padding(16.dp),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            items(callHistory) { call ->
                CallHistoryItem(call = call, onClick = {
                    // FIX: Set the user and show the dialog when clicking a history item
                    selectedUserForCall = call
                    showCallDialog = true
                })
            }
        }

        // Floating Action Button to start a new call from contacts
        FloatingActionButton(
            onClick = {
                if (!isPhoneVerified) {    // Redirect to Phone Auth if not verified
                    navController.navigate("phone_auth")
                } else {
                    // Proceed with Agora Call
                    showCallDialog = true
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 150.dp, end = 26.dp),
            containerColor = colorResource(id = R.color.black)
        ) {
            Icon(painter = painterResource(id = R.drawable.dialpad), modifier = Modifier.size(20.dp), contentDescription = "New Call", tint = Color.White)
        }
    }

    // Call Selection Dialog
    if (showCallDialog && selectedUserForCall != null) {
        CallSelectionDialog(
            userName = selectedUserForCall!!.name,
            onDismiss = { showCallDialog = false },
            onVoiceCall = {
                showCallDialog = false
                if (permissionState.allPermissionsGranted) {
                    // Start Voice Call Logic (e.g., navigate to CallView or Agora Room)
                    Toast.makeText(context, "Starting Voice Call with ${selectedUserForCall!!.name}", Toast.LENGTH_SHORT).show()
                } else {
                    permissionState.launchMultiplePermissionRequest()
                }
            },
            onVideoCall = {
                showCallDialog = false
                if (permissionState.allPermissionsGranted) {
                    Toast.makeText(context, "Starting Video Call with ${selectedUserForCall!!.name}", Toast.LENGTH_SHORT).show()
                } else {
                    permissionState.launchMultiplePermissionRequest()
                }
            }
        )

    }

}
@Composable
fun CallSelectionDialog(
    userName: String,
    onDismiss: () -> Unit,
    onVoiceCall: () -> Unit,
    onVideoCall: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Calling $userName?",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Voice Call Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onVoiceCall,
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFFE3F2FD), CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.phone_call),
                                contentDescription = "Voice Call",
                                tint = Color.Blue
                            )
                        }
                        Text("Voice", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                    // Video Call Button
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onVideoCall,
                            modifier = Modifier
                                .size(60.dp)
                                .background(Color(0xFFE8F5E9), CircleShape)
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.video_call),
                                contentDescription = "Video Call",
                                tint = Color.Green
                            )
                        }
                        Text("Video", fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp))
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = Color.Gray)
                }
            }
        }
    }
}


@Composable
fun CallHistoryItem(call: CallLog, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Profile Image
        AsyncImage(
            model = call.imageUrl,
            contentDescription = null,
            modifier = Modifier
                .size(50.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )

        Spacer(modifier = Modifier.width(16.dp))

        // Name and Time
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = call.name,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = if (call.isMissed) Color.Red else Color.Blue
            )
            Text(
                text = call.time,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }

        // Call Action Button
        IconButton(onClick = onClick) {
            if (call.isVideo) {
                // Use painterResource for custom drawables
                Icon(
                    painter = painterResource(id = R.drawable.videocall),
                    contentDescription = "Video Call",
                    tint = colorResource(id = R.color.black),
                    modifier = Modifier.size(33.dp)
                )
            } else {
                // Use imageVector for default material icons
                Icon(
                    painter = painterResource(id = R.drawable.phonecall),
                    contentDescription = "Audio Call",
                    tint = colorResource(id = R.color.black),
                    modifier = Modifier.size(30.dp)
                )
            }
        }
    }
}

@Composable
@Preview(showBackground = true)
fun CallScreenPreview() {
    val navController = rememberNavController()
    CallScreen(navController = navController)
}