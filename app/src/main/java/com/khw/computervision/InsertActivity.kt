package com.khw.computervision

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.gowtham.ratingbar.StepSize
import com.khw.computervision.ui.theme.ComputerVisionTheme

class InsertActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                InsertScreen()
            }
        }
    }

    @Composable
    fun InsertScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            LogoScreen(context, "Insert")
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                var popupVisiableState by remember { mutableStateOf(false) }

                FunTextButton("꾸미기") {
                    context.startActivity(Intent(context, DecorateActivity::class.java))
                }
                Spacer(modifier = Modifier.weight(1f))

                FunTextButton("수정") {
                    popupVisiableState = true
                }

                FunTextButton("저장") {
                    context.startActivity(Intent(context, SalesActivity::class.java))
                }

                if (popupVisiableState) {
                    MessagePopup { popupVisiableState = false }
                }
            }
            Button(onClick = {
                context.startActivity(Intent(context, DetectionActivity::class.java))
            }){
                Text(text = "gogo")
            }
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "",
                modifier = Modifier.size(320.dp)
            )
            StateScreen()

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

    @Composable
    private fun StateScreen() {
        Divider(color = colorDang, thickness = 2.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Spacer(modifier = Modifier.weight(1f))
            Text(text = "가격")

            Spacer(modifier = Modifier.weight(1f))
            Text(text = "거래방법")

            Spacer(modifier = Modifier.weight(1f))
            Text(text = "상태")
            Spacer(modifier = Modifier.weight(1f))
        }
        Divider(color = colorDang, thickness = 2.dp)

    }

    @Composable
    fun MessagePopup(close: () -> Unit) {
        var price: String by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { close() },
            title = { Text(text = "") },
            text = {
                Column(
                ) {
                    Spacer(modifier = Modifier.height(20.dp))
                    OutlinedTextField(
                        value = price,
                        onValueChange = { price = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorDang,
                            unfocusedBorderColor = colorDang,
                        ),
                        textStyle = TextStyle(color = Color.Black),
                        label = { Text(text = "가격", color = colorDang) },
                    )

                    var dealMethod: String by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = dealMethod,
                        onValueChange = { dealMethod = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorDang,
                            unfocusedBorderColor = colorDang,
                        ),
                        textStyle = TextStyle(color = Color.Black),
                        label = { Text(text = "거래방법", color = colorDang) },
                        modifier = Modifier
                            .padding(bottom = 8.dp)
                    )

                    var rating: Float by remember { mutableFloatStateOf(0f) }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                color = colorDang,
                                width = 1.dp,
                                shape = RoundedCornerShape(4.dp)
                            ),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,

                        ) {
                        Spacer(modifier = Modifier.size(16.dp, 0.dp))
                        Text(text = "상태", color = colorDang)
                        Spacer(modifier = Modifier.size(16.dp, 0.dp))
                        RatingBar(
                            value = rating,
                            style = RatingBarStyle.Fill(),
                            stepSize = StepSize.HALF,
                            onValueChange = {
                                rating = it
                            },
                            size = 24.dp,
                            spaceBetween = 4.dp,
                            onRatingChanged = {
                                Log.d("TAG", "onRatingChanged: $it")
                            }
                        )
                    }
                    var productDescription: String by remember { mutableStateOf("") }
                    OutlinedTextField(
                        value = productDescription,
                        onValueChange = { productDescription = it },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = colorDang,
                            unfocusedBorderColor = colorDang,
                        ),
                        textStyle = TextStyle(color = Color.Black),
                        label = { Text(text = "제품설명", color = colorDang) },
                        modifier = Modifier.height(320.dp)
                    )

                }
            },
            confirmButton = {
                Button(onClick = {}) {
                    Text("Upload")
                }
            },
            dismissButton = {
                Button(onClick = { close() }) {
                    Text("Cancel")
                }
            }
        )

    }


}

