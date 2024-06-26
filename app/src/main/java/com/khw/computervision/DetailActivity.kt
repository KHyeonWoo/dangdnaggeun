package com.khw.computervision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.khw.computervision.ui.theme.ComputerVisionTheme

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                var userID by remember {
                    mutableStateOf("")
                }
                userID = intent.getStringExtra("user") ?: ""

                DetailScreen(userID)
            }
        }
    }

    @Composable
    fun DetailScreen(userID: String) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            LogoScreen("Detail")
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "",
                modifier = Modifier
                    .padding(20.dp)
                    .size(320.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "",
                    modifier = Modifier
                        .padding(start = 20.dp)
                        .size(60.dp)
                )
                Text(
                    text = "사용자", modifier = Modifier
                        .padding(top = 10.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                var popupVisiableState by remember { mutableStateOf(false) }
                TextButton(
                    onClick = {
                        popupVisiableState = true
                    },
                ) {
                    Text(
                        text = "메세지\n보내기",
                        modifier = Modifier
                            .padding(
                                top = 10.dp,
                                end = 20.dp
                            )
                    )
                }
                if (popupVisiableState) {
                    MessagePopup(userID) { popupVisiableState = false }
                }

            }
            Divider(color = colorDang, thickness = 2.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "가격: 10000원")

                Spacer(modifier = Modifier.weight(1f))
                Text(text = "거래방법: 직거래")

                Spacer(modifier = Modifier.weight(1f))
                Text(text = "상태: 별별별")
                Spacer(modifier = Modifier.weight(1f))
            }
            Divider(color = colorDang, thickness = 2.dp)

            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = "게시글 (판매 이유, 구입 장소, 기타 등등)")
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }

}