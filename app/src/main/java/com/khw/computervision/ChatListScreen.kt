package com.khw.computervision

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter

@Composable
fun ChatListScreen(navController: NavHostController, chatViewModel: ChatViewModel) {

    LaunchedEffect(Unit) {
        chatViewModel.getChatData()
    }
    val chatListData by chatViewModel.chatData.observeAsState(emptyList())
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        val sortedChatList = chatListData.sortedBy { it.lastMessageDate }
        items(sortedChatList) { chat ->
            val otherUserID = if (chat.sendUserID == UserIDManager.userID.value) {
                chat.receiveUserID
            } else {
                chat.sendUserID
            }
            var otherUserProfile: String? by remember { mutableStateOf(null) }

            LaunchedEffect(Unit) {
                otherUserProfile = getProfile(otherUserID)
            }

            otherUserProfile?.let {
                Row(verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clickable {
                        navController.navigate(
                            "messageScreen/$otherUserID/${
                                encodeUrl(it)
                            }"
                        )
                    }) {
                    val painter = rememberAsyncImagePainter(it)
                    Image(
                        painter = painter,
                        contentDescription = "profile",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier
                            .size(80.dp)
                            .clip(RoundedCornerShape(40.dp))
                            .padding(8.dp)
                    )
                    Column(modifier = Modifier.padding(8.dp)) {
                        Text(
                            text = "상대방: $otherUserID",
                            fontSize = 16.sp,
                            style = TextStyle(lineHeight = 18.sp)
                        )
                        Text(
                            text = "시간: " +
                                    "${chat.lastMessageDate.substring(4, 6)}월 " +
                                    "${chat.lastMessageDate.substring(6, 8)}일 " +
                                    "${chat.lastMessageDate.substring(8, 10)}시 " +
                                    "${chat.lastMessageDate.substring(10, 12)}분",
                            fontSize = 16.sp,
                            style = TextStyle(lineHeight = 18.sp)
                        )
                        Text(
                            text = "내용: ${chat.lastMessage}",
                            maxLines = 1,
                            fontSize = 16.sp,
                            style = TextStyle(lineHeight = 18.sp)
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth(),
                thickness = 1.dp,
                color = colorDang
            )
        }
    }
}

class Chat(
    var sendUserID: String,
    var receiveUserID: String,
    var lastMessageDate: String,
    var lastMessage: String
)