package com.khw.computervision

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

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

            Text(
                text = "$otherUserID : ${chat.lastMessage}",
                modifier = Modifier.clickable {
                    navController.navigate("messageScreen/$otherUserID")
                }
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