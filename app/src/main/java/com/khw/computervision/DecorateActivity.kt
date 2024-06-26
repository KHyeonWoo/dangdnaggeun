package com.khw.computervision

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.khw.computervision.ui.theme.ComputerVisionTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class DecorateActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                DecorateScreen()
            }
        }
    }

    @Composable
    fun DecorateScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            LogoScreen("Decorate")
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.weight(12f))
                val context = LocalContext.current
                FunTextButton("저장") {
                    finish()
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = R.drawable.character2),
                    contentDescription = "",
                    modifier = Modifier
                        .size(320.dp)
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var inputImage by remember { mutableStateOf<Bitmap?>(null) }
                ImagePicker(onImageSelected = { bitmap ->
                    inputImage = bitmap
                })

                var isLoading by remember { mutableStateOf(false) }
                var responseMessage by remember { mutableStateOf("") }

                inputImage?.let { bitmap ->

                    sendImageToServer(bitmap) {
                        responseMessage += "\n" + it
                        isLoading = false
                        inputImage = null
                    }

                    isLoading = true
// 서버에서 받은 이미지는 Segmentation을 통해 배경 제거 후 firebase에 저장
//                    ImageSegmentation(
//                        inputImage = bitmap,
//                        onSegmentationComplete = { segmentedBitmap ->
//                            segmentedImage = segmentedBitmap
//                            showDialog = true
//                        }
//                    )
                }

                Text(text = responseMessage)
                if (isLoading) {
                    CircularProgressIndicator()
                }

//                var segmentedImage by remember { mutableStateOf<Bitmap?>(null) }
//                var showDialog by remember { mutableStateOf(false) }
//                segmentedImage?.let { bitmap ->
//                    ImageUploadPopup(
//                        showDialog = showDialog,
//                        bitmap = bitmap,
//                        onUpload = {
//                            showDialog = false
//                            segmentedImage = null
//                        },
//                        onCancel = {
//                            showDialog = false
//                            segmentedImage = null
//                        }
//                    )
//                }
            }
            CustomTabRow()
        }
    }

    private fun sendImageToServer(
        bitmap: Bitmap,
        responseEvent: (String) -> Unit
    ) {
        setRetrofit() // 레트로핏 세팅

        val image = bitmapToByteArray(bitmap) // 실제 이미지 파일 경로
        val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), image)
        val body = MultipartBody.Part.createFormData("image", "image.png", requestFile)

        mRetrofitAPI.uploadImage(body).enqueue(object : Callback<ImageResponseBody> {
            override fun onResponse(
                call: Call<ImageResponseBody>,
                response: Response<ImageResponseBody>
            ) {
                if (response.isSuccessful) {
                    val uploadResponse = response.body()
                    if (uploadResponse != null) {
                        responseEvent("이미지 서버 전송 성공: class_label=${uploadResponse.classLabel}, cropped_image_url=${uploadResponse.croppedImageUrl}")
                    } else {
                        responseEvent("응답은 성공했지만 본문이 없습니다.")
                    }
                } else {
                    responseEvent("이미지 서버 전송 실패: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ImageResponseBody>, t: Throwable) {
                t.printStackTrace()
                responseEvent("이미지 서버 전송 실패: ${t.message}")
            }
        })
    }

    class ImageResponseBody(
        val classLabel: Int,
        val croppedImageUrl: String
    )

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    private fun CustomTabRow() {
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
                        Text(
                            text = title,
                            color = Color.White,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .background(colorDang)
                                .size(48.dp, 28.dp)
                        )
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
            Text(
                modifier = Modifier.wrapContentSize(),
                text = page.toString(),
                textAlign = TextAlign.Center,
                fontSize = 30.sp
            )
        }

    }

    @Composable
    fun ImagePicker(
        onImageSelected: (Bitmap) -> Unit
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

    @Composable
    fun ImageSegmentation(
        inputImage: Bitmap,
        onSegmentationComplete: (Bitmap) -> Unit
    ) {
        var loading: Boolean by remember { mutableStateOf(true) }
        val coroutineScope = rememberCoroutineScope()

        LaunchedEffect(inputImage) {
            coroutineScope.launch {
                val output = withContext(Dispatchers.Default) {
                    ImageSegmentationHelper.getResult(inputImage)
                }
                if (output != null) {
                    onSegmentationComplete(output)
                }
                loading = false
            }
        }

        if (loading) {
            CircularProgressIndicator()
        }
    }

    object ImageSegmentationHelper {

        // Options for configuring the SubjectSegmenter
        private val options = SubjectSegmenterOptions.Builder()
            .enableForegroundConfidenceMask()
            .enableForegroundBitmap()
            .build()

        // SubjectSegmenter instance initialized with the specified options
        private val segmenter = SubjectSegmentation.getClient(options)

        /**
         * Asynchronously processes the given input Bitmap image and retrieves the foreground segmentation result.
         *
         * @param image The input image in Bitmap format to be segmented.
         * @return A suspend function that, when invoked, provides the result Bitmap of the foreground segmentation.
         * @throws Exception if there is an error during the segmentation process.
         */
        suspend fun getResult(image: Bitmap) = suspendCoroutine {
            // Convert the input Bitmap image to InputImage format
            val inputImage = InputImage.fromBitmap(image, 0)

            // Process the input image using the SubjectSegmenter
            segmenter.process(inputImage)
                .addOnSuccessListener { result ->
                    // Resume the coroutine with the foreground Bitmap result on success
                    it.resume(result.foregroundBitmap)
                }
                .addOnFailureListener { e ->
                    // Resume the coroutine with an exception in case of failure
                    it.resumeWithException(e)
                }
        }
    }

    @Composable
    fun ImageUploadPopup(
        showDialog: Boolean,
        bitmap: Bitmap,
        onUpload: () -> Unit,
        onCancel: () -> Unit
    ) {
        if (showDialog) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { onCancel() },
                title = { androidx.compose.material3.Text("Upload Image") },
                text = { Image(bitmap = bitmap.asImageBitmap(), contentDescription = null) },
                confirmButton = {
                    Button(onClick = onUpload) {
                        androidx.compose.material3.Text("Upload")
                    }
                },
                dismissButton = {
                    Button(onClick = onCancel) {
                        androidx.compose.material3.Text("Cancel")
                    }
                }
            )
        }
    }

    interface RetrofitAPI {
        @Multipart
        @POST("infer")
        fun uploadImage(@Part image: MultipartBody.Part): Call<ImageResponseBody>
    }

    private var baseUrl = "http://192.168.45.162:8080" // 레트로핏의 기훈 주소
//    private var baseUrl = "http://192.168.45.205:8080" // 레트로핏의 동환 주소

    private lateinit var mRetrofit: Retrofit // 사용할 레트로핏 객체입니다.
    private lateinit var mRetrofitAPI: RetrofitAPI // 레트로핏 api객체입니다.

    private fun setRetrofit() {
        mRetrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        mRetrofitAPI = mRetrofit.create(RetrofitAPI::class.java)
    }

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

}