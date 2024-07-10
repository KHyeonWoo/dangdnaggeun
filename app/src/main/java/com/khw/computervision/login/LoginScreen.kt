package com.khw.computervision.login


import android.content.Context
import android.location.Geocoder
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.khw.computervision.ClosetViewModel
import com.khw.computervision.FunButton
import com.khw.computervision.ProductViewModel
import com.khw.computervision.R
import com.khw.computervision.UserIDManager
import com.khw.computervision.colorDang
import com.khw.computervision.customFont
import com.khw.computervision.gifImageDecode
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

@Composable
fun LoginScreen(
    navController: NavController,
    closetViewModel: ClosetViewModel,
    productsViewModel: ProductViewModel
) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocoder = remember { Geocoder(context, Locale.KOREA) }
    val coroutineScope = rememberCoroutineScope()

    var userID by remember { mutableStateOf("dangdanggeun@intel.com") }
    var userPassword by remember { mutableStateOf("123123") }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getUserDataAndNavigate(
                navController,
                closetViewModel,
                productsViewModel,
                fusedLocationClient,
                geocoder,
                coroutineScope
            )
        } else {
            Log.d("PermissionDenied", "Location permission was denied")
            UserIDManager.userAddress.value = "위치 권한이 거부되었습니다"
            navController.navigate("sales")
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(colorDang)) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(1f))
            TitleSection()
            Image(
                painter = gifImageDecode(R.raw.dangkki),
                contentDescription = "mascot",
                modifier = Modifier.size(256.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            InputField(userID, "EMAIL", onValueChange = { userID = it })
            Spacer(modifier = Modifier.height(20.dp))
            InputField(userPassword, "PASSWORD", visualTransformation = PasswordVisualTransformation(), onValueChange = { userPassword = it })
            Spacer(modifier = Modifier.height(16.dp))
            LoginButton(userID, userPassword, context, closetViewModel, productsViewModel, navController, fusedLocationClient, requestPermissionLauncher, coroutineScope, geocoder)
            Spacer(modifier = Modifier.padding(8.dp))
            FunTextButton("회원가입") {
                navController.navigate("signUp")
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun TitleSection() {
    Box {
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
}

@Composable
private fun InputField(
    value: String,
    label: String,
    visualTransformation: VisualTransformation = VisualTransformation.None,
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
        visualTransformation = visualTransformation,
        modifier = Modifier.fillMaxWidth().padding(32.dp, 8.dp)
    )
}

@Composable
private fun LoginButton(
    userID: String,
    userPassword: String,
    context: Context,
    closetViewModel: ClosetViewModel,
    productsViewModel: ProductViewModel,
    navController: NavController,
    fusedLocationClient: FusedLocationProviderClient,
    requestPermissionLauncher: ActivityResultLauncher<String>,
    coroutineScope: CoroutineScope,
    geocoder: Geocoder
) {
    FunButton("로그인", null) {
        if (userID.isEmpty() || userPassword.isEmpty()) {
            Toast.makeText(context, "이메일 / 비밀번호를 입력하세요", Toast.LENGTH_SHORT).show()
        } else {
            loginUser(
                userID, userPassword, context, closetViewModel, productsViewModel,
                navController, fusedLocationClient, requestPermissionLauncher, coroutineScope, geocoder
            )
        }
    }
}
