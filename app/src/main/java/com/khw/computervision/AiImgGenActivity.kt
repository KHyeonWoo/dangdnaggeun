package com.khw.computervision

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.storage.StorageReference
import com.khw.computervision.ui.theme.ComputerVisionTheme
import com.skydoves.landscapist.glide.GlideImage
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
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
        var extraClickedCategory by remember { mutableStateOf("") }

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HeaderSection(Modifier.weight(1f), clickedUri)
            BodySection(Modifier.weight(5f),
                clickedUri = clickedUri,
                clickedCategory = clickedCategory,
                extraClickedUri = extraClickedUri,
                onExtraClick = { onClickedRef, uri, category ->
                    extraClickedUri = uri
                    extraClickedCategory = category
                }
            )
        }
    }

    @Composable
    fun HeaderSection(modifier: Modifier, clickedUri: String) {
        Box(
            modifier = modifier
                .fillMaxWidth()
        ) {
            LogoScreen(activityName = "AiImgGen") {}
            Row(
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                val context = LocalContext.current
                FunTextButton(buttonText = "다음") {
                    val userIntent = Intent(context, InsertActivity::class.java)
                    userIntent.putExtra("clickedUri", clickedUri)
                    context.startActivity(userIntent)
                }
            }
        }
    }

    @Composable
    fun BodySection(
        modifier: Modifier,
        clickedUri: String,
        clickedCategory: String,
        extraClickedUri: String,
        onExtraClick: (StorageReference, String, String) -> Unit
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
                    GenderSelection(Modifier.weight(1f))
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
                modifier = Modifier.weight(2f)) {
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
    fun GenderSelection(modifier: Modifier) {
        var sex by remember { mutableStateOf(true) }

        Row(
            modifier = modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            GenderOption(
                label = "여",
                isSelected = sex,
                onCheckedChange = { sex = !sex }
            )
            GenderOption(
                label = "남",
                isSelected = !sex,
                onCheckedChange = { sex = !sex }
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
        list: List<String>,
        successEvent: (String) -> Unit
    ) {

        // JSON 변환을 위한 Moshi 초기화
        val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
        val jsonAdapter = moshi.adapter(List::class.java)
        val json = jsonAdapter.toJson(list)

        // JSON 문자열을 RequestBody로 생성
        val listRequestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json)
        val listPart = MultipartBody.Part.createFormData("list", null, listRequestBody)

        // userIdPart 생성
        val userIdPart =
            RequestBody.create("text/plain".toMediaTypeOrNull(), UserIDManager.userID.value)


        RetrofitClient.instance.uploadList(listPart, userIdPart)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody>,
                    response: Response<ResponseBody>
                ) {
                    if (response.isSuccessful) {
                        successEvent("성공")
                    } else {
                        successEvent("에러 메시지: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                    successEvent("요청이 실패했습니다: ${t.message}")
                }
            })
    }

}
