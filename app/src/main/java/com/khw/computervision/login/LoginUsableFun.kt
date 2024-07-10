package com.khw.computervision.login

import android.Manifest
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.khw.computervision.ClosetViewModel
import com.khw.computervision.ProductViewModel
import com.khw.computervision.UserIDManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

fun loginUser(
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
    Firebase.auth.signInWithEmailAndPassword(userID, userPassword)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(ContentValues.TAG, "signInWithEmail:success")
                val user = Firebase.auth.currentUser
                if (user != null) {
                    UserIDManager.userID.value = userID
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        getUserDataAndNavigate(navController, closetViewModel, productsViewModel, fusedLocationClient, geocoder, coroutineScope)
                    } else {
                        requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                    }
                }
            } else {
                Log.w(ContentValues.TAG, "signInWithEmail:failure", task.exception)
                Toast.makeText(context, task.exception.toString(), Toast.LENGTH_SHORT).show()
            }
        }
}

fun getUserDataAndNavigate(
    navController: NavController,
    closetViewModel: ClosetViewModel,
    productsViewModel: ProductViewModel,
    fusedLocationClient: FusedLocationProviderClient,
    geocoder: Geocoder,
    coroutineScope: CoroutineScope
) {
    getLocation(fusedLocationClient) { location ->
        coroutineScope.launch(Dispatchers.IO) {
            val address = getAddressFromLocation(geocoder, location)
            val updatedAddress = address ?: "주소를 찾을 수 없습니다."
            withContext(Dispatchers.Main) {
                UserIDManager.userAddress.value = updatedAddress
                closetViewModel.getItemsFromFirebase(Firebase.storage.reference.child(UserIDManager.userID.value))
                productsViewModel.getProductsFromFireStore()
                productsViewModel.getTotalLikedFromFireStore()
                productsViewModel.getLikedFromFireStore()
                Log.d("FinalAddress", UserIDManager.userAddress.value)
                navController.navigate("sales")
            }
        }
    }
}

fun uploadBitmapImage(
    context: Context,
    bitmap: Bitmap,
    user: String,
    pathName: String,
    successUpload: () -> Unit,
    inputImageNullEvent: () -> Unit
) {

    val mountainsRef = Firebase.storage.reference.child("$user/$pathName")

    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
    val data = baos.toByteArray()

    val uploadTask = mountainsRef.putBytes(data)
    uploadTask.addOnSuccessListener {
        Toast.makeText(context, "프로필 업로드 성공", Toast.LENGTH_SHORT).show()
        successUpload()
        inputImageNullEvent()
    }.addOnProgressListener {
        Toast.makeText(context, "프로필 업로드 중", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener {
        Toast.makeText(context, "프로필 업로드 실패", Toast.LENGTH_SHORT).show()
        inputImageNullEvent()
    }
}

fun getLocation(
    fusedLocationProviderClient: FusedLocationProviderClient,
    onLocationReceived: (Location) -> Unit
) {
    try {
        fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                onLocationReceived(it)
            }
        }
    } catch (e: SecurityException) {
        // 권한이 없는 경우 예외 처리
    }
}

fun getAddressFromLocation(geocoder: Geocoder, location: Location): String? {
    return try {
        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        Log.d("AddressLookup", "Received addresses: ${addresses?.size}")

        addresses?.firstOrNull()?.let { address ->
            val adminArea = address.adminArea ?: ""
            val locality = address.locality ?: ""
            val subLocality = address.subLocality ?: ""
            val thoroughfare = address.thoroughfare ?: ""
            val subThoroughfare = address.subThoroughfare ?: ""
            val addressLine = address.getAddressLine(0) ?: ""

            // 여기에 로그 추가
            Log.d("AddressLookup", "Address components:")
            Log.d("AddressLookup", "adminArea: $adminArea")
            Log.d("AddressLookup", "locality: $locality")
            Log.d("AddressLookup", "subLocality: $subLocality")
            Log.d("AddressLookup", "thoroughfare: $thoroughfare")
            Log.d("AddressLookup", "subThoroughfare: $subThoroughfare")
            Log.d("AddressLookup", "Full address: $addressLine")

            val result = "$adminArea $subLocality $thoroughfare"
            Log.d("AddressLookup", "Final result: $result")

            result
        }
    } catch (e: Exception) {
        Log.e("AddressLookup", "Error getting address", e)
        null
    }
}

@Composable
fun FunTextButton(buttonText: String, clickEvent: () -> Unit) {
    TextButton(
        onClick = { clickEvent() },
        colors = ButtonDefaults.buttonColors(
            Color.Transparent
        )
    ) {
        Text(text = buttonText,
            color = Color.Black,
            fontWeight = FontWeight.Bold)
    }
}