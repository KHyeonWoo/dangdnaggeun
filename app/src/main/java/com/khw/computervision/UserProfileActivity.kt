package com.khw.computervision

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.khw.computervision.ui.theme.ComputerVisionTheme

class UserProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                UserProfileScreen()
            }
        }
    }

    @Composable
    fun UserProfileScreen() {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            LogoScreen(activityName = "UserProfile")
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "",
                modifier = Modifier
                    .padding(20.dp)
                    .size(320.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            FunTextButton("내가 판매 중인 제품"){ }
            FunTextButton("내가 판매한 제품"){ }
            FunTextButton("내게 온 메세지"){ }
            Spacer(modifier = Modifier.weight(1f))
            val context = LocalContext.current
            FunTextButton("로그아웃") {
                context.startActivity(Intent(context, LoginActivity::class.java))
            }
            Spacer(modifier = Modifier.weight(1f))
        }

    }

}