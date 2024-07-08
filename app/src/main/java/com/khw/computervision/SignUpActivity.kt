package com.khw.computervision

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            val context = LocalContext.current
            LogoScreen("SignUp") { finish() }

            Spacer(modifier = Modifier.weight(1f))

            Row (modifier = Modifier.fillMaxWidth()){
                Spacer(modifier = Modifier.weight(.4f))
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
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorDang,
                    unfocusedBorderColor = colorDang,
                ),
                textStyle = TextStyle(color = Color.Black),
                label = { Text(text = "EMAIL", color = colorDang) },
                modifier = Modifier.size(210.dp, 60.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            var userPassword by remember { mutableStateOf("") }
            OutlinedTextField(
                value = userPassword,
                onValueChange = { userPassword = it },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = colorDang,
                    unfocusedBorderColor = colorDang,
                ),
                textStyle = TextStyle(color = Color.Black),
                label = { Text(text = "PASSWORD", color = colorDang) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.size(210.dp, 60.dp)
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
                
                Spacer(modifier = Modifier.weight(0.8f))

                FunTextButton(buttonText = "뒤로") {
                    finish()
                }
                Spacer(modifier = Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.weight(2f))
        }
    }
}
