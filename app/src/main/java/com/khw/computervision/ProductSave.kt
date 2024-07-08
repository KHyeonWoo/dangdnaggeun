package com.khw.computervision

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.gowtham.ratingbar.StepSize

@Composable
fun InsertScreen(
    navController: NavHostController,
    aiViewModel: AiViewModel,
    productsViewModel: ProductViewModel,
    salesViewModel: SalesViewModel
) {
    val aiResponseData by aiViewModel.responseData.observeAsState()
    val clickedUrlLiveData by salesViewModel.clickedUrlData.observeAsState()
    val clickedCategoryLiveData by salesViewModel.categoryData.observeAsState()

    var newPopupDetails by remember {
        mutableStateOf(
            PopupDetails(
                UserIDManager.userID.value,
                "",
                clickedUrlLiveData?:"",
                "",
                clickedCategoryLiveData?:"",
                0,
                "",
                0f,
                ""
            )
        )
    }
    var checkedOption by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        HeaderSection(
            Modifier.weight(1f),
            navController,
            aiResponseData,
            productsViewModel,
            newPopupDetails,
            checkedOption
        ) {
            checkedOption = it
        }
        ImageSection(Modifier.weight(3f), aiResponseData, checkedOption, clickedUrlLiveData)

        StateScreen(
            newPopupDetails,
            Modifier.weight(2f)
        ) { newPopupDetails = it }
    }
}


@Composable
fun ImageSection(
    modifier: Modifier,
    aiResponseData: String?,
    checkedOption: Int,
    clickedUrlLiveData: String?
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (checkedOption == 0) {
            val painter = rememberAsyncImagePainter(clickedUrlLiveData)
            Image(
                painter = painter,
                contentDescription = "",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            aiResponseData?.let { aiUrl ->
                val painter = rememberAsyncImagePainter(aiUrl)
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } ?: run {
                CircularProgressIndicator()
            }
        }
    }

}

@Composable
fun HeaderSection(
    modifier: Modifier,
    navController: NavHostController,
    aiResponseData: String?,
    productsViewModel: ProductViewModel,
    newPopupDetails: PopupDetails,
    checkedOption: Int,
    changeCheckedOpt: (Int) -> Unit
) {
    Column(
        modifier = modifier
    ) {
        TopBar(
            title = "",
            onBackClick = { navController.popBackStack() },
            onAddClick = { },
            addIcon = null
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Spacer(modifier = Modifier.weight(2f))
            val options = listOf(
                " 옷 ",
                "모델"
            )
            ChoiceSegButton(options, checkedOption) { changeCheckedOpt(it) }
            Spacer(modifier = Modifier.weight(1f))

            aiResponseData?.let { aiUrl ->
                newPopupDetails.aiUrl = aiUrl
                val coroutineScope = rememberCoroutineScope()
                val context = LocalContext.current

                Row(
                    modifier = Modifier.weight(1f),
                ) {
                    FunTextButton("저장") {
                        navController.popBackStack()
                        saveEvent(coroutineScope, context, null, newPopupDetails)
                        productsViewModel.getProductsFromFireStore()
                    }
                }
            } ?: run {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceSegButton(options: List<String>, checkedOption: Int, changeCheckedOpt: (Int) -> Unit) {
    MultiChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                icon = {},
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = colorDang,
                    activeContentColor = Color.White,
                    inactiveContainerColor = Color.White,
                    inactiveContentColor = Color.White,
                    activeBorderColor = colorDang,
                    inactiveBorderColor = colorDang,
                ),
                onCheckedChange = {
                    if (label == options[0]) {
                        changeCheckedOpt(0)
                    } else {
                        changeCheckedOpt(1)

                    }
                },
                checked = index == checkedOption,
                modifier = Modifier.size(64.dp,48.dp)
            ) {
                if (checkedOption == index) {
                    Text(label, color = Color.White)
                } else {
                    Text(label, color = colorDang)
                }
            }
        }
    }

}

@Composable
private fun StateScreen(
    newPopupDetails: PopupDetails,
    modifier: Modifier,
    updateDetail: (PopupDetails) -> Unit
) {

    var popupVisibleState by remember { mutableStateOf(false) }
    Column(
        modifier = modifier
            .clickable {
                popupVisibleState = true
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Divider(color = colorDang, thickness = 2.dp)
            Row {
                Text(text = "제품명: ", color = colorDang)
                Text(text = newPopupDetails.name)
            }
            Divider(color = colorDang, thickness = 2.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(text = "가격", modifier = Modifier.weight(1f), color = colorDang)
                Text(text = " ${newPopupDetails.price}", modifier = Modifier.weight(1f))

                Text(text = "거래방법", modifier = Modifier.weight(1f), color = colorDang)
                Text(text = " ${newPopupDetails.dealMethod}", modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.weight(3f)
                ) {
                    Spacer(modifier = Modifier.size(16.dp, 0.dp))
                    Text(text = "상태", color = colorDang)
                    Spacer(modifier = Modifier.size(16.dp, 0.dp))
                    RatingBar(
                        value = newPopupDetails.rating,
                        style = RatingBarStyle.Fill(),
                        stepSize = StepSize.HALF,
                        onValueChange = {},
                        size = 16.dp,
                        spaceBetween = 2.dp,
                        onRatingChanged = {
                            Log.d("TAG", "onRatingChanged: $it")
                        }
                    )
                }
            }
            Divider(color = colorDang, thickness = 2.dp)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .horizontalScroll(rememberScrollState())
            ) {
                Text(text = " \n ${newPopupDetails.productDescription}")
            }
        }

        if (popupVisibleState) {
            SavePopup(newPopupDetails, {
                updateDetail(it)
            }, {
                popupVisibleState = false
            })
        }
    }
}