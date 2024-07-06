package com.khw.computervision

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDateTime

@Composable
fun MessageScreen(chatViewModel: ChatViewModel, otherUserID: String) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        MessageList(Modifier.align(Alignment.TopStart), chatViewModel, messageRef(otherUserID))
        ChatInput(Modifier.align(Alignment.BottomCenter), chatViewModel, messageRef(otherUserID), otherUserID)
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
    messageRef: String
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
                Text(
                    text = message.date + " : " + message.message,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            } else {
                Text(text = message.date + " : " + message.message)
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
