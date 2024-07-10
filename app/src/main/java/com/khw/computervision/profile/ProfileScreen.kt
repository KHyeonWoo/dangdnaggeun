package com.khw.computervision.profile

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.navigation.NavHostController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.khw.computervision.FunButton
import com.khw.computervision.R
import com.khw.computervision.UserIDManager
import com.khw.computervision.colorBack
import com.khw.computervision.colorDang
import com.khw.computervision.getProfile
import kotlinx.coroutines.CoroutineScope
import java.util.Locale

@Composable
fun ProfileScreen(navController: NavHostController) {
    var addressText by remember { mutableStateOf("주소 정보가 여기에 표시됩니다") }
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val geocoder = remember { Geocoder(context, Locale.KOREA) }
    val coroutineScope = rememberCoroutineScope()
    var profileUrl: String? by remember { mutableStateOf(null) }
    var inputImage by remember { mutableStateOf<Bitmap?>(null) }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            fetchLocationAndAddress(fusedLocationClient, geocoder, coroutineScope) {
                addressText = it
                UserIDManager.userAddress.value = it
            }
        }
    }

    LaunchedEffect(Unit) {
        profileUrl = getProfile(UserIDManager.userID.value)
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
            Spacer(modifier = Modifier.weight(2f))
            ProfileImage(profileUrl) { inputImage = it }
            Spacer(modifier = Modifier.weight(.5f))

            UserInfo()
            LocationButton(context, fusedLocationClient, geocoder, coroutineScope, requestPermissionLauncher, {addressText = it})
            AddressRow()
            Spacer(modifier = Modifier.weight(1f))

            ActionButtons(navController, Modifier.weight(1f))
            Spacer(modifier = Modifier.weight(2f))
        }
    }
}

@Composable
fun UserInfo() {
    Text(
        text = UserIDManager.userID.value,
        fontSize = 16.sp
    )
}

@Composable
fun LocationButton(
    context: Context,
    fusedLocationClient: FusedLocationProviderClient,
    geocoder: Geocoder,
    coroutineScope: CoroutineScope,
    requestPermissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    setUserAddress: (String) -> Unit
) {
    TextButton(onClick = {
        when {
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                fetchLocationAndAddress(fusedLocationClient, geocoder, coroutineScope) {
                    setUserAddress(it)
                    UserIDManager.userAddress.value = it
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
}

@Composable
fun AddressRow() {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(id = R.drawable.current_location_icon),
            contentDescription = "location searching",
            tint = colorDang
        )
        Text(text = UserIDManager.userAddress.value)
    }
}

@Composable
fun ActionButtons(navController: NavHostController, modifier: Modifier) {
    FunButton("나의 판매 제품", image = R.drawable.list_icon) {
        navController.navigate("myUploaded")
    }
    Spacer(modifier = modifier)
    FunButton("나의 좋아요 목록", image = R.drawable.baseline_favorite_24) {
        navController.navigate("myLiked")
    }
    Spacer(modifier = modifier)
    FunButton("로그아웃", null) {
        navController.navigate("login") {
            popUpTo("sales") { inclusive = true }
        }
    }
}
