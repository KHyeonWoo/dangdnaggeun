package com.khw.computervision


import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.khw.computervision.ui.theme.ComputerVisionTheme

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ComputerVisionTheme {
                LoginScreen()
            }
        }
    }
}

@Composable
fun LoginScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "당당근",
            fontSize = 50.sp,
            color = Color(0xFFF3BB66)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "우리 당당하게 거래해요",
            fontSize = 15.sp,
            color = Color(0xFFF3BB66)
        )
        Spacer(modifier = Modifier.height(10.dp))

        Image(
            painter = gifImageDecode(R.raw.dangkki),
            contentDescription = "mascot",
            modifier = Modifier.size(280.dp)
        )
        Spacer(modifier = Modifier.height(20.dp))

        var userID by remember { mutableStateOf("") }
        OutlinedTextField(
            value = userID,
            onValueChange = { userID = it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF3BB66),
                unfocusedBorderColor = Color(0xFFF3BB66),
            ),
            label = { Text(text = "EMAIL", color = Color(0xFFF3BB66))}
        )
        Spacer(modifier = Modifier.height(20.dp))

        var userPassword by remember { mutableStateOf("") }
        OutlinedTextField(
            value = userPassword,
            onValueChange = { userPassword = it },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color(0xFFF3BB66),
                unfocusedBorderColor = Color(0xFFF3BB66),
            ),
            label = { Text(text = "PASSWORD", color = Color(0xFFF3BB66))}
        )
        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(
                Color(0xFFF3BB66)
            )
        ) {
            Text(text = "회원가입")
        }
        Spacer(modifier = Modifier.height(20.dp))

        Divider(color = Color(0xFFF3BB66))
        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { /*TODO*/ },
            colors = ButtonDefaults.buttonColors(
                Color(0xFFF3BB66)
            )
        ) {
            Text(text = "Google로 가입하기")
        }
    }
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