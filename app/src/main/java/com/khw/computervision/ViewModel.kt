package com.khw.computervision

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.getValue
import com.google.firebase.database.ktx.database
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.khw.computervision.server.RetrofitClient
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime

data class PopupDetails(
    val userID: String,
    val name: String = "",
    var imageUrl: String = "",
    var aiUrl: String = "",
    val category: String = "",
    val price: Int = 0,
    val dealMethod: String = "",
    val rating: Float = 0f,
    val productDescription: String = "",
    val address: String = ""
)

class Chat(
    var sendUserID: String,
    var receiveUserID: String,
    var lastMessageDate: String,
    var lastMessage: String
)

class Message(
    var date: String,
    var sendUserID: String,
    var receiveUserID: String,
    var message: String
)

class ProductViewModel : ViewModel() {
    private val _productsData = MutableLiveData<Map<String, Map<String, String>>>()
    val productsData: LiveData<Map<String, Map<String, String>>> get() = _productsData

    fun getProductsFromFireStore() {
        viewModelScope.launch {
            fetchProducts(_productsData)
        }
    }

    private suspend fun fetchProducts(
        productsData: MutableLiveData<Map<String, Map<String, String>>>
    ) {
        resetProductsData()
        try {
            val productResult = Firebase.firestore.collection("product").get().await()
            // 데이터 가져오기가 성공하면, 문서 ID와 필드들을 맵으로 만듭니다.
            val newProductMap = productResult.documents.associate { document ->
                val fields = mapOf(
                    "InsertUser" to (document.getString("InsertUser") ?: ""),
                    "name" to (document.getString("name") ?: ""),
                    "date" to (document.getString("date") ?: ""),
                    "dealMethod" to (document.getString("dealMethod") ?: ""),
                    "address" to (document.get("address")?.toString() ?: ""),
                    "imageUrl" to (document.getString("imageUrl") ?: ""),
                    "aiUrl" to (document.getString("aiUrl") ?: ""),
                    "category" to (document.getString("category") ?: ""),
                    "price" to (document.get("price")?.toString() ?: ""),
                    "productDescription" to (document.getString("productDescription") ?: ""),
                    "rating" to (document.get("rating")?.toString() ?: ""),
                    "liked" to (document.get("liked")?.toString() ?: "")
                )
                document.id to fields
            }
            productsData.postValue(newProductMap)
        } catch (e: Exception) {
            // 데이터 가져오기가 실패하면, 에러 메시지를 토스트로 보여줍니다.
            //Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
        }

    }

    // Method to reset responseData
    private fun resetProductsData() {
        _productsData.value = mapOf()
    }

    private val _likedData = MutableLiveData<List<String>>()
    val likedData: LiveData<List<String>> get() = _likedData

    fun getLikedFromFireStore() {
        viewModelScope.launch {
            fetchLikes(_likedData)
        }
    }

    private suspend fun fetchLikes(
        likedData: MutableLiveData<List<String>>
    ) {
        resetLikedData()
        try {
            val likedResult =
                Firebase.firestore.collection("${UserIDManager.userID.value}liked").get().await()
            // 데이터 가져오기가 성공하면, 문서 ID와 필드들을 맵으로 만듭니다.
            val newLickedList = likedResult.documents.map { document ->
                document.id
            }
            likedData.postValue(newLickedList)
        } catch (e: Exception) {
            // 데이터 가져오기가 실패하면, 에러 메시지를 토스트로 보여줍니다.
            e.printStackTrace()
        }
    }

    // Method to reset responseData
    private fun resetLikedData() {
        _likedData.value = listOf()
    }

    private val _totalLikedData = MutableLiveData<Map<String, Map<String, String>>>()
    val totalLikedData: LiveData<Map<String, Map<String, String>>> get() = _totalLikedData

    fun getTotalLikedFromFireStore() {
        viewModelScope.launch {
            fetchTotalLikes(_totalLikedData)
        }
    }

    private suspend fun fetchTotalLikes(
        totalLikedData: MutableLiveData<Map<String, Map<String, String>>>
    ) {
        resetTotalLikedData()
        try {
            val totalLikedResult = Firebase.firestore.collection("favoriteProduct").get().await()
            // 데이터 가져오기가 성공하면, 문서 ID와 필드들을 맵으로 만듭니다.
            val newTotalLikedMap = totalLikedResult.documents.associate { document ->
                val fields = mapOf(
                    "liked" to (document.get("liked")?.toString() ?: ""),
                    "viewCount" to (document.get("viewCount")?.toString() ?: ""),
                )
                document.id to fields
            }
            totalLikedData.postValue(newTotalLikedMap)
        } catch (e: Exception) {
            // 데이터 가져오기가 실패하면, 에러 메시지를 토스트로 보여줍니다.
            //Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
        }
    }

    // Method to reset responseData
    private fun resetTotalLikedData() {
        _totalLikedData.value = mapOf()
    }
}


class ClosetViewModel : ViewModel() {
    private val _topsRefData = MutableLiveData<List<StorageReference>>()
    private val _topsUrlData = MutableLiveData<List<String>>()
    val topsRefData: LiveData<List<StorageReference>> get() = _topsRefData
    val topsUrlData: LiveData<List<String>> get() = _topsUrlData

    private val _bottomsRefData = MutableLiveData<List<StorageReference>>()
    private val _bottomsUrlData = MutableLiveData<List<String>>()
    val bottomsRefData: LiveData<List<StorageReference>> get() = _bottomsRefData
    val bottomsUrlData: LiveData<List<String>> get() = _bottomsUrlData

    fun getItemsFromFirebase(storageRef: StorageReference) {
        viewModelScope.launch {
            fetchItems(storageRef.child("top"), _topsRefData, _topsUrlData)
            fetchItems(storageRef.child("bottom"), _bottomsRefData, _bottomsUrlData)
        }
    }

    private suspend fun fetchItems(
        categoryRef: StorageReference,
        refLiveData: MutableLiveData<List<StorageReference>>,
        urlLiveData: MutableLiveData<List<String>>
    ) {
        resetResponseData()
        val itemsRef = mutableListOf<StorageReference>()
        val itemsUrl = mutableListOf<String>()

        try {
            val listResult = categoryRef.listAll().await()
            listResult.items.forEach { clothRef ->
                try {
                    val url = clothRef.downloadUrl.await().toString()
                    itemsRef.add(clothRef)
                    itemsUrl.add(url)
                } catch (e: Exception) {
                    // Handle individual downloadUrl failure if needed
                }
            }
            refLiveData.postValue(itemsRef)
            urlLiveData.postValue(itemsUrl)
        } catch (e: Exception) {
            // Handle listAll failure if needed
        }
    }

    // Method to reset responseData
    private fun resetResponseData() {
        _topsRefData.value = listOf()
        _topsUrlData.value = listOf()
        _bottomsRefData.value = listOf()
        _bottomsUrlData.value = listOf()
    }
}


class AiViewModel : ViewModel() {
    private val _responseData = MutableLiveData<String?>()
    val responseData: LiveData<String?> get() = _responseData

    fun sendServerRequest(
        topURL: String,
        bottomURL: String,
        gender: String
    ) {
        // 서버 요청 로직
        val userIDPart =
            RequestBody.create("text/plain".toMediaTypeOrNull(), UserIDManager.userID.value)
        val topURLPart = RequestBody.create("text/plain".toMediaTypeOrNull(), topURL)
        val bottomURLPart = RequestBody.create("text/plain".toMediaTypeOrNull(), bottomURL)
        val genderPart = RequestBody.create("text/plain".toMediaTypeOrNull(), gender)

        val dataMap = mapOf(
            "userID" to userIDPart,
            "topURL" to topURLPart,
            "bottomURL" to bottomURLPart,
            "gender" to genderPart
        )

        RetrofitClient.instance.uploadList(dataMap)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        _responseData.postValue(response.body()?.string()?.replace("\"", ""))
                    } else {
                        _responseData.postValue("Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _responseData.postValue("Request failed: ${t.message}")
                }
            })
    }

    // Method to reset responseData
    fun resetResponseData() {
        _responseData.value = null
    }
}

class ChatViewModel : ViewModel() {
    private val database =
        Firebase.database("https://dangdanggeun-1b552-default-rtdb.asia-southeast1.firebasedatabase.app").reference

    private val _chatData = MutableLiveData<List<Chat>>(emptyList())
    val chatData: LiveData<List<Chat>> get() = _chatData

    fun getChatData() {
        resetChatData()
        val userIDValue = replaceUserIDToRef(UserIDManager.userID.value)
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatList = arrayListOf<Chat>()
                val chatData =
                    snapshot.getValue<HashMap<String, HashMap<String, HashMap<String, String>>>>()
                chatData?.forEach { (key, value) ->
                    if (key.contains(userIDValue)) {
                        value.forEach { (date, messageData) ->
                            if (date == value.keys.maxOrNull()) {
                                chatList.add(
                                    Chat(
                                        sendUserID = messageData["sendUserID"] as String,
                                        receiveUserID = messageData["receiveUserID"] as String,
                                        lastMessageDate = date as String,
                                        lastMessage = messageData["message"] as String
                                    )
                                )
                            }
                        }
                    }
                }
                _chatData.postValue(chatList.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                // Log.d(TAG, "loadMessage:onCancelled", error.toException())
            }
        }
        database.addValueEventListener(postListener)
    }

    private fun resetChatData() {
        _chatData.value = arrayListOf()
    }

    private val _messageData = MutableLiveData<List<Message>>(emptyList())
    val messageData: LiveData<List<Message>> get() = _messageData

    fun getMessageData(messageRef: String) {
        resetMessageData()
        val chatRef = replaceUserIDToRef(messageRef)
        val postListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatMessage = arrayListOf<Message>()
                val messageData = snapshot.getValue<HashMap<String, HashMap<String, String>>>()
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
                _messageData.postValue(chatMessage.toList())
            }

            override fun onCancelled(error: DatabaseError) {
                // Log.d(TAG, "loadMessage:onCancelled", error.toException())
            }
        }
        database.child(chatRef).addValueEventListener(postListener)
    }

    fun writeNewUser(
        messageRef: String,
        sendUserId: String,
        receiveUserID: String,
        message: String
    ) {
        val chatRef = replaceUserIDToRef(messageRef)
        val date =
            LocalDateTime.now().toLocalDate().toString().replace("-", "") +
                    LocalDateTime.now().toLocalTime().toString().replace(":", "")
                        .substring(0, 6)
        val user = Message(
            date,
            sendUserId,
            receiveUserID,
            message
        )

        database.child(chatRef).child(date).setValue(user)
    }
    private fun resetMessageData() {
        _messageData.value = arrayListOf()
    }
}

class SalesViewModel : ViewModel() {
    private val _clickedUrlData = MutableLiveData<String>()
    val clickedUrlData: LiveData<String> get() = _clickedUrlData

    fun setClickedUrl(data: String) {
        _clickedUrlData.postValue(data)
    }


    private val _categoryData = MutableLiveData<String>()
    val categoryData: LiveData<String> get() = _categoryData

    fun setCategoryData(data: String) {
        _categoryData.postValue(data)
    }


    private val _genderData = MutableLiveData<Boolean>()
    val genderData: LiveData<Boolean> get() = _genderData

    fun setGenderData(data: Boolean) {
        _genderData.postValue(data)
    }
}
fun replaceUserIDToRef(ref: String): String {
    return ref.replace("[.#$\\[\\]@]".toRegex(), "")
}