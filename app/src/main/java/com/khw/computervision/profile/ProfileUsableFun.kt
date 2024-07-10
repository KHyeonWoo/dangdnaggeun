package com.khw.computervision.profile

import android.content.ContentValues
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.khw.computervision.ProductViewModel
import com.khw.computervision.R
import com.khw.computervision.UserIDManager
import com.khw.computervision.colorDang
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun MyProductSwipeBox(
    key: String,
    productMap: Map<String, String>,
    productsViewModel: ProductViewModel,
    deleteValue: String
) {
    val squareSize = 48.dp
    val swipeState = rememberSwipeableState(0)
    val sizePx = with(LocalDensity.current) { squareSize.toPx() }
    val anchors = mapOf(0f to 0, -sizePx to 1) // Maps anchor points (in px) to states

    Box(
        modifier = Modifier
            .padding(8.dp)
            .width(350.dp)
            .swipeable(
                state = swipeState,
                anchors = anchors,
                thresholds = { _, _ -> FractionalThreshold(0.4f) },
                orientation = Orientation.Horizontal
            )
            .clip(RoundedCornerShape(15.dp))
    ) {
        Row(
            modifier = Modifier
                .offset { IntOffset(swipeState.offset.value.roundToInt(), 0) }
                .padding(start = 4.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(colorDang),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProductImage(Modifier.weight(3f),productMap["imageUrl"])
            ProductDetails(Modifier.weight(8f), productMap)
        }

        if (swipeState.currentValue == 1) {
            DeleteIcon(Modifier.align(Alignment.CenterEnd), key, productsViewModel, deleteValue)
        }
    }
}

@Composable
fun ProductDetails(modifier: Modifier, productMap: Map<String, String>) {
    Box(
        modifier = modifier
            .padding(4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    ) {
        Column(modifier = Modifier
            .fillMaxSize()
            .padding(10.dp, 0.dp)) {
            productMap.forEach { (fieldKey, fieldValue) ->
                if (fieldKey == "name" || fieldKey == "price") {
                    Text(text = fieldKey, color = colorDang)
                    Text(text = fieldValue, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun DeleteIcon(
    modifier: Modifier,
    key: String,
    productsViewModel: ProductViewModel,
    deleteValue: String
) {
    Box(
        modifier = modifier
            .size(48.dp, 100.dp),
        contentAlignment = Alignment.Center
    ) {
        val productFavoriteData by productsViewModel.totalLikedData.observeAsState()
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "Delete",
            tint = colorDang,
            modifier = Modifier.clickable {
                if(deleteValue == "myUploaded") {
                    deleteFireStoreData("product", key) {
                        productsViewModel.getProductsFromFireStore()
                    }
                } else {
                    val totalLiked = productFavoriteData?.get(key)
                    deleteLiked(productsViewModel, key)
                    updateProductLike(
                        productsViewModel,
                        key,
                        totalLiked,
                        -1
                    )
                }
            }
        )
    }
}

fun deleteFireStoreData(collectionName: String, documentId: String, successEvent: () -> Unit) {
    Firebase.firestore.collection(collectionName).document(documentId)
        .delete()
        .addOnSuccessListener {
            successEvent()
        }
        .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error deleting document", e) }
}

@Composable
fun ProfileImage(profileUrl: String?, setInputImage: (Bitmap) -> Unit) {

    val context = LocalContext.current
    val imageCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            setInputImage(
                MediaStore.Images.Media.getBitmap(
                    context.contentResolver, result.uriContent
                )
            )
        } else {
            Log.d("PhotoPicker", "No media selected")
        }
    }

    val cropOption = CropImageContractOptions(
        CropImage.CancelledResult.uriContent, CropImageOptions()
    )
    val painter = rememberAsyncImagePainter(profileUrl)

    profileUrl?.let {
        Image(
            painter = painter,
            contentDescription = "Image",
            modifier = Modifier
                .size(136.dp)
                .clip(RoundedCornerShape(80.dp))
                .clickable {
                    imageCropLauncher.launch(cropOption)
                })
    } ?: Image(painter = painterResource(id = R.drawable.dangkki_img_noback),
        contentDescription = "",
        modifier = Modifier
            .size(136.dp)
            .clip(RoundedCornerShape(80.dp))
            .clickable {
                imageCropLauncher.launch(cropOption)
            })

}

fun deleteLiked(productsViewModel: ProductViewModel, productKey: String) {
    Firebase.firestore.collection("${UserIDManager.userID.value}liked").document(productKey)
        .delete().addOnSuccessListener {}.addOnFailureListener {}
    productsViewModel.getLikedFromFireStore()
}

fun updateProductLike(
    productsViewModel: ProductViewModel,
    productKey: String,
    totalLiked: Map<String, String>?,
    likedCount: Int
) {
    Firebase.firestore.collection("favoriteProduct").document(productKey)
        .update("liked", totalLiked?.get("liked")?.let { it.toInt() + likedCount } ?: 0)
    productsViewModel.getTotalLikedFromFireStore()
}

@Composable
fun ProductImage(modifier: Modifier, imageUrl: String?) {
    val painter = rememberAsyncImagePainter(imageUrl)
    Image(
        painter = painter,
        contentDescription = "",
        contentScale = ContentScale.FillBounds,
        modifier = modifier
            .padding(4.dp)
            .aspectRatio(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.White)
    )
}

fun fetchLocationAndAddress(
    fusedLocationClient: FusedLocationProviderClient,
    geocoder: Geocoder,
    coroutineScope: CoroutineScope,
    onAddressFetched: (String) -> Unit
) {
    getLocation(fusedLocationClient) { location ->
        coroutineScope.launch(Dispatchers.IO) {
            val address = getAddressFromLocation(geocoder, location)
            withContext(Dispatchers.Main) {
                onAddressFetched(address ?: "주소를 찾을 수 없습니다.")
            }
        }
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
