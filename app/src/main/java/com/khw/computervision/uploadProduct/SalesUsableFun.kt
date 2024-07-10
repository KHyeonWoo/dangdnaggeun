package com.khw.computervision.uploadProduct

import com.khw.computervision.server.ChatGPTMessage
import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.gowtham.ratingbar.StepSize
import com.khw.computervision.BuildConfig
import com.khw.computervision.PopupDetails
import com.khw.computervision.ProductViewModel
import com.khw.computervision.R
import com.khw.computervision.UserIDManager
import com.khw.computervision.colorDang
import com.khw.computervision.colorDong
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.khw.computervision.server.sendChatGPTRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.tasks.await
import java.io.IOException
import java.util.Locale

@Composable
fun SavePopup(
    newPopupDetails: PopupDetails, saveData: (PopupDetails) -> Unit, close: () -> Unit
) {
    var name: String by remember { mutableStateOf(newPopupDetails.name) }
    val imageUrl: String by remember { mutableStateOf(newPopupDetails.imageUrl) }
    val aiUrl: String by remember { mutableStateOf(newPopupDetails.aiUrl) }
    val category: String by remember { mutableStateOf(newPopupDetails.category) }
    var price: String by remember { mutableStateOf(newPopupDetails.price.toString()) }
    var dealMethod: String by remember { mutableStateOf(newPopupDetails.dealMethod) }
    var rating: Float by remember { mutableFloatStateOf(newPopupDetails.rating) }
    var productDescription: String by remember { mutableStateOf(newPopupDetails.productDescription) }
    var address: String? by remember { mutableStateOf(newPopupDetails.address) }  // 주소 상태 추가

    val coroutineScope = rememberCoroutineScope()
    val isFormValid = name.isNotBlank() && price.isNotBlank() && dealMethod.isNotBlank()

    AlertDialog(onDismissRequest = { close() }, title = { Text(text = "") }, text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
            var showMapPopup by remember { mutableStateOf(false) }
            if (showMapPopup) {
                // 주소 업데이트 콜백을 MapPopup에 전달
                MapPopup(close = { showMapPopup = false }, onAddressSelected = { selectedAddress ->
                    address = selectedAddress
                })
            }
            Spacer(modifier = Modifier.height(20.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorDang,
                    unfocusedBorderColor = colorDang,
                ),
                textStyle = TextStyle(color = Color.Black),
                label = { Text(text = "제품명", color = colorDong) },
                shape = RoundedCornerShape(8.dp),
            )

            OutlinedTextField(
                value = if (price == "0") {
                    ""
                } else {
                    price
                },
                onValueChange = { price = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorDang,
                    unfocusedBorderColor = colorDang,
                ),
                textStyle = TextStyle(color = Color.Black),
                label = { Text(text = "가격", color = colorDong) },
                shape = RoundedCornerShape(8.dp)
            )

            OutlinedTextField(
                value = dealMethod,
                onValueChange = { dealMethod = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorDang,
                    unfocusedBorderColor = colorDang,
                ),
                textStyle = TextStyle(color = Color.Black),
                label = { Text(text = "거래방법", color = colorDong) },
                modifier = Modifier.padding(bottom = 8.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
//                    .border(
//                        color = colorDang, width = 1.dp, shape = RoundedCornerShape(8.dp))
                , verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(16.dp, 0.dp))
                Text(text = "상태:", color = colorDong)
                Spacer(modifier = Modifier.size(16.dp, 0.dp))
                RatingBar(value = rating,
                    style = RatingBarStyle.Fill(),
                    stepSize = StepSize.HALF,
                    onValueChange = { rating = it },
                    size = 24.dp,
                    spaceBetween = 4.dp,
                    onRatingChanged = { Log.d("TAG", "onRatingChanged: $it") })
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.baseline_gps_fixed_24),
                    contentDescription = "gps",
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { showMapPopup = true }
                )
                // 거래 위치에 address를 표시
                Text(
                    text = "거래 위치:${address ?: "위치를 선택해주세요"}",
                    fontSize = 16.sp,
                    color = colorDong,
//                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            OutlinedTextField(
                value = productDescription,
                onValueChange = { productDescription = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorDang,
                    unfocusedBorderColor = colorDang,
                ),
                textStyle = TextStyle(color = Color.Black),
                label = { Text(text = "제품설명\n게시글 (판매 이유, 구입 장소, 기타 등등)", color = colorDong) },
                modifier = Modifier.height(320.dp),
                shape = RoundedCornerShape(10.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(end = 8.dp)) {
                    Text(
                        text = "아이콘을 클릭하면 ChatGPT가 글을 대신 써줘요!",
                        fontSize = 12.sp,
                        color = colorDong,
                        style = TextStyle(lineHeight = 12.sp) // Adjust line height as needed
                    )
                    Text(
                        text = "(제품명/가격/거래 방법/상태 입력 필수)",
                        fontSize = 10.sp,
                        color = colorDong,
                        fontWeight = FontWeight.Black,
                        style = TextStyle(lineHeight = 12.sp) // Adjust line height as needed
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.gpt_icon),
                    contentDescription = "gpt",
                    modifier = Modifier
                        .size(25.dp)
                        .clickable {
                            if (isFormValid) {
                                coroutineScope.launch {
                                    val apiKey = BuildConfig.OPENAI_API_KEY
                                    val prompt = """
                                    다음 세부 정보를 기반으로 사람들이 제대로 된 상품 정보를 확인하게끔
                                    판매글을 한글로 작성해줘.

                                    - Product Name: $name
                                    - Price: $price
                                    - Deal Method: $dealMethod
                                    - 상태(5점만점) : $rating
                                """.trimIndent()

                                    val messages = listOf(
                                        ChatGPTMessage(role = "user", content = prompt)
                                    )
                                    try {
                                        val response = sendChatGPTRequest(apiKey, messages)
                                        withContext(Dispatchers.Main) {
                                            productDescription =
                                                response.choices.first().message.content
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            // 요청 실패했을 때
                                        }
                                    }
                                }
                            }
                        }
                )

            }
        }
    }, buttons = {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = { close() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorDong, // 버튼의 배경색
                    contentColor = Color.White  // 버튼 내용(텍스트)의 색상
                )
            ) {
                Text("취소")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    saveData(
                        PopupDetails(
                            UserIDManager.userID.value,
                            name,
                            imageUrl,
                            aiUrl,
                            category,
                            price.toInt(),
                            dealMethod,
                            rating,
                            productDescription,
                            address ?: ""
                        )
                    )
                    close()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorDong  // 버튼 내용(텍스트)의 색상
                )
            ) {
                Text("등록")
            }
        }
    })
}

//20240707 신동환 네이버 지도입니다
@Composable
fun MapPopup(close: () -> Unit, onAddressSelected: (String) -> Unit) {
    var address by remember { mutableStateOf<String?>(null) }

    Dialog(onDismissRequest = { close() }) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            color = MaterialTheme.colors.background
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                NaverMapView(address = address, onAddressChange = { newAddress ->
                    address = newAddress
                })
                Spacer(modifier = Modifier.height(16.dp))
                Row {
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = {
                        address?.let {
                            val addressPart = extractAddressPart(it)
                            Log.d("Addreees", addressPart)
                            onAddressSelected(addressPart)
                        }  // 선택된 주소를 콜백으로 전달
                        close()
                    }) {
                        Text("확인")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Button(onClick = { close() }) {
                        Text("닫기")
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
fun extractAddressPart(address: String): String {
    val regex = "서울특별시(.*)".toRegex()
    val matchResult = regex.find(address)
    return matchResult?.groups?.get(1)?.value?.trim() ?: address
}
@Composable
fun NaverMapView(
    address: String?,
    onAddressChange: (String) -> Unit
) {
    val context = LocalContext.current
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }
    var marker by remember { mutableStateOf<Marker?>(null) }
    val handler = remember { Handler(Looper.getMainLooper()) }
    var isCameraMoving by remember { mutableStateOf(false) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun updateMapWithLocation() {
        val map = naverMap
        val location = currentLocation
        if (map != null && location != null) {
            val cameraUpdate = CameraUpdate.scrollTo(location)
            map.moveCamera(cameraUpdate)
        }
    }

    fun updateMarkerLocation(location: LatLng) {
        val map = naverMap
        if (map != null) {
            marker?.map = null // Remove previous marker if any
            marker = Marker().apply {
                position = location
                icon = MarkerIcons.BLACK
                iconTintColor = colorDang.toArgb()
                this.map = map
            }
        }
    }

    fun fetchAddress(location: LatLng) {
        // Reverse geocode to get address
        val geocoder = Geocoder(context, Locale.getDefault())
        try {
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                onAddressChange(addresses[0].getAddressLine(0))
                Log.d("Lookup", "Received addresses: ${addresses.size}")
            } else {
                onAddressChange("주소를 찾을 수 없습니다")
            }
        } catch (e: IOException) {
            onAddressChange("주소를 가져오는 데 실패했습니다")
            Log.e("NaverMapView", "Geocoder failed", e)
        }
    }

    LaunchedEffect(Unit) {
        // 위치 권한 확인
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED || coarseLocationPermission == PackageManager.PERMISSION_GRANTED) {
            // 위치 가져오기
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    currentLocation = LatLng(location.latitude, location.longitude)
                    updateMapWithLocation()
                } else {
                    // 위치 정보를 가져오는 데 실패한 경우의 처리
                    Log.e("NaverMapView", "Failed to get location", task.exception)
                }
            }
        } else {
            // 권한 요청 로직 (Activity나 Fragment에서 처리해야 함)
        }
    }

    Column {
        AndroidView(
            factory = { content ->
                MapView(content).apply {
                    getMapAsync { map ->
                        naverMap = map
                        map.minZoom = 6.0
                        map.maxZoom = 18.0
                        map.addOnCameraChangeListener { reason, animated ->
                            val targetLocation = map.cameraPosition.target
                            updateMarkerLocation(targetLocation)
                            isCameraMoving = true
                            handler.removeCallbacksAndMessages(null)
                            handler.postDelayed({
                                fetchAddress(targetLocation)
                                isCameraMoving = false
                            }, 100) // Adjust the delay as needed
                        }
                        updateMapWithLocation()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp) // You can adjust the height as needed
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = if (isCameraMoving) "주소를 가져오는 중..." else address ?: "주소를 가져오는 중...",
            modifier = Modifier.padding(16.dp)
        )
    }
}


suspend fun insertLiked(productsViewModel: ProductViewModel, productName: String) {

    val likedProduct = hashMapOf(
        "liked" to "true"
    )

    Firebase.firestore.collection("${UserIDManager.userID.value}liked").document(productName)
        .set(likedProduct).addOnSuccessListener {}.addOnFailureListener {}.await()
    productsViewModel.getLikedFromFireStore()

}

fun deleteLiked(productsViewModel: ProductViewModel, productKey: String) {
    Firebase.firestore.collection("${UserIDManager.userID.value}liked").document(productKey)
        .delete().addOnSuccessListener {}.addOnFailureListener {}
    productsViewModel.getLikedFromFireStore()
}

fun updateProductLike(
    productsViewModel: ProductViewModel,
    productKey: String,
    totalLiked: Map<String, String>?,
    likedCount: Int
) {
    Firebase.firestore.collection("favoriteProduct").document(productKey)
        .update("liked", totalLiked?.get("liked")?.let { it.toInt() + likedCount } ?: 0)
    productsViewModel.getTotalLikedFromFireStore()
}


fun saveEvent(
    coroutineScope: CoroutineScope,
    context: Context,
    dateTimeNow: String,
    newPopupDetails: PopupDetails
) {
    coroutineScope.launch(Dispatchers.IO) {

        val db = Firebase.firestore
        val sendMessage = hashMapOf(
            "InsertUser" to UserIDManager.userID.value,
            "name" to newPopupDetails.name,
            "date" to dateTimeNow,
            "imageUrl" to newPopupDetails.imageUrl,
            "aiUrl" to newPopupDetails.aiUrl,
            "category" to newPopupDetails.category,
            "price" to newPopupDetails.price,
            "dealMethod" to newPopupDetails.dealMethod,
            "rating" to newPopupDetails.rating,
            "productDescription" to newPopupDetails.productDescription,
            "state" to 1, //1: 판매중, 2: 판매완료, 3:숨기기, 4:삭제
            "address" to newPopupDetails.address,
        )

        db.collection("product")
            .document(dateTimeNow)
            .set(sendMessage)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                Toast.makeText(context, "업로드 성공", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error writing document", e)
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }


        val favoriteProduct = hashMapOf(
            "liked" to 0,
            "viewCount" to 0
        )

        db.collection("favoriteProduct")
            .document(dateTimeNow)
            .set(favoriteProduct)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                Toast.makeText(context, "업로드 성공", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error writing document", e)
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
    }
}
