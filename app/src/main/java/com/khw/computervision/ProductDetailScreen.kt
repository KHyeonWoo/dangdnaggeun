package com.khw.computervision

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
    navController: NavHostController, productsViewModel: ProductViewModel, productKey: String?
) {
    val productData by productsViewModel.productsData.observeAsState()
    val productMap = productData?.get(productKey)

    Column(
        modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally
    ) {

        productMap?.let { productMap ->
            HeaderSection()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                    SegmentImageSection(
                        Modifier.weight(10f),
                        productsViewModel,
                        productKey,
                        productMap
                    )
                    UserInfoSection(
                        productsViewModel,
                        navController,
                        productMap,
                        productKey
                    )
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.padding(16.dp),
                        color = colorDang
                    )
                    ProductNameSection(Modifier.weight(1f), productMap)
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.padding(16.dp),
                        color = colorDang
                    )
                    PriceAndMethodSection(productMap)
                    HorizontalDivider(
                        thickness = 2.dp,
                        modifier = Modifier.padding(16.dp),
                        color = colorDang
                    )
                    ProductDescriptionSection(productMap)
                }

        }
    }
}

@Composable
private fun HeaderSection() {
    TopBar(
        title = "판매제품",
        onBackClick = { /*TODO*/ },
        onAddClick = { /*TODO*/ },
        addIcon = null
    )

}

@Composable
fun ProductNameSection(modifier: Modifier, productMap: Map<String, String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (productMap["name"] != "") {
            Text(text = productMap["name"] ?: "제목 없음")
        } else {
            Text(text = "제목 없음")
        }
    }
}

@Composable
fun SegmentImageSection(
    modifier: Modifier,
    productsViewModel: ProductViewModel,
    productKey: String?,
    productMap: Map<String, String>
) {
    Column(
        modifier = Modifier
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        var checkedOption by remember { mutableIntStateOf(0) }
        val options = listOf(
            "옷", "모델"
        )

        SegmentButtonAndLikeSection(
            Modifier.weight(1f), productsViewModel, productKey, options, checkedOption, productMap
        ) { checkedOption = it }

        val painter = if (checkedOption == 0) {
            rememberAsyncImagePainter(productMap["imageUrl"])
        } else {
            rememberAsyncImagePainter(productMap["aiUrl"])
        }
        Image(
            painter = painter,
            contentDescription = "",
            modifier = Modifier
                .size(360.dp)
                .aspectRatio(1f)
                .padding(20.dp)
        )
    }
}

@Composable
fun SegmentButtonAndLikeSection(
    modifier: Modifier,
    productsViewModel: ProductViewModel,
    productKey: String?,
    options: List<String>,
    checkedOption: Int,
    productMap: Map<String, String>,
    changeCheckedOpt: (Int) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
    ) {
        val coroutineScope = rememberCoroutineScope()
        Spacer(modifier = Modifier.weight(2f))
        ChoiceSegButton(options, checkedOption) { changeCheckedOpt(it) }

        Spacer(modifier = Modifier.weight(1f))
        val likedData by productsViewModel.likedData.observeAsState()
        likedData?.let { likedList ->
            productKey?.let { productName ->

                val productFavoriteData by productsViewModel.totalLikedData.observeAsState()
                val totalLiked = productFavoriteData?.get(productKey)

                if (productMap["InsertUser"] != UserIDManager.userID.value) {
                    if (productName in likedList) {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_favorite_24),
                            contentDescription = "unLiked",
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        deleteLiked(productsViewModel, productKey)
                                        updateProductLike(
                                            productsViewModel,
                                            productKey,
                                            totalLiked,
                                            -1
                                        )
                                    }
                                },
                        )

                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.baseline_favorite_border_24),
                            contentDescription = "liked",
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    coroutineScope.launch(Dispatchers.IO) {
                                        insertLiked(productsViewModel, productName)
                                        updateProductLike(
                                            productsViewModel,
                                            productKey,
                                            totalLiked,
                                            1
                                        )
                                    }
                                },
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }

}

suspend fun insertLiked(productsViewModel: ProductViewModel, productName: String) {

    val likedProduct = hashMapOf(
        "liked" to "true"
    )

    Firebase.firestore.collection("${UserIDManager.userID.value}liked").document(productName)
        .set(likedProduct).addOnSuccessListener {}.addOnFailureListener {}.await()
    productsViewModel.getLikedFromFireStore()

}

fun updateProductLike(
    productsViewModel: ProductViewModel,
    productKey: String,
    totalLiked: Map<String, String>?,
    likedCount: Int
) {
    Firebase.firestore.collection("favoriteProduct").document(productKey)
        .update("liked", totalLiked?.get("liked")?.let { it.toInt() + likedCount } ?: 0)
    productsViewModel.getTotalLikedFromFireStore()
}

fun deleteLiked(productsViewModel: ProductViewModel, productKey: String) {
    Firebase.firestore.collection("${UserIDManager.userID.value}liked").document(productKey)
        .delete().addOnSuccessListener {}.addOnFailureListener {}
    productsViewModel.getLikedFromFireStore()
}

@Composable
fun UserInfoSection(
    productsViewModel: ProductViewModel,
    navController: NavHostController,
    productMap: Map<String, String>,
    productKey: String?
) {
    val insertUser = productMap["InsertUser"] ?: ""
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        var uploadUserProfile: String? by remember { mutableStateOf(null) }
        LaunchedEffect(Unit) {
            uploadUserProfile = getProfile(insertUser)
        }
        val painter = rememberAsyncImagePainter(uploadUserProfile)

        Image(
            painter = painter,
            contentDescription = "",
            modifier = Modifier
                .padding(8.dp)
                .aspectRatio(1f)
                .clip(CircleShape)
        )
        Text(
            text = "$insertUser\n${productMap["거래장소: address"]}"
        )

        Spacer(modifier = Modifier.weight(1f))

        var popupVisibleState by remember { mutableStateOf(false) }
        if (insertUser == UserIDManager.userID.value) {
            FunTextButton("수정", clickEvent = {
                popupVisibleState = true
            })
        } else {
            FunTextButton("메세지", clickEvent = {
                val encodedProfileUrl = uploadUserProfile?.let { encodeUrl(it) }
                navController.navigate("messageScreen/${insertUser}/${encodedProfileUrl}")
            })
        }
        PopupVisible(
            popupVisibleState,
            productsViewModel,
            productMap,
            productKey
        ) { popupVisibleState = false }
    }
}

@Composable
fun PopupVisible(
    popupVisibleState: Boolean,
    productsViewModel: ProductViewModel,
    productMap: Map<String, String>,
    productKey: String?,
    close: () -> Unit
) {

    var newPopupDetails by remember {
        mutableStateOf(
            PopupDetails(
                UserIDManager.userID.value,
                productMap["name"] ?: "",
                productMap["imageUrl"] ?: "",
                productMap["aiUrl"] ?: "",
                productMap["category"] ?: "",
                productMap["price"]?.toInt() ?: 0,
                productMap["dealMethod"] ?: "",
                productMap["rating"]?.toFloat() ?: 0f,
                productMap["productDescription"] ?: ""
            )
        )
    }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    if (popupVisibleState) {
        InsertPopup(newPopupDetails, {
            saveEvent(coroutineScope, context, productKey, it)
            productsViewModel.getProductsFromFireStore()
        }, {
            close()
        })
    }
}

@Composable
fun PriceAndMethodSection(productMap: Map<String, String>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
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
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(text = productMap["productDescription"] ?: "")

    }
}