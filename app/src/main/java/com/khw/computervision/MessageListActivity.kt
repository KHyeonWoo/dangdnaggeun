package com.khw.computervision

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

//class MessageListActivity : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            // Intent에서 Bundle을 가져옵니다.
//            val bundle = intent.getBundleExtra("messageList")
//            var messageMap: Map<String, String> = mutableMapOf()
//            if (bundle != null) {
//                messageMap = bundleToMap(bundle)
//            }
//            ComputerVisionTheme {
//                MessageScreen(messageMap, "User's Image")
//            }
//        }
//    }
//}

@Composable
fun MessageScreen(messageMap: Map<String, String>, profileUrl: String?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        TopBar(title = "메시지", onBackClick = { /*TODO*/ }, onAddClick = { /*TODO*/})

//        Box(
//            modifier = Modifier.width(350.dp),
//            contentAlignment = Alignment.Center
//        ) {
//            Row(
//                modifier = Modifier.fillMaxWidth(),
//                horizontalArrangement = Arrangement.SpaceBetween
//            ) {
//
//                IconButton(onClick = { /*TODO*/ }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.arrow_back_icon),
//                        contentDescription = null,
//                        tint = colorDang,
//                        modifier = Modifier.size(40.dp)
//                    )
//                }
//
//
//
//                IconButton(onClick = { /*TODO*/ }) {
//                    Icon(
//                        painter = painterResource(id = R.drawable.search_icon),
//                        contentDescription = null,
//                        tint = colorDang,
//                        modifier = Modifier.size(40.dp)
//                    )
//                }
//            }
//        }

        HorizontalDivider(modifier = Modifier.width(350.dp), thickness = 1.dp, color = colorDang)

        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(3f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(5.dp))
                for ((key, value) in messageMap) {
                    Box(
                        modifier = Modifier
                            .width(320.dp)
                            .height(100.dp),
                        contentAlignment = Alignment.Center
                    ) {

                        var inputImage by remember { mutableStateOf<Bitmap?>(null) }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Box(modifier = Modifier.weight(3f)) {

                                Text("User's Image")

//                ProfileImage(profileUri) { inputImage = it }
                            }
                            Box(modifier = Modifier.weight(7f)) {
                                Text(text = value)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    HorizontalDivider(
                        modifier = Modifier.width(350.dp),
                        thickness = 1.dp,
                        color = colorDang
                    )
                }
            }
        }
    }
}