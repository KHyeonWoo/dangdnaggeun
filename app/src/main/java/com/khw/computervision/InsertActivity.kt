package com.khw.computervision

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.gowtham.ratingbar.RatingBar
import com.gowtham.ratingbar.RatingBarStyle
import com.gowtham.ratingbar.StepSize
import com.khw.computervision.ui.theme.ComputerVisionTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime

class InsertActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComputerVisionTheme {
                var userID by remember {
                    mutableStateOf("")
                }
                userID = intent.getStringExtra("user") ?: ""

                InsertScreen(userID)
            }
        }
    }

    @Composable
    fun InsertScreen(userID: String) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current
            var newPopupDetails by remember {
                mutableStateOf(
                    PopupDetails(
                        userID,
                        "",
                        0,
                        "",
                        0f,
                        ""
                    )
                )
            }
            LogoScreen("Insert") { finish() }
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                Spacer(modifier = Modifier.weight(1f))

                var popupVisibleState by remember { mutableStateOf(false) }
                FunTextButton("수정") {
                    popupVisibleState = true
                }

                if (popupVisibleState) {
                    InsertPopup(userID, newPopupDetails, {
                        newPopupDetails = it
                    }, {
                        popupVisibleState = false
                    })
                }

                Spacer(modifier = Modifier.width(8.dp))
                val coroutineScope = rememberCoroutineScope()
                val insertIndex = returnInsertIndex()
                FunTextButton("저장") {
                    saveEvent(coroutineScope, context, userID, insertIndex, newPopupDetails)
                    DataManager.reLoading = !DataManager.reLoading
                    finish()
                }
            }
            Image(
                painter = painterResource(id = R.drawable.character4),
                contentDescription = "",
                modifier = Modifier
                    .size(320.dp)
                    .clickable {
                        context.startActivity(Intent(context, DecorateActivity::class.java))
                    }
            )
            StateScreen(newPopupDetails)

            Spacer(modifier = Modifier.weight(1f))
        }

    }

    private fun saveEvent(
        coroutineScope: CoroutineScope,
        context: Context,
        userID: String,
        insertIndex: Int,
        newPopupDetails: PopupDetails
    ) {

        val db = Firebase.firestore
        val dateTimeNow =
            LocalDateTime.now().toLocalDate().toString().replace("-", "") +
                    LocalDateTime.now().toLocalTime().toString().replace(":", "")
                        .substring(0, 4)
        val sendMessage = hashMapOf(
            "InsertUser" to userID,
            "date" to dateTimeNow,
            "imageUrl" to "",
            "price" to newPopupDetails.price,
            "dealMethod" to newPopupDetails.dealMethod,
            "rating" to newPopupDetails.rating,
            "productDescription" to newPopupDetails.productDescription,
            "state" to 1 //1: 판매중, 2: 판매완료, 3:숨기기, 4:삭제
        )

        coroutineScope.launch(Dispatchers.IO) {
            db.collection("product")
                .document("$insertIndex")
                .set(sendMessage)
                .addOnSuccessListener {
                    Log.d(ContentValues.TAG, "DocumentSnapshot successfully written!")
                    Toast.makeText(context, "업로드 성공", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Log.w(ContentValues.TAG, "Error writing document", e)
                    Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    @Composable
    private fun StateScreen(newPopupDetails: PopupDetails) {
        Divider(color = colorDang, thickness = 2.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
        ) {
            Text(text = "가격", modifier = Modifier.weight(1f), color = colorDang)
            Text(text = " ${newPopupDetails.price}", modifier = Modifier.weight(1f))

            Text(text = "거래방법", modifier = Modifier.weight(1f), color = colorDang)
            Text(text = " ${newPopupDetails.dealMethod}", modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier.weight(3f)
            ) {
                Spacer(modifier = Modifier.size(16.dp, 0.dp))
                Text(text = "상태", color = colorDang)
                Spacer(modifier = Modifier.size(16.dp, 0.dp))
                RatingBar(
                    value = newPopupDetails.rating,
                    style = RatingBarStyle.Fill(),
                    stepSize = StepSize.HALF,
                    onValueChange = {},
                    size = 16.dp,
                    spaceBetween = 2.dp,
                    onRatingChanged = {
                        Log.d("TAG", "onRatingChanged: $it")
                    }
                )
            }
        }
        Divider(color = colorDang, thickness = 2.dp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .horizontalScroll(rememberScrollState())
        ) {
            Text(text = " \n ${newPopupDetails.productDescription}")
        }
    }
}

