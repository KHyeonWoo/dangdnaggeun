package com.khw.computervision

import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.gson.JsonObject
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
import okhttp3.ResponseBody
import okio.ByteString
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

@Suppress("DEPRECATION")
class DetectionActivity : ComponentActivity() {

    interface RetrofitAPI {
        @Multipart
        @POST("infer")
        fun uploadImage(@Part image: MultipartBody.Part): Call<ResponseBody>
    }

    private var baseUrl = "http://192.168.45.205:8080" // 레트로핏의 기본 주소

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComputerVisionTheme {
                MainScreen()
            }
        }
    }

    @Composable
    fun MainScreen() {
        Column {
            var inputImage by remember { mutableStateOf<Bitmap?>(null) }
            var segmentedImage by remember { mutableStateOf<Bitmap?>(null) }
            var showDialog by remember { mutableStateOf(false) }

            ImagePicker(Modifier, onImageSelected = { bitmap ->
                inputImage = bitmap
            })


            var isLoading by remember { mutableStateOf(false) }
            var responseMessage by remember { mutableStateOf("") }

            Button(onClick = {
                inputImage?.let {bitmap ->
                    setRetrofit() // 레트로핏 세팅
                    val image = bitmapToByteArray(bitmap) // 실제 이미지 파일 경로
                    val requestFile = RequestBody.create("image/jpeg".toMediaTypeOrNull(), image)
                    val body = MultipartBody.Part.createFormData("image", "image.png", requestFile)

                    mRetrofitAPI.uploadImage(body).enqueue(object : Callback<ResponseBody> {
                        override fun onResponse(
                            call: Call<ResponseBody>,
                            response: Response<ResponseBody>
                        ) {
                            responseMessage = "업로드 성공: ${response.message()}"
                            isLoading = false
                        }

                        override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                            t.printStackTrace()
                            responseMessage = "업로드 실패: ${t.message}"
                            isLoading = false
                        }
                    })

                    isLoading = true
                }
            }) {
                Text("이미지 업로드")
            }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Text(responseMessage)
            }

            inputImage?.let { bitmap ->
                ImageSegmentation(
                    inputImage = bitmap,
                    onSegmentationComplete = { segmentedBitmap ->
                        segmentedImage = segmentedBitmap
                        showDialog = true
                    }
                )
            }
//
//            segmentedImage?.let { bitmap ->
//                ImageUploadPopup(
//                    showDialog = showDialog,
//                    bitmap = bitmap,
//                    onUpload = {
//                        showDialog = false
//                        segmentedImage = null
//                    },
//                    onCancel = {
//                        showDialog = false
//                        segmentedImage = null
//                    }
//                )
//            }


        }
    }

    @Composable
    fun ImagePicker(
        modifier: Modifier,
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

        Image(
            painter = painterResource(id = R.drawable.addicon),
            contentDescription = "add",
            modifier = modifier
                .size(72.dp)
                .padding(end = 16.dp)
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
            AlertDialog(
                onDismissRequest = { onCancel() },
                title = { Text("Upload Image") },
                text = { Image(bitmap = bitmap.asImageBitmap(), contentDescription = null) },
                confirmButton = {
                    Button(onClick = onUpload) {
                        Text("Upload")
                    }
                },
                dismissButton = {
                    Button(onClick = onCancel) {
                        Text("Cancel")
                    }
                }
            )
        }
    }


}

