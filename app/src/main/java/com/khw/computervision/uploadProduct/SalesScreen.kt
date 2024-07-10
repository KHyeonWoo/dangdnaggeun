@file:JvmName("HomeScreenKt")

package com.khw.computervision.uploadProduct

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.khw.computervision.R
import com.khw.computervision.SalesViewModel
import com.khw.computervision.TopBar
import com.khw.computervision.colorBack
import com.khw.computervision.colorDong
import com.khw.computervision.gifImageDecode

@Composable
fun SalesScreen(
    navController: NavHostController,
    salesViewModel: SalesViewModel,
    encodedClickedUrl: String,
    clickedCategory: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBack),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SalesScreenTopBar(navController, salesViewModel, encodedClickedUrl, clickedCategory)
        SalesScreenContent(encodedClickedUrl, navController)
    }
}

@Composable
private fun SalesScreenTopBar(
    navController: NavHostController,
    salesViewModel: SalesViewModel,
    encodedClickedUrl: String,
    clickedCategory: String
) {
    TopBar(
        title = "",
        onBackClick = {},
        onAddClick = {
            salesViewModel.setClickedUrl(encodedClickedUrl)
            salesViewModel.setCategoryData(clickedCategory)
            navController.navigate("aiImgGen/ ")
        },
        addIcon = Icons.Default.KeyboardArrowRight,
        showBackIcon = false
    )
}

@Composable
private fun SalesScreenContent(
    encodedClickedUrl: String,
    navController: NavHostController
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))

        DisplayImage(encodedClickedUrl)
        InstructionText()
        UploadPrompt(navController)

        Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
private fun DisplayImage(encodedClickedUrl: String) {
    if (encodedClickedUrl == " ") {
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
}

@Composable
private fun InstructionText() {
    Text(
        text = "정면 사진 사용시 AI 이미지가\n더욱 좋아요!",
        fontSize = 20.sp,
        color = colorDong,
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}

@Composable
private fun UploadPrompt(navController: NavHostController) {
    Text(
        text = "여기를 클릭해서\n판매할 옷 이미지를 올리세요",
        color = colorDong,
        textDecoration = TextDecoration.Underline,
        fontSize = 16.sp,
        modifier = Modifier.clickable { navController.navigate("closet/decorate") },
        textAlign = TextAlign.Center,
        fontWeight = FontWeight.Bold
    )
}