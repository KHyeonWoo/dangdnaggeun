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
fun ChatScreen() {
    val otherUserID = "test@intel.com"
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        val database = databaseRef(otherUserID)
        ChatList(Modifier.align(Alignment.TopStart), database)
        ChatInput(Modifier.align(Alignment.BottomCenter), database)
    }
}

fun databaseRef(otherUserID: String): DatabaseReference {
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
fun ChatList(modifier: Modifier, database: DatabaseReference) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        var chatMessages = MutableStateFlow<List<Post>>(emptyList())
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatMessage = arrayListOf<Post>()
                val messageData = snapshot.getValue<HashMap<String, HashMap<String, String>>>()
                // snapshot은 hashMap 형태로 오기때문에 객체 형태로 변환해줘야함
                messageData?.forEach { (key, value) ->
                    chatMessage.add(
                        Post(
                            date = key,
                            userID = value["userID"] as String,
                            message = value["message"] as String
                        )
                    )
                }
                chatMessages.value = arrayListOf()
                chatMessages.value = chatMessage.toList()
            }

            override fun onCancelled(error: DatabaseError) {
            }
        }
        database.addValueEventListener(postListener)
        val chatMessage by chatMessages.collectAsState()
        chatMessage.forEach {
            if(it.userID == UserIDManager.userID.value) {
                Text(text = it.date + " : " + it.message, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.End)
            } else {
                Text(text = it.date + " : " + it.message)
            }
        }

    }
}

data class Post(var date: String, var userID: String, var message: String)

@Composable
fun ChatInput(modifier: Modifier, database: DatabaseReference) {
    Row(
        modifier = modifier
    ) {
        var sendMessage: String? by remember { mutableStateOf(null) }

        OutlinedTextField(
            value = sendMessage ?: "",
            onValueChange = { sendMessage = it }
        )
        Button(onClick = {
            sendMessage?.let {
                writeNewUser(
                    database.child(
                        LocalDateTime.now().toLocalDate().toString().replace("-", "") +
                                LocalDateTime.now().toLocalTime().toString().replace(":", "")
                                    .substring(0, 6)
                    ), UserIDManager.userID.value, it
                )
            }
        }) {
            Text(text = "Send")
        }

    }

}

fun writeNewUser(database: DatabaseReference, userId: String, message: String) {
    val user = Post(
        LocalDateTime.now().toLocalDate().toString().replace("-", "") +
                LocalDateTime.now().toLocalTime().toString().replace(":", "")
                    .substring(0, 6), userId, message
    )

    database.setValue(user)
}