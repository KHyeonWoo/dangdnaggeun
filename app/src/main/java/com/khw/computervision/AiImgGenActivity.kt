package com.khw.computervision

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.google.firebase.storage.StorageReference
import com.skydoves.landscapist.glide.GlideImage

//class AiImgGenActivity : ComponentActivity() {
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
//                var clickedCategory by remember {
//                    mutableStateOf("")
//                }
//                clickedCategory = intent.getStringExtra("clickedCategory") ?: ""
//
//                AiImgGenScreen(clickedUri, clickedCategory)
//            }
//        }
//    }
//
//    @Composable
//    fun AiImgGenScreen(clickedUri: String, clickedCategory: String) {
//        var extraClickedUri by remember { mutableStateOf("") }
//        var gender by remember { mutableStateOf(true) }
//
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.Center
//        ) {
//            HeaderSection(Modifier.weight(1f), clickedUri, clickedCategory, extraClickedUri, gender)
//            BodySection(Modifier.weight(5f),
//                gender,
//                clickedUri = clickedUri,
//                clickedCategory = clickedCategory,
//                extraClickedUri = extraClickedUri,
//                onExtraClick = { onClickedRef, uri, category ->
//                    extraClickedUri = uri
//                },
//                changeSex = { gender = !gender }
//            )
//        }
//    }
//
//    @Composable
//    fun HeaderSection(
//        modifier: Modifier,
//        clickedUri: String,
//        clickedCategory: String,
//        extraClickedUri: String,
//        gender: Boolean
//    ) {
//        Box(
//            modifier = modifier
//                .fillMaxWidth()
//        ) {
//            LogoScreen(activityName = "AiImgGen") {}
//            Row(
//                modifier = Modifier.align(Alignment.BottomEnd)
//            ) {
//                val context = LocalContext.current
//                val modelGender =
//                    if (gender) {
//                        "2"
//                    } else {
//                        "1"
//                    }
//                var requestMsg by remember {
//                    mutableStateOf("")
//                }
//                var requestAiImg by remember {
//                    mutableStateOf("")
//                }
//
//                Text(text = requestMsg)
//                FunTextButton(buttonText = "다음") {
//                    if (clickedCategory == "top") {
//                        sendListToServer(
//                            topURL = clickedUri,
//                            bottomURL = extraClickedUri,
//                            gender = modelGender,
//                            successEvent = { requestAiImg = it },
//                            errorEvent = { requestMsg = it }
//                        )
//                    } else if (clickedCategory == "bottom") {
//                        sendListToServer(
//                            topURL = extraClickedUri,
//                            bottomURL = clickedUri,
//                            gender = modelGender,
//                            successEvent = { requestAiImg = it },
//                            errorEvent = { requestMsg = it }
//                        )
//                    }
//
////                    finish()
//                    val userIntent = Intent(context, InsertActivity::class.java)
//                    userIntent.putExtra("clickedUri", clickedUri)
//                    userIntent.putExtra("requestAiImg", requestAiImg)
//                    context.startActivity(userIntent)
//                }
//            }
//        }
//    }
//
//    @Composable
//    fun BodySection(
//        modifier: Modifier,
//        gender: Boolean,
//        clickedUri: String,
//        clickedCategory: String,
//        extraClickedUri: String,
//        onExtraClick: (StorageReference, String, String) -> Unit,
//        changeSex: () -> Unit
//    ) {
//        Column(
//            modifier = modifier
//                .fillMaxWidth(),
//            verticalArrangement = Arrangement.Center,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            Row(
//                modifier = Modifier.weight(3f)
//            ) {
//                Column(modifier = Modifier.weight(2f)) {
//                    GenderSelection(Modifier.weight(1f), gender) { changeSex() }
//                    Image(
//                        painter = painterResource(id = R.drawable.character2),
//                        contentDescription = "AIModel",
//                        modifier = Modifier.weight(9f),
//                        alignment = Alignment.Center,
//                        contentScale = ContentScale.Fit
//                    )
//                }
//                SideSection(
//                    Modifier.weight(1f),
//                    clickedUri = clickedUri,
//                    clickedCategory = clickedCategory,
//                    extraClickedUri = extraClickedUri
//                )
//            }
//            Row(
//                modifier = Modifier.weight(2f)
//            ) {
//                ImageGridSection(clickedCategory, onExtraClick)
//            }
//        }
//    }
//
//    @Composable
//    fun SideSection(
//        modifier: Modifier,
//        clickedUri: String,
//        clickedCategory: String,
//        extraClickedUri: String
//    ) {
//        Column(modifier = modifier) {
//            FunTextButton(buttonText = "판매옷") { }
//            GlideImage(
//                imageModel = clickedUri,
//                modifier = Modifier.size(80.dp)
//            )
//            when (clickedCategory) {
//                "top" -> FunTextButton(buttonText = "하의 선택") { }
//                "bottom" -> FunTextButton(buttonText = "상의 선택") { }
//            }
//            GlideImage(
//                imageModel = extraClickedUri,
//                modifier = Modifier.size(80.dp)
//            )
//        }
//    }
//
//    @Composable
//    fun ImageGridSection(
//        clickedCategory: String,
//        onExtraClick: (StorageReference, String, String) -> Unit
//    ) {
//        Row(modifier = Modifier.fillMaxWidth()) {
//            when (clickedCategory) {
//                "top" -> ImageGrid("bottom", true, onExtraClick)
//                "bottom" -> ImageGrid("top", true, onExtraClick)
//            }
//        }
//    }
//
//    @Composable
//    fun GenderSelection(modifier: Modifier, gender: Boolean, changeSex: () -> Unit) {
//
//        Row(
//            modifier = modifier.fillMaxWidth(),
//            horizontalArrangement = Arrangement.Center,
//            verticalAlignment = Alignment.CenterVertically
//        ) {
//            GenderOption(
//                label = "여",
//                isSelected = gender,
//                onCheckedChange = { changeSex() }
//            )
//            GenderOption(
//                label = "남",
//                isSelected = !gender,
//                onCheckedChange = { changeSex() }
//            )
//        }
//    }
//
//    @Composable
//    fun GenderOption(
//        label: String,
//        isSelected: Boolean,
//        onCheckedChange: () -> Unit
//    ) {
//        Column {
//            Text(
//                text = label,
//                modifier = Modifier.align(Alignment.CenterHorizontally),
//                color = colorDang
//            )
//            Checkbox(
//                checked = isSelected,
//                onCheckedChange = { onCheckedChange() },
//                colors = CheckboxDefaults.colors(
//                    checkedColor = colorDang,
//                    uncheckedColor = colorDang,
//                    checkmarkColor = Color.White
//                )
//            )
//        }
//    }
//
//    private fun sendListToServer(
//        topURL: String,
//        bottomURL: String,
//        gender: String, // Assuming gender is passed as a string ('1' for male, '2' for female)
//        successEvent: (String) -> Unit,
//        errorEvent: (String) -> Unit
//    ) {
//
//        val userIDPart =
//            RequestBody.create("text/plain".toMediaTypeOrNull(), UserIDManager.userID.value)
//        val topURLPart =
//            RequestBody.create("text/plain".toMediaTypeOrNull(), topURL)
//        val bottomURLPart =
//            RequestBody.create("text/plain".toMediaTypeOrNull(), bottomURL)
//        val genderPart =
//            RequestBody.create("text/plain".toMediaTypeOrNull(), gender)
//
//        // Map으로 데이터 구성
//        val dataMap = mapOf(
//            "userID" to userIDPart,
//            "topURL" to topURLPart,
//            "bottomURL" to bottomURLPart,
//            "gender" to genderPart
//        )
//
//        RetrofitClient.instance.uploadList(dataMap)
//            .enqueue(object : Callback<ResponseBody> {
//                override fun onResponse(
//                    call: Call<ResponseBody>,
//                    response: Response<ResponseBody>
//                ) {
//                    if (response.isSuccessful) {
//                        successEvent("${response.body()?.string()}")
//                    } else {
//                        errorEvent("에러 메시지: ${response.errorBody()?.string()}")
//                    }
//                }
//
//                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
//                    errorEvent("요청이 실패했습니다: ${t.message}")
//                }
//            })
//    }
//}

@Composable
fun AiImgGenScreen(
    navController: NavHostController,
    encodingClickedUrl: String,
    clickedCategory: String,
    aiViewModel: AiViewModel,
    closetViewModel: ClosetViewModel
) {
    var extraClickedUrl by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        HeaderSection(Modifier.weight(1f), navController, encodingClickedUrl, clickedCategory, aiViewModel, extraClickedUrl, gender)
        BodySection(Modifier.weight(5f),
            gender,
            clickedUrl = encodingClickedUrl,
            clickedCategory = clickedCategory,
            extraClickedUrl = extraClickedUrl,
            closetViewModel = closetViewModel,
            onExtraClick = {
                extraClickedUrl = it
            },
            changeWoman = { gender = true },
            changeMan = { gender = false }
        )
    }
}

@Composable
fun HeaderSection(
    modifier: Modifier,
    navController: NavHostController,
    clickedUrl: String,
    clickedCategory: String,
    viewModel: AiViewModel,
    extraClickedUrl: String,
    gender: Boolean
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.align(Alignment.BottomEnd)
        ) {
            val modelGender = if (gender) "2" else "1"

            FunTextButton(buttonText = "다음") {

                viewModel.resetResponseData()
                if (clickedCategory == "top") {
                    viewModel.sendServerRequest(
                        topURL = clickedUrl,
                        bottomURL = extraClickedUrl,
                        gender = modelGender,
                    )
                } else if (clickedCategory == "bottom") {
                    viewModel.sendServerRequest(
                        topURL = extraClickedUrl,
                        bottomURL = clickedUrl,
                        gender = modelGender,
                    )
                }
                val encodeClickedUrl = encodeUrl(clickedUrl)
                navController.navigate("insert/$encodeClickedUrl/$clickedCategory")
            }
        }
    }
}

@Composable
fun BodySection(
    modifier: Modifier,
    gender: Boolean,
    clickedUrl: String,
    clickedCategory: String,
    extraClickedUrl: String,
    closetViewModel: ClosetViewModel,
    onExtraClick: (String) -> Unit,
    changeWoman: () -> Unit,
    changeMan: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.weight(3f)
        ) {
            Column(modifier = Modifier.weight(2f)) {
                GenderSelection(Modifier.weight(1f), gender, { changeWoman() }, { changeMan() })
                Image(
                    painter = painterResource(id = R.drawable.character2),
                    contentDescription = "AIModel",
                    modifier = Modifier.weight(9f),
                    alignment = Alignment.Center,
                    contentScale = ContentScale.Fit
                )
            }
            SideSection(
                Modifier.weight(1f),
                clickedUrl = clickedUrl,
                clickedCategory = clickedCategory,
                extraClickedUrl = extraClickedUrl
            )
        }
        Row(
            modifier = Modifier.weight(2f)
        ) {
            ImageGridSection(clickedCategory, closetViewModel) { _, url, _ ->
                onExtraClick(url)
            }
        }
    }
}

@Composable
fun SideSection(
    modifier: Modifier,
    clickedUrl: String,
    clickedCategory: String,
    extraClickedUrl: String
) {
    Column(modifier = modifier) {
        FunTextButton(buttonText = "판매옷") { }
        GlideImage(
            imageModel = clickedUrl,
            modifier = Modifier.size(80.dp)
        )
        when (clickedCategory) {
            "top" -> FunTextButton(buttonText = "하의 선택") { }
            "bottom" -> FunTextButton(buttonText = "상의 선택") { }
        }
        GlideImage(
            imageModel = extraClickedUrl,
            modifier = Modifier.size(80.dp)
        )
    }
}

@Composable
fun ImageGridSection(
    clickedCategory: String,
    closetViewModelUrl: ClosetViewModel,
    onExtraClick: (StorageReference, String, String) -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        when (clickedCategory) {
            "top" -> ImageGrid("bottom", onExtraClick, closetViewModelUrl)
            "bottom" -> ImageGrid("top", onExtraClick, closetViewModelUrl)
        }
    }
}

@Composable
fun GenderSelection(
    modifier: Modifier,
    gender: Boolean,
    changeWoman: () -> Unit,
    changeMan: () -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        GenderOption(
            label = "여",
            isSelected = gender,
            onCheckedChange = { changeWoman() }
        )
        GenderOption(
            label = "남",
            isSelected = !gender,
            onCheckedChange = { changeMan() }
        )
    }
}

@Composable
fun GenderOption(
    label: String,
    isSelected: Boolean,
    onCheckedChange: () -> Unit
) {
    Column {
        Text(
            text = label,
            modifier = Modifier.align(Alignment.CenterHorizontally),
            color = colorDang
        )
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onCheckedChange() },
            colors = CheckboxDefaults.colors(
                checkedColor = colorDang,
                uncheckedColor = colorDang,
                checkmarkColor = Color.White
            )
        )
    }
}
