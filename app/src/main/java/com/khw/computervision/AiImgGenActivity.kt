package com.khw.computervision

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
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
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.storage.StorageReference
import com.khw.computervision.ui.theme.ComputerVisionTheme
import com.skydoves.landscapist.glide.GlideImage
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AiImgGenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {

                var clickedUri by remember {
                    mutableStateOf("")
                }
                clickedUri = intent.getStringExtra("clickedUri") ?: ""

                var clickedCategory by remember {
                    mutableStateOf("")
                }
                clickedCategory = intent.getStringExtra("clickedCategory") ?: ""

                AiImgGenScreen(clickedUri, clickedCategory)
            }
        }
    }

    @Composable
    fun AiImgGenScreen(clickedUri: String, clickedCategory: String) {
        var extraClickedUri by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf(true) }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HeaderSection(Modifier.weight(1f), clickedUri, clickedCategory, extraClickedUri, gender)
            BodySection(Modifier.weight(5f),
                gender,
                clickedUri = clickedUri,
                clickedCategory = clickedCategory,
                extraClickedUri = extraClickedUri,
                onExtraClick = { onClickedRef, uri, category ->
                    extraClickedUri = uri
                },
                changeSex = { gender = !gender }
            )
        }
    }

    @Composable
    fun HeaderSection(
        modifier: Modifier,
        clickedUri: String,
        clickedCategory: String,
        extraClickedUri: String,
        gender: Boolean
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
        ) {
            LogoScreen(activityName = "AiImgGen") {}
            Row(
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                val context = LocalContext.current
                val modelGender =
                    if (gender) {
                        "2"
                    } else {
                        "1"
                    }
                var requestMsg by remember {
                    mutableStateOf("")
                }
                var requestAiImg by remember {
                    mutableStateOf("")
                }

                Text(text = requestMsg)
                var isLoading by remember {
                    mutableStateOf(false)
                }

                val viewModel: SharedViewModel by viewModels { SharedViewModelFactory() }
                FunTextButton(buttonText = "다음") {
                    isLoading = true
                    if (clickedCategory == "top") {
                        viewModel.sendServerRequest(
                            topURL = clickedUri,
                            bottomURL = extraClickedUri,
                            gender = modelGender
                        )
                    } else if (clickedCategory == "bottom") {
                        viewModel.sendServerRequest(
                            topURL = extraClickedUri,
                            bottomURL = clickedUri,
                            gender = modelGender
                        )
                    }
//                    finish()
                    putInsertActivity(context, clickedUri, isLoading, requestAiImg)
                }
                viewModel.responseData.observeAsState().value?.let { Text(text = it) }
            }
        }
    }

    private fun putInsertActivity(
        context: Context,
        clickedUri: String,
        isLoading: Boolean,
        requestAiImg: String
    ) {

        val userIntent = Intent(context, InsertActivity::class.java)
        userIntent.putExtra("clickedUri", clickedUri)
        userIntent.putExtra("requestAiImg", requestAiImg)
        userIntent.putExtra("isLoading", isLoading)
        userIntent.putExtra("isLoading", isLoading)
        startActivity(userIntent)
    }

    @Composable
    fun BodySection(
        modifier: Modifier,
        gender: Boolean,
        clickedUri: String,
        clickedCategory: String,
        extraClickedUri: String,
        onExtraClick: (StorageReference, String, String) -> Unit,
        changeSex: () -> Unit
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
                    GenderSelection(Modifier.weight(1f), gender) { changeSex() }
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
                    clickedUri = clickedUri,
                    clickedCategory = clickedCategory,
                    extraClickedUri = extraClickedUri
                )
            }
            Row(
                modifier = Modifier.weight(2f)
            ) {
                ImageGridSection(clickedCategory, onExtraClick)
            }
        }
    }

    @Composable
    fun SideSection(
        modifier: Modifier,
        clickedUri: String,
        clickedCategory: String,
        extraClickedUri: String
    ) {
        Column(modifier = modifier) {
            FunTextButton(buttonText = "판매옷") { }
            GlideImage(
                imageModel = clickedUri,
                modifier = Modifier.size(80.dp)
            )
            when (clickedCategory) {
                "top" -> FunTextButton(buttonText = "하의 선택") { }
                "bottom" -> FunTextButton(buttonText = "상의 선택") { }
            }
            GlideImage(
                imageModel = extraClickedUri,
                modifier = Modifier.size(80.dp)
            )
        }
    }

    @Composable
    fun ImageGridSection(
        clickedCategory: String,
        onExtraClick: (StorageReference, String, String) -> Unit
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            when (clickedCategory) {
                "top" -> ImageGrid("bottom", true, onExtraClick)
                "bottom" -> ImageGrid("top", true, onExtraClick)
            }
        }
    }

    @Composable
    fun GenderSelection(modifier: Modifier, gender: Boolean, changeSex: () -> Unit) {

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GenderOption(
                label = "여",
                isSelected = gender,
                onCheckedChange = { changeSex() }
            )
            GenderOption(
                label = "남",
                isSelected = !gender,
                onCheckedChange = { changeSex() }
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

    private fun sendListToServer(
        topURL: String,
        bottomURL: String,
        gender: String, // Assuming gender is passed as a string ('1' for male, '2' for female)
        successEvent: (String) -> Unit,
        errorEvent: (String) -> Unit
    ) {

        val userIDPart =
            RequestBody.create("text/plain".toMediaTypeOrNull(), UserIDManager.userID.value)
        val topURLPart =
            RequestBody.create("text/plain".toMediaTypeOrNull(), topURL)
        val bottomURLPart =
            RequestBody.create("text/plain".toMediaTypeOrNull(), bottomURL)
        val genderPart =
            RequestBody.create("text/plain".toMediaTypeOrNull(), gender)

        // Map으로 데이터 구성
        val dataMap = mapOf(
            "userID" to userIDPart,
            "topURL" to topURLPart,
            "bottomURL" to bottomURLPart,
            "gender" to genderPart
        )

        RetrofitClient.instance.uploadList(dataMap)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        successEvent("${response.body()?.string()}")
                    } else {
                        errorEvent("에러 메시지: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    errorEvent("요청이 실패했습니다: ${t.message}")
                }
            })
    }
}

// ViewModel 클래스 정의
class SharedViewModel : ViewModel() {

    private val _responseData = MutableLiveData<String>()
    val responseData: LiveData<String> get() = _responseData

    fun sendServerRequest(
        topURL: String,
        bottomURL: String,
        gender: String
    ) {
        val userIDPart =
            RequestBody.create("text/plain".toMediaTypeOrNull(), UserIDManager.userID.value)
        val topURLPart = RequestBody.create("text/plain".toMediaTypeOrNull(), topURL)
        val bottomURLPart = RequestBody.create("text/plain".toMediaTypeOrNull(), bottomURL)
        val genderPart = RequestBody.create("text/plain".toMediaTypeOrNull(), gender)

        val dataMap = mapOf(
            "userID" to userIDPart,
            "topURL" to topURLPart,
            "bottomURL" to bottomURLPart,
            "gender" to genderPart
        )

        RetrofitClient.instance.uploadList(dataMap).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    _responseData.postValue(response.body()?.string())
                } else {
                    _responseData.postValue(response.errorBody()?.string())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                _responseData.postValue(t.message)
            }
        })
    }
}

class SharedViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SharedViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SharedViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

