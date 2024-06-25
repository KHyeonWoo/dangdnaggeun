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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.khw.computervision.ui.theme.ComputerVisionTheme

class SalesActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                SaleScreen()
            }
        }
    }

    @Composable
    fun SaleScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
        ) {
            val context = LocalContext.current
            Box(
                modifier = Modifier.fillMaxWidth(),
            ) {
                LogoScreen(context, "Sales")
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
                if (visiablePopup) {
                    ProfilePopup { visiablePopup = false }
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

    @Composable
    fun ProfilePopup(close: () -> Unit) {
        val context = LocalContext.current
        AlertDialog(
            onDismissRequest = { close() },
            title = { Text(text = "") },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(600.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Text(text = "")
                    Spacer(modifier = Modifier.weight(1f))
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "",
                        modifier = Modifier
                            .size(160.dp)
                    )
                    Text(text = "user name")
                    Spacer(modifier = Modifier.weight(2f))

                    FunTextButton("내가 판매 중인 제품") { }
                    FunTextButton("내가 판매한 제품") { }
                    FunTextButton("내게 온 메세지") { }
                    FunTextButton("로그아웃") {
                        context.startActivity(Intent(context, LoginActivity::class.java))
                    }
                }
            },
            confirmButton =
            { },
            dismissButton =
            {
                Button(onClick = { close() }) {
                    Text("Cancel")
                }
            }
        )

    }

}
