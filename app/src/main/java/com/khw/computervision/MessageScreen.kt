package com.khw.computervision

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import java.time.LocalDateTime

@Composable
fun MessageScreen(otherUserID: String) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val database = messageRef(otherUserID)
        MessageList(Modifier.align(Alignment.TopStart), database)
        ChatInput(Modifier.align(Alignment.BottomCenter), database, otherUserID)
    }
}

fun messageRef(otherUserID: String): DatabaseReference {
    var chatRef = if (UserIDManager.userID.value >= otherUserID) {
        UserIDManager.userID.value + "&" + otherUserID
    } else {
        otherUserID + "&" + UserIDManager.userID.value
    }
    chatRef = chatRef.replace(".", "")
        .replace("#", "")
        .replace("$", "")
        .replace("[", "")
        .replace("]", "")
        .replace("@", "")

    val database =
        Firebase.database("https://dangdanggeun-1b552-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child(
            chatRef
        )
    return database
}

@Composable
fun MessageList(modifier: Modifier, database: DatabaseReference) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        var chatMessages = MutableStateFlow<List<Message>>(emptyList())
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatMessage = arrayListOf<Message>()
                val messageData = snapshot.getValue<HashMap<String, HashMap<String, String>>>()
                // snapshot은 hashMap 형태로 오기때문에 객체 형태로 변환해줘야함
                messageData?.forEach { (key, value) ->
                    chatMessage.add(
                        Message(
                            date = key as String,
                            sendUserID = value["sendUserID"] as String,
                            receiveUserID = value["receiveUserID"] as String,
                            message = value["message"] as String
                        )
                    )
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
            if (it.sendUserID == UserIDManager.userID.value) {
                Text(
                    text = it.date + " : " + it.message,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.End
                )
            } else {
                Text(text = it.date + " : " + it.message)
            }
        }

    }
}


@Composable
fun ChatInput(modifier: Modifier, database: DatabaseReference, otherUserID: String) {
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
                val date =
                    LocalDateTime.now().toLocalDate().toString().replace("-", "") +
                            LocalDateTime.now().toLocalTime().toString().replace(":", "")
                                .substring(0, 6)
                writeNewUser(
                    database.child(date), date, UserIDManager.userID.value, otherUserID, it
                )
                sendMessage = null
            }
        }) {
            Text(text = "Send")
        }

    }

}

fun writeNewUser(
    database: DatabaseReference,
    date: String,
    sendUserId: String,
    receiveUserID: String,
    message: String
) {
    val user = Message(
        date,
        sendUserId,
        receiveUserID,
        message
    )

    database.setValue(user)
}

class Message(
    var date: String,
    var sendUserID: String,
    var receiveUserID: String,
    var message: String
)
