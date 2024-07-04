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
        LogoScreen(activityName = "MessageList") { }

        Spacer(modifier = Modifier.weight(0.1f))

        Box(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Box(modifier = Modifier.weight(2.5f)) {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            painter = painterResource(id = R.drawable.arrow_back_icon),
                            contentDescription = null,
                            tint = colorDang,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
                Box(modifier = Modifier.weight(8f)) {
                    SearchTextField {}
                }
            }
        }

        Spacer(modifier = Modifier.weight(0.2f))
        Divider(thickness = 1.dp, modifier = Modifier.width(350.dp), color = colorDang)
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
                    Divider(
                        thickness = 1.dp,
                        modifier = Modifier.width(350.dp),
                        color = colorDang
                    )
                }
            }
        }
        Spacer(modifier = Modifier.weight(0.3f))
    }
}
//
//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    ComputerVisionTheme {
//        val sampleMap = mapOf("key1" to "value1", "key2" to "value2")
//        MessageScreen(sampleMap, "User's Image")
//    }
//}