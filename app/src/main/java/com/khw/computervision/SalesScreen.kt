package com.khw.computervision

import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Tab
import androidx.compose.material.TabRow
import androidx.compose.material.TabRowDefaults
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch

@Composable
fun DecorateScreen(
    navController: NavHostController,
    encodedClickedUrl: String,
    clickedCategory: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(
            title = "",
            onBackClick = {},
            onAddClick = {
                val encodedUrl = encodeUrl(encodedClickedUrl)
                navController.navigate("aiImgGen/$encodedUrl/$clickedCategory/ ") },
            addIcon = Icons.Default.KeyboardArrowRight,
            showBackIcon = false
        )

        Column(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))

            if(encodedClickedUrl == " "){
                Image(
                    painter = gifImageDecode(R.raw.dangkki_closetimage2),
                    contentDescription = "mascot",
                    modifier = Modifier.aspectRatio(1f)
                )
            } else {
                val painter = rememberAsyncImagePainter(encodedClickedUrl)
                Image(
                    painter = painter,
                    contentDescription = "mascot",
                    modifier = Modifier.aspectRatio(1f)
                )
            }

            Text(
                "정면 사진 사용시 AI 이미지가\n더욱 좋아요!",
                fontSize = 20.sp,
                color = colorDang,
                textAlign = TextAlign.Center
            )
//            Text(
//                "더욱 좋아요!",
//                fontSize = 18.sp,
//                color = colorDang
//            )

            Spacer(modifier = Modifier.weight(5f))

            Text(
                text = "여기를 클릭해서\n판매할 옷 이미지를 올리세요",
                color = colorDang,
                textDecoration = TextDecoration.Underline,
                fontSize = 16.sp,
                modifier = Modifier
                    .clickable {
                        navController.navigate("closet/decorate/ / ")
                    },
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(1f))
//            Text(
//                text = "판매할 옷 이미지를 올리세요",
//                color = colorDang,
//                fontSize = 15.sp
//            )
        }
    }
}

//            LogoScreen("Decorate") { navController.popBackStack() }
//            Spacer(modifier = Modifier.weight(1f))

//            if (displayedImageUrl.isNotEmpty()) {
//                Row(
//                    modifier = Modifier.fillMaxWidth(),
//                ) {
//                    Spacer(modifier = Modifier.weight(12f))
//
//                    //url에 특수문자 되있는 경우가 있어 encode함
//                    FunTextButton("다음") {
//                        val encodedUrl = encodeUrl(displayedImageUrl)
//                        navController.navigate("aiImgGen/$encodedUrl/$clickedCategory")
//                    }
//
//                    Spacer(modifier = Modifier.weight(1f))
//                }
//            }


//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(2f)
//                .padding(20.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Text(text = uploadServerResult)
//            GlideImage(
//                imageModel = displayedImageUrl,
//                modifier = Modifier.fillMaxSize(),
//                contentScale = ContentScale.Fit
//            )
//        }


//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .weight(2f),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            var inputImage by remember { mutableStateOf<Bitmap?>(null) }
//            var isLoading by remember { mutableStateOf(false) }
//
//            ImagePicker(onImageSelected = { bitmap ->
//                inputImage = bitmap
//
//                sendImageToServer(bitmap) {
//                    uploadServerResult += it
//                    uploadTrigger = !uploadTrigger
//                    inputImage = null
//                    isLoading = false
//                    closetViewModel.getItemsFromFirebase(Firebase.storage.reference.child(UserIDManager.userID.value))
//                }
//                isLoading = true
//            })

//
//            CustomTabRow(isLoading, closetViewModel) { _, onClickedUri, onClickedCategory ->
//                clickedCategory = onClickedCategory
//                displayedImageUrl = onClickedUri // 이미지 클릭 시 화면에 표시할 이미지 URI 업데이트
//            }



@OptIn(ExperimentalPagerApi::class)
@Composable
private fun CustomTabRow(
    isLoading: Boolean,
    closetViewModel: ClosetViewModel,
    onImageClick: (StorageReference, String, String) -> Unit,
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
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            if (page.toString() == "0") {
                ImageGrid(
                    category = "top",
                    onImageClick = onImageClick,
                    closetViewModel = closetViewModel
                )
            } else if (page.toString() == "1") {
                ImageGrid(
                    category = "bottom",
                    onImageClick = onImageClick,
                    closetViewModel = closetViewModel
                )
            }
        }
    }
}

@Composable
fun ImagePicker(
    onImageSelected: (Bitmap) -> Unit,
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

    Text(text = "옷추가",
        color = colorDang,
        textAlign = TextAlign.Center,
        fontSize = 16.sp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                val cropOption = CropImageContractOptions(
                    CropImage.CancelledResult.uriContent,
                    CropImageOptions()
                )
                imageCropLauncher.launch(cropOption)
            }
    )
}

