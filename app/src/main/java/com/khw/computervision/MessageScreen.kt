package com.khw.computervision

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter

@Composable
fun MessageScreen(chatViewModel: ChatViewModel, otherUserID: String, otherUserProfile: String) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MessageList(
            Modifier.align(Alignment.TopStart),
            chatViewModel,
            messageRef(otherUserID),
            otherUserProfile
        )
        ChatInput(
            Modifier.align(Alignment.BottomCenter),
            chatViewModel,
            messageRef(otherUserID),
            otherUserID
        )
    }
}

fun messageRef(otherUserID: String): String {
    return if (UserIDManager.userID.value >= otherUserID) {
        UserIDManager.userID.value + "&" + otherUserID
    } else {
        otherUserID + "&" + UserIDManager.userID.value
    }
}

@Composable
fun MessageList(
    modifier: Modifier,
    chatViewModel: ChatViewModel = ChatViewModel(),
    messageRef: String,
    otherUserProfile: String
) {
    LaunchedEffect(Unit) {
        chatViewModel.getMessageData(messageRef)
    }
    val messageData by chatViewModel.messageData.observeAsState(emptyList())
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
    ) {
        val sortChatMessage = messageData.sortedBy { it.date }
        items(sortChatMessage) { message ->
            if (message.sendUserID == UserIDManager.userID.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = message.message,
                        modifier = Modifier
                            .padding(4.dp)
                            .background(
                                color = colorDang,
                                shape = RoundedCornerShape(8.dp)
                            ),
                        textAlign = TextAlign.End
                    )
                }

            } else {

                Row(
                    modifier = Modifier
                ) {
                    val painter = rememberAsyncImagePainter(otherUserProfile)
                    Image(
                        painter = painter,
                        contentDescription = null,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .padding(4.dp)
                            .border(2.dp, color = colorDang),
                    )
                    Text(
                        text = message.message,
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .background(
                                color = Color.LightGray,
                                shape = RoundedCornerShape(8.dp)
                            )
                    )
                }
            }
        }
    }
}


@Composable
fun ChatInput(
    modifier: Modifier,
    chatViewModel: ChatViewModel,
    messageRef: String,
    otherUserID: String
) {
    Row(
        modifier = modifier
    ) {
        var sendMessage: String? by remember { mutableStateOf(null) }

        OutlinedTextField(
            value = sendMessage ?: "",
            onValueChange = { sendMessage = it },
        )
        Button(onClick = {
            sendMessage?.let {
                chatViewModel.writeNewUser(
                    messageRef, UserIDManager.userID.value, otherUserID, it
                )
                sendMessage = null
            }
        }) {
            Text(text = "Send")
        }

    }

}

class Message(
    var date: String,
    var sendUserID: String,
    var receiveUserID: String,
    var message: String
)
