package com.khw.computervision

import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream

//class DecorateActivity : ComponentActivity() {
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            ComputerVisionTheme {
//
//                var changedUri by remember {
//                    mutableStateOf("")
//                }
//                changedUri = intent.getStringExtra("clickedUri") ?: ""
//
//                DecorateScreen(changedUri)
//            }
//        }
//    }
//
//    @Composable
//    fun DecorateScreen(changedUri: String) {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//        ) {
//
//            var uploadTrigger by remember { mutableStateOf(false) }
//            var clickedRef by remember { mutableStateOf<StorageReference?>(null) }
//            var clickedUri by remember { mutableStateOf<String?>(changedUri) }
//            var clickedCategory by remember { mutableStateOf<String?>(null) }
//            var uploadServerResult by remember { mutableStateOf("") }
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(1f)
//            ) {
//
//                LogoScreen("Decorate") { finish() }
//                Spacer(modifier = Modifier.weight(1f))
//                if (clickedUri != "") {
//                    Row(
//                        modifier = Modifier.fillMaxWidth(),
//                    ) {
//                        Spacer(modifier = Modifier.weight(12f))
//                        val context = LocalContext.current
//                        FunTextButton("다음") {
//                            //240701 김현우 - 이미지 저장 시 InsertActivity로 imageUri 전달 추가
//                            finish()
//                            val userIntent = Intent(context, AiImgGenActivity::class.java)
//                            userIntent.putExtra("clickedUri", clickedUri)
//                            userIntent.putExtra("clickedCategory", clickedCategory)
//                            context.startActivity(userIntent)
//                        }
//                        Spacer(modifier = Modifier.weight(1f))
//                    }
//                }
//            }
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(2f)
//                    .padding(20.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//
//                Text(text = uploadServerResult)
//                clickedUri?.let {
//                    GlideImage(
//                        imageModel = it,
//                        modifier = Modifier.fillMaxSize(),
//                        contentScale = ContentScale.Fit
//                    )
//                }
//
//            }
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(2f),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                var inputImage by remember { mutableStateOf<Bitmap?>(null) }
//                var isLoading by remember { mutableStateOf(false) }
//
//                ImagePicker(onImageSelected = { bitmap ->
//                    inputImage = bitmap
//
//                    sendImageToServer(bitmap) {
//                        uploadServerResult += it
//                        uploadTrigger = !uploadTrigger
//                        inputImage = null
//                        isLoading = false
//                    }
//                    isLoading = true
//                })
//
//                CustomTabRow(uploadTrigger, isLoading)
//                { onClickedRef: StorageReference, onClickedUri: String, onClickedCategory: String ->
//                    clickedRef = onClickedRef
//                    clickedUri = onClickedUri
//                    clickedCategory = onClickedCategory
//                }
//            }
//        }
//    }
//
//    private fun sendImageToServer(
//        bitmap: Bitmap,
//        successEvent: (String) -> Unit
//    ) {
//        val image = bitmapToByteArray(bitmap) // 실제 이미지 파일 경로
//        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), image)
//        val imagePart = MultipartBody.Part.createFormData(
//            "image",
//            "${UserIDManager.userID.value}.png",
//            requestFile
//        )
//        val userIdPart =
//            RequestBody.create("text/plain".toMediaTypeOrNull(), UserIDManager.userID.value)
//
//        val dataMap = mapOf("userID" to userIdPart)
//
//        RetrofitClient.instance.uploadImage(imagePart, dataMap)
//            .enqueue(object : Callback<ResponseBody> {
//                override fun onResponse(
//                    call: Call<ResponseBody>,
//                    response: Response<ResponseBody>
//                ) {
//                    if (response.isSuccessful) {
//                        successEvent("성공: ${response.body()}")
//                    } else {
//                        successEvent("에러 메시지: ${response.errorBody()?.string()}")
//                    }
//                }
//
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    successEvent("요청이 실패했습니다: ${t.message}")
//                }
//            })
//    }
//
//    @OptIn(ExperimentalPagerApi::class)
//    @Composable
//    private fun CustomTabRow(
//        uploadTrigger: Boolean,
//        isLoading: Boolean,
//        onImageClick: (StorageReference, String, String) -> Unit
//    ) {
//        val pages = listOf("상의", "하의")
//        val pagerState = rememberPagerState()
//        val coroutineScope = rememberCoroutineScope()
//
//        TabRow(
//            selectedTabIndex = pagerState.currentPage,
//            indicator = { tabPositions ->
//                TabRowDefaults.Indicator(
//                    Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
//                )
//            },
//            backgroundColor = Color.White,
//            contentColor = Color.Black
//        ) {
//            pages.forEachIndexed { index, title ->
//                Tab(
//                    text = {
//                        Button(
//                            onClick = {
//                                coroutineScope.launch {
//                                    pagerState.scrollToPage(index)
//                                }
//                            },
//                            colors = ButtonDefaults.buttonColors(
//                                contentColor = colorDang,
//                                containerColor = colorDang
//                            )
//                        ) {
//                            Text(
//                                text = title,
//                                color = Color.White,
//                                fontSize = 20.sp,
//                            )
//                        }
//                    },
//                    selected = pagerState.currentPage == index,
//                    onClick = {
//                        coroutineScope.launch {
//                            pagerState.scrollToPage(index)
//                        }
//                    }
//                )
//            }
//        }
//        HorizontalPager(
//            count = pages.size,
//            state = pagerState,
//        ) { page ->
////          loading 이미지
//            if (isLoading) {
//                CircularProgressIndicator()
//            } else {
//                if (page.toString() == "0") {
//                    ImageGrid(
//                        category = "top",
//                        successUpload = uploadTrigger,
//                        onImageClick = onImageClick
//                    )
//                } else if (page.toString() == "1") {
//                    ImageGrid(
//                        category = "bottom",
//                        successUpload = uploadTrigger,
//                        onImageClick = onImageClick
//                    )
//                }
//            }
//        }
//    }
//
//    @Composable
//    fun ImagePicker(
//        onImageSelected: (Bitmap) -> Unit
//    ) {
//        val context = LocalContext.current
//
//        val imageCropLauncher =
//            rememberLauncherForActivityResult(CropImageContract()) { result ->
//                if (result.isSuccessful) {
//                    val bitmap =
//                        MediaStore.Images.Media.getBitmap(
//                            context.contentResolver,
//                            result.uriContent
//                        )
//                    onImageSelected(bitmap)
//                } else {
//                    Log.d("PhotoPicker", "No media selected")
//                }
//            }
//
//        Text(text = "옷 추가",
//            color = Color.White,
//            textAlign = TextAlign.Center,
//            fontSize = 16.sp,
//            modifier = Modifier
//                .fillMaxWidth()
//                .background(colorDang)
//                .clickable {
//                    val cropOption = CropImageContractOptions(
//                        CropImage.CancelledResult.uriContent,
//                        CropImageOptions()
//                    )
//                    imageCropLauncher.launch(cropOption)
//                }
//        )
//    }
//
//    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
//        val byteArrayOutputStream = ByteArrayOutputStream()
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
//        return byteArrayOutputStream.toByteArray()
//    }
//
//}

@Composable
fun DecorateScreen(navController: NavHostController, encodedClickedUrl: String, closetViewModel: ClosetViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        var uploadTrigger by remember { mutableStateOf(false) }
        var clickedCategory by remember { mutableStateOf<String?>(null) }
        var uploadServerResult by remember { mutableStateOf("") }
        var displayedImageUrl by remember { mutableStateOf(encodedClickedUrl) }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
//            LogoScreen("Decorate") { navController.popBackStack() }
            Spacer(modifier = Modifier.weight(1f))
            if (displayedImageUrl.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.weight(12f))

                    //url에 특수문자 되있는 경우가 있어 encode함
                    FunTextButton("다음") {
                        val encodedUrl = encodeUrl(displayedImageUrl)
                        navController.navigate("aiImgGen/$encodedUrl/$clickedCategory")
                    }

                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = uploadServerResult)
            GlideImage(
                imageModel = displayedImageUrl,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var inputImage by remember { mutableStateOf<Bitmap?>(null) }
            var isLoading by remember { mutableStateOf(false) }

            ImagePicker(onImageSelected = { bitmap ->
                inputImage = bitmap

                sendImageToServer(bitmap) {
                    uploadServerResult += it
                    uploadTrigger = !uploadTrigger
                    inputImage = null
                    isLoading = false
                    closetViewModel.getItemsFromFirebase(Firebase.storage.reference.child(UserIDManager.userID.value))
                }
                isLoading = true
            })

            CustomTabRow(uploadTrigger, isLoading, closetViewModel) { _, onClickedUri, onClickedCategory ->
                clickedCategory = onClickedCategory
                displayedImageUrl = onClickedUri // 이미지 클릭 시 화면에 표시할 이미지 URI 업데이트
            }
        }
    }
}


private fun sendImageToServer(
    bitmap: Bitmap,
    successEvent: (String) -> Unit
) {
    val image = bitmapToByteArray(bitmap) // 실제 이미지 파일 경로
    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), image)
    val imagePart = MultipartBody.Part.createFormData(
        "image",
        "${UserIDManager.userID.value}.png",
        requestFile
    )
    val userIdPart =
        RequestBody.create("text/plain".toMediaTypeOrNull(), UserIDManager.userID.value)

    val dataMap = mapOf("userID" to userIdPart)

    RetrofitClient.instance.uploadImage(imagePart, dataMap)
        .enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    successEvent("성공: ${response.body()}")
                } else {
                    successEvent("에러 메시지: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                successEvent("요청이 실패했습니다: ${t.message}")
            }
        })
}

@OptIn(ExperimentalPagerApi::class)
@Composable
private fun CustomTabRow(
    uploadTrigger: Boolean,
    isLoading: Boolean,
    closetViewModel: ClosetViewModel,
    onImageClick: (StorageReference, String, String) -> Unit,
) {
    val pages = listOf("상의", "하의")
    val pagerState = rememberPagerState()
    val coroutineScope = rememberCoroutineScope()

    TabRow(
        selectedTabIndex = pagerState.currentPage,
        indicator = { tabPositions ->
            TabRowDefaults.Indicator(
                Modifier.pagerTabIndicatorOffset(pagerState, tabPositions)
            )
        },
        backgroundColor = Color.White,
        contentColor = Color.Black
    ) {
        pages.forEachIndexed { index, title ->
            Tab(
                text = {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                pagerState.scrollToPage(index)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            contentColor = colorDang,
                            containerColor = colorDang
                        )
                    ) {
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 20.sp,
                        )
                    }
                },
                selected = pagerState.currentPage == index,
                onClick = {
                    coroutineScope.launch {
                        pagerState.scrollToPage(index)
                    }
                }
            )
        }
    }
    HorizontalPager(
        count = pages.size,
        state = pagerState,
    ) { page ->
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            if (page.toString() == "0") {
                ImageGrid(
                    category = "top",
                    onImageClick = onImageClick,
                    closetViewModel = closetViewModel
                )
            } else if (page.toString() == "1") {
                ImageGrid(
                    category = "bottom",
                    onImageClick = onImageClick,
                    closetViewModel = closetViewModel
                )
            }
        }
    }
}

@Composable
fun ImagePicker(
    onImageSelected: (Bitmap) -> Unit,
) {
    val context = LocalContext.current

    val imageCropLauncher =
        rememberLauncherForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        result.uriContent
                    )
                onImageSelected(bitmap)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    Text(text = "옷 추가",
        color = Color.White,
        textAlign = TextAlign.Center,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .background(colorDang)
            .clickable {
                val cropOption = CropImageContractOptions(
                    CropImage.CancelledResult.uriContent,
                    CropImageOptions()
                )
                imageCropLauncher.launch(cropOption)
            }
    )
}

private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
    val byteArrayOutputStream = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
    return byteArrayOutputStream.toByteArray()
}