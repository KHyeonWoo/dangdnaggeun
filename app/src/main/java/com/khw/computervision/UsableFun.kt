package com.khw.computervision

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import coil.size.Size

val colorDang = Color(0xFFF3BB66)

@Composable
fun LogoScreen(context: Context, activityName: String) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (activityName != "Login" ) {
            Spacer(modifier = Modifier.height(20.dp))
        }
        Text(
            text = "당당근",
            fontSize = 50.sp,
            color = colorDang,
            modifier = Modifier.clickable {
                context.startActivity(Intent(context, SalesActivity::class.java))
            }
        )
        Spacer(modifier = Modifier.height(10.dp))

        when (activityName) {
            "Login" -> {
                TextBox("우리 당당하게 팔아요")
            }

            "Sales" -> {
                TextBox("우리 당당하게 보여줘요")
            }

            "Detail" -> {
                TextBox("우리 당당하게 알려줘요")
            }

            "Insert" -> {
                TextBox("우리 당당하게 팔아요")
            }

            "Decorate" -> {
                TextBox("우리 당당하게 꾸며봐요")
            }

            "UserProfile" -> {
                TextBox("우리 당당하게 확인해요")
            }
        }
    }
}

@Composable
fun TextBox(text: String) {
    Text(
        text = text,
        fontSize = 15.sp,
        color = colorDang
    )
}


@Composable
fun gifImageDecode(name: Int): AsyncImagePainter {
    val context = LocalContext.current

    val mascotImageUri = remember {
        Uri.parse("android.resource://${context.packageName}/${name}")
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(mascotImageUri)
            .size(Size.ORIGINAL)
            .build(),
        imageLoader = imageLoader
    )

    return painter
}

@Composable
fun FunTextButton(buttonText: String, clickEvent: () -> Unit) {
    Button(
        onClick = { clickEvent() },
        colors = ButtonDefaults.buttonColors(
            colorDang
        )
    ) {
        Text(text = buttonText, color = Color.White)
    }
}

