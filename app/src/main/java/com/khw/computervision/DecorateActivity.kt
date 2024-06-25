package com.khw.computervision

import android.content.Intent
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
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.khw.computervision.ui.theme.ComputerVisionTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    fun DecorateScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val context = LocalContext.current
            LogoScreen(context, "Decorate")
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
            ) {
                Spacer(modifier = Modifier.weight(12f))
                FunTextButton("저장") {
                    context.startActivity(Intent(context, InsertActivity::class.java))
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
                var segmentedImage by remember { mutableStateOf<Bitmap?>(null) }
                var showDialog by remember { mutableStateOf(false) }

                ImagePicker( onImageSelected = { bitmap ->
                    inputImage = bitmap
                })

                inputImage?.let { bitmap ->
                    ImageSegmentation(
                        inputImage = bitmap,
                        onSegmentationComplete = { segmentedBitmap ->
                            segmentedImage = segmentedBitmap
                            showDialog = true
                        }
                    )
                }

                segmentedImage?.let { bitmap ->
                    ImageUploadPopup(
                        showDialog = showDialog,
                        bitmap = bitmap,
                        onUpload = {
                            showDialog = false
                            segmentedImage = null
                        },
                        onCancel = {
                            showDialog = false
                            segmentedImage = null
                        }
                    )
                }
            }
            val pages = listOf("상의", "하의")
            val pagerState = rememberPagerState()
            val coroutineScope = rememberCoroutineScope()

            CustomTabRow(pages, pagerState, coroutineScope)

        }
    }

    @OptIn(ExperimentalPagerApi::class)
    @Composable
    private fun CustomTabRow(
        pages: List<String>,
        pagerState: PagerState,
        coroutineScope: CoroutineScope
    ) {
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
                                .size(48.dp,28.dp)
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

}