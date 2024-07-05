package com.khw.computervision

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.gowtham.ratingbar.StepSize
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.TextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.Marker

@Composable
fun ProfilePopup(
    profileUri: String?,
    close: () -> Unit,
    successUpload: () -> Unit
) {
    var addressText by remember { mutableStateOf("주소 정보가 여기에 표시됩니다") }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { close() },
        title = { Text(text = "") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                val fusedLocationClient =
                    remember { LocationServices.getFusedLocationProviderClient(context) }
                val geocoder = remember { Geocoder(context, Locale.KOREA) }
                val coroutineScope = rememberCoroutineScope()

                val requestPermissionLauncher =
                    rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.RequestPermission()
                    ) { isGranted: Boolean ->
                        if (isGranted) {
                            getLocation(fusedLocationClient) { location ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    val address = getAddressFromLocation(geocoder, location)
                                    addressText = address ?: "주소를 찾을 수 없습니다."
                                    withContext(Dispatchers.Main) {
                                        UserIDManager.userAddress.value = addressText
                                    }
                                }
                            }
                        }
                    }

                Text(text = "")

                var inputImage by remember { mutableStateOf<Bitmap?>(null) }

                ProfileImage(profileUri) { inputImage = it }

                inputImage?.let { bitmap ->
                    uploadBitmapImage(context, bitmap, UserIDManager.userID.value, "profile.jpg", {
                        successUpload()
                    }, {
                        inputImage = null
                    })
                }
                Text(text = UserIDManager.userID.value)
                TextButton(onClick = {
                    when {
                        ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED -> {
                            getLocation(fusedLocationClient) { location ->
                                coroutineScope.launch(Dispatchers.IO) {
                                    val address = getAddressFromLocation(geocoder, location)
                                    addressText = address ?: "주소를 찾을 수 없습니다."
                                    withContext(Dispatchers.Main) {
                                        UserIDManager.userAddress.value = addressText
                                    }
                                }
                            }
                        }

                        else -> {
                            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        }
                    }

                }) {
                    Text(text = "현재 위치 확인")
                }
                Text(text = UserIDManager.userAddress.value)
                Spacer(modifier = Modifier.weight(2f))

                val messageMap = getMessage()


                FunButton("내가 올린 제품", image = R.drawable.list_icon) {
                    val productIntent = Intent(context, MyUploadedActivity::class.java)
                    productIntent.putExtra("userID", UserIDManager.userID.value)
                    context.startActivity(productIntent)
                }

//                FunButton("내게 온 메세지 : ${messageMap.size}", null) {
//                    val userIntent = Intent(context, MessageListActivity::class.java)
//                    userIntent.putExtra("messageList", mapToBundle(messageMap))
//                    context.startActivity(userIntent)
//                }

                Spacer(modifier = Modifier.weight(1f))
                FunButton("로그아웃", null) {
//                    context.startActivity(Intent(context, LoginActivity::class.java))
                }
            }
        },
        confirmButton =
        { },
        dismissButton =
        { }
    )
}


fun uploadBitmapImage(
    context: Context,
    bitmap: Bitmap,
    user: String,
    pathName: String,
    successUpload: () -> Unit,
    inputImageNullEvent: () -> Unit
) {

    val mountainsRef = Firebase.storage.reference.child("$user/$pathName")

    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()

    val uploadTask = mountainsRef.putBytes(data)
    uploadTask.addOnSuccessListener {
        Toast.makeText(context, "사진 업로드 성공", Toast.LENGTH_SHORT).show()
        successUpload()
        inputImageNullEvent()
    }.addOnProgressListener {
        Toast.makeText(context, "사진 업로드 중", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener {
        Toast.makeText(context, "사진 업로드 실패", Toast.LENGTH_SHORT).show()
        inputImageNullEvent()
    }
}

@Composable
fun ProfileImage(profileUrl: String?, setInputImage: (Bitmap) -> Unit) {

    val context = LocalContext.current
    val imageCropLauncher =
        rememberLauncherForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                setInputImage(
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        result.uriContent
                    )
                )
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    val cropOption = CropImageContractOptions(
        CropImage.CancelledResult.uriContent,
        CropImageOptions()
    )

    profileUrl?.let {
        GlideImage(
            imageModel = it,
            contentDescription = "Image",
            modifier = Modifier
                .size(160.dp)
                .clickable {
                    imageCropLauncher.launch(cropOption)
                }
                .clip(RoundedCornerShape(120.dp))
        )
    } ?: Image(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = "",
        modifier = Modifier
            .size(160.dp)
            .clickable {
                imageCropLauncher.launch(cropOption)
            }
            .clip(RoundedCornerShape(32.dp))
    )

}

@Composable
fun MessagePopup(
    receiveUser: String,
    close: () -> Unit
) {
    var message: String by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { close() },
        title = { Text(text = "") },
        text = {
            Column(
            ) {
                Text(text = "당당하게 보내세요", color = colorDang)
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = receiveUser,
                    onValueChange = { },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDang,
                        unfocusedBorderColor = colorDang,
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "받는 사람", color = colorDang) },
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDang,
                        unfocusedBorderColor = colorDang,
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "메시지", color = colorDang) },
                    modifier = Modifier.height(320.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                // Create a new user with a first and last name
                val dateTimeNow = LocalDateTime.now().toLocalDate().toString().replace("-", "") +
                        LocalDateTime.now().toLocalTime().toString().replace(":", "")
                            .substring(0, 4)
                val sendMessage = hashMapOf(
                    "sendUser" to UserIDManager.userID.value,
                    "date" to dateTimeNow,
                    "message" to message,
                    "read" to "false"
                )

                val db = Firebase.firestore
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        db.collection(receiveUser)
                            .document(dateTimeNow)
                            .set(sendMessage)
                            .await() // suspend function to await the task completion
                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "DocumentSnapshot successfully written!")
                            Toast.makeText(context, "메세지 전송 성공", Toast.LENGTH_SHORT).show()
                            close()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Log.w(TAG, "Error writing document", e)
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }) {
                Text("Upload")
            }
        },
        dismissButton = {
            Button(onClick = { close() }) {
                Text("Cancel")
            }
        }
    )

}

data class PopupDetails(
    val userID: String,
    val name: String = "",
    var imageUrl: String = "",
    var aiUrl: String = "",
    val category: String = "",
    val price: Int = 0,
    val dealMethod: String = "",
    val rating: Float = 0f,
    val productDescription: String = ""
)

@Composable
fun InsertPopup(
    newPopupDetails: PopupDetails,
    saveData: (PopupDetails) -> Unit,
    close: () -> Unit
) {
    var name: String by remember { mutableStateOf(newPopupDetails.name) }
    val imageUrl: String by remember { mutableStateOf(newPopupDetails.imageUrl) }
    val aiUrl: String by remember { mutableStateOf(newPopupDetails.aiUrl) }
    val category: String by remember { mutableStateOf(newPopupDetails.category) }
    var price: String by remember { mutableStateOf(newPopupDetails.price.toString()) }
    var dealMethod: String by remember { mutableStateOf(newPopupDetails.dealMethod) }
    var rating: Float by remember { mutableFloatStateOf(newPopupDetails.rating) }
    var productDescription: String by remember { mutableStateOf(newPopupDetails.productDescription) }
    AlertDialog(
        onDismissRequest = { close() },
        title = { Text(text = "") },
        text = {
            Column(
            ) {
                var showMapPopup by remember { mutableStateOf(false) }
                if (showMapPopup) {
                    MapPopup(close = { showMapPopup = false })
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
                    label = { Text(text = "제품명", color = colorDang) },
                )

                OutlinedTextField(
                    value =
                    if (price == "0") {
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
                    label = { Text(text = "가격", color = colorDang) },
                )

                OutlinedTextField(
                    value = dealMethod,
                    onValueChange = { dealMethod = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDang,
                        unfocusedBorderColor = colorDang,
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "거래방법", color = colorDang) },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            color = colorDang,
                            width = 1.dp,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically,

                    ) {
                    Spacer(modifier = Modifier.size(16.dp, 0.dp))
                    Text(text = "상태", color = colorDang)
                    Spacer(modifier = Modifier.size(16.dp, 0.dp))
                    RatingBar(
                        value = rating,
                        style = RatingBarStyle.Fill(),
                        stepSize = StepSize.HALF,
                        onValueChange = {
                            rating = it
                        },
                        size = 24.dp,
                        spaceBetween = 4.dp,
                        onRatingChanged = {
                            Log.d("TAG", "onRatingChanged: $it")
                        }
                    )
                }
                Row {
                    Button(onClick = { showMapPopup = true }) {
                        Text("지도 보기")
                    }
                    Text(text = "거래 위치: ${UserIDManager.userAddress.value}")
                }
                OutlinedTextField(
                    value = productDescription,
                    onValueChange = { productDescription = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDang,
                        unfocusedBorderColor = colorDang,
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "제품설명", color = colorDang) },
                    modifier = Modifier.height(320.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
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
                        productDescription
                    )
                )
                close()
            }) {
                Text("Upload")
            }
        },
        dismissButton = {
            Button(onClick = { close() }) {
                Text("Cancel")
            }
        }
    )

}

//20240705 신동환 네이버 지도입니다
@Composable
fun MapPopup(close: () -> Unit) {
    AlertDialog(onDismissRequest = { close() },
        confirmButton = { /*TODO*/ },
        title = { Text(text = "") },
        text = {
            Column {
                NaverMapView()
            }
        },
        dismissButton = {
            Button(onClick = { close() }) {
                Text("닫기")
            }
        })
}

@Composable
fun NaverMapView() {
    val context = LocalContext.current
    var naverMap by remember { mutableStateOf<NaverMap?>(null) }
    var currentLocation by remember { mutableStateOf<LatLng?>(null) }

    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    fun updateMapWithLocation() {
        val map = naverMap
        val location = currentLocation
        if (map != null && location != null) {
            val cameraUpdate = CameraUpdate.scrollTo(location)
            map.moveCamera(cameraUpdate)

            // 현재 위치에 마커 추가
            Marker().apply {
                position = location
                this.map = map
            }
        }
    }

    LaunchedEffect(Unit) {
        // 위치 권한 확인
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val coarseLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )

        if (fineLocationPermission == PackageManager.PERMISSION_GRANTED ||
            coarseLocationPermission == PackageManager.PERMISSION_GRANTED
        ) {
            // 위치 가져오기
            fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                if (task.isSuccessful && task.result != null) {
                    val location = task.result
                    currentLocation = LatLng(location.latitude, location.longitude)
                    updateMapWithLocation()
                } else {
                    // 위치 정보를 가져오는 데 실패한 경우의 처리
                    // 예: 기본 위치 설정 또는 사용자에게 오류 메시지 표시
                    Log.e("NaverMapView", "Failed to get location", task.exception)
                    // 여기에 기본 위치 설정이나 오류 메시지 표시 로직을 추가할 수 있습니다.
                    // 예: currentLocation = LatLng(37.5665, 126.9780) // 서울 시청
                }
            }
        } else {
            // 권한 요청 로직 (Activity나 Fragment에서 처리해야 함)
        }
    }

    AndroidView(
        factory = { content ->
            MapView(content).apply {
                getMapAsync { map ->
                    naverMap = map
                    map.minZoom = 6.0
                    map.maxZoom = 18.0
                    updateMapWithLocation()
                }
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
