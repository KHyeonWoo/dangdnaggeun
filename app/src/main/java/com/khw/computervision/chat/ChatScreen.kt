package com.khw.computervision.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.khw.computervision.ChatViewModel
import com.khw.computervision.Message
import com.khw.computervision.R
import com.khw.computervision.TopBar
import com.khw.computervision.UserIDManager
import com.khw.computervision.colorBack
import com.khw.computervision.colorChat
import com.khw.computervision.colorDang
import com.khw.computervision.colorDong

@Composable
fun MessageScreen(
    navController: NavHostController,
    chatViewModel: ChatViewModel,
    otherUserID: String,
    otherUserProfile: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            TopBar(
                title = otherUserID,
                onBackClick = { navController.popBackStack() },
                onAddClick = { /*TODO*/ },
                addIcon = null
            )
            MessageList(
                chatViewModel,
                Modifier.weight(8f),
                messageRef(otherUserID),
                otherUserProfile
            )
            ChatInput(
                chatViewModel,
                messageRef(otherUserID),
                otherUserID
            )
        }
    }
}

private fun messageRef(otherUserID: String): String {
    return if (UserIDManager.userID.value >= otherUserID) {
        UserIDManager.userID.value + "&" + otherUserID
    } else {
        otherUserID + "&" + UserIDManager.userID.value
    }
}

@Composable
private fun MessageList(
    chatViewModel: ChatViewModel = ChatViewModel(),
    modifier: Modifier,
    messageRef: String,
    otherUserProfile: String
) {
    Column(
        modifier = modifier
            .padding(8.dp)
    ) {
        LaunchedEffect(Unit) {
            chatViewModel.getMessageData(messageRef)
        }
        val messageData by chatViewModel.messageData.observeAsState(emptyList())
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            state = rememberLazyListState(),
        ) {
            val sortChatMessage = messageData.sortedBy { it.date }
            var date = "20240101"
            items(sortChatMessage) { message ->
                MessageDate(message, date) { date = it }
                if (message.sendUserID == UserIDManager.userID.value) {
                    MyMessage(message)
                } else {
                    OtherMessage(message, otherUserProfile)
                }
            }
        }
    }
}

@Composable
fun MessageDate(message: Message, date: String, changeDate: (String) -> Unit) {

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
        changeDate(message.date.substring(0, 8))
    }
}

@Composable
fun OtherMessage(message: Message, otherUserProfile: String) {
    Row(
        modifier = Modifier.padding(4.dp)
    ) {
        Row(
            modifier = Modifier.weight(2f),
            verticalAlignment = Alignment.Bottom
        ) {
            val painter = if (otherUserProfile == " ") {
                painterResource(id = R.drawable.dangkki_img_noback)
            } else {
                rememberAsyncImagePainter(otherUserProfile)
            }
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, color = colorDang),
            )
            Spacer(modifier = Modifier.padding(1.dp))
            Box(
                modifier = Modifier
                    .background(Color.LightGray, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = message.message,
                    modifier = Modifier
                        .padding(4.dp)
                )
            }
            DateText(message.date)
        }
        Spacer(modifier = Modifier.weight(1f))
    }

}

@Composable
fun MyMessage(message: Message) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier.weight(2f),
            verticalAlignment = Alignment.Bottom
        ) {
            Spacer(modifier = Modifier.weight(1f))
            DateText(message.date)
            Box(
                modifier = Modifier
                    .background(Color(0xFFF9D7A5), RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = message.message,
                    modifier = Modifier
                        .padding(4.dp)
                )
            }
        }
    }
}


@Composable
private fun DateText(date: String) {
    Text(
        text = "${date.substring(8, 10)}시 " +
                "${date.substring(10, 12)}분",
        color = Color.Gray,
        fontSize = 8.sp
    )

}


@Composable
private fun ChatInput(
    chatViewModel: ChatViewModel,
    messageRef: String,
    otherUserID: String
) {
    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            var sendMessage: String? by remember { mutableStateOf(null) }

            OutlinedTextField(
                value = sendMessage ?: "",
                onValueChange = { sendMessage = it },
                colors =
                OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colorChat,
                    unfocusedContainerColor = colorChat,
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = colorDang
                )
            )
            Spacer(modifier = Modifier.weight(0.1f))
            Button(
                onClick = {
                    sendMessage?.let {
                        chatViewModel.writeNewUser(
                            messageRef, UserIDManager.userID.value, otherUserID, it
                        )
                        sendMessage = null
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .background(color = Color.Transparent, shape = CircleShape)
                    .border(1.dp, color = colorChat, shape = CircleShape),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent,
                    contentColor = colorDong
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.send_icon),
                    contentDescription = "send"
                )
            }
        }
    }
}
