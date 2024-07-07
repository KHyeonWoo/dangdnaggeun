package com.khw.computervision

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil.compose.rememberAsyncImagePainter
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

//class SalesActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            ComputerVisionTheme {
//                SaleScreen(navController)
//            }
//        }
//    }
//
//    @Composable
//    fun SaleScreen() {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//        ) {
//            val context = LocalContext.current
//            Box(
//                modifier = Modifier.fillMaxWidth(),
//            ) {
//                LogoScreen("Sales") { finish() }
//                var successUpload by remember { mutableStateOf(false) }
//                var profileUri: String? by remember { mutableStateOf(null) }
//
//                LaunchedEffect(successUpload) {
//                    profileUri = getProfile()
//                }
//
//                var visiblePopup by remember { mutableStateOf(false) }
//                val modifier = Modifier
//                    .size(40.dp)
//                    .clickable {
//                        visiblePopup = !visiblePopup
//                    }
//
//                Column(
//                    modifier = Modifier
//                        .align(Alignment.TopEnd)
//                        .padding(20.dp)
//                ) {
//                    profileUri?.let {
//                        GlideImage(
//                            imageModel = it,
//                            contentDescription = "Image",
//                            modifier = modifier
//                                .clip(RoundedCornerShape(20.dp))
//                        )
//                    } ?: Image(
//                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
//                        contentDescription = "",
//                        modifier = modifier
//                    )
//                }
//
//                if (visiblePopup) {
//                    ProfilePopup(
//                        profileUri,
//                        { visiblePopup = false },
//                        { successUpload = !successUpload })
//                }
//            }
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center
//            ) {
//                FunTextButton("현재 판매하는 제품이에요") {}
//            }
//            ImageList(ReLoadingManager.reLoadingValue.value)
//
//            Spacer(modifier = Modifier.weight(1f))
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.Center
//            ) {
//                Spacer(modifier = Modifier.weight(1f))
//                FunTextButton("+ 글쓰기") {
//                    context.startActivity(Intent(context, DecorateActivity::class.java))
//                }
//                Spacer(modifier = Modifier.weight(1f))
//            }
//        }
//    }
//
//    private suspend fun getProfile(): String? {
//        val storageRef =
//            Firebase.storage.reference.child("${UserIDManager.userID.value}/profile.jpg")
//        var profileUri: String? = null
//        try {
//            profileUri = storageRef.downloadUrl.await().toString()
//        } catch (_: StorageException) {
//
//        }
//        return profileUri
//    }
//
//
//    @Composable
//    fun ImageList(reLoading: Boolean) {
//        // rememberSaveable로 상태를 저장하고 복원할 수 있도록 합니다.
//        var productMap: Map<String, Map<String, String>> by remember { mutableStateOf(emptyMap()) }
//        GetProduct(reLoading) { productMap = it }
//
//        val context = LocalContext.current
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//        ) {
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .horizontalScroll(rememberScrollState()),
//                horizontalArrangement = Arrangement.Center
//            ) {
//                for ((key, value) in productMap) {
//                    Column(
//                        modifier = Modifier.clickable {
//                            val productIntent = Intent(context, DetailActivity::class.java)
//                            productIntent.putExtra("product", mapToBundle(value))
//                            context.startActivity(productIntent)
//                        }) {
//                        for ((fieldKey, fieldValue) in value) {
//                            if (fieldKey == "imageUrl") {
//                                GlideImage(
//                                    imageModel = fieldValue,
//                                    modifier = Modifier.size(90.dp, 160.dp),
//                                    contentDescription = "Image"
//                                )
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
//}
@Composable
fun SaleScreen(navController: NavHostController, productsViewModel: ProductViewModel) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        var checkedOption by remember { mutableIntStateOf(0) }
        var sortOpt by remember { mutableStateOf("date") }
        var searchText: String by remember { mutableStateOf("") }
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.align(Alignment.CenterStart)) {
                SearchDropdownMenu { searchText = it }
            }
            Row(modifier = Modifier.align(Alignment.Center)) {
                val options = listOf("상의", "하의")
                ChoiceSegButton(options, checkedOption) { checkedOption = it }
            }
            Row(modifier = Modifier.align(Alignment.CenterEnd)) {
                SortDropdownMenu({ sortOpt = "liked" }, { sortOpt = "date" })
            }
        }

        HorizontalDivider(color = colorDang, modifier = Modifier.width(350.dp).padding(0.dp,8.dp))
        if (checkedOption == 0) {
            ImageList(navController, productsViewModel, "top", sortOpt, searchText)
        } else {
            ImageList(navController, productsViewModel, "bottom", sortOpt, searchText)
        }
    }
}

@Composable
fun SearchDropdownMenu(searchEvent: (String) -> Unit) {
    var searchDropdownVisble by remember { mutableStateOf(false) }
    IconButton(onClick = { searchDropdownVisble = !searchDropdownVisble }) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "More",
            modifier = Modifier.size(24.dp),
            tint = colorDang
        )
    }

    DropdownMenu(
        expanded = searchDropdownVisble,
        onDismissRequest = { searchDropdownVisble = false }) {
        Row(
            modifier = Modifier
                .width(200.dp)
                .height(40.dp)
        ) {
            var searchText: String by remember { mutableStateOf("") }
            CustomOutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier.weight(6f)
            )
            Image(
                imageVector = Icons.Default.Search, contentDescription = "Search",
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
                    .clickable {
                        searchEvent(searchText)
                    }
            )
        }
    }
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
    searchText: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        val productData by productsViewModel.productsData.observeAsState()

        var searchedProductData by remember { mutableStateOf(productData) }
        if (searchText != "") {
            searchedProductData = productData?.filter { (key, value) ->
                value["name"]?.contains(searchText) ?: false
            }
        } else {
            searchedProductData = productData
        }

        val productFavoriteData by productsViewModel.totalLikedData.observeAsState()
        var sortedProductData by remember {
            mutableStateOf(searchedProductData)
        }
        productData?.let { productMap ->
            productFavoriteData?.let { productFavoriteMap ->
                if (sortOpt == "liked") {
                    val sortedLikedList =
                        productFavoriteMap.entries.sortedBy { it.value[sortOpt] }.map { it.key }
                    sortedProductData = sortedLikedList.mapNotNull { key ->
                        productMap[key]?.let { key to it }
                    }.toMap()
                } else {
                    sortedProductData = searchedProductData
                }
            }

        }

        sortedProductData?.entries?.chunked(2)?.forEach { chunkedProduct ->
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                for ((key, value) in chunkedProduct) {
                    val totalLiked = productFavoriteData?.get(key)
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
                        if (value["category"] == categoryOption) {
                            val painter = rememberAsyncImagePainter(value["imageUrl"])
                            Image(
                                painter = painter,
                                contentDescription = "Image",
                                contentScale = ContentScale.FillBounds,
                                modifier = Modifier
                                    .size(136.dp, 136.dp)
                                    .border(
                                        2.dp,
                                        color = colorDang,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )

                            Column(
                                modifier = Modifier
                                    .size(136.dp, 80.dp)
                                    .border(
                                        2.dp,
                                        color = colorDang,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp)
                                ) {
                                    Column {
                                        Text(text = "상품명")
                                        value["name"]?.let { Text(text = it) }
                                        value["price"]?.let {
                                            Text(text = "$${it}원")
                                        }
                                        Spacer(modifier = Modifier.weight(1f))
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(end = 4.dp)
                                        ) {

                                            totalLiked?.get("viewCount")
                                                ?.let { Text(text = "조회수 : $it") }
                                            Spacer(modifier = Modifier.weight(1f))
                                            totalLiked?.get("liked")
                                                ?.let { Text(text = "좋아요 : $it") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (chunkedProduct.size != 2) {
                        Box(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    label: @Composable() (() -> Unit)? = null,
    placeholder: @Composable() (() -> Unit)? = null,
    leadingIcon: @Composable() (() -> Unit)? = null,
    trailingIcon: @Composable() (() -> Unit)? = null,
    isError: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp) // 텍스트 상하 여백 줄이기
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            enabled = enabled,
            readOnly = readOnly,
            textStyle = TextStyle(
                fontSize = 16.sp, // 원하는 글자 크기로 설정
                color = Color.Black
            ),
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            cursorBrush = SolidColor(Color.Black),
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            decorationBox = { innerTextField ->
                // OutlinedTextField 스타일의 테두리를 적용
                OutlinedTextFieldDefaults.DecorationBox(
                    value = value,
                    visualTransformation = VisualTransformation.None,
                    innerTextField = innerTextField,
                    placeholder = placeholder,
                    label = label,
                    leadingIcon = leadingIcon,
                    trailingIcon = trailingIcon,
                    singleLine = singleLine,
                    enabled = enabled,
                    isError = isError,
                    interactionSource = remember { MutableInteractionSource() },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = colorDang,  // 테두리 색상을 하얀색으로 설정
                        focusedBorderColor = colorDang     // 포커스된 상태의 테두리 색상을 하얀색으로 설정
                    ),
                    contentPadding = PaddingValues(0.dp) // 내부 여백을 0으로 설정
                )
            }
        )
    }
}