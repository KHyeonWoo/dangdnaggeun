package com.khw.computervision

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.khw.computervision.ui.theme.ComputerVisionTheme
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.tasks.await

class SalesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var userID by remember {
                mutableStateOf("")
            }
            userID = intent.getStringExtra("userID") ?: ""

            ComputerVisionTheme {
                SaleScreen(userID)
            }
        }
    }

    @Composable
    fun SaleScreen(userID: String) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val context = LocalContext.current
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                LogoScreen("Sales") { finish() }
                var successUpload by remember { mutableStateOf(false) }
                var profileUri: String? by remember { mutableStateOf(null) }

                LaunchedEffect(successUpload) {
                    profileUri = getProfile(userID)
                }

                var visiblePopup by remember { mutableStateOf(false) }
                val modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        visiblePopup = !visiblePopup
                    }

                Column(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                ) {
                    profileUri?.let {
                        GlideImage(
                            imageModel = it,
                            contentDescription = "Image",
                            modifier = modifier
                                .clip(RoundedCornerShape(20.dp))
                        )
                    } ?: Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "",
                        modifier = modifier
                    )
                }

                if (visiblePopup) {
                    ProfilePopup(
                        profileUri,
                        userID,
                        { visiblePopup = false },
                        { successUpload = !successUpload })
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FunTextButton("현재 판매하는 제품이에요") {}
            }
            ImageList(userID, ReLoadingManager.reLoadingValue.value)

            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))
                FunTextButton("+ 글쓰기") {
                    val userIntent = Intent(context, InsertActivity::class.java)
                    userIntent.putExtra("userID", userID)
                    context.startActivity(userIntent)
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }

    private suspend fun getProfile(userID: String): String? {
        val storageRef = Firebase.storage.reference.child("$userID/profile.jpg")
        var faceUri: String? = null
        try {
            faceUri = storageRef.downloadUrl.await().toString()
        } catch (_: StorageException) {

        }
        return faceUri
    }


    @Composable
    fun ImageList(userID: String, reLoading: Boolean) {
        // rememberSaveable로 상태를 저장하고 복원할 수 있도록 합니다.
        var productMap: Map<String, Map<String, String>> by remember { mutableStateOf(emptyMap()) }
        GetProduct(reLoading) { productMap = it }

        val context = LocalContext.current
        Column(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.Center
            ) {
                for ((key, value) in productMap) {
                    Column(
                        modifier = Modifier.clickable {
                            val productIntent = Intent(context, DetailActivity::class.java)
                            productIntent.putExtra("userID", userID)
                            productIntent.putExtra("product", mapToBundle(value))
                            context.startActivity(productIntent)
                        }) {
                        for ((fieldKey, fieldValue) in value) {
                            Text(text = "$fieldKey: $fieldValue")
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun ImageBox(userID: String) {
        val context = LocalContext.current
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "",
            modifier = Modifier
                .padding(20.dp)
                .clickable {
                    val userIntent = Intent(context, DetailActivity::class.java)
                    userIntent.putExtra("userID", userID)
                    context.startActivity(userIntent)
                }
        )
    }
}
