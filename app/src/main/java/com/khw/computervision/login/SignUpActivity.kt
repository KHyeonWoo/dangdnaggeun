package com.khw.computervision.login

import android.content.ContentValues
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.khw.computervision.*
import com.khw.computervision.R

@Composable
fun SignUpScreen(navController: NavHostController) {
    var userID by remember { mutableStateOf("") }
    var userPassword by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorDang)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopBar(
                title = "회원가입",
                onBackClick = { navController.popBackStack() },
                onAddClick = { /*TODO*/ },
                addIcon = null
            )

            HorizontalDividerColorDang(16.dp,16.dp,0.dp,0.dp)
            Spacer(modifier = Modifier.weight(1f))
            Logo()
            Spacer(modifier = Modifier.weight(1f))

            UserInputField(
                label = "EMAIL",
                value = userID,
                onValueChange = { userID = it }
            )

            Spacer(modifier = Modifier.height(20.dp))

            UserInputField(
                label = "PASSWORD",
                value = userPassword,
                onValueChange = { userPassword = it }
            )

            Spacer(modifier = Modifier.weight(.5f))

            SignUpButton(
                userID = userID,
                userPassword = userPassword,
                onSignUpSuccess = {
                    val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.dangkki_img_noback)
                    uploadBitmapImage(context, bitmap, userID, "profile.jpg", {}, {})
                    Toast.makeText(context, "회원가입 완료", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
            )
            Spacer(modifier = Modifier.weight(1f))
        }
        BottomImage(Modifier.align(Alignment.BottomEnd))
    }
}

@Composable
fun Logo() {
    Row {
        Text(
            text = "당당근",
            fontSize = 70.sp,
            fontFamily = customFont,
            color = Color.Black
        )

        Text(
            text = "AI",
            textAlign = TextAlign.Center,
            color = Color.Black,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun UserInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Color.White,
            unfocusedBorderColor = Color(0xFFF9D7A5),
            focusedContainerColor = Color(0xFFF9D7A5),
            unfocusedContainerColor = Color(0xFFF9D7A5)
        ),
        textStyle = TextStyle(color = Color.Black),
        label = { Text(text = label, color = Color.Black) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp, 8.dp)
    )
}

@Composable
fun SignUpButton(
    userID: String,
    userPassword: String,
    onSignUpSuccess: () -> Unit
) {
    val context = LocalContext.current
    val auth = Firebase.auth

    FunButton("가입하기", null) {
        if (userID.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(
                context,
                "이메일 / 비밀번호를 입력하세요",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            auth.createUserWithEmailAndPassword(userID, userPassword)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(ContentValues.TAG, "createUserWithEmail:success")
                        onSignUpSuccess()
                    } else {
                        Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            context,
                            task.exception.toString(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }
}

@Composable
fun BottomImage(modifier: Modifier) {
    Column(modifier = modifier) {
        Spacer(modifier = Modifier.weight(8.5f))
        Box(modifier = Modifier.weight(1.5f)) {
            Image(
                painter = painterResource(id = R.drawable.dangkki_pic_line),
                contentDescription = "mascot_line"
            )
        }
    }
}
