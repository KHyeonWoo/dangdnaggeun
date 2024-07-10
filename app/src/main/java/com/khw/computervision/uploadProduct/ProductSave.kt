package com.khw.computervision.uploadProduct

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.gowtham.ratingbar.StepSize
import com.khw.computervision.AiViewModel
import com.khw.computervision.ChoiceSegButton
import com.khw.computervision.HorizontalDividerColorDang
import com.khw.computervision.PopupDetails
import com.khw.computervision.ProductViewModel
import com.khw.computervision.R
import com.khw.computervision.SalesViewModel
import com.khw.computervision.TopBar
import com.khw.computervision.UserIDManager
import com.khw.computervision.colorBack
import com.khw.computervision.colorDang
import com.khw.computervision.gifImageDecode
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
                "",
                ""
            )
        )
    }
    var checkedOption by remember { mutableIntStateOf(1) }
    var popupVisibleState by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBack),
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
            HorizontalDividerColorDang(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 0.dp)
            ProductNameAndEditSection(newPopupDetails) {
                popupVisibleState = true
            }
            HorizontalDividerColorDang(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 16.dp)
            StateScreen(
                newPopupDetails,
                popupVisibleState,
                { newPopupDetails = it },
                { popupVisibleState = false })
            HorizontalDividerColorDang(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
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
private fun ProductNameAndEditSection(
    newPopupDetails: PopupDetails,
    popupVisible: () -> Unit
) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = "상품명 : ${newPopupDetails.name}"
        )
        Spacer(modifier = Modifier
            .weight(1f))

        Icon(
            imageVector = Icons.Default.Edit,
            contentDescription = "Edit",
            modifier = Modifier
                .padding(end = 8.dp)
                .clickable {
                    popupVisible()
                },
            tint = colorDang
        )
    }
}


@Composable
private fun ImageSection(
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

                    navController.navigate("sales")
                }
            },
            addIcon = aiResponseData?.let {
                Icons.Default.Check
            }
        )
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
                    text = "가격 : ${newPopupDetails.price}"
                )
                Text(
                    text = "거래방법 : ${newPopupDetails.dealMethod}"
                )
                Row {
                    Text(text = "평점 ")
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
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "거래 위치:\n${newPopupDetails.address}")
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