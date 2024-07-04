package com.khw.computervision

// 이전의 ProfilePopup을 ProfileScreen으로 변환
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
) {
    var addressText by remember { mutableStateOf("주소 정보가 여기에 표시됩니다") }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocoder = remember { Geocoder(context, Locale.KOREA) }
    val coroutineScope = rememberCoroutineScope()
    var profileUrl: String? by remember { mutableStateOf(null) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            getLocation(fusedLocationClient) { location ->
                coroutineScope.launch(Dispatchers.IO) {
                    val address = getAddressFromLocation(geocoder, location)
                    addressText = address ?: "주소를 찾을 수 없습니다."
                    withContext(Dispatchers.Main) {
                        UserIDManager.userAddress.value = addressText
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Profile Screen")
        LaunchedEffect(Unit) {
            profileUrl = getProfile()
        }

        profileUrl?.let {
            GlideImage(
                imageModel = it,
                contentDescription = "Image",
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(20.dp))
            )
        }

        Text(text = UserIDManager.userID.value)
        TextButton(onClick = {
            when {
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED -> {
                    getLocation(fusedLocationClient) { location ->
                        coroutineScope.launch(Dispatchers.IO) {
                            val address = getAddressFromLocation(geocoder, location)
                            addressText = address ?: "주소를 찾을 수 없습니다."
                            withContext(Dispatchers.Main) {
                                UserIDManager.userAddress.value = addressText
                            }
                        }
                    }
                }

                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }
            }

        }) {
            Text(text = "현재 위치 확인")
        }
        Text(text = UserIDManager.userAddress.value)
//        Spacer(modifier = Modifier.weight(2f))

        val messageMap = getMessage()

        FunButton("내가 올린 제품", image = R.drawable.list_icon) {
            val productIntent = Intent(context, MyUploadedActivity::class.java)
            productIntent.putExtra("userID", UserIDManager.userID.value)
            context.startActivity(productIntent)
        }

        FunButton("내가 좋아요 누른 제품", image = R.drawable.list_icon) {
            val productIntent = Intent(context, LikeActivity::class.java)
            productIntent.putExtra("userID", UserIDManager.userID.value)
            context.startActivity(productIntent)
        }

//        FunButton("내게 온 메세지 : ${messageMap.size}", null) {
//            val userIntent = Intent(context, MessageListActivity::class.java)
//            userIntent.putExtra("messageList", mapToBundle(messageMap))
//            context.startActivity(userIntent)
//        }

//        Spacer(modifier = Modifier.weight(1f))
        FunButton("로그아웃", null) {
            // 로그아웃 처리
        }
    }
}