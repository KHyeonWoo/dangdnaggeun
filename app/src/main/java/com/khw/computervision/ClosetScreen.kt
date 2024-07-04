package com.khw.computervision

import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import coil.compose.rememberImagePainter
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.firebase.storage.StorageReference

@Composable
fun CustomImageGridPage(
    isLoading: Boolean,
    closetViewModel: ClosetViewModel,
    onImageClick: (StorageReference, String, String) -> Unit,
    onBackClick: () -> Unit,
    onAddClick: (Bitmap) -> Unit
) {
    var expandedImage by remember { mutableStateOf<Pair<StorageReference, String>?>(null) }
    var showImagePicker by remember { mutableStateOf(false) }

    if (showImagePicker) {
        LaunchImagePicker(onImageSelected = {
            onAddClick(it)
            showImagePicker = false
        })
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // 나의 옷장 타이틀과 버튼
        TopBar(title = "나의 옷장", onBackClick = onBackClick, onAddClick = { showImagePicker = true })
        HorizontalDivider(color = colorDang)
        // 상의 섹션
        SectionHeader(title = "상의")
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            ImageGridLimited(
                category = "top",
                onImageClick = { ref, url, category ->
                    expandedImage = Pair(ref, url)
                },
                closetViewModel = closetViewModel
            )
        }

        // 하의 섹션
        SectionHeader(title = "하의")
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            ImageGridLimited(
                category = "bottom",
                onImageClick = { ref, url, category ->
                    expandedImage = Pair(ref, url)
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
fun TopBar(title: String, onBackClick: () -> Unit, onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = colorDang)
        }
        Text(
            text = title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorDang,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "Add", tint = colorDang)
        }
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

    val maxRows = 2
    val maxColumns = 3
    val displayItems = itemsRef.zip(itemsUrl).take(maxRows * maxColumns)

    Column(
        modifier = Modifier
            .fillMaxWidth()
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