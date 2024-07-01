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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.khw.computervision.ui.theme.ComputerVisionTheme

class DetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {

                // Intent에서 Bundle을 가져옵니다.
                val bundle = intent.getBundleExtra("product")
                var productMap: Map<String, String> = mutableMapOf()
                if (bundle != null) {
                    productMap = bundleToMap(bundle)
                }

                DetailScreen(productMap)
            }
        }
    }

    @Composable
    fun DetailScreen(productMap: Map<String, String>) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LogoScreen("Detail") { finish() }
            Image(
                painter = painterResource(id = R.drawable.character4),
                contentDescription = "",
                modifier = Modifier
                    .padding(20.dp)
                    .size(320.dp)
            )
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
                    text = productMap.getValue("InsertUser"), modifier = Modifier
                        .padding(top = 10.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                var messagePopUp by remember { mutableStateOf(false) }
                TextButton(
                    onClick = {
                        messagePopUp = true
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
                if (messagePopUp) {
                    MessagePopup(
                        productMap.getValue("InsertUser")
                    ) { messagePopUp = false }
                }

            }
            Divider(color = colorDang, thickness = 2.dp)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "가격: ${productMap.getValue("price")}")

                Spacer(modifier = Modifier.weight(1f))
                Text(text = "거래방법: ${productMap.getValue("dealMethod")}")

                Spacer(modifier = Modifier.weight(1f))
                Text(text = "상태: ${productMap.getValue("rating")}")
                Spacer(modifier = Modifier.weight(1f))
            }
            Divider(color = colorDang, thickness = 2.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(text = productMap.getValue("productDescription"))
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}