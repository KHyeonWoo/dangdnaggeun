package com.khw.computervision

// 이전의 ProfilePopup을 ProfileScreen으로 변환
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.google.android.gms.location.LocationServices
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

@Composable
fun ProfileScreen(navController: NavHostController) {
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorBack)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
//        Text(text = "Profile Screen")
            Spacer(modifier = Modifier.weight(2f))
            LaunchedEffect(Unit) {
                profileUrl = getProfile(UserIDManager.userID.value)
            }

            profileUrl?.let {
                GlideImage(
                    imageModel = it,
                    contentDescription = "Image",
                    modifier = Modifier
                        .size(136.dp)
                        .clip(RoundedCornerShape(80.dp))
                )
            }
            Spacer(modifier = Modifier.weight(.5f))
            Text(
                text = UserIDManager.userID.value,
                fontSize = 16.sp
            )

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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.searching_location_icon),
                        contentDescription = "location searching",
                        tint = colorDang
                    )
                    Text(text = "현재 위치 확인")
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.current_location_icon),
                    contentDescription = "location searching",
                    tint = colorDang
                )
                Text(text = UserIDManager.userAddress.value)
            }
//        Spacer(modifier = Modifier.weight(2f))

            val messageMap = getMessage()
            Spacer(modifier = Modifier.weight(1f))
            FunButton("나의 판매 제품", image = R.drawable.list_icon) {
                navController.navigate("myUploaded")
            }
            Spacer(modifier = Modifier.weight(1f))
            FunButton("나의 좋아요 목록", image = R.drawable.baseline_favorite_24) {
                navController.navigate("myLiked")
            }

//        FunButton("췟팅창(테스트용용~~)", image = R.drawable.list_icon) {
//            navController.navigate("messageScreen/test@intel.com/zz")
//
//        }
//        FunButton("췟리스트(업뎃중~~)", image = R.drawable.list_icon) {
//            navController.navigate("chatListScreen")


//        FunButton("내게 온 메세지 : ${messageMap.size}", null) {
//            val userIntent = Intent(context, MessageListActivity::class.java)
//            userIntent.putExtra("messageList", mapToBundle(messageMap))
//            context.startActivity(userIntent)
//        }

            Spacer(modifier = Modifier.weight(1f))
            FunButton("로그아웃", null) {
                navController.navigate("login") {
                    popUpTo("sales") { inclusive = true }
                } // 로그아웃 처리 후 로그인 시 home으로
            }
            Spacer(modifier = Modifier.weight(2f))
        }
    }
}
