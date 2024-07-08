package com.khw.computervision

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController

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
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(
                title = "판매내역",
                onBackClick = { navController.popBackStack() },
                onAddClick = { },
                addIcon = null
            )
            LazyColumn {
                item {
                    productData?.let { product ->
                        product.forEach { (productName, product) ->
                            if (product["InsertUser"] == UserIDManager.userID.value) {
                                MyProductSwipeBox(productName, product)
                            }
                        }
                    }
                }
            }
        }
    }
}