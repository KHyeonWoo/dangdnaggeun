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
import androidx.compose.foundation.layout.fillMaxSize
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
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.segmentation.subject.SubjectSegmentation
import com.google.mlkit.vision.segmentation.subject.SubjectSegmenterOptions
import com.khw.computervision.ui.theme.ComputerVisionTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@Suppress("DEPRECATION")
class MainActivity : ComponentActivity() {
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

            ImagePicker(Modifier.fillMaxSize(), onImageSelected = { bitmap ->
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

