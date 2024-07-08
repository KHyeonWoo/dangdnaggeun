package com.khw.computervision

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import java.time.LocalDateTime

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
                clickedUrlLiveData ?: "",
                "",
                clickedCategoryLiveData ?: "",
                0,
                "",
                0f,
                ""
            )
        )
    }
    var checkedOption by remember { mutableIntStateOf(0) }
    var popupVisibleState by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {

        HeaderSection(
            navController,
            aiResponseData,
            productsViewModel,
            newPopupDetails
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            ImageSection(aiResponseData, checkedOption, clickedUrlLiveData) {
                checkedOption = it
            }

            HorizontalDivider(
                thickness = 2.dp,
                modifier = Modifier.padding(horizontal = 16.dp).padding(top = 16.dp),
                color = colorDang
            )
            ProductNameAndEditSection(newPopupDetails) {
                popupVisibleState = true
            }

            HorizontalDivider(
                thickness = 2.dp,
                modifier = Modifier.padding(horizontal = 16.dp).padding(bottom = 16.dp),
                color = colorDang
            )
            StateScreen(
                newPopupDetails,
                popupVisibleState,
                { newPopupDetails = it },
                { popupVisibleState = false })
            HorizontalDivider(
                thickness = 2.dp,
                modifier = Modifier.padding(16.dp),
                color = colorDang
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(text = " \n ${newPopupDetails.productDescription}")
            }
        }
    }
}

@Composable
fun ProductNameAndEditSection(
    newPopupDetails: PopupDetails,
    popupVisible: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()
        .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            newPopupDetails.name
        )
        Spacer(modifier = Modifier
            .weight(1f))

        FunTextButton("수정", clickEvent = {
            popupVisible()
        })
    }
}


@Composable
fun ImageSection(
    aiResponseData: String?,
    checkedOption: Int,
    clickedUrlLiveData: String?,
    changeCheckedOpt: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
        ) {
            Spacer(modifier = Modifier.weight(1f))
            val options = listOf(
                " 옷 ",
                "모델"
            )
            ChoiceSegButton(options, checkedOption) { changeCheckedOpt(it) }
            Spacer(modifier = Modifier.weight(1f))
        }

        if (checkedOption == 0) {
            val painter = rememberAsyncImagePainter(clickedUrlLiveData)
            Image(
                painter = painter,
                contentDescription = "",
                modifier = Modifier
                    .size(360.dp),
                contentScale = ContentScale.Fit
            )
        } else {
            aiResponseData?.let { aiUrl ->
                val painter = rememberAsyncImagePainter(aiUrl)
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .size(360.dp),
                    contentScale = ContentScale.Fit
                )
            } ?: run {
                Image(
                    painter = gifImageDecode(R.raw.dangkki_loadingicon),
                    contentDescription = "mascot",
                    modifier = Modifier
                        .size(360.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    }

}

@Composable
private fun HeaderSection(
    navController: NavHostController,
    aiResponseData: String?,
    productsViewModel: ProductViewModel,
    newPopupDetails: PopupDetails
) {
    Column {
        val coroutineScope = rememberCoroutineScope()
        val context = LocalContext.current

        TopBar(
            title = "",
            onBackClick = { navController.popBackStack() },
            onAddClick = {
                aiResponseData?.let { aiUrl ->
                    newPopupDetails.aiUrl = aiUrl

                    val dateTimeNow = LocalDateTime.now().toLocalDate().toString().replace("-", "") +
                                LocalDateTime.now().toLocalTime().toString().replace(":", "")
                                    .substring(0, 4)
                    saveEvent(coroutineScope, context, dateTimeNow, newPopupDetails)
                    productsViewModel.getProductsFromFireStore()

                    navController.navigate("detailProduct/$dateTimeNow")
                }
            },
            addIcon = aiResponseData?.let {
                Icons.Default.Check
            } ?: run { null }
        )
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
    popupVisibleState: Boolean,
    updateDetail: (PopupDetails) -> Unit,
    popupVisible: () -> Unit
) {
    Column(
        modifier = Modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "가격: ${newPopupDetails.price}",
                    color = colorDang
                )
                Text(
                    text = "거래방법 ${newPopupDetails.dealMethod}",
                    color = colorDang
                )
                Row {
                    Text(text = "평점", color = colorDang)
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
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.End
            ) {
                Text(text = "거래 위치: ", modifier = Modifier.weight(1f), color = colorDang)
            }
        }
    }

    if (popupVisibleState) {
        SavePopup(newPopupDetails, {
            updateDetail(it)
        }, {
            popupVisible()
        })
    }

}