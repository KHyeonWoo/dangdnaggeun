package com.khw.computervision

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.firebase.storage.StorageReference

@Composable
fun CustomImageGridPage(
    isLoading: Boolean,
    closetViewModel: ClosetViewModel,
    onImageClick: (StorageReference, String, String) -> Unit,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
       horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 나의 옷장 타이틀과 버튼
        TopBar(title = "나의 옷장", onBackClick = onBackClick, onAddClick = onAddClick)
        HorizontalDivider(color = colorDang, modifier = Modifier.width(350.dp))
        // 상의 섹션
        SectionHeader(title = "상의")
        if (isLoading) {
            CircularProgressIndicator()
        } else {
            ImageGridLimited(
                category = "top",
                onImageClick = onImageClick,
                closetViewModel = closetViewModel
            )
        }

        HorizontalDivider(color = colorDang, modifier = Modifier.width(350.dp))
        // 하의 섹션
        SectionHeader(title = "하의")
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            ImageGridLimited(
                category = "bottom",
                onImageClick = onImageClick,
                closetViewModel = closetViewModel
            )
        }
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
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = colorDang,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center
        )
        IconButton(onClick = onAddClick) {
            Icon(Icons.Default.Add, contentDescription = "Add",tint = colorDang)
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Box(modifier = Modifier
        .padding(8.dp)
        .background(color = Color.Transparent)
        .padding(8.dp)
        .width(300.dp)
        .height(30.dp)
        .border(2.dp, color = colorDang, shape = RoundedCornerShape(10.dp)),
        contentAlignment = Alignment.Center) {
        Text(
            text = title,
            fontSize = 15.sp,
            color = Color.Gray ,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
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