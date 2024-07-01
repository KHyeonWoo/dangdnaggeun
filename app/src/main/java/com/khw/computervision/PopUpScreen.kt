package com.khw.computervision

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.gowtham.ratingbar.StepSize
import com.skydoves.landscapist.glide.GlideImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.time.LocalDateTime


@Composable
fun ProfilePopup(
    profileUri: String?,
    userID: String,
    close: () -> Unit,
    successUpload: () -> Unit
) {

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
                Text(text = "")

                var inputImage by remember { mutableStateOf<Bitmap?>(null) }

                ProfileImage(profileUri) { inputImage = it }

                inputImage?.let { bitmap ->
                    uploadBitmapImage(context, bitmap, userID, "profile.jpg", {
                        successUpload()
                    }, {
                        inputImage = null
                    })
                }
                Text(text = userID)
                Spacer(modifier = Modifier.weight(2f))

                val messageMap = getMessage()

                FunTextButton("내가 올린 제품") {
                    val productIntent = Intent(context, MyUploadedActivity::class.java)
                    productIntent.putExtra("userID", userID)
                    context.startActivity(productIntent) }
                FunTextButton("내가 판매한 제품") { }
                FunTextButton("내게 온 메세지 : ${messageMap.size}") {
                    val userIntent = Intent(context, MessageListActivity::class.java)
                    userIntent.putExtra("messageList", mapToBundle(messageMap))
                    context.startActivity(userIntent)
                }
                Spacer(modifier = Modifier.weight(1f))
                FunTextButton("로그아웃") {
                    context.startActivity(Intent(context, LoginActivity::class.java))
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
    userID: String,
    receiveUser: String,
    returnMessageIndex: Int,
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
                val db = Firebase.firestore
                val dateTimeNow = LocalDateTime.now().toLocalDate().toString().replace("-", "") +
                        LocalDateTime.now().toLocalTime().toString().replace(":", "")
                            .substring(0, 4)
                val sendMessage = hashMapOf(
                    "sendUser" to userID,
                    "date" to dateTimeNow,
                    "message" to message,
                    "read" to "false"
                )

                coroutineScope.launch(Dispatchers.IO) {
                    db.collection(receiveUser)
                        .document("$returnMessageIndex")
                        .set(sendMessage)
                        .addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully written!")
                            Toast.makeText(context, "메세지 전송 성공", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error writing document", e)
                            Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                        }
                }
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
                        if (price== "0") {
                            ""
                        } else {
                            price
                        }
                    ,
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
