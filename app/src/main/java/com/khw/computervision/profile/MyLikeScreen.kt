package com.khw.computervision.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import com.khw.computervision.ProductViewModel
import com.khw.computervision.SearchTextField
import com.khw.computervision.TopBar

@Composable
fun MyLikedScreen(navController: NavHostController, productsViewModel: ProductViewModel) {
    val productData by productsViewModel.productsData.observeAsState()
    val likedData by productsViewModel.likedData.observeAsState()
    var searchText by remember { mutableStateOf("") }
    var isSearchBarVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        TopBar(
            title = "좋아요",
            onBackClick = { navController.popBackStack() },
            onAddClick = { isSearchBarVisible = !isSearchBarVisible },
            addIcon = Icons.Default.Search
        )

        if (isSearchBarVisible) {
            SearchTextField(searchText = searchText, onSearchTextChange = { searchText = it })
        }

        val filteredProducts = productData?.filter { (productName, product) ->
            (product["name"]?.contains(searchText, ignoreCase = true) == true) && (likedData?.contains(productName) == true)
        } ?: emptyMap()

        LazyColumn {
            items(filteredProducts.entries.toList()) { (productName, product) ->
                MyProductSwipeBox(productName, product, productsViewModel, "liked")
            }
        }
    }
}
