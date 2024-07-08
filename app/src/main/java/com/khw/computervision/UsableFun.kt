package com.khw.computervision

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime


val colorDang = Color(0xFFF3BB66)
val colorDong = Color(0xFF714809)
//val colorBack = Color(0xFFF5F5EB)
val colorBack = Color(0xFFfbf6dd)
val customFont = FontFamily(Font(R.font.santokki_regular, FontWeight.Normal))

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

@Composable
fun LogoScreen(activityName: String, goBack: () -> Unit) {
    Box(
        modifier = Modifier.background(colorBack)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//            if (activityName != "Login") {
//                Spacer(modifier = Modifier.height(20.dp))
//            }

//            Text(
//                text = "당당하게 거래해요",
//                fontSize = 20.sp,
//                color = colorDong,
//                fontFamily = customFont,
//                modifier = Modifier.clickable {
//                    if (activityName != "Login" && activityName != "Sales") {
//                        goBack()
//                    }
//                }
//            )

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
}

@Composable
fun TextBox(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        color = colorDang
    )
}

suspend fun getProfile(userID: String): String? {
    val storageRef = Firebase.storage.reference.child("${userID}/profile.jpg")
    return try {
        storageRef.downloadUrl.await().toString()
    } catch (e: Exception) {
        null
    }
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
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { clickEvent() },
            colors = ButtonDefaults.buttonColors(
                colorDong
            )
        ) {
            image?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = "icon",
                    tint = Color.White,
                    modifier = Modifier.padding(5.dp,0.dp)
                )
            }
            Text(text = buttonText, color = Color.White)
        }
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
            Color.Transparent
        )
    ) {
        Text(text = buttonText,
            color = Color.Black,
            fontWeight = FontWeight.Bold)
    }
}

@Composable
fun GetProduct(
    reLoading: Boolean,
    getProductEvent: (Map<String, Map<String, String>>) -> Unit
) {
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
                        "aiUrl" to (document.getString("aiUrl") ?: ""),
                        "category" to (document.getString("category") ?: ""),
                        "price" to (document.get("price")?.toString() ?: ""),
                        "productDescription" to (document.getString("productDescription")
                            ?: ""),
                        "rating" to (document.get("rating")?.toString() ?: ""),
                        "liked" to (document.get("liked")?.toString() ?: ""),
                        "viewCount" to (document.get("viewCount")?.toString() ?: ""),
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
    onImageClick: (StorageReference, String, String) -> Unit,
    closetViewModel: ClosetViewModel
) {
    val itemsRef: List<StorageReference> by if (category == "top") {
        closetViewModel.topsRefData.observeAsState(emptyList())
    } else {
        closetViewModel.bottomsRefData.observeAsState(emptyList())
    }

    val itemsUrl: List<String> by if (category == "top") {
        closetViewModel.topsUrlData.observeAsState(emptyList())
    } else {
        closetViewModel.bottomsUrlData.observeAsState(emptyList())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 4.dp, start = 2.dp)
    ) {
        val rowSize: Int = 4
        itemsRef.zip(itemsUrl).chunked(rowSize).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start, // 왼쪽 정렬
                verticalAlignment = Alignment.Top
            ) { // 상단 정렬
                rowItems.forEach { (ref, url) ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f) // 정사각형 비율 유지
                            .padding(4.dp)
                    ) {
                        ImageItem(
                            url = url,
                            ref = ref,
                            category = category,
                            onImageClick = onImageClick
                        )
                    }
                }

                // 빈 공간 채우기
                repeat(rowSize - rowItems.size) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .padding(4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ImageItem(
    url: String,
    ref: StorageReference,
    category: String,
    onImageClick: (StorageReference, String, String) -> Unit
) {
    Column {
        GlideImage(
            imageModel = url,
            contentDescription = "Image",
            modifier = Modifier
                .size(80.dp)
                .clickable {
                    onImageClick(ref, url, category)
                },
            contentScale = ContentScale.FillBounds
        )
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
            val addressLine = address.getAddressLine(0) ?: ""

            // 여기에 로그 추가
            Log.d("AddressLookup", "Address components:")
            Log.d("AddressLookup", "adminArea: $adminArea")
            Log.d("AddressLookup", "locality: $locality")
            Log.d("AddressLookup", "subLocality: $subLocality")
            Log.d("AddressLookup", "thoroughfare: $thoroughfare")
            Log.d("AddressLookup", "subThoroughfare: $subThoroughfare")
            Log.d("AddressLookup", "Full address: $addressLine")

            val result = "$adminArea $subLocality $thoroughfare"
            Log.d("AddressLookup", "Final result: $result")

            result
        }
    } catch (e: Exception) {
        Log.e("AddressLookup", "Error getting address", e)
        null
    }
}

@Composable
fun TopBar(
    title: String,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    addIcon: ImageVector?,
    showBackIcon: Boolean = true
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(showBackIcon){
                IconButton(onClick = onBackClick) {
                    androidx.compose.material.Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorDong
                    )
                }
            }
            Spacer(modifier = Modifier.weight(.1f))
            androidx.compose.material.Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorDong,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(5f))
            if (addIcon != null) {
                IconButton(onClick = onAddClick) {
                    androidx.compose.material.Icon(
                        addIcon,
                        contentDescription = "Add",
                        tint = colorDong
                    )
                }
            }
        }
        HorizontalDivider(
            color = colorDang,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
        )
    }
}

fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable() (() -> Unit)? = null,
    placeholder: @Composable() (() -> Unit)? = null,
    leadingIcon: @Composable() (() -> Unit)? = null,
    trailingIcon: @Composable() (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp) // 텍스트 상하 여백 줄이기
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = TextStyle(
                fontSize = 16.sp, // 원하는 글자 크기로 설정
                color = Color.Black
            ),
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            cursorBrush = SolidColor(Color.Black),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            decorationBox = { innerTextField ->
                // OutlinedTextField 스타일의 테두리를 적용
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    placeholder = placeholder,
                    label = label,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    singleLine = singleLine,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = remember { MutableInteractionSource() },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colorDang,  // 테두리 색상을 하얀색으로 설정
                        focusedBorderColor = colorDang     // 포커스된 상태의 테두리 색상을 하얀색으로 설정
                    ),
                    contentPadding = PaddingValues(0.dp) // 내부 여백을 0으로 설정
                )
            }
        )
    }
}

fun saveEvent(
    coroutineScope: CoroutineScope,
    context: Context,
    productKey: String?,
    newPopupDetails: PopupDetails
) {
    coroutineScope.launch(Dispatchers.IO) {

        val db = Firebase.firestore
        val dateTimeNow =
            productKey ?: (LocalDateTime.now().toLocalDate().toString().replace("-", "") +
                    LocalDateTime.now().toLocalTime().toString().replace(":", "")
                        .substring(0, 4))

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
