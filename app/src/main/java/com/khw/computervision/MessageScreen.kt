package com.khw.computervision

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter

@Composable
fun MessageScreen(chatViewModel: ChatViewModel, otherUserID: String, otherUserProfile: String) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier.weight(8f)
        ) {
            MessageList(
                chatViewModel,
                messageRef(otherUserID),
                otherUserProfile
            )
        }
        Column(
            modifier = Modifier.weight(1f)
        ) {
            ChatInput(
                chatViewModel,
                messageRef(otherUserID),
                otherUserID
            )
        }
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
    chatViewModel: ChatViewModel = ChatViewModel(),
    messageRef: String,
    otherUserProfile: String
) {
    LaunchedEffect(Unit) {
        chatViewModel.getMessageData(messageRef)
    }
    val messageData by chatViewModel.messageData.observeAsState(emptyList())
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            // IME(Input Method Editor) 즉, 키보드에 대한 padding() 적용
            .imePadding(),
        state = rememberLazyListState(),
    ) {
        val sortChatMessage = messageData.sortedBy { it.date }
        var date = "20240101"
        items(sortChatMessage) { message ->
            if (date != message.date.substring(0, 8)) {
                Text(
                    text = "${message.date.substring(0, 4)}년 " +
                            "${message.date.substring(4, 6)}월 " +
                            "${message.date.substring(6, 8)}일",
                    modifier = Modifier
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray,
                    fontSize = 12.sp
                )
                date = message.date.substring(0, 8)
            }
            if (message.sendUserID == UserIDManager.userID.value) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        DateText(message.date)
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
                }

            } else {

                Row(
                    modifier = Modifier
                ) {
                    val painter = rememberAsyncImagePainter(otherUserProfile)
                    Row(
                        modifier = Modifier.weight(1f),
                    ) {
                        Row(
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Image(
                                painter = painter,
                                contentDescription = null,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(2.dp, color = colorDang),
                            )
                            Text(
                                text = message.message,
                                modifier = Modifier
                                    .padding(4.dp)
                                    .background(
                                        color = Color.LightGray,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                            DateText(message.date)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}


@Composable
fun DateText(date: String) {
    Text(
        text = "${date.substring(8, 10)}시 " +
                "${date.substring(10, 12)}분",
        color = Color.Gray,
        fontSize = 8.sp
    )

}


@Composable
fun ChatInput(
    chatViewModel: ChatViewModel,
    messageRef: String,
    otherUserID: String
) {
    Row(
        modifier = Modifier
            .height(52.dp)
    ) {
        var sendMessage: String? by remember { mutableStateOf(null) }

        OutlinedTextField(
            value = sendMessage ?: "",
            onValueChange = { sendMessage = it },
            colors =
            OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedTextColor = Color.Black,
                unfocusedTextColor = Color.Black,
                focusedBorderColor = colorDang,
                unfocusedBorderColor = colorDang
            ),
            modifier = Modifier.weight(4f)
        )
        Button(
            onClick = {
                sendMessage?.let {
                    chatViewModel.writeNewUser(
                        messageRef, UserIDManager.userID.value, otherUserID, it
                    )
                    sendMessage = null
                }
            },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorDang,
                contentColor = Color.White
            )
        ) {
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