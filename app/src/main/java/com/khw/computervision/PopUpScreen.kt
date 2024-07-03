package com.khw.computervision

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.location.Geocoder
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.TextButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.gowtham.ratingbar.StepSize
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient

@Composable
fun ProfilePopup(
    profileUri: String?,
    close: () -> Unit,
    successUpload: () -> Unit
) {
    var addressText by remember { mutableStateOf("주소 정보가 여기에 표시됩니다") }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = { close() },
        title = { Text(text = "") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(600.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {

                val fusedLocationClient =
                    remember { LocationServices.getFusedLocationProviderClient(context) }
                val geocoder = remember { Geocoder(context, Locale.KOREA) }
                val coroutineScope = rememberCoroutineScope()

                val requestPermissionLauncher =
                    rememberLauncherForActivityResult(
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

                Text(text = "")

                var inputImage by remember { mutableStateOf<Bitmap?>(null) }

                ProfileImage(profileUri) { inputImage = it }

                inputImage?.let { bitmap ->
                    uploadBitmapImage(context, bitmap, UserIDManager.userID.value, "profile.jpg", {
                        successUpload()
                    }, {
                        inputImage = null
                    })
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
                Spacer(modifier = Modifier.weight(2f))

                val messageMap = getMessage()


                FunButton("내가 올린 제품", image = R.drawable.list_icon) {
                    val productIntent = Intent(context, MyUploadedActivity::class.java)
                    productIntent.putExtra("userID", UserIDManager.userID.value)
                    context.startActivity(productIntent)
                }

                FunButton("내게 온 메세지 : ${messageMap.size}", null) {
                    val userIntent = Intent(context, MessageListActivity::class.java)
                    userIntent.putExtra("messageList", mapToBundle(messageMap))
                    context.startActivity(userIntent)
                }

                Spacer(modifier = Modifier.weight(1f))
                FunButton("로그아웃", null) {
//                    context.startActivity(Intent(context, LoginActivity::class.java))
                }
            }
        },
        confirmButton =
        { },
        dismissButton =
        { }
    )
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
        Toast.makeText(context, "사진 업로드 성공", Toast.LENGTH_SHORT).show()
        successUpload()
        inputImageNullEvent()
    }.addOnProgressListener {
        Toast.makeText(context, "사진 업로드 중", Toast.LENGTH_SHORT).show()
    }.addOnFailureListener {
        Toast.makeText(context, "사진 업로드 실패", Toast.LENGTH_SHORT).show()
        inputImageNullEvent()
    }
}

@Composable
fun ProfileImage(profileUri: String?, setInputImage: (Bitmap) -> Unit) {

    val context = LocalContext.current
    val imageCropLauncher =
        rememberLauncherForActivityResult(CropImageContract()) { result ->
            if (result.isSuccessful) {
                setInputImage(
                    MediaStore.Images.Media.getBitmap(
                        context.contentResolver,
                        result.uriContent
                    )
                )
            } else {
                Log.d("PhotoPicker", "No media selected")
            }
        }

    val cropOption = CropImageContractOptions(
        CropImage.CancelledResult.uriContent,
        CropImageOptions()
    )

    profileUri?.let {
        GlideImage(
            imageModel = it,
            contentDescription = "Image",
            modifier = Modifier
                .size(160.dp)
                .clickable {
                    imageCropLauncher.launch(cropOption)
                }
                .clip(RoundedCornerShape(120.dp))
        )
    } ?: Image(
        painter = painterResource(id = R.drawable.ic_launcher_foreground),
        contentDescription = "",
        modifier = Modifier
            .size(160.dp)
            .clickable {
                imageCropLauncher.launch(cropOption)
            }
            .clip(RoundedCornerShape(32.dp))
    )

}

@Composable
fun MessagePopup(
    receiveUser: String,
    close: () -> Unit
) {
    var message: String by remember { mutableStateOf("") }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    AlertDialog(
        onDismissRequest = { close() },
        title = { Text(text = "") },
        text = {
            Column(
            ) {
                Text(text = "당당하게 보내세요", color = colorDang)
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = receiveUser,
                    onValueChange = { },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDang,
                        unfocusedBorderColor = colorDang,
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "받는 사람", color = colorDang) },
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDang,
                        unfocusedBorderColor = colorDang,
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "메시지", color = colorDang) },
                    modifier = Modifier.height(320.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                // Create a new user with a first and last name
                val dateTimeNow = LocalDateTime.now().toLocalDate().toString().replace("-", "") +
                        LocalDateTime.now().toLocalTime().toString().replace(":", "")
                            .substring(0, 4)
                val sendMessage = hashMapOf(
                    "sendUser" to UserIDManager.userID.value,
                    "date" to dateTimeNow,
                    "message" to message,
                    "read" to "false"
                )

                val db = Firebase.firestore
                coroutineScope.launch(Dispatchers.IO) {
                    try {
                        db.collection(receiveUser)
                            .document(dateTimeNow)
                            .set(sendMessage)
                            .await() // suspend function to await the task completion
                        withContext(Dispatchers.Main) {
                            Log.d(TAG, "DocumentSnapshot successfully written!")
                            Toast.makeText(context, "메세지 전송 성공", Toast.LENGTH_SHORT).show()
                            close()
                        }
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Log.w(TAG, "Error writing document", e)
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }

            }) {
                Text("Upload")
            }
        },
        dismissButton = {
            Button(onClick = { close() }) {
                Text("Cancel")
            }
        }
    )

}

data class PopupDetails(
    val userID: String,
    val name: String = "",
    val imageUri: String = "",
    val price: Int = 0,
    val dealMethod: String = "",
    val rating: Float = 0f,
    val productDescription: String = ""
)

@Composable
fun InsertPopup(
    newPopupDetails: PopupDetails,
    saveData: (PopupDetails) -> Unit,
    close: () -> Unit
) {
    var name: String by remember { mutableStateOf(newPopupDetails.name) }
    val imageUri: String by remember { mutableStateOf(newPopupDetails.imageUri) }
    var price: String by remember { mutableStateOf(newPopupDetails.price.toString()) }
    var dealMethod: String by remember { mutableStateOf(newPopupDetails.dealMethod) }
    var rating: Float by remember { mutableFloatStateOf(newPopupDetails.rating) }
    var productDescription: String by remember { mutableStateOf(newPopupDetails.productDescription) }
    AlertDialog(
        onDismissRequest = { close() },
        title = { Text(text = "") },
        text = {
            Column(
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDang,
                        unfocusedBorderColor = colorDang,
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "제품명", color = colorDang) },
                )

                OutlinedTextField(
                    value =
                    if (price == "0") {
                        ""
                    } else {
                        price
                    },
                    onValueChange = { price = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDang,
                        unfocusedBorderColor = colorDang,
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "가격", color = colorDang) },
                )

                OutlinedTextField(
                    value = dealMethod,
                    onValueChange = { dealMethod = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDang,
                        unfocusedBorderColor = colorDang,
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "거래방법", color = colorDang) },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            color = colorDang,
                            width = 1.dp,
                            shape = RoundedCornerShape(4.dp)
                        ),
                    verticalAlignment = Alignment.CenterVertically,

                    ) {
                    Spacer(modifier = Modifier.size(16.dp, 0.dp))
                    Text(text = "상태", color = colorDang)
                    Spacer(modifier = Modifier.size(16.dp, 0.dp))
                    RatingBar(
                        value = rating,
                        style = RatingBarStyle.Fill(),
                        stepSize = StepSize.HALF,
                        onValueChange = {
                            rating = it
                        },
                        size = 24.dp,
                        spaceBetween = 4.dp,
                        onRatingChanged = {
                            Log.d("TAG", "onRatingChanged: $it")
                        }
                    )
                }
                OutlinedTextField(
                    value = productDescription,
                    onValueChange = { productDescription = it },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = colorDang,
                        unfocusedBorderColor = colorDang,
                    ),
                    textStyle = TextStyle(color = Color.Black),
                    label = { Text(text = "제품설명", color = colorDang) },
                    modifier = Modifier.height(320.dp)
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                saveData(
                    PopupDetails(
                        UserIDManager.userID.value,
                        name,
                        imageUri,
                        price.toInt(),
                        dealMethod,
                        rating,
                        productDescription
                    )
                )
                close()
            }) {
                Text("Upload")
            }
        },
        dismissButton = {
            Button(onClick = { close() }) {
                Text("Cancel")
            }
        }
    )

}
