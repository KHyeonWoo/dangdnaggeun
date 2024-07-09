package com.khw.computervision

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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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

@Composable
fun SaleScreen(navController: NavHostController, productsViewModel: ProductViewModel) {
    var isSearchBarVisible by rememberSaveable { mutableStateOf(false) }
    var searchText by rememberSaveable { mutableStateOf("") }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBack)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var sortOpt by remember { mutableStateOf("date") }

            HeaderSection(
                isSearchBarVisible,
                searchText,
                { isSearchBarVisible = !isSearchBarVisible },
                { sortOpt = "liked" },
                { sortOpt = "date" },
                { searchText = it })


            HorizontalDivider(
                color = colorDang, modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp, 0.dp)
            )

            var checkedOption by remember { mutableIntStateOf(0) }
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

            if (checkedOption == 0) {
                ImageList(navController, productsViewModel, "top", sortOpt, searchText)
            } else {
                ImageList(navController, productsViewModel, "bottom", sortOpt, searchText)
            }
        }
    }
}

@Composable
private fun HeaderSection(
    isSearchBarVisible: Boolean,
    searchText: String,
    searchBarVisible: () -> Unit,
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
        IconButton(onClick = { searchBarVisible() }) {
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

        SortDropdownMenu({ likeSort() }, { dateSort() })
    }

    if (isSearchBarVisible) {
        SearchTextField(searchText) { setSearchText(it) }
    }
}

@Composable
fun SearchTextField(searchText: String, onSearchTextChange: (String) -> Unit) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        label = { Text("검색어를 입력하세요") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    )
}

@Composable
fun SortDropdownMenu(setLike: () -> Unit, setDate: () -> Unit) {
    var sortDropdownVisble by remember { mutableStateOf(false) }
    IconButton(onClick = { sortDropdownVisble = !sortDropdownVisble }) {
        Icon(
            imageVector = Icons.Default.ArrowDropDown,
            contentDescription = "More",
            modifier = Modifier.size(24.dp),
            tint = colorDang
        )
    }

    DropdownMenu(expanded = sortDropdownVisble, onDismissRequest = { sortDropdownVisble = false }) {
        DropdownMenuItem(
            text = { Text("인기순") },
            onClick = {
                setLike()
            }
        )
        DropdownMenuItem(
            text = { Text("최신순") },
            onClick = {
                setDate()
            }
        )
    }
}


@Composable
fun ImageList(
    navController: NavHostController,
    productsViewModel: ProductViewModel,
    categoryOption: String,
    sortOpt: String,
    searchText: String,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val productData by productsViewModel.productsData.observeAsState()

        val categoryProductData = if (categoryOption == "top") {
            productData?.filter { (key, value) ->
                value["category"] == "top"
            }
        } else {
            productData?.filter { (key, value) ->
                value["category"] == "bottom"
            }
        }

        val searchedProductData = if (searchText.isNotEmpty()) {
            categoryProductData?.filter { (key, value) ->
                value["name"]?.contains(searchText, ignoreCase = true) == true
            }
        } else {
            categoryProductData
        }

        val productFavoriteData by productsViewModel.totalLikedData.observeAsState()
        var sortedKeyList by remember { mutableStateOf(emptyList<String>()) }

        searchedProductData?.let { searchedProductMap ->
            productFavoriteData?.let { productFavoriteMap ->
                sortedKeyList = if (sortOpt == "liked") {
                    productFavoriteMap.entries.sortedByDescending { it.value[sortOpt] }
                        .map { it.key }
                } else {
                    searchedProductMap.keys.toList()
                }
            }
        }

        val sortedSearchedProductData = sortedKeyList.mapNotNull { key ->
            key to searchedProductData?.get(key)
        }.toMap()

        sortedKeyList.chunked(2).forEach { chunkedKeys ->
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                chunkedKeys.forEach { key ->
                    val value = sortedSearchedProductData[key]
                    val totalLiked = productFavoriteData?.get(key)
                    value?.let { product ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    Firebase.firestore
                                        .collection("favoriteProduct")
                                        .document(key)
                                        .update("viewCount",
                                            totalLiked
                                                ?.get("viewCount")
                                                ?.let { it.toInt() + 1 } ?: 1
                                        )
                                    productsViewModel.getTotalLikedFromFireStore()
                                    navController.navigate("detailProduct/$key")
                                },
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            val painter = rememberAsyncImagePainter(product["imageUrl"])

                            Image(
                                painter = painter,
                                contentDescription = "Image",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .size(136.dp, 136.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.LightGray)
                            )

                            Column(
                                modifier = Modifier
                                    .width(136.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                ) {
                                    Column {
                                        value["name"]?.let {
                                            Text(
                                                text = it,
                                                fontSize = 16.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        value["price"]?.let {
                                            Text(text = "${it}원", fontSize = 16.sp)
                                        }
                                        Row(
                                            horizontalArrangement = Arrangement.Start,
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            totalLiked?.get("liked")
                                                ?.let {
                                                    Text(
                                                        text = "좋아요 $it",
                                                        fontSize = 12.sp,
                                                        modifier = Modifier.padding(end = 4.dp)
                                                    )
                                                }
                                            totalLiked?.get("viewCount")
                                                ?.let {
                                                    Text(
                                                        text = "조회수 $it",
                                                        fontSize = 12.sp
                                                    )
                                                }
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                    if (chunkedKeys.size != 2) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}
