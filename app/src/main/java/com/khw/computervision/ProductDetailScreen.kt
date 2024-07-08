package com.khw.computervision

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


@Composable
fun DetailScreen(
    navController: NavHostController,
    productsViewModel: ProductViewModel,
    productKey: String?,
) {
    val productData by productsViewModel.productsData.observeAsState()
    val productMap = productData?.get(productKey)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(
            title = "제품 상세정보",
            onBackClick = { navController.popBackStack() },
            onAddClick = { },
            addIcon = null,
            showBackIcon = true
        )

        if (productMap != null) {

            var checkedOption by remember { mutableIntStateOf(0) }
            val options = listOf(
                "옷",
                "모델"
            )
            VerticalDivider(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                val coroutineScope = rememberCoroutineScope()
                ChoiceSegButton(options, checkedOption) { checkedOption = it }
                val likedData by productsViewModel.likedData.observeAsState()
                likedData?.let { likedList ->
                    productKey?.let { productName ->

                        val productFavoriteData by productsViewModel.totalLikedData.observeAsState()
                        val totalLiked = productFavoriteData?.get(productKey)

                        if (productName in likedList) {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_favorite_24),
                                contentDescription = "unLiked",
                                modifier = Modifier.clickable {
                                    coroutineScope.launch(Dispatchers.IO) {

                                        Firebase.firestore.collection("${UserIDManager.userID.value}liked")
                                            .document(productKey)
                                            .delete()
                                            .addOnSuccessListener {}
                                            .addOnFailureListener {}
                                        productsViewModel.getLikedFromFireStore()

                                        Firebase.firestore.collection("favoriteProduct")
                                            .document(productKey)
                                            .update("liked",
                                                totalLiked?.get("liked")?.let { it.toInt() - 1 }
                                                    ?: 0
                                            )
                                        productsViewModel.getTotalLikedFromFireStore()
                                    }
                                })

                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.baseline_favorite_border_24),
                                contentDescription = "liked",
                                modifier = Modifier.clickable {

                                    coroutineScope.launch(Dispatchers.IO) {
                                        val likedProduct = hashMapOf(
                                            "liked" to "true"
                                        )
                                        Firebase.firestore.collection("${UserIDManager.userID.value}liked")
                                            .document(productName)
                                            .set(likedProduct)
                                            .addOnSuccessListener {}
                                            .addOnFailureListener {}
                                            .await()
                                        productsViewModel.getLikedFromFireStore()

                                        Firebase.firestore.collection("favoriteProduct")
                                            .document(productKey)
                                            .update(
                                                "liked",
                                                totalLiked?.get("liked")?.let { it.toInt() + 1 }
                                                    ?: 1)
                                        productsViewModel.getTotalLikedFromFireStore()
                                    }
                                })
                        }
                    }
                }
            }


            val painter = if (checkedOption == 0) {
                rememberAsyncImagePainter(productMap["imageUrl"])
            } else {
                rememberAsyncImagePainter(productMap["aiUrl"])
            }
            Image(
                painter = painter,
                contentDescription = "",
                modifier = Modifier
                    .padding(20.dp)
                    .size(320.dp)
            )
            UserInfoSection(productMap)
            HorizontalDivider(thickness = 2.dp, color = colorDang)
            PriceAndMethodSection(productMap)
            HorizontalDivider(thickness = 2.dp, color = colorDang)
            ProductDescriptionSection(productMap)
        }
    }
}

@Composable
fun UserInfoSection(productMap: Map<String, String>) {
    val insertUser = productMap["InsertUser"] ?: ""
    Row(
        modifier = Modifier.fillMaxWidth()
    ) {
        Image(
            painter = painterResource(id = R.drawable.character4),
            contentDescription = "",
            modifier = Modifier
                .padding(start = 20.dp)
                .size(60.dp)
        )
        Text(
            text = insertUser,
            modifier = Modifier.padding(top = 10.dp)
        )

        Spacer(modifier = Modifier.weight(1f))

        var messagePopUp by remember { mutableStateOf(false) }

        FunTextButton("메세지", clickEvent = { messagePopUp = true })

        if (messagePopUp) {
            MessagePopup(insertUser) { messagePopUp = false }
        }
    }
}

@Composable
fun PriceAndMethodSection(productMap: Map<String, String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = "가격: ${productMap["price"]}")
        Text(text = "거래방법: ${productMap["dealMethod"]}")
        Text(text = "상태: ${productMap["rating"]}")
    }
}

@Composable
fun ProductDescriptionSection(productMap: Map<String, String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(text = productMap["productDescription"] ?: "")
    }
}