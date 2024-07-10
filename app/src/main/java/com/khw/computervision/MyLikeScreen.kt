package com.khw.computervision

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import kotlin.math.roundToInt


@Composable
fun MyLikedScreen(navController: NavHostController, productsViewModel: ProductViewModel) {
    val productData by productsViewModel.productsData.observeAsState()
    val likedData by productsViewModel.likedData.observeAsState()
    var searchText by remember {
        mutableStateOf("")
    }
    var isSearchBarVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(
            title = "좋아요",
            onBackClick = { navController.popBackStack() },
            onAddClick = { isSearchBarVisible = !isSearchBarVisible },
            addIcon = Icons.Default.Search
        )

        if (isSearchBarVisible) {
            SearchTextField(searchText = searchText,
                onSearchTextChange = { searchText = it }
            )
        }

        val filteredProducts = productData?.filter { (productName, product) ->
            (product["name"]?.contains(
                searchText,
                ignoreCase = true
            ) == true) && (likedData?.contains(productName) == true)
        } ?: emptyMap()

        LazyColumn {
            items(filteredProducts.entries.toList()) { (productName, product) ->
                MyProductSwipeBox(productName, product)
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MyProductSwipeBox(key: String, productMap: Map<String, String>) {

    val squareSize = 48.dp
    val swipeState = rememberSwipeableState(0)
    val sizePx = with(LocalDensity.current) { squareSize.toPx() }
    val anchors =
        mapOf(0f to 0, -sizePx to 1) // Maps anchor points (in px) to states

    Box(
        modifier = Modifier
            .padding(8.dp)
            .width(350.dp)
            .swipeable(
                state = swipeState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.4f) },
                orientation = Orientation.Horizontal
            )
            .clip(RoundedCornerShape(15.dp))
    ) {
        Row(
            modifier = Modifier
                .offset { IntOffset(swipeState.offset.value.roundToInt(), 0) }
                .padding(start = 4.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colorDang),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val painter = rememberAsyncImagePainter(productMap["imageUrl"])
            Image(
                painter = painter,
                contentDescription = "",
                contentScale = ContentScale.FillBounds,
                modifier = Modifier
                    .weight(3f)
                    .padding(4.dp)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            )

            Box(
                modifier = Modifier
                    .weight(8f)
                    .padding(4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(10.dp, 0.dp)
                ) {
                    for ((fieldKey, fieldValue) in productMap) {
                        if (fieldKey == "name" || fieldKey == "price") {
                            Text(
                                text = fieldKey,
                                color = colorDang
                            )
                            Text(
                                text = fieldValue,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        if (swipeState.currentValue == 1) {
            Box(
                modifier = Modifier
                    .size(48.dp, 100.dp)
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
