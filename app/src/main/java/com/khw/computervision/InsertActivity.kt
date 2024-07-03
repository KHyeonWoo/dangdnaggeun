package com.khw.computervision

import android.content.ContentValues
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.rememberImagePainter
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.gowtham.ratingbar.StepSize
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

//
//class InsertActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            ComputerVisionTheme {
//
//                var clickedUri by remember {
//                    mutableStateOf("")
//                }
//                clickedUri = intent.getStringExtra("clickedUri") ?: ""
//
//                var requestAiImg by remember {
//                    mutableStateOf("")
//                }
//                requestAiImg = intent.getStringExtra("requestAiImg") ?: ""
//
//                InsertScreen(clickedUri, requestAiImg)
//            }
//        }
//    }
//
//    @Composable
//    fun InsertScreen(clickedUri: String, requestAiImg: String) {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            val context = LocalContext.current
//            var newPopupDetails by remember {
//                mutableStateOf(
//                    PopupDetails(
//                        UserIDManager.userID.value,
//                        "",
//                        clickedUri,
//                        0,
//                        "",
//                        0f,
//                        ""
//                    )
//                )
//            }
//            Column(
//                modifier = Modifier
//                    .weight(1f)
//            ) {
//                LogoScreen("Insert") { finish() }
//                Spacer(modifier = Modifier.height(20.dp))
//                Row(
//                    modifier = Modifier.fillMaxWidth()
//                ) {
//                    Spacer(modifier = Modifier.weight(1f))
//
//                    val coroutineScope = rememberCoroutineScope()
//                    FunTextButton("저장") {
//                        finish()
//                        saveEvent(coroutineScope, context, newPopupDetails)
//                        ReLoadingManager.reLoading()
//                    }
//                }
//            }
//            Column(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .weight(2f)
//                    .clickable {
//                        //240701 김현우 - 이미지 추가 수정 시 DecorateActivity imageUri 전달 추가
//                        finish()
//                        val userIntent = Intent(context, DecorateActivity::class.java)
//                        userIntent.putExtra("clickedUri", clickedUri)
//                        context.startActivity(userIntent)
//                    },
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//
//                //240701 김현우 - 꾸미기 화면에서 이미지 선택 후 저장 시 GLIDE 이미지 show
//                GlideImage(
//                    imageModel = clickedUri,
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.Fit
//                )
//            }
//            var popupVisibleState by remember { mutableStateOf(false) }
//            Column(
//                modifier = Modifier
//                    .weight(2f)
//                    .clickable {
//                        popupVisibleState = true
//                    }
//            ) {
//                StateScreen(newPopupDetails)
//
//                if (popupVisibleState) {
//                    InsertPopup(newPopupDetails, {
//                        newPopupDetails = it
//                    }, {
//                        popupVisibleState = false
//                    })
//                }
//            }
//        }
//    }
//
//    private fun saveEvent(
//        coroutineScope: CoroutineScope,
//        context: Context,
//        newPopupDetails: PopupDetails
//    ) {
//        val db = Firebase.firestore
//        val dateTimeNow =
//            LocalDateTime.now().toLocalDate().toString().replace("-", "") +
//                    LocalDateTime.now().toLocalTime().toString().replace(":", "")
//                        .substring(0, 4)
//        val sendMessage = hashMapOf(
//            "InsertUser" to UserIDManager.userID.value,
//            "name" to newPopupDetails.name,
//            "date" to dateTimeNow,
//            "imageUrl" to newPopupDetails.imageUri,
//            "price" to newPopupDetails.price,
//            "dealMethod" to newPopupDetails.dealMethod,
//            "rating" to newPopupDetails.rating,
//            "productDescription" to newPopupDetails.productDescription,
//            "state" to 1 //1: 판매중, 2: 판매완료, 3:숨기기, 4:삭제
//        )
//
//        coroutineScope.launch(Dispatchers.IO) {
//            db.collection("product")
//                .document(dateTimeNow)
//                .set(sendMessage)
//                .addOnSuccessListener {
//                    Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
//                    Toast.makeText(context, "업로드 성공", Toast.LENGTH_SHORT).show()
//                }
//                .addOnFailureListener { e ->
//                    Log.w(ContentValues.TAG, "Error writing document", e)
//                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
//                }
//        }
//    }
//
//    @Composable
//    private fun StateScreen(newPopupDetails: PopupDetails) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .padding(20.dp)
//        ) {
//            Divider(color = colorDang, thickness = 2.dp)
//            Row {
//                Text(text = "제품명: ", color = colorDang)
//                Text(text = newPopupDetails.name)
//            }
//            Divider(color = colorDang, thickness = 2.dp)
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth(),
//            ) {
//                Text(text = "가격", modifier = Modifier.weight(1f), color = colorDang)
//                Text(text = " ${newPopupDetails.price}", modifier = Modifier.weight(1f))
//
//                Text(text = "거래방법", modifier = Modifier.weight(1f), color = colorDang)
//                Text(text = " ${newPopupDetails.dealMethod}", modifier = Modifier.weight(1f))
//
//                Row(
//                    modifier = Modifier.weight(3f)
//                ) {
//                    Spacer(modifier = Modifier.size(16.dp, 0.dp))
//                    Text(text = "상태", color = colorDang)
//                    Spacer(modifier = Modifier.size(16.dp, 0.dp))
//                    RatingBar(
//                        value = newPopupDetails.rating,
//                        style = RatingBarStyle.Fill(),
//                        stepSize = StepSize.HALF,
//                        onValueChange = {},
//                        size = 16.dp,
//                        spaceBetween = 2.dp,
//                        onRatingChanged = {
//                            Log.d("TAG", "onRatingChanged: $it")
//                        }
//                    )
//                }
//            }
//            Divider(color = colorDang, thickness = 2.dp)
//
//            Row(
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .padding(20.dp)
//                    .horizontalScroll(rememberScrollState())
//            ) {
//                Text(text = " \n ${newPopupDetails.productDescription}")
//            }
//        }
//    }
//}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsertScreen(
    navController: NavHostController,
    encodingClickedUri: String,
    viewModel: SharedViewModel
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val context = LocalContext.current
        var newPopupDetails by remember {
            mutableStateOf(
                PopupDetails(
                    UserIDManager.userID.value,
                    "",
                    encodingClickedUri,
                    0,
                    "",
                    0f,
                    ""
                )
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
        ) {
//            LogoScreen("Insert") { navController.popBackStack() }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(1f))

                var checkedOption by remember { mutableIntStateOf(0) }
                val options = listOf(
                    " 옷 ",
                    "모델"
                )
                MultiChoiceSegmentedButtonRow {
                    options.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = options.size
                            ),
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = colorDang,
                                activeContentColor = Color.White,
                                inactiveContainerColor = Color.White,
                                inactiveContentColor = Color.White,
                                activeBorderColor = colorDang,
                                inactiveBorderColor = colorDang,
                            ),
                            onCheckedChange = {
                                checkedOption =
                                    if (label == " 옷 ") {
                                        0
                                    } else {
                                        1
                                    }
                            },
                            checked = index == checkedOption
                        ) {
                            Text(label)
                        }
                    }
                }

                val coroutineScope = rememberCoroutineScope()
                FunTextButton("저장") {
                    navController.popBackStack()
                    saveEvent(coroutineScope, context, newPopupDetails)
                    ReLoadingManager.reLoading()
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val responseData by viewModel.responseData.observeAsState()
            val aiUrlText =
                "https://storage.googleapis.com/dangdanggeun-1b552.appspot.com/dangdanggeun%40intel.com/AIresults/20240703122308885560.png".replace(
                    "\"",
                    ""
                )
            responseData?.let { aiUrl ->
                val replaceAiUrl = aiUrl.replace("\"", "")
                val painter = rememberImagePainter(replaceAiUrl)
                Text(text = replaceAiUrl)
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
                //glide로 하니까 이미지 로드가 안됨 ㅜㅜㅜㅜ
//                GlideImage(
//                    imageModel = replaceAiUrl,
//                    modifier = Modifier.fillMaxSize(),
//                    contentScale = ContentScale.Fit
//                )

            } ?: run {
                CircularProgressIndicator()
            }

        }
        var popupVisibleState by remember { mutableStateOf(false) }
        Column(
            modifier = Modifier
                .weight(2f)
                .clickable {
                    popupVisibleState = true
                }
        ) {
            StateScreen(newPopupDetails)

            if (popupVisibleState) {
                InsertPopup(newPopupDetails, {
                    newPopupDetails = it
                }, {
                    popupVisibleState = false
                })
            }
        }
    }
}

private fun saveEvent(
    coroutineScope: CoroutineScope,
    context: Context,
    newPopupDetails: PopupDetails
) {
    val db = Firebase.firestore
    val dateTimeNow =
        LocalDateTime.now().toLocalDate().toString().replace("-", "") +
                LocalDateTime.now().toLocalTime().toString().replace(":", "")
                    .substring(0, 4)
    val sendMessage = hashMapOf(
        "InsertUser" to UserIDManager.userID.value,
        "name" to newPopupDetails.name,
        "date" to dateTimeNow,
        "imageUrl" to newPopupDetails.imageUri,
        "price" to newPopupDetails.price,
        "dealMethod" to newPopupDetails.dealMethod,
        "rating" to newPopupDetails.rating,
        "productDescription" to newPopupDetails.productDescription,
        "state" to 1 //1: 판매중, 2: 판매완료, 3:숨기기, 4:삭제
    )

    coroutineScope.launch(Dispatchers.IO) {
        db.collection("product")
            .document(dateTimeNow)
            .set(sendMessage)
            .addOnSuccessListener {
                Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                Toast.makeText(context, "업로드 성공", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.w(ContentValues.TAG, "Error writing document", e)
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
    }
}

@Composable
private fun StateScreen(newPopupDetails: PopupDetails) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Divider(color = colorDang, thickness = 2.dp)
        Row {
            Text(text = "제품명: ", color = colorDang)
            Text(text = newPopupDetails.name)
        }
        Divider(color = colorDang, thickness = 2.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Text(text = "가격", modifier = Modifier.weight(1f), color = colorDang)
            Text(text = " ${newPopupDetails.price}", modifier = Modifier.weight(1f))

            Text(text = "거래방법", modifier = Modifier.weight(1f), color = colorDang)
            Text(text = " ${newPopupDetails.dealMethod}", modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.weight(3f)
            ) {
                Spacer(modifier = Modifier.size(16.dp, 0.dp))
                Text(text = "상태", color = colorDang)
                Spacer(modifier = Modifier.size(16.dp, 0.dp))
                RatingBar(
                    value = newPopupDetails.rating,
                    style = RatingBarStyle.Fill(),
                    stepSize = StepSize.HALF,
                    onValueChange = {},
                    size = 16.dp,
                    spaceBetween = 2.dp,
                    onRatingChanged = {
                        Log.d("TAG", "onRatingChanged: $it")
                    }
                )
            }
        }
        Divider(color = colorDang, thickness = 2.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Text(text = " \n ${newPopupDetails.productDescription}")
        }
    }
}