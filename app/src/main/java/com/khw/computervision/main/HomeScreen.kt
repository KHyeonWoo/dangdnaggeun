package com.khw.computervision.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.khw.computervision.ChoiceSegButton
import com.khw.computervision.HorizontalDividerColorDang
import com.khw.computervision.ProductViewModel
import com.khw.computervision.SearchTextField
import com.khw.computervision.colorBack
import com.khw.computervision.colorDang
import com.khw.computervision.colorDong
import com.khw.computervision.customFont
@Composable
fun HomeScreen(navController: NavHostController, productsViewModel: ProductViewModel) {
    var isSearchBarVisible by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }
    var sortOption by remember { mutableStateOf("date") }
    var checkedOption by remember { mutableIntStateOf(0) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HeaderSection(
                isSearchBarVisible,
                searchText,
                { isSearchBarVisible = !isSearchBarVisible },
                { sortOption = "liked" },
                { sortOption = "date" },
                { searchText = it }
            )

            HorizontalDividerColorDang(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 0.dp)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(modifier = Modifier.align(Alignment.Center)) {
                    val options = listOf("상의", "하의")
                    ChoiceSegButton(options, checkedOption) { checkedOption = it }
                }
            }

            ImageList(navController, productsViewModel, if (checkedOption == 0) "top" else "bottom", sortOption, searchText)
        }
    }
}

@Composable
private fun HeaderSection(
    isSearchBarVisible: Boolean,
    searchText: String,
    toggleSearchBarVisibility: () -> Unit,
    likeSort: () -> Unit,
    dateSort: () -> Unit,
    setSearchText: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = toggleSearchBarVisibility) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.size(24.dp),
                tint = colorDang
            )
        }

        Spacer(modifier = Modifier.weight(2f))

        Text(
            "당당하게 거래해요",
            fontSize = 20.sp,
            fontFamily = customFont,
            color = colorDong
        )

        Spacer(modifier = Modifier.weight(2f))

        SortDropdownMenu(likeSort, dateSort)
    }

    if (isSearchBarVisible) {
        SearchTextField(searchText, setSearchText)
    }
}

@Composable
private fun SortDropdownMenu(setLike: () -> Unit, setDate: () -> Unit) {
    var sortDropdownVisible by remember { mutableStateOf(false) }

    Box {
        IconButton(onClick = { sortDropdownVisible = !sortDropdownVisible }) {
            Icon(
                imageVector = Icons.Default.ArrowDropDown,
                contentDescription = "More",
                modifier = Modifier.size(24.dp),
                tint = colorDang
            )
        }

        DropdownMenu(expanded = sortDropdownVisible, onDismissRequest = { sortDropdownVisible = false }) {
            DropdownMenuItem(
                text = { Text("인기순") },
                onClick = {
                    setLike()
                    sortDropdownVisible = false
                }
            )
            DropdownMenuItem(
                text = { Text("최신순") },
                onClick = {
                    setDate()
                    sortDropdownVisible = false
                }
            )
        }
    }
}

@Composable
private fun ImageList(
    navController: NavHostController,
    productsViewModel: ProductViewModel,
    categoryOption: String,
    sortOption: String,
    searchText: String
) {
    val productData by productsViewModel.productsData.observeAsState()
    val productFavoriteData by productsViewModel.totalLikedData.observeAsState()
    val searchedProductData = filterProducts(productData, categoryOption, searchText)
    val sortedKeyList = sortProducts(searchedProductData, productFavoriteData, sortOption)
    val sortedSearchedProductData = sortedKeyList.mapNotNull { key ->
        key to searchedProductData?.get(key)
    }.toMap()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        sortedKeyList.chunked(2).forEach { chunkedKeys ->
            ProductRow(chunkedKeys, sortedSearchedProductData, navController, productsViewModel)
        }
    }
}

@Composable
private fun ProductRow(
    chunkedKeys: List<String>,
    sortedSearchedProductData: Map<String, Map<String, String>?>,
    navController: NavHostController,
    productsViewModel: ProductViewModel
) {
    val productFavoriteData by productsViewModel.totalLikedData.observeAsState()

    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        chunkedKeys.forEach { key ->
            val product = sortedSearchedProductData[key]
            val totalLiked = productFavoriteData?.get(key)
            product?.let {
                ProductCard(Modifier.weight(1f),
                    product = it,
                    totalLiked = totalLiked,
                    onClick = {
                        incrementViewCount(key, totalLiked)
                        productsViewModel.getTotalLikedFromFireStore()
                        navController.navigate("detailProduct/$key")
                    }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }
            if (chunkedKeys.size != 2) {
                Box(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun ProductCard(
    modifier: Modifier,
    product: Map<String, String>,
    totalLiked: Map<String, String>?,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val painter = rememberAsyncImagePainter(product["imageUrl"])

        Image(
            painter = painter,
            contentDescription = "Image",
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .size(136.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.LightGray)
        )

        Column(
            modifier = Modifier
                .width(136.dp)
        ) {
            Text(
                text = product["name"] ?: "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${product["price"] ?: ""}원",
                fontSize = 16.sp
            )
            Row(
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier.fillMaxWidth()
            ) {
                totalLiked?.get("liked")?.let {
                    Text(
                        text = "좋아요 $it",
                        fontSize = 12.sp,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                }
                totalLiked?.get("viewCount")?.let {
                    Text(
                        text = "조회수 $it",
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}
