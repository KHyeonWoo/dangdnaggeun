package com.khw.computervision

import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage

@Composable
fun ClosetScreen(
    closetViewModel: ClosetViewModel,
    onBackClick: () -> Unit,
    navController: NavHostController,
    beforeScreen: String?
) {
    BackHandler {
        navController.navigateUp()
    }

    var expandedImage by remember { mutableStateOf<Pair<StorageReference, String>?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val imageCropLauncher =
        rememberLauncherForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                val bitmap = MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    result.uriContent
                )

                sendImageToServer(bitmap) {
                    closetViewModel.getItemsFromFirebase(
                        Firebase.storage.reference.child(
                            UserIDManager.userID.value
                        )
                    )
                    showImagePicker = false
                    isLoading = false

                }
                isLoading = true

            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 나의 옷장 타이틀과 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) {
            if(isLoading) {
                TopBar(
                    title = "나의 옷장",
                    onBackClick = onBackClick,
                    onAddClick = { },
                    addIcon = Icons.Default.Refresh
                )
            } else {
                TopBar(
                    title = "나의 옷장",
                    onBackClick = onBackClick,
                    onAddClick = {
                        startImagePicker(imageCropLauncher)
                    },
                    addIcon = Icons.Default.Add
                )
            }
        }
        HorizontalDivider(color = colorDang, modifier = Modifier.width(350.dp))
        // 상의 섹션
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(5f),
        ) {
            SectionHeader(title = "상의")

            ImageGrid(
                category = "top",
                onImageClick = { ref, url, _ ->
                    if (beforeScreen == "decorate") {
                        val encodedUrl = encodeUrl(url)
                        navController.navigate("decorate/$encodedUrl/top")
                    } else if (beforeScreen == "bottomNav") {
                        expandedImage = Pair(ref, url)
                    }
                },
                closetViewModel = closetViewModel
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(5f),
        ) {
            HorizontalDivider(color = colorDang, modifier = Modifier.width(350.dp))
            // 하의 섹션
            SectionHeader(title = "하의")

            ImageGrid(
                category = "bottom",
                onImageClick = { ref, url, _ ->
                    if (beforeScreen == "decorate") {
                        val encodedUrl = encodeUrl(url)
                        navController.navigate("decorate/$encodedUrl/bottom")
                    } else if (beforeScreen == "bottomNav") {
                        expandedImage = Pair(ref, url)
                    }
                },
                closetViewModel = closetViewModel
            )
        }
    }

    expandedImage?.let { (ref, url) ->
        ExpandedImageDialog(url = url, onDismiss = { expandedImage = null })
    }
}


@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        modifier = Modifier
            .padding(8.dp)
            .background(colorDang)
            .padding(8.dp)
            .fillMaxWidth(),
        textAlign = TextAlign.Center
    )
}

@Composable
fun ExpandedImageDialog(url: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Adjusting image size to fill more of the dialog space
            LoadImageFromUrl(
                url = url,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(fraction = 0.7f) // Set the image to fill 90% of the dialog size
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
fun LoadImageFromUrl(url: String, modifier: Modifier = Modifier) {
    Image(
        painter = rememberAsyncImagePainter(model = url),
        contentDescription = null,
        modifier = modifier
    )
}

// 외부에서 호출될 때마다 실행할 수 있도록 함수로 분리
fun startImagePicker(imageCropLauncher: ActivityResultLauncher<CropImageContractOptions>) {
    val cropOption = CropImageContractOptions(
        null, // 초기값 설정
        CropImageOptions()
    )
    imageCropLauncher.launch(cropOption)
}