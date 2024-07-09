package com.khw.computervision

import android.content.ContentValues
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.khw.computervision.ui.theme.ComputerVisionTheme

class SignUpActivity : ComponentActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                SignUpScreen()
            }
        }
    }


    @Composable
    fun SignUpScreen() {
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
                val context = LocalContext.current
//            LogoScreen("SignUp") { finish() }

                TopBar(
                    title = "회원가입",
                    onBackClick = { finish() },
                    onAddClick = { /*TODO*/ },
                    addIcon = null
                )
                HorizontalDivider(
                    color = colorDong, modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 0.dp)
                )


                Spacer(modifier = Modifier.weight(1f))
                Box() {
                    Row {
                        Text(
                            text = "당당근", fontSize = 70.sp,
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
                Spacer(modifier = Modifier.weight(1f))
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.weight(.2f))
                    Text(
                        text = "*필수 입력 사항",
                        fontSize = 15.sp,
                        color = Color.Red
                    )
                    Spacer(modifier = Modifier.weight(1f))
                }

                var userID by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = userID,
                    onValueChange = { userID = it },
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedBorderColor = colorDang,
//                        unfocusedBorderColor = colorDang,
//                    ),
//                    textStyle = TextStyle(color = Color.Black),
//                    label = { Text(text = "EMAIL", color = colorDang) },
//                    modifier = Modifier.size(210.dp, 60.dp)
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFF9D7A5),
                        focusedContainerColor = Color(0xFFF9D7A5),
                        unfocusedContainerColor = Color(0xFFF9D7A5)
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "EMAIL", color = Color.Black) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp, 8.dp)
                )
                Spacer(modifier = Modifier.height(20.dp))

                var userPassword by remember { mutableStateOf("") }
                OutlinedTextField(
                    value = userPassword,
                    onValueChange = { userPassword = it },
//                    colors = OutlinedTextFieldDefaults.colors(
//                        focusedBorderColor = colorDang,
//                        unfocusedBorderColor = colorDang,
//                    ),
//                    textStyle = TextStyle(color = Color.Black),
//                    label = { Text(text = "PASSWORD", color = colorDang) },
//                    visualTransformation = PasswordVisualTransformation(),
//                    modifier = Modifier.size(210.dp, 60.dp)
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFFF9D7A5),
                        focusedContainerColor = Color(0xFFF9D7A5),
                        unfocusedContainerColor = Color(0xFFF9D7A5)
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "PASSWORD", color = Color.Black) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp, 8.dp)
                )
                Spacer(modifier = Modifier.weight(.5f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.weight(1f))
                    FunButton("가입하기", null) {
                        auth = Firebase.auth
                        if (userID.isEmpty() || userPassword.isEmpty()) {

                            Toast.makeText(
                                baseContext,
                                "이메일 / 비밀번호를 입력하세요",
                                Toast.LENGTH_SHORT,
                            ).show()
                        } else {
                            auth.createUserWithEmailAndPassword(userID, userPassword)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {

                                        val bitmap = BitmapFactory.decodeResource(context.resources, R.drawable.dangkki_img_noback)
                                        uploadBitmapImage(context, bitmap, userID, "profile.jpg", {}, {})
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(
                                            context,
                                            "회원가입 완료",
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                        Log.d(ContentValues.TAG, "createUserWithEmail:success")
                                        finish()
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Log.w(
                                            ContentValues.TAG,
                                            "createUserWithEmail:failure",
                                            task.exception
                                        )
                                        Toast.makeText(
                                            context,
                                            task.exception.toString(),
                                            Toast.LENGTH_SHORT,
                                        ).show()
                                    }
                                }

                        }
                    }
//
//                    Spacer(modifier = Modifier.weight(0.8f))
//
//                    FunTextButton(buttonText = "뒤로") {
//                        finish()
//                    }
//                    Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Column(modifier = Modifier.align(Alignment.BottomEnd)) {
                Spacer(modifier = Modifier.weight(8.5f))
                Box(modifier = Modifier.weight(1.5f)) {
                    Image(
                        painter = painterResource(id = R.drawable.dangkki_pic_line),
                        contentDescription = "mascot_line"
                    )
                }
            }
        }
    }
}
