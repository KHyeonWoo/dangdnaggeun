package com.khw.computervision

import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

@Composable
fun CustomImageGridPage(
    isLoading: Boolean,
    closetViewModel: ClosetViewModel,
    onImageClick: (StorageReference, String, String) -> Unit,
    onBackClick: () -> Unit,
    onAddClick: (Bitmap) -> Unit,
    navController: NavHostController
) {
    BackHandler {
        navController.navigateUp()
    }

    var expandedImage by remember { mutableStateOf<Pair<StorageReference, String>?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }

    if (showImagePicker) {
        LaunchImagePicker(onImageSelected = {
            onAddClick(it)
            showImagePicker = false
            sendImageToServer(it) { result ->
                // Handle server response if neededㅇㅇ
                Log.d("Server Response", result)
                if (result.startsWith("성공")) {
                    closetViewModel.getItemsFromFirebase(FirebaseStorage.getInstance().reference.child(UserIDManager.userID.value))
                }
            }
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 나의 옷장 타이틀과 버튼
        TopBar(title = "나의 옷장",
            onBackClick = onBackClick,
            onAddClick = { showImagePicker = true },
            addIcon = Icons.Default.Add)
        HorizontalDivider(color = colorDang, modifier = Modifier.width(350.dp))
        // 상의 섹션
        SectionHeader(title = "상의")

        ImageGridLimited(
            category = "top",
            onImageClick = { ref, url, category ->
                expandedImage = Pair(ref, url)
            },
            closetViewModel = closetViewModel
        )

        HorizontalDivider(color = colorDang, modifier = Modifier.width(350.dp))
        // 하의 섹션
        SectionHeader(title = "하의")

        ImageGridLimited(
            category = "bottom",
            onImageClick = { ref, url, category ->
                expandedImage = Pair(ref, url)
            },
            closetViewModel = closetViewModel
        )
    }

    expandedImage?.let { (ref, url) ->
        ExpandedImageDialog(url = url, onDismiss = { expandedImage = null })
    }
}



@Composable
fun LaunchImagePicker(onImageSelected: (Bitmap) -> Unit) {
    val context = LocalContext.current

    val imageCropLauncher =
        rememberLauncherForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                val bitmap = MediaStore.Images.Media.getBitmap(
                    context.contentResolver,
                    result.uriContent
                )
                onImageSelected(bitmap)
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    LaunchedEffect(Unit) {
        val cropOption = CropImageContractOptions(
            CropImage.CancelledResult.uriContent,
            CropImageOptions()
        )
        imageCropLauncher.launch(cropOption)
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
fun ImageGridLimited(
    category: String,
    onImageClick: (StorageReference, String, String) -> Unit,
    closetViewModel: ClosetViewModel
) {
    val itemsRefState = if (category == "top") {
        closetViewModel.topsRefData.observeAsState(emptyList())
    } else {
        closetViewModel.bottomsRefData.observeAsState(emptyList())
    }

    val itemsUrlState = if (category == "top") {
        closetViewModel.topsUrlData.observeAsState(emptyList())
    } else {
        closetViewModel.bottomsUrlData.observeAsState(emptyList())
    }

    val itemsRef = itemsRefState.value
    val itemsUrl = itemsUrlState.value

    val maxRows = 4
    val maxColumns = 3
    val displayItems = itemsRef.zip(itemsUrl).take(maxRows * maxColumns)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(4.dp)
            .verticalScroll(rememberScrollState())
    ) {
        displayItems.chunked(maxColumns).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start, // 왼쪽 정렬
                verticalAlignment = Alignment.Top // 상단 정렬
            ) {
                rowItems.forEach { (ref, url) ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f) // 정사각형 비율 유지
                            .padding(4.dp) // 패딩 추가
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
                repeat(maxColumns - rowItems.size) {
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
                modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
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