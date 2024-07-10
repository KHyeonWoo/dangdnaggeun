package com.khw.computervision.chat

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.khw.computervision.Chat
import com.khw.computervision.ChatViewModel
import com.khw.computervision.HorizontalDividerColorDang
import com.khw.computervision.R
import com.khw.computervision.UserIDManager
import com.khw.computervision.colorBack
import com.khw.computervision.encodeUrl
import com.khw.computervision.getProfile

@Composable
fun ChatListScreen(navController: NavHostController, chatViewModel: ChatViewModel) {

    LaunchedEffect(Unit) {
        chatViewModel.getChatData()
    }
    val chatListData by chatViewModel.chatData.observeAsState(emptyList())
    Column(
        Modifier
            .fillMaxSize()
            .background(colorBack),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            val sortedChatList = chatListData.sortedByDescending { it.lastMessageDate }
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

                    Row(verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.CenterHorizontally)
                            .padding(16.dp, 8.dp)
                            .clickable {
                                navController.navigate(
                                    "messageScreen/$otherUserID/${
                                        otherUserProfile?.let { encodeUrl(it) } ?: " "
                                    }"
                                )
                            }) {
                        Spacer(modifier = Modifier.weight(1f))
                        ProfileImage(otherUserProfile)
                        Spacer(modifier = Modifier.weight(1f))
                        LastMessage(chat, otherUserID)
                        Spacer(modifier = Modifier.weight(1f))
                    }
                HorizontalDividerColorDang(16.dp, 16.dp, 8.dp, 8.dp)
            }
        }
    }
}

@Composable
private fun LastMessage(chat: Chat, otherUserID: String?) {
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

@Composable
private fun ProfileImage(otherUserProfile: String?){
    val painter = if(otherUserProfile == null) {
        painterResource(id = R.drawable.dangkki_img_noback)
    } else {
        rememberAsyncImagePainter(otherUserProfile)
    }

    Image(
        painter = painter,
        contentDescription = "profile",
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
            .size(80.dp)
            .clip(RoundedCornerShape(80.dp))
    )
}