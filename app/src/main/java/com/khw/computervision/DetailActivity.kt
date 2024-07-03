package com.khw.computervision

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

//class DetailActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            ComputerVisionTheme {
//
//                // Intent에서 Bundle을 가져옵니다.
//                val bundle = intent.getBundleExtra("product")
//                var productMap: Map<String, String> = mutableMapOf()
//                if (bundle != null) {
//                    productMap = bundleToMap(bundle)
//                }
//
//                DetailScreen(productMap)
//            }
//        }
//    }

//    @Composable
//    fun DetailScreen(productMap: Map<String, String>) {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            LogoScreen("Detail") { finish() }
//            Image(
//                painter = painterResource(id = R.drawable.character4),
//                contentDescription = "",
//                modifier = Modifier
//                    .padding(20.dp)
//                    .size(320.dp)
//            )
//            UserInfoSection(productMap)
//            Divider(color = colorDang, thickness = 2.dp)
//            PriceAndMethodSection(productMap)
//            Divider(color = colorDang, thickness = 2.dp)
//            ProductDescriptionSection(productMap)
//        }
//    }
//
//
//    @Composable
//    fun UserInfoSection(productMap: Map<String, String>) {
//        val insertUser = productMap.getValue("InsertUser")
//        Row(
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Image(
//                painter = painterResource(id = R.drawable.character4),
//                contentDescription = "",
//                modifier = Modifier
//                    .padding(start = 20.dp)
//                    .size(60.dp)
//            )
//            Text(
//                text = insertUser,
//                modifier = Modifier.padding(top = 10.dp)
//            )
//
//            Spacer(modifier = Modifier.weight(1f))
//
//            var messagePopUp by remember { mutableStateOf(false) }
//
//            if(insertUser == UserIDManager.userID.value) {
//                FunTextButton("메세지", clickEvent = { messagePopUp = true })
//            } else {
//                FunTextButton("메세지", clickEvent = { messagePopUp = true })
//            }
//
//            if (messagePopUp) {
//                MessagePopup(insertUser) { messagePopUp = false }
//            }
//        }
//    }
//
//    @Composable
//    fun PriceAndMethodSection(productMap: Map<String, String>) {
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp),
//            horizontalArrangement = Arrangement.SpaceBetween,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            Text(text = "가격: ${productMap.getValue("price")}")
//            Text(text = "거래방법: ${productMap.getValue("dealMethod")}")
//            Text(text = "상태: ${productMap.getValue("rating")}")
//        }
//    }
//
//    @Composable
//    fun ProductDescriptionSection(productMap: Map<String, String>) {
//        Row(
//            modifier = Modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center
//        ) {
//            Text(text = productMap.getValue("productDescription"))
//        }
//    }
//}

@Composable
fun DetailScreen(navController: NavHostController, productId: String?) {
    var productMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LaunchedEffect(productId) {
        if (productId != null) {
            Firebase.firestore.collection("product").document(productId).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        productMap = mapOf(
                            "InsertUser" to (document.getString("InsertUser") ?: ""),
                            "name" to (document.getString("name") ?: ""),
                            "date" to (document.getString("date") ?: ""),
                            "dealMethod" to (document.getString("dealMethod") ?: ""),
                            "imageUrl" to (document.getString("imageUrl") ?: ""),
                            "price" to (document.get("price")?.toString() ?: ""),
                            "productDescription" to (document.getString("productDescription")
                                ?: ""),
                            "rating" to (document.get("rating")?.toString() ?: ""),
                            "state" to (document.get("state")?.toString() ?: ""),
                        )
                    }
                }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
//        LogoScreen("Detail") { navController.popBackStack() }
        Image(
            painter = painterResource(id = R.drawable.character4),
            contentDescription = "",
            modifier = Modifier
                .padding(20.dp)
                .size(320.dp)
        )
        UserInfoSection(productMap)
        Divider(color = colorDang, thickness = 2.dp)
        PriceAndMethodSection(productMap)
        Divider(color = colorDang, thickness = 2.dp)
        ProductDescriptionSection(productMap)
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