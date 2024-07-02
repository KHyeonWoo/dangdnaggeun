package com.khw.computervision

import android.graphics.Bitmap
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.khw.computervision.ui.theme.ComputerVisionTheme

class MessageListActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Intent에서 Bundle을 가져옵니다.
            val bundle = intent.getBundleExtra("messageList")
            var messageMap: Map<String, String> = mutableMapOf()
            if (bundle != null) {
                messageMap = bundleToMap(bundle)
            }
            ComputerVisionTheme {
                MessageScreen(messageMap, "User's Image")
            }
        }
    }
}

@Composable
fun MessageScreen(messageMap: Map<String, String>, profileUri: String?) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        LogoScreen(activityName = "MessageList") { }

        Spacer(modifier = Modifier.weight(0.1f))
        SearchTextField {}

        Spacer(modifier = Modifier.weight(0.2f))
        Divider(thickness = 1.dp, modifier = Modifier.width(350.dp), color = colorDang)

        Box(modifier = Modifier
            .fillMaxSize()
            .weight(3f)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                for ((key, value) in messageMap) {
                    Spacer(modifier = Modifier.height(5.dp))
                    var inputImage by remember { mutableStateOf<Bitmap?>(null) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Text("User's Image")

//                ProfileImage(profileUri) { inputImage = it }

                        Text(text = value)
                    }

                    Spacer(modifier = Modifier.height(5.dp))
                    Divider(thickness = 1.dp, modifier = Modifier.width(350.dp), color = colorDang)
                }
            }
        }
        Spacer(modifier = Modifier.weight(0.3f))
    }
}