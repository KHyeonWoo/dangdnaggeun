package com.khw.computervision

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun ChatListScreen(navController: NavHostController) {
    val database =
        Firebase.database("https://dangdanggeun-1b552-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    ChatList(navController, modifier = Modifier, database = database)

}

@Composable
fun ChatList(navController: NavHostController, modifier: Modifier, database: DatabaseReference) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        val userID = UserIDManager.userID.value.replace(".", "")
            .replace("#", "")
            .replace("$", "")
            .replace("[", "")
            .replace("]", "")
            .replace("@", "")

        val chatMessages = MutableStateFlow<List<Chat>>(emptyList())
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatMessage = arrayListOf<Chat>()
                val chatData =
                    snapshot.getValue<HashMap<String, HashMap<String, HashMap<String, String>>>>()
                // snapshot은 hashMap 형태로 오기때문에 객체 형태로 변환해줘야함
                chatData?.forEach { (key, value) ->
                    if (key.contains(userID)) {
                        value.forEach { (date, messageData) ->
                            if (date == value.keys.maxOrNull()) {
                                chatMessage.add(
                                    Chat(
                                        sendUserID = messageData["sendUserID"] as String,
                                        receiveUserID = messageData["receiveUserID"] as String,
                                        lastMessageDate = key as String,
                                        lastMessage = messageData["message"] as String
                                    )
                                )
                            }
                        }
                    }
                }
                chatMessages.value = arrayListOf()
                chatMessages.value = chatMessage.toList()
//                Log.d("변화 리스너2", chatMessages.value.toString())
            }

            override fun onCancelled(error: DatabaseError) {
//                Log.d(TAG, "loadMessage:onCancelled", error.toException())
            }
        }
        database.addValueEventListener(postListener)
        val chatMessage by chatMessages.collectAsState()
        chatMessage.forEach {
            val otherUserID = if (it.sendUserID == UserIDManager.userID.value) {
                it.receiveUserID
            } else {
                it.sendUserID
            }
            Text(text = "$otherUserID : ${it.lastMessage}",
                modifier = Modifier.clickable {
                    navController.navigate("messageScreen/$otherUserID")
                })
        }

    }
}

class Chat(
    var sendUserID: String,
    var receiveUserID: String,
    var lastMessageDate: String,
    var lastMessage: String
)


fun chatListRef(): DatabaseReference {

    val database =
        Firebase.database("https://dangdanggeun-1b552-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    return database
}
