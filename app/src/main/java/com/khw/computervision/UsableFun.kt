package com.khw.computervision

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

val colorDang = Color(0xFFF3BB66)

// 싱글톤 클래스 정의
object DataManager {
    var reLoading: Boolean = false
}

@Composable
fun LogoScreen(activityName: String, goBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (activityName != "Login") {
            Spacer(modifier = Modifier.height(20.dp))
        }
        Text(
            text = "당당근",
            fontSize = 50.sp,
            color = colorDang,
            modifier = Modifier.clickable {
                if (activityName != "Login" && activityName != "Sales") {
                    goBack()
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))

        when (activityName) {
            "Login" -> {
                TextBox("우리 당당하게 팔아요")
            }

            "Sales" -> {
                TextBox("우리 당당하게 보여줘요")
            }

            "Detail" -> {
                TextBox("우리 당당하게 알려줘요")
            }

            "Insert" -> {
                TextBox("우리 당당하게 팔아요")
            }

            "Decorate" -> {
                TextBox("우리 당당하게 꾸며봐요")
            }

            "UserProfile" -> {
                TextBox("우리 당당하게 확인해요")
            }
        }
    }
}

@Composable
fun TextBox(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        color = colorDang
    )
}


@Composable
fun gifImageDecode(name: Int): AsyncImagePainter {
    val context = LocalContext.current

    val mascotImageUri = remember {
        Uri.parse("android.resource://${context.packageName}/${name}")
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(mascotImageUri)
            .size(Size.ORIGINAL)
            .build(),
        imageLoader = imageLoader
    )

    return painter
}

@Composable
fun FunTextButton(buttonText: String, clickEvent: () -> Unit) {
    Button(
        onClick = { clickEvent() },
        colors = ButtonDefaults.buttonColors(
            colorDang
        )
    ) {
        Text(text = buttonText, color = Color.White)
    }
}


@Composable
fun returnMessageIndex(userID: String): Int {
    val insertMap = produceState(initialValue = emptyMap()) {
        Firebase.firestore.collection(userID)
            .get()
            .addOnSuccessListener { result ->
                // 데이터 가져오기가 성공하면, 문서 ID와 메시지 내용을 맵으로 만듭니다.
                // 결과를 'value'에 할당하여 상태를 업데이트합니다.
                value = result.documents.associate {
                    it.id to it.data?.get("date") as String
                }
            }
    }
    return insertMap.value.size + 1
}

@Composable
fun returnInsertIndex(): Int {
    val insertMap = produceState<Map<String, String>>(initialValue = emptyMap()) {
        Firebase.firestore.collection("product")
            .get()
            .addOnSuccessListener { result ->
                // 데이터 가져오기가 성공하면, 문서 ID와 메시지 내용을 맵으로 만듭니다.
                // 결과를 'value'에 할당하여 상태를 업데이트합니다.
                value = result.documents.associate {
                    it.id to it.data?.get("InsertUser") as String
                }
            }
    }
    return insertMap.value.size + 1
}

@Composable
fun getMessage(userID: String): Map<String, String> {
    val context = LocalContext.current

    // Firebase에서 데이터를 가져오고, 이 데이터를 상태로 관리합니다.
    // 초기값은 빈 맵(emptyMap)으로 설정합니다.
    val messageMap = produceState<Map<String, String>>(initialValue = emptyMap()) {
        Firebase.firestore.collection(userID)
            .get()
            .addOnSuccessListener { result ->
                // 데이터 가져오기가 성공하면, 문서 ID와 메시지 내용을 맵으로 만듭니다.
                // 결과를 'value'에 할당하여 상태를 업데이트합니다.
                value = result.documents.associate {
                    it.id to "보낸일시: ${
                        it.getString("date").orEmpty()
                    }\n보낸사람: ${
                        it.getString("sendUser").orEmpty()
                    }\n메세지: ${
                        it.getString("message").orEmpty()
                    }"
                }
            }
            .addOnFailureListener { exception ->
                // 데이터 가져오기가 실패하면, 에러 메시지를 토스트로 보여줍니다.
                Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
            }
    }

    // messageMap의 크기(size)를 반환합니다.
    return messageMap.value
}

@Composable
fun GetProduct(reloading: Boolean, getProductEvent: (Map<String, Map<String, String>>) -> Unit) {
        val context = LocalContext.current

        // LaunchedEffect로 비동기 작업을 처리합니다.
        LaunchedEffect(reloading) {
            Firebase.firestore.collection("product")
                .get()
                .addOnSuccessListener { result ->
                    // 데이터 가져오기가 성공하면, 문서 ID와 필드들을 맵으로 만듭니다.
                    val newProductMap = result.documents.associate { document ->
                        val fields = mapOf(
                            "InsertUser" to (document.getString("InsertUser") ?: ""),
                            "date" to (document.getString("date") ?: ""),
                            "dealMethod" to (document.getString("dealMethod") ?: ""),
                            "imageUrl" to (document.getString("imageUrl") ?: ""),
                            "price" to (document.get("price")?.toString() ?: ""),
                            "productDescription" to (document.getString("productDescription")
                                ?: ""),
                            "rating" to (document.get("rating")?.toString() ?: ""),
                            "state" to (document.get("state")?.toString() ?: ""),
                        )
                        document.id to fields
                    }
                    getProductEvent(newProductMap)
                }
                .addOnFailureListener { exception ->
                    // 데이터 가져오기가 실패하면, 에러 메시지를 토스트로 보여줍니다.
                    Toast.makeText(context, exception.message, Toast.LENGTH_SHORT).show()
                }
        }
}


fun mapToBundle(map: Map<String, String>): Bundle {
    val bundle = Bundle()
    for ((key, value) in map) {
        bundle.putString(key, value)
    }
    return bundle
}


// Bundle을 Map으로 변환하는 함수
fun bundleToMap(bundle: Bundle): Map<String, String> {
    val map = mutableMapOf<String, String>()
    for (key in bundle.keySet()) {
        map[key] = bundle.getString(key).orEmpty()
    }
    return map
}