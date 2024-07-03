package com.khw.computervision


import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationServices
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

//class LoginActivity : ComponentActivity() {
//    private lateinit var auth: FirebaseAuth
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            ComputerVisionTheme {
//                LoginScreen()
//            }
//        }
//    }
//
//    @Composable
//    fun LoginScreen() {
//        Column(
//            modifier = Modifier.fillMaxSize(),
//            verticalArrangement = Arrangement.Top,
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//
//            Spacer(modifier = Modifier.weight(1f))
//            LogoScreen("Login") { finish() }
//
//            Image(
//                painter = gifImageDecode(R.raw.dangkki),
//                contentDescription = "mascot",
//                modifier = Modifier.size(260.dp)
//            )
//
//            var userID by remember { mutableStateOf("dangdanggeun@intel.com") }
//            OutlinedTextField(
//                value = userID,
//                onValueChange = { userID = it },
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = colorDang,
//                    unfocusedBorderColor = colorDang,
//                ),
//                textStyle = TextStyle(color = Color.Black),
//                label = { Text(text = "EMAIL", color = colorDang) },
//                modifier = Modifier.size(210.dp, 60.dp)
//            )
//            Spacer(modifier = Modifier.height(20.dp))
//
//            var userPassword by remember { mutableStateOf("123123") }
//            OutlinedTextField(
//                value = userPassword,
//                onValueChange = { userPassword = it },
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = colorDang,
//                    unfocusedBorderColor = colorDang,
//                ),
//                textStyle = TextStyle(color = Color.Black),
//                label = { Text(text = "PASSWORD", color = colorDang) },
//                visualTransformation = PasswordVisualTransformation(),
//                modifier = Modifier.size(210.dp, 60.dp)
//            )
//            Spacer(modifier = Modifier.height(30.dp))
//
//            val context = LocalContext.current
//            FunButton("로그인", null) {
//                auth = Firebase.auth
//                if (userID.isEmpty() || userPassword.isEmpty()) {
//
//                    Toast.makeText(
//                        baseContext,
//                        "이메일 / 비밀번호를 입력하세요",
//                        Toast.LENGTH_SHORT,
//                    ).show()
//                } else {
//                    auth.signInWithEmailAndPassword(userID, userPassword)
//                        .addOnCompleteListener { task ->
//                            if (task.isSuccessful) {
//                                // Sign in success, update UI with the signed-in user's information
//                                Log.d(ContentValues.TAG, "signInWithEmail:success")
//                                val user = auth.currentUser
//                                if (user != null) {
//                                    UserIDManager.userID.value = userID
//                                    context.startActivity(Intent(context, SalesActivity::class.java))
//                                }
//                            } else {
//                                // If sign in fails, display a message to the user.
//                                Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
//
//                                Toast.makeText(
//                                    baseContext,
//                                    task.exception.toString(),
//                                    Toast.LENGTH_SHORT,
//                                ).show()
//                            }
//                        }
//                }
//            }
//            Spacer(modifier = Modifier.padding(8.dp))
//            FunTextButton("회원가입") {
//                context.startActivity(Intent(context, SignUpActivity::class.java))
//            }
//
//            Spacer(modifier = Modifier.weight(1f))
//        }
//    }
//}

@Composable
fun LoginScreen(navController: NavController) {
    var auth: FirebaseAuth = Firebase.auth

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.weight(1f))
//        LogoScreen("Login") { /* Handle finish */ }

        Image(
            painter = gifImageDecode(R.raw.dangkki),
            contentDescription = "mascot",
            modifier = Modifier.size(260.dp)
        )

        var userID by remember { mutableStateOf("dangdanggeun@intel.com") }
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

        var userPassword by remember { mutableStateOf("123123") }
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
        Spacer(modifier = Modifier.height(30.dp))
        val context = LocalContext.current
        var addressText by remember { mutableStateOf("주소 정보가 여기에 표시됩니다") }
        val fusedLocationClient =
            remember { LocationServices.getFusedLocationProviderClient(context) }
        val geocoder = remember { Geocoder(context, Locale.KOREA) }
        val coroutineScope = rememberCoroutineScope()

        // 위치 정보를 가져오고 네비게이션하는 함수
        fun getLocationAndNavigate() {
            getLocation(fusedLocationClient) { location ->
                coroutineScope.launch(Dispatchers.IO) {
                    val address = getAddressFromLocation(geocoder, location)
                    addressText = address ?: "주소를 찾을 수 없습니다."
                    withContext(Dispatchers.Main) {
                        UserIDManager.userAddress.value = addressText
                        Log.d("FinalAddress", UserIDManager.userAddress.value)
                        navController.navigate("sales")
                    }
                }
            }
        }

        // requestPermissionLauncher 수정
        val requestPermissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getLocationAndNavigate()
            } else {
                // 권한이 거부된 경우의 처리
                Log.d("PermissionDenied", "Location permission was denied")
                UserIDManager.userAddress.value = "위치 권한이 거부되었습니다"
                navController.navigate("sales")
            }
        }

        FunButton("로그인", null) {
            auth = Firebase.auth
            if (userID.isEmpty() || userPassword.isEmpty()) {
                Toast.makeText(context, "이메일 / 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(userID, userPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d(ContentValues.TAG, "signInWithEmail:success")
                            val user = auth.currentUser
                            if (user != null) {
                                UserIDManager.userID.value = userID
                                when {
                                    ActivityCompat.checkSelfPermission(
                                        context,
                                        Manifest.permission.ACCESS_FINE_LOCATION
                                    ) == PackageManager.PERMISSION_GRANTED -> {
                                        getLocationAndNavigate()
                                    }
                                    else -> {
                                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                                    }
                                }
                            }
                        } else {
                            Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                            Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
        Spacer(modifier = Modifier.padding(8.dp))
        FunTextButton("회원가입") {
            context.startActivity(Intent(context, SignUpActivity::class.java))
        }

        Spacer(modifier = Modifier.weight(1f))

    }

}