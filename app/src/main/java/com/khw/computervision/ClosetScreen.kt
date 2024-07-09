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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
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
    salesViewModel: SalesViewModel,
    beforeScreen: String?,
    backIconVisible: String?
) {
    BackHandler {
        navController.navigateUp()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBack)
    ) {
        HeaderSection(Modifier.weight(1f), closetViewModel, onBackClick, backIconVisible)
        BodySection(
            navController,
            Modifier.weight(8f),
            closetViewModel,
            beforeScreen,
            salesViewModel
        )

    }
}

@Composable
private fun HeaderSection(
    modifier: Modifier,
    closetViewModel: ClosetViewModel,
    onBackClick: () -> Unit,
    backIconVisible: String?
) {
    var isLoading by remember { mutableStateOf(false) }
    var showImagePicker by remember { mutableStateOf(false) }
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
    Row(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        if (isLoading) {

            TopBar(
                title = "나의 옷장",
                onBackClick = onBackClick,
                onAddClick = { },
                addIcon = Icons.Default.Refresh,
                showBackIcon = backIconVisible.toBoolean()
            )
        } else {
            TopBar(
                title = "나의 옷장",
                onBackClick = onBackClick,
                onAddClick = {
                    startImagePicker(imageCropLauncher)
                },
                addIcon = Icons.Default.Add,
                showBackIcon = backIconVisible.toBoolean()
            )
        }
    }
}

@Composable
private fun BodySection(
    navController: NavHostController,
    modifier: Modifier,
    closetViewModel: ClosetViewModel,
    beforeScreen: String?,
    salesViewModel: SalesViewModel,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("상의", "하의")
    TabRow(
        selectedTabIndex = selectedTabIndex,
        contentColor = colorDong,
        backgroundColor = colorBack,
        modifier = Modifier.padding(0.dp, 4.dp)
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedTabIndex == index,
                onClick = { selectedTabIndex = index },
                text = {
                    Text(
                        title,
                        fontSize = 16.sp,
                        color = Color.Black
                    )
                }
            )
        }
    }

    val categoryUrl by salesViewModel.categoryData.observeAsState()
    if (beforeScreen == "bottomNav") {
        salesViewModel.setCategoryData(" ")
    }
    when (selectedTabIndex) {
        0 -> ImgGridSection(
            modifier,
            "top",
            navController,
            closetViewModel,
            beforeScreen,
            categoryUrl
        )

        1 ->
            ImgGridSection(
                modifier,
                "bottom",
                navController,
                closetViewModel,
                beforeScreen,
                categoryUrl
            )
    }
}

@Composable
fun ImgGridSection(
    modifier: Modifier,
    category: String,
    navController: NavHostController,
    closetViewModel: ClosetViewModel,
    beforeScreen: String?,
    categoryUrl: String?,
) {
    var expandedImage by remember { mutableStateOf<Pair<StorageReference, String>?>(null) }
    Column(
        modifier = modifier
            .fillMaxWidth(),
    ) {
        if (categoryUrl != category) {
            ImageGrid(
                category = category,
                onImageClick = { ref, url, _ ->
                    val encodedUrl = encodeUrl(url)
                    when (beforeScreen) {
                        "decorate" -> {
                            navController.navigate("decorate/$encodedUrl/$category")
                        }

                        "bottomNav" -> {
                            expandedImage = Pair(ref, url)
                        }

                        "aiImgGen" -> {
                            navController.navigate("aiImgGen/$encodedUrl")
                        }
                    }
                },
                closetViewModel = closetViewModel
            )
        } else {

            val choiceMessage = when (category) {
                "top" -> "하의를 선택하세요"
                else -> "상의를 선택하세요"
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = choiceMessage,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
        }
    }
    expandedImage?.let { (ref, url) ->
        ExpandedImageDialog(
            ref = ref,
            url = url,
            closetViewModel = closetViewModel,
            onDismiss = { expandedImage = null })
    }

}

@Composable
fun ExpandedImageDialog(
    ref: StorageReference,
    url: String,
    closetViewModel: ClosetViewModel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(Color.White)
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
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
            }
            FunButton("삭제", null) {
                ref.delete()
                    .addOnSuccessListener {
                        closetViewModel.getItemsFromFirebase(
                            Firebase.storage.reference.child(
                                UserIDManager.userID.value
                            )
                        )
                        onDismiss()
                    }
                    .addOnFailureListener {
                    }
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