package com.khw.computervision.uploadProduct

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.khw.computervision.AiViewModel
import com.khw.computervision.FunButton
import com.khw.computervision.R
import com.khw.computervision.SalesViewModel
import com.khw.computervision.TopBar
import com.khw.computervision.colorBack
import com.khw.computervision.colorDang
import com.khw.computervision.colorDong

@Composable
fun AiImgGenScreen(
    navController: NavHostController,
    salesViewModel: SalesViewModel,
    encodingExtraClickedUrl: String,
    aiViewModel: AiViewModel
) {
    val gender by salesViewModel.genderData.observeAsState(true)
    val clickedUrlLiveData by salesViewModel.clickedUrlData.observeAsState()
    val clickedCategoryLiveData by salesViewModel.categoryData.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBack),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HeaderSection(
            navController,
            clickedUrlLiveData,
            clickedCategoryLiveData,
            aiViewModel,
            encodingExtraClickedUrl,
            gender
        )

        BodySection(Modifier.weight(5f),
            gender,
            changeWoman = { salesViewModel.setGenderData(true) },
            changeMan = { salesViewModel.setGenderData(false) }
        )

        BottomSection(
            Modifier.weight(2f),
            navController,
            clickedUrl = clickedUrlLiveData,
            clickedCategory = clickedCategoryLiveData,
            encodingExtraClickedUrl = encodingExtraClickedUrl
        )
    }


}

@Composable
private fun HeaderSection(
    navController: NavHostController,
    clickedUrl: String?,
    clickedCategory: String?,
    aiViewModel: AiViewModel,
    encodingExtraClickedUrl: String,
    gender: Boolean
) {
    val context = LocalContext.current
    val modelGender = if (gender) "2" else "1"
    TopBar(
        title = "AI 모델",
        onBackClick = { navController.popBackStack() },
        onAddClick = {
            if (encodingExtraClickedUrl != " " && clickedUrl != null && clickedCategory != null) {
                aiViewModel.resetResponseData()
                if (clickedCategory == "top") {
                    aiViewModel.sendServerRequest(
                        topURL = clickedUrl,
                        bottomURL = encodingExtraClickedUrl,
                        gender = modelGender,
                    )
                } else if (clickedCategory == "bottom") {
                    aiViewModel.sendServerRequest(
                        topURL = encodingExtraClickedUrl,
                        bottomURL = clickedUrl,
                        gender = modelGender,
                    )
                }
                navController.navigate("insert")
            } else {
                Toast.makeText(context, "모델에 입힐 이미지를 선택해주세요", Toast.LENGTH_SHORT).show()
            }
        },
        addIcon = Icons.Default.KeyboardArrowRight
    )
}


@Composable
private fun BodySection(
    modifier: Modifier,
    gender: Boolean,
    changeWoman: () -> Unit,
    changeMan: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        GenderSelection(Modifier.weight(1f), gender, { changeWoman() }, { changeMan() })

        if (gender) {
            Image(
                painter = painterResource(id = R.drawable.model_women_noback),
                contentDescription = "AIModel",
                modifier = Modifier.weight(5f),
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.model_men_noback),
                contentDescription = "AIModel",
                modifier = Modifier.weight(5f),
                alignment = Alignment.Center,
                contentScale = ContentScale.Fit
            )
        }
    }
}

@Composable
private fun BottomSection(
    modifier: Modifier,
    navController: NavHostController,
    clickedUrl: String?,
    clickedCategory: String?,
    encodingExtraClickedUrl: String
) {

    HorizontalDivider(
        color = colorDang, modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    )
    Row(
        modifier = modifier.fillMaxSize(),
    ) {
        Spacer(modifier = Modifier.weight(.5f))
        Column(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            FunButton(buttonText = "판매옷", null) {}
            val painter = rememberAsyncImagePainter(clickedUrl)
            Image(
                painter = painter,
                contentDescription = "Image",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .aspectRatio(1f)
            )
        }

        Spacer(modifier = Modifier.weight(.5f))

        Column(
            modifier = Modifier
                .weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (clickedCategory) {
                "top" -> FunButton(buttonText = "하의 선택", null) {}
                "bottom" -> FunButton(buttonText = "상의 선택", null) { }
            }
            if (encodingExtraClickedUrl != " ") {
                val painter = rememberAsyncImagePainter(encodingExtraClickedUrl)
                Image(
                    painter = painter,
                    contentDescription = "Image",
                    contentScale = ContentScale.FillBounds,
                    modifier = Modifier
                        .aspectRatio(1f)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "",
                    tint = colorDang,
                    modifier = Modifier
                        .padding(24.dp)
                        .size(80.dp)
                        .clickable {
                            navController.navigate("closet/aiImgGen")
                        }
                )
            }
        }
        Spacer(modifier = Modifier.weight(.5f))
    }
}

@Composable
private fun GenderSelection(
    modifier: Modifier,
    gender: Boolean,
    changeWoman: () -> Unit,
    changeMan: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Top
    ) {
        GenderOption(
            label = "여",
            isSelected = gender,
            onCheckedChange = { changeWoman() }
        )

        GenderOption(
            label = "남",
            isSelected = !gender,
            onCheckedChange = { changeMan() }
        )
    }
}

@Composable
private fun GenderOption(
    label: String,
    isSelected: Boolean,
    onCheckedChange: () -> Unit
) {
    Column {
        Text(
            text = label,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = Color.Black,
            fontSize = 12.sp
        )
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = colorDong,
                uncheckedColor = colorDong,
                checkmarkColor = Color.White,
            )
        )
    }
}
