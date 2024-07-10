package com.khw.computervision.main

import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.navigation.NavHostController
import com.canhub.cropper.CropImageContract
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.khw.computervision.ClosetViewModel
import com.khw.computervision.SalesViewModel
import com.khw.computervision.TopBar
import com.khw.computervision.UserIDManager
import com.khw.computervision.colorBack
import com.khw.computervision.colorDong
import com.khw.computervision.encodeUrl
import com.khw.computervision.server.sendImageToServer

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
    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            result.uriContent?.let { uri ->
                context.contentResolver?.let {
                    val bitmap = MediaStore.Images.Media.getBitmap(it, uri)
                    sendImageToServer(bitmap) {
                        closetViewModel.getItemsFromFirebase(
                            Firebase.storage.reference.child(UserIDManager.userID.value)
                        )
                        showImagePicker = false
                        isLoading = false
                    }
                    isLoading = true
                }
            }
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    Row(modifier = modifier.fillMaxWidth()) {
        TopBar(
            title = "나의 옷장",
            onBackClick = onBackClick,
            onAddClick = {
                startImagePicker(imageCropLauncher)
            },
            addIcon = if (isLoading) Icons.Default.Refresh else Icons.Default.Add,
            showBackIcon = backIconVisible.toBoolean()
        )
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
    var selectedTabIndex by remember { mutableStateOf(0) }
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
        1 -> ImgGridSection(
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
private fun ImgGridSection(
    modifier: Modifier,
    category: String,
    navController: NavHostController,
    closetViewModel: ClosetViewModel,
    beforeScreen: String?,
    categoryUrl: String?,
) {
    var expandedImage by remember { mutableStateOf<Pair<StorageReference, String>?>(null) }
    Column(modifier = modifier.fillMaxWidth()) {
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
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = if (category == "top") "하의를 선택하세요" else "상의를 선택하세요",
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
            onDismiss = { expandedImage = null }
        )
    }
}

@Composable
private fun ImageGrid(
    category: String,
    onImageClick: (StorageReference, String, String) -> Unit,
    closetViewModel: ClosetViewModel
) {
    val itemsRef: List<StorageReference> by if (category == "top") {
        closetViewModel.topsRefData.observeAsState(emptyList())
    } else {
        closetViewModel.bottomsRefData.observeAsState(emptyList())
    }

    val itemsUrl: List<String> by if (category == "top") {
        closetViewModel.topsUrlData.observeAsState(emptyList())
    } else {
        closetViewModel.bottomsUrlData.observeAsState(emptyList())
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(top = 4.dp, start = 2.dp)
    ) {
        val rowSize: Int = 4
        itemsRef.zip(itemsUrl).chunked(rowSize).forEach { rowItems ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start, // 왼쪽 정렬
                verticalAlignment = Alignment.Top
            ) { // 상단 정렬
                rowItems.forEach { (ref, url) ->
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f) // 정사각형 비율 유지
                            .padding(4.dp)
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
                repeat(rowSize - rowItems.size) {
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



