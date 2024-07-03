package com.khw.computervision

import android.content.ContentValues
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.concurrent.TimeUnit

val colorDang = Color(0xFFF3BB66)

// 싱글톤 클래스 정의
object ReLoadingManager {
    var reLoadingValue: MutableState<Boolean> =
        mutableStateOf(false)

    fun reLoading() {
        reLoadingValue.value = !reLoadingValue.value
    }
}

object UserIDManager {
    var userID: MutableState<String> =
        mutableStateOf("")
    var userAddress: MutableState<String> = mutableStateOf("주소 정보가 여기에 표시됩니다.")
}

//240702 김현우 - 서버통신을 위한 함수 UsableFun으로 이동
interface ApiService {
    @Multipart
    @POST("/infer")
    fun uploadImage(
        @Part image: MultipartBody.Part,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>
    ): Call<ResponseBody>

    @Multipart
    @POST("/tryon")
    fun uploadList(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>
    ): Call<ResponseBody>
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.45.140:8080/"

    private val client = OkHttpClient.Builder()
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(60, TimeUnit.SECONDS)
        .build()


    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}

@Composable
fun LogoScreen(activityName: String, goBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (activityName != "Login") {
            Spacer(modifier = Modifier.height(20.dp))
        }
        Text(
            text = "당당근",
            fontSize = 50.sp,
            color = colorDang,
            modifier = Modifier.clickable {
                if (activityName != "Login" && activityName != "Sales") {
                    goBack()
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))

        when (activityName) {
            "Login" -> {
                TextBox("우리 당당하게 팔아요")
            }

            "SignUp" -> {
                TextBox("모두 당당하게 가입하세요") //20240701 하승수 - 회원가입 페이지 추가
            }

            "Sales" -> {
                TextBox("우리 당당하게 보여줘요")
            }

            "Detail" -> {
                TextBox("우리 당당하게 알려줘요")
            }

            "Insert" -> {
                TextBox("우리 당당하게 팔아요")
            }

            "Decorate" -> {
                TextBox("우리 당당하게 꾸며봐요")
            }

            "UserProfile" -> {
                TextBox("우리 당당하게 확인해요")
            }

            "MyUploaded" -> {
                TextBox("나의 게시글을 확인해요")
            }

            "MessageList" -> {
                TextBox("나의 메시지를 확인해요") //07022024 하승수 - 메시지 페이지 추가
            }

            "AiImgGen" -> {
                TextBox("나의 당당하게 꾸며봐요") //07021336 김현우 - AI 이미지 생성 페이지 추가
            }
        }
    }
}

@Composable
fun TextBox(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        color = colorDang
    )
}


@Composable
fun gifImageDecode(name: Int): AsyncImagePainter {
    val context = LocalContext.current

    val mascotImageUri = remember {
        Uri.parse("android.resource://${context.packageName}/${name}")
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(mascotImageUri)
            .size(Size.ORIGINAL)
            .build(),
        imageLoader = imageLoader
    )

    return painter
}

//20240701 하승수 - fun 이름 FunTextButton에서 FunButton으로 변경 (button 함수)
@Composable
fun FunButton(buttonText: String, image: Int?, clickEvent: () -> Unit) {
    Button(
        onClick = { clickEvent() },
        colors = ButtonDefaults.buttonColors(
            colorDang
        )
    ) {
        image?.let { Image(painter = painterResource(id = it), contentDescription = "icon") }
        Text(text = buttonText, color = Color.White)
    }
}

//20240701 하승수 - textbutton 추가 (textbutton 함수)
//@Composable
//fun FunTextButton(buttonText: String, clickEvent: @Composable () -> Unit) {
//    TextButton(
////        onClick = { clickEvent() },
//        onClick = {clickEvent()},
//        colors = ButtonDefaults.buttonColors(
//            Color.White
//        )
//    ) {
//        Text(text = buttonText, color = colorDang)
//    }
//}
//20240703 jkh - clickEvent는 단순히 클릭 이벤트라서 () -> Unit 타입으로 정의
@Composable
fun FunTextButton(buttonText: String, clickEvent: () -> Unit) {
    TextButton(
        onClick = { clickEvent() },
        colors = ButtonDefaults.buttonColors(
            Color.White
        )
    ) {
        Text(text = buttonText, color = colorDang)
    }
}

@Composable
fun getMessage(): Map<String, String> {
    val context = LocalContext.current

    // Firebase에서 데이터를 가져오고, 이 데이터를 상태로 관리합니다.
    // 초기값은 빈 맵(emptyMap)으로 설정합니다.
    val messageMap = produceState<Map<String, String>>(initialValue = emptyMap()) {
        Firebase.firestore.collection(UserIDManager.userID.value)
            .get()
            .addOnSuccessListener { result ->
                // 데이터 가져오기가 성공하면, 문서 ID와 메시지 내용을 맵으로 만듭니다.
                // 결과를 'value'에 할당하여 상태를 업데이트합니다.
                value = result.documents.associate {
                    it.id to "보낸일시: ${
                        it.getString("date").orEmpty()
                    }\n보낸사람: ${
                        it.getString("sendUser").orEmpty()
                    }\n메세지: ${
                        it.getString("message").orEmpty()
                    }"
                }
            }
            .addOnFailureListener { exception ->
                // 데이터 가져오기가 실패하면, 에러 메시지를 토스트로 보여줍니다.
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            }
    }

    // messageMap의 크기(size)를 반환합니다.
    return messageMap.value
}

@Composable
fun GetProduct(reLoading: Boolean, getProductEvent: (Map<String, Map<String, String>>) -> Unit) {
    val context = LocalContext.current

    // LaunchedEffect로 비동기 작업을 처리합니다.
    LaunchedEffect(reLoading) {
        Firebase.firestore.collection("product")
            .get()
            .addOnSuccessListener { result ->
                // 데이터 가져오기가 성공하면, 문서 ID와 필드들을 맵으로 만듭니다.
                val newProductMap = result.documents.associate { document ->
                    val fields = mapOf(
                        "InsertUser" to (document.getString("InsertUser") ?: ""),
                        "name" to (document.getString("name") ?: ""),
                        "date" to (document.getString("date") ?: ""),
                        "dealMethod" to (document.getString("dealMethod") ?: ""),
                        "imageUrl" to (document.getString("imageUrl") ?: ""),
                        "price" to (document.get("price")?.toString() ?: ""),
                        "productDescription" to (document.getString("productDescription")
                            ?: ""),
                        "rating" to (document.get("rating")?.toString() ?: ""),
                        "state" to (document.get("state")?.toString() ?: ""),
                    )
                    document.id to fields
                }
                getProductEvent(newProductMap)
            }
            .addOnFailureListener { exception ->
                // 데이터 가져오기가 실패하면, 에러 메시지를 토스트로 보여줍니다.
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            }
    }
}


fun mapToBundle(map: Map<String, String>): Bundle {
    val bundle = Bundle()
    for ((key, value) in map) {
        bundle.putString(key, value)
    }
    return bundle
}


// Bundle을 Map으로 변환하는 함수
fun bundleToMap(bundle: Bundle): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (key in bundle.keySet()) {
        map[key] = bundle.getString(key).orEmpty()
    }
    return map
}

fun deleteFirestoreData(collectionName: String, documentId: String, successEvent: () -> Unit) {
    Firebase.firestore.collection(collectionName).document(documentId)
        .delete()
        .addOnSuccessListener {
            successEvent()
        }
        .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error deleting document", e) }
}


@Composable
fun ImageGrid(
    category: String,
    successUpload: Boolean,
    onImageClick: (StorageReference, String, String) -> Unit
) {
    val userRef = Firebase.storage.reference.child(UserIDManager.userID.value)
    val storageRef = userRef.child(category)
    val itemsRef = remember { mutableStateListOf<StorageReference>() }
    val itemsUri = remember { mutableStateListOf<String>() }

    LaunchedEffect(successUpload) {
        itemsRef.clear()
        itemsUri.clear()

        coroutineScope {
            val listResult = storageRef.listAll().await()
            val downloadTasks = listResult.items.map { clothRef ->
                async {
                    try {
                        val uri = clothRef.downloadUrl.await().toString()
                        itemsRef.add(clothRef)
                        itemsUri.add(uri)
                    } catch (e: Exception) {
                        // Handle exceptions if needed
                    }
                }
            }
            downloadTasks.forEach { it.await() }

            // 정렬 로직 추가
            val sortedItems =
                itemsRef.zip(itemsUri).sortedBy { it.first.name } // name을 기준으로 정렬
            itemsRef.clear()
            itemsUri.clear()
            sortedItems.forEach {
                itemsRef.add(it.first)
                itemsUri.add(it.second)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 4.dp, start = 2.dp)
    ) {
        (itemsRef zip itemsUri).chunked(5).forEach { item ->
            Row(modifier = Modifier.fillMaxWidth()) {
                item.forEach {
                    Column {
                        GlideImage(
                            imageModel = it.second,
                            contentDescription = "Image",
                            modifier = Modifier
                                .size(80.dp)
                                .clickable {
                                    onImageClick(it.first, it.second, category)
                                },
                            contentScale = ContentScale.FillBounds
                        )
                    }
                }
            }
        }

        if (itemsUri.isEmpty()) {
            Text(text = "Loading image...", modifier = Modifier.padding(16.dp))
        }
    }
}

//07022024 하승수 - 검색 fun 추가
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchTextField(onSearch: (String) -> Unit) {
    var searchQuery by remember { mutableStateOf("") }

    OutlinedTextField(
        value = searchQuery,
        onValueChange = { searchQuery = it },
        label = { Text("검색") },
        modifier = Modifier.size(210.dp, 60.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedTextColor = colorDang,
            focusedBorderColor = colorDang,
            unfocusedTextColor = colorDang,
            unfocusedBorderColor = colorDang,
            focusedLabelColor = colorDang,
            unfocusedLabelColor = colorDang
        ),
        leadingIcon = {
            Icon(
                painter = painterResource(id = R.drawable.search_icon),
                contentDescription = "Search Icon",
                tint = colorDang
            )
        }
    )
}

fun encodeUrl(url: String): String {
    return URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
}
// 20240703 신동환 - 현재 위치 확인하는 함수입니다


fun getLocation(
    fusedLocationProviderClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
    try {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                onLocationReceived(it)
            }
        }
    } catch (e: SecurityException) {
        // 권한이 없는 경우 예외 처리
    }
}

fun getAddressFromLocation(geocoder: Geocoder, location: Location): String? {
    return try {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        Log.d("AddressLookup", "Received addresses: ${addresses?.size}")

        addresses?.firstOrNull()?.let { address ->
            val adminArea = address.adminArea ?: ""
            val locality = address.locality ?: ""
            val subLocality = address.subLocality ?: ""
            val thoroughfare = address.thoroughfare ?: ""
            val subThoroughfare = address.subThoroughfare ?: ""

            // 여기에 로그 추가
            Log.d("AddressLookup", "Address components:")
            Log.d("AddressLookup", "adminArea: $adminArea")
            Log.d("AddressLookup", "locality: $locality")
            Log.d("AddressLookup", "subLocality: $subLocality")
            Log.d("AddressLookup", "thoroughfare: $thoroughfare")
            Log.d("AddressLookup", "subThoroughfare: $subThoroughfare")
            Log.d("AddressLookup", "Full address: ${address.getAddressLine(0)}")


            val result = "$adminArea $subLocality $thoroughfare"
            Log.d("AddressLookup", "Final result: $result")

            result
        }
    } catch (e: Exception) {
        Log.e("AddressLookup", "Error getting address", e)
        null
    }
}


class SharedViewModel : ViewModel() {
    private val _responseData = MutableLiveData<String>()
    val responseData: LiveData<String> get() = _responseData

    fun sendServerRequest(
        topURL: String,
        bottomURL: String,
        gender: String
    ) {
        // 서버 요청 로직
        val userIDPart = RequestBody.create("text/plain".toMediaTypeOrNull(), UserIDManager.userID.value)
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
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    if (response.isSuccessful) {
                        _responseData.postValue(response.body()?.string())
                    } else {
                        _responseData.postValue("Error: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    _responseData.postValue("Request failed: ${t.message}")
                }
            })
    }
}