package com.khw.computervision

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
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
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.storage.StorageReference
import com.khw.computervision.ui.theme.ComputerVisionTheme
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.launch
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
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

class DecorateActivity : ComponentActivity() {

    interface ApiService {
        @Multipart
        @POST("/infer")
        fun uploadImage(
            @Part image: MultipartBody.Part,
            @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>
        ): Call<ResponseBody>
    }

    object RetrofitClient {
        private const val BASE_URL = "http://192.168.45.140:8080/"

        private val client = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var userID by remember {
                mutableStateOf("")
            }
            userID = intent.getStringExtra("userID") ?: ""

            ComputerVisionTheme {
                DecorateScreen(userID)
            }
        }
    }

    @Composable
    fun DecorateScreen(userID: String) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {

                LogoScreen("Decorate") { finish() }
                Spacer(modifier = Modifier.weight(1f))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Spacer(modifier = Modifier.weight(12f))
                    FunTextButton("저장") {
                        finish()
                    }
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
            var uploadTrigger by remember { mutableStateOf(false) }
            var clickedRef by remember { mutableStateOf<StorageReference?>(null) }
            var clickedUri by remember { mutableStateOf<String?>(null) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f)
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                clickedUri?.let {
                    GlideImage(
                        imageModel = it,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }

            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                var inputImage by remember { mutableStateOf<Bitmap?>(null) }
                ImagePicker(onImageSelected = { bitmap ->
                    inputImage = bitmap
                })

                var isLoading by remember { mutableStateOf(false) }

                inputImage?.let { bitmap ->

                    sendImageToServer(userID, bitmap) {
                        uploadTrigger = !uploadTrigger
                        inputImage = null
                        isLoading = false
                    }

                    isLoading = true
                }
                CustomTabRow(userID, uploadTrigger, isLoading)
                { onClickedRef: StorageReference, onClickedUri: String ->
                    clickedRef = onClickedRef
                    clickedUri = onClickedUri
                }
            }
        }
    }

    private fun sendImageToServer(
        userID: String,
        bitmap: Bitmap,
        successEvent: () -> Unit
    ) {

        val image = bitmapToByteArray(bitmap) // 실제 이미지 파일 경로
        val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), image)
        val imagePart = MultipartBody.Part.createFormData("image", "$userID.png", requestFile)
        val userIdPart = RequestBody.create("text/plain".toMediaTypeOrNull(), userID)


        val dataMap = mapOf("userID" to userIdPart)

        RetrofitClient.instance.uploadImage(imagePart, dataMap)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        successEvent()
                    } else {
                        println("요청이 실패했습니다. 상태 코드: ${response.code()}")
                        println("에러 메시지: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    println("요청이 실패했습니다: ${t.message}")
                }
            })
    }


    @OptIn(ExperimentalPagerApi::class)
    @Composable
    private fun CustomTabRow(
        userID: String,
        uploadTrigger: Boolean,
        isLoading: Boolean,
        onImageClick: (StorageReference, String) -> Unit
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
//                  loading 이미지
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                if (page.toString() == "0") {

                    ImageGrid(
                        userID = userID,
                        category = "top",
                        successUpload = uploadTrigger,
                        onImageClick = onImageClick
                    )
                } else if (page.toString() == "1") {
                    ImageGrid(
                        userID = userID,
                        category = "bottom",
                        successUpload = uploadTrigger,
                        onImageClick = onImageClick
                    )
                }
            }

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

    private fun bitmapToByteArray(bitmap: Bitmap): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

}