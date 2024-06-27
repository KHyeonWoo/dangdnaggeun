package com.khw.computervision

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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

                MessageScreen(messageMap)
            }
        }
    }
}

@Composable
fun MessageScreen(messageMap: Map<String, String>) {
    Column {
        for ((key, value) in messageMap) {
            Text(text = value)

            Divider(thickness = 1.dp, color = colorDang)
        }
    }
}
