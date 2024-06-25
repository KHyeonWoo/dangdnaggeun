package com.khw.computervision

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.ktx.storage
import com.khw.computervision.ui.theme.ComputerVisionTheme
import kotlinx.coroutines.tasks.await

class SalesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {

                var user by remember {
                    mutableStateOf("")
                }
                user = intent.getStringExtra("user") ?: ""

                SaleScreen(user)
            }
        }
    }

    @Composable
    fun SaleScreen(user: String) {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val context = LocalContext.current
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                LogoScreen("Sales")
                var visiablePopup by remember { mutableStateOf(false) }
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "",
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(80.dp)
                        .padding(32.dp)
                        .clickable {
                            visiablePopup = true
                        }
                )
                var successUpload by remember { mutableStateOf(false) }
                var faceUri: String? by remember { mutableStateOf(null) }

                LaunchedEffect(successUpload) {
                    faceUri = getProfile(user)
                }

                if (visiablePopup) {
                    ProfilePopup(faceUri, user, { visiablePopup = false }, {successUpload = !successUpload})
                }
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                FunTextButton("현재 판매하는 제품이에요") { /* TODO */ }
            }
            ImageList()

            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.weight(1f))
                FunTextButton("+ 글쓰기") {
                    context.startActivity(Intent(context, InsertActivity::class.java))
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    private suspend fun getProfile(user: String): String? {
        val storageRef = Firebase.storage.reference.child("$user/profile.jpg")
        var faceUri: String? = null
        try {
            faceUri = storageRef.downloadUrl.await().toString()
        } catch (_: StorageException) {

        }
        return faceUri
    }

    @Composable
    fun ImageList() {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ImageBox()
                ImageBox()
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ImageBox()
                ImageBox()
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                ImageBox()
                ImageBox()
            }
        }
    }

    @Composable
    fun ImageBox() {
        val context = LocalContext.current
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = "",
            modifier = Modifier
                .padding(20.dp)
                .clickable {
                    context.startActivity(Intent(context, DetailActivity::class.java))
                }
        )
    }
}
