package com.khw.computervision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.khw.computervision.ui.theme.ComputerVisionTheme
import kotlin.math.roundToInt

class LikeActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                LikeScreen(ReLoadingManager.reLoadingValue.value)
            }
        }
    }

    @Composable
    fun LikeScreen(reLoading: Boolean) {
        var productMap: Map<String, Map<String, String>> by remember { mutableStateOf(emptyMap()) }
        GetProduct(reLoading) { productMap = it }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF5F5EB))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TopBar(title = "좋아요", onBackClick = { /*TODO*/ }, onAddClick = { /*TODO*/ }, Icons.Default.Add)

                HorizontalDivider(
                    modifier = Modifier.width(350.dp),
                    thickness = 1.dp,
                    color = colorDang
                )

                MyProductSwipeBox(productMap)
            }
        }
    }

//    @Composable
//    private fun SearchScreen() {
//        var searchText by remember { mutableStateOf("") }
//        Row(
//            modifier = Modifier
//                .fillMaxWidth()
//                .height(40.dp)
//        ) {
//            Spacer(modifier = Modifier.weight(1f))
//            Row(
//                modifier = Modifier
//                    .weight(4f),
//                verticalAlignment = Alignment.CenterVertically
//            ) {
//                Text(
//                    text = "검색",
//                    textAlign = TextAlign.Center,
//                    color = Color.White,
//                    modifier = Modifier
//                        .weight(1f)
//                        .background(colorDang)
//                        .height(40.dp)
//                        .padding(top = 10.dp)
//                )
//                OutlinedTextField(
//                    value = searchText,
//                    onValueChange = { searchText = it },
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedBorderColor = colorDang,
//                        unfocusedBorderColor = colorDang,
//                    ),
//                    textStyle = TextStyle(color = Color.Black, fontSize = 8.sp),
//                    modifier = Modifier
//                        .height(40.dp)
//                        .weight(3f)
//                )
//            }
//            Spacer(modifier = Modifier.weight(1f))
//        }
//    }

    @OptIn(ExperimentalMaterialApi::class)
    @Composable
    private fun MyProductSwipeBox(productMap: Map<String, Map<String, String>>) {

        for ((key, value) in productMap) {
            if (value["InsertUser"] == UserIDManager.userID.value) {
                val squareSize = 48.dp
                val swipeState = rememberSwipeableState(0)
                val sizePx = with(LocalDensity.current) { squareSize.toPx() }
                val anchors =
                    mapOf(0f to 0, -sizePx to 1) // Maps anchor points (in px) to states

                Box(
                    modifier = Modifier
                        .padding(8.dp)
                        .width(350.dp)
                        .swipeable(
                            state = swipeState,
                            anchors = anchors,
                            thresholds = { _, _ -> FractionalThreshold(0.4f) },
                            orientation = Orientation.Horizontal
                        )
                        .clip(RoundedCornerShape(15.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .offset { IntOffset(swipeState.offset.value.roundToInt(), 0) }
                            .height(110.dp)
                            .padding(start = 4.dp)
                            .clip(RoundedCornerShape(15.dp))
                            .background(colorDang)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.character2),
                            contentDescription = "",
                            contentScale = ContentScale.FillBounds,
                            modifier = Modifier
                                .weight(3f)
                                .padding(5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                        )

                        Box(
                            modifier = Modifier
                                .weight(7f)
                                .padding(5.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.White)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(10.dp, 0.dp)
                            ) {
                                for ((fieldKey, fieldValue) in value) {
                                    if (fieldKey == "제품명" || fieldKey == "가격") {
                                        Text(
                                            text = fieldKey,
                                            color = colorDang
                                        )
                                        Text(
                                            text = fieldValue,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    if (swipeState.currentValue == 1) {
                        Box(
                            modifier = Modifier
                                .size(48.dp, 100.dp)
                                .align(Alignment.CenterEnd),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = colorDang,
                                modifier = Modifier.clickable {
                                    deleteFirestoreData("product", key) {
                                        ReLoadingManager.reLoading()
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}