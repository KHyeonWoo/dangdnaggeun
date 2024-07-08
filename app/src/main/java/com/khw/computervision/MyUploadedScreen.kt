package com.khw.computervision

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlin.math.roundToInt

@Composable
fun MyUploadedScreen(navController: NavHostController, productsViewModel: ProductViewModel) {
    val productData by productsViewModel.productsData.observeAsState()
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            TopBar(title = "판매내역",
                onBackClick = { navController.popBackStack() },
                onAddClick = { },
                addIcon = null)
//        SearchScreen()
            productData?.let { MyProductSwipeBox(it) }
        }
    }
}

@Composable
private fun SearchScreen() {
    var searchText by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        Spacer(modifier = Modifier.weight(1f))
        Row(
            modifier = Modifier
                .weight(4f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "검색",
                textAlign = TextAlign.Center,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .background(colorDang)
                    .height(40.dp)
                    .padding(top = 10.dp)
            )
            CustomOutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .height(40.dp)
                    .weight(3f)
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun MyProductSwipeBox(productMap: Map<String, Map<String, String>>) {

    for ((key, value) in productMap) {
        if (value["InsertUser"] == UserIDManager.userID.value) {
            val squareSize = 48.dp

            val swipeState = rememberSwipeableState(0)
            val sizePx = with(LocalDensity.current) { squareSize.toPx() }
            val anchors =
                mapOf(0f to 0, -sizePx to 1) // Maps anchor points (in px) to states

            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .height(80.dp)
                    .swipeable(
                        state = swipeState,
                        anchors = anchors,
                        thresholds = { _, _ -> FractionalThreshold(0.3f) },
                        orientation = Orientation.Horizontal
                    )
            ) {
                Row(
                    modifier = Modifier
                        .height(80.dp)
                        .padding(start = 4.dp)
                        .offset { IntOffset(swipeState.offset.value.roundToInt(), 0) }
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.character2),
                        contentDescription = "",
                        contentScale = ContentScale.FillBounds,
                        modifier = Modifier.weight(1f)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp)
                            .border(width = 1.dp, color = colorDang)
                            .weight(4f)
                    ) {

                        for ((fieldKey, fieldValue) in value) {
                            if (fieldKey == "name" || fieldKey == "price") {
                                Text(
                                    text = fieldKey, color = colorDang
                                )
                                Text(
                                    text = fieldValue
                                )
                            }
                        }
                    }
                }
                if (swipeState.currentValue == 1) {
                    Box(
                        modifier = Modifier
                            .size(48.dp, 80.dp)
                            .align(Alignment.CenterEnd),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = colorDang,
                            modifier = Modifier.clickable {
                                deleteFirestoreData("product", key) {
                                    ReLoadingManager.reLoading()
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
