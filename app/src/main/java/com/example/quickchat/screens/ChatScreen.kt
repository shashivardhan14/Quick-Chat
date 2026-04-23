package com.example.quickchat.screens


import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.navigation.NavController
import com.example.quickchat.models.ChatMessage
import com.example.quickchat.ui.theme.QuickChatTheme
import com.parse.ParseObject
import com.parse.ParseQuery
import com.parse.ParseUser
import com.parse.livequery.ParseLiveQueryClient
import com.parse.livequery.SubscriptionHandling
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.Locale


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(userId: String,navController: NavController) {
    val currentUser = ParseUser.getCurrentUser()
    val (messages, setMessages) = remember { mutableStateOf(emptyList<ChatMessage>()) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        fetchMessages(currentUser.objectId,userId) { setMessages(it)}
        startLiveQuery(currentUser.objectId, userId, context) {
            setMessages(messages + it)
        }
    }



    QuickChatTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        titleContentColor = MaterialTheme.colorScheme.primary,
                    ),
                    title = {
                        Text(currentUser.username ?: "")
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = {
                                navController.popBackStack()
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                        }
                    },

                    )

            }

        ) { paddingValues ->
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                val (messagesList, chatBox) = createRefs()

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(messagesList) {
                            top.linkTo(parent.top)
                            bottom.linkTo(chatBox.top)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            height = Dimension.fillToConstraints

                        },
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(messages){ChatItem(it)}
                }
                ChatBox(
                    onSend = { messageContent ->
                        scope.launch {
                            val newMessage =
                                sendMessage(currentUser.objectId, userId, messageContent)
                            setMessages(messages + newMessage)
                        }


                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .constrainAs(chatBox) {
                            bottom.linkTo(parent.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                        }
                )

            }
        }

    }
}

@Composable
fun ChatItem(chatMessage: ChatMessage) {
    val isCurrentUser = chatMessage.senderId == ParseUser.getCurrentUser().objectId
    Column(
        modifier = Modifier.padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .align(if (isCurrentUser) Alignment.End else Alignment.Start)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isCurrentUser) 16.dp else 0.dp,
                        bottomEnd = if (isCurrentUser) 0.dp else 16.dp
                    )
                )
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(16.dp)
        ) {
            Column {
                Text(text = chatMessage.message)
                Spacer(modifier = Modifier.size(4.dp))
                Text(text = formatTimestamp(chatMessage.timestamp),
                    style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray), modifier = Modifier.align (
                        Alignment.End
                    )
                )
            }
        }
    }
}

@Composable
fun formatTimestamp(timestamp: Date): String {
 val formatter = remember { java.text.SimpleDateFormat("hh:mm a", Locale.getDefault()) }
    return formatter.format(timestamp)
}

@Composable
fun ChatBox(
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var chatBoxValue by remember { mutableStateOf("") }

    Row(
        modifier = modifier.padding(16.dp)
    ) {
        OutlinedTextField(
            value = chatBoxValue,
            onValueChange = { chatBoxValue = it },
            modifier = Modifier
                .weight(1f)
                .padding(end = 10.dp),
            shape = RoundedCornerShape(28.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
            ),
            placeholder = {
                Text(text = "Type a message...")
            }
        )
        IconButton(
            onClick = {
                if (chatBoxValue.isNotBlank()) {
                    onSend(chatBoxValue)
                    chatBoxValue = ""
                }
            },
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondary)
                .align(Alignment.CenterVertically)
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Send",
                tint = MaterialTheme.colorScheme.onSecondary,
                modifier = Modifier
                    .size(24.dp)
                    .padding(4.dp)

            )
        }
    }
}

private fun startLiveQuery(
    currentUserId: String,
    otherUserId: String,
    context: Context,
    onNewMessage: (ChatMessage) -> Unit
) {
    val client = ParseLiveQueryClient.Factory.getClient()
    val query = ParseQuery.or(
        listOf(
            ParseQuery.getQuery<ParseObject>("Messages")
                .whereEqualTo("sender", ParseUser.createWithoutData("_User", currentUserId))
                .whereEqualTo("receiver", ParseUser.createWithoutData("_User", otherUserId)),
            ParseQuery.getQuery<ParseObject>("Messages")
                .whereEqualTo("sender", ParseUser.createWithoutData("_User", otherUserId))
                .whereEqualTo("receiver", ParseUser.createWithoutData("_User", currentUserId))

        )
    )
    client.subscribe(query).handleEvent(SubscriptionHandling.Event.CREATE){ _,message ->
        (context as? ComponentActivity)?.runOnUiThread{
            onNewMessage(toMessage(message))
        }
    }


}

private fun fetchMessages(
    currentUserId: String,
    otherUserId: String,
    onMessagesFetched: (List<ChatMessage>) -> Unit
) {
    val query = ParseQuery.or(
        listOf(
            ParseQuery.getQuery<ParseObject>("Messages")
                .whereEqualTo("sender", ParseUser.createWithoutData("_User", currentUserId))
                .whereEqualTo("receiver", ParseUser.createWithoutData("_User", otherUserId)),
            ParseQuery.getQuery<ParseObject>("Messages")
                .whereEqualTo("sender", ParseUser.createWithoutData("_User", otherUserId))
                .whereEqualTo("receiver", ParseUser.createWithoutData("_User", currentUserId))


        )
    ).orderByAscending("timestamp")

    query.findInBackground { objects, e ->
        if (e == null) {
            onMessagesFetched(objects.map { toMessage(it) })
        } else {
            e.printStackTrace()
        }

    }
}

private suspend fun sendMessage(
    senderId: String,
    receiverId: String,
    messageContent: String
): ChatMessage {

    return withContext(Dispatchers.IO) {
        val message = ParseObject("Messages").apply {
            put("sender", ParseUser.createWithoutData("_User", senderId))
            put("receiver", ParseUser.createWithoutData("_User", receiverId))
            put("messageContent", messageContent)
            put("timestamp", Date())

        }
        message.saveInBackground()
        toMessage(message)

    }

}

private fun toMessage(parseObject: ParseObject) = ChatMessage(
    id = parseObject.objectId ?: "",
    senderId = parseObject.getParseUser("sender")?.objectId ?: "",
    receiverId = parseObject.getParseUser("receiver")?.objectId ?: "",
    message = parseObject.getString("messageContent") ?: "",
    timestamp = parseObject.getDate("timestamp") ?: Date(),
    parseObject.getParseUser("sender")?.objectId == ParseUser.getCurrentUser().objectId
)



