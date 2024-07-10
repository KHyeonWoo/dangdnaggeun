package com.khw.computervision

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.ImageLoader
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.net.URLEncoder
import java.nio.charset.StandardCharsets


val colorDang = Color(0xFFF3BB66)
val colorDong = Color(0xFF714809)
val colorBack = Color(0xFFfefbf6)
val colorChat = Color(0xFFF9D7A5)
val customFont = FontFamily(Font(R.font.santokki_regular, FontWeight.Normal))

object UserIDManager {
    var userID: MutableState<String> =
        mutableStateOf("")
    var userAddress: MutableState<String> = mutableStateOf("주소 정보가 여기에 표시됩니다.")
}

suspend fun getProfile(userID: String): String? {
    val storageRef = Firebase.storage.reference.child("${userID}/profile.jpg")
    return try {
        storageRef.downloadUrl.await().toString()
    } catch (e: Exception) {
        null
    }
}

@Composable
fun gifImageDecode(name: Int): AsyncImagePainter {
    val context = LocalContext.current

    val mascotImageUri = remember {
        Uri.parse("android.resource://${context.packageName}/${name}")
    }

    val imageLoader = remember {
        ImageLoader.Builder(context)
            .components {
                add(GifDecoder.Factory())
            }
            .build()
    }

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(mascotImageUri)
            .size(Size.ORIGINAL)
            .build(),
        imageLoader = imageLoader
    )

    return painter
}

//20240701 하승수 - fun 이름 FunTextButton에서 FunButton으로 변경 (button 함수)
@Composable
fun FunButton(buttonText: String, image: Int?, clickEvent: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { clickEvent() },
            colors = ButtonDefaults.buttonColors(
                colorDong
            )
        ) {
            image?.let {
                Icon(
                    painter = painterResource(id = it),
                    contentDescription = "icon",
                    tint = Color.White,
                    modifier = Modifier.padding(5.dp,0.dp)
                )
            }
            Text(text = buttonText, color = Color.White)
        }
    }
}

@Composable
fun SearchTextField(searchText: String, onSearchTextChange: (String) -> Unit) {
    OutlinedTextField(
        value = searchText,
        onValueChange = onSearchTextChange,
        label = { androidx.compose.material.Text("검색어를 입력하세요", color = Color.Black) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
    )
}

fun encodeUrl(url: String): String {
    return URLEncoder.encode(url, StandardCharsets.UTF_8.toString())
}
// 20240703 신동환 - 현재 위치 확인하는 함수입니다

@Composable
fun TopBar(
    title: String,
    onBackClick: () -> Unit,
    onAddClick: () -> Unit,
    addIcon: ImageVector?,
    showBackIcon: Boolean = true
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if(showBackIcon){
                IconButton(onClick = onBackClick) {
                    androidx.compose.material.Icon(
                        Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = colorDong
                    )
                }
            } else {
                Spacer(modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.weight(.1f))
            androidx.compose.material.Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colorDong,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.weight(5f))
            if (addIcon != null) {
                IconButton(onClick = onAddClick) {
                    androidx.compose.material.Icon(
                        addIcon,
                        contentDescription = "Add",
                        tint = colorDong
                    )
                }
            }
        }
        HorizontalDivider(
            color = colorDang,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp, 0.dp)
        )
    }
}

@Composable
fun HorizontalDividerColorDang(start: Dp, end: Dp, top: Dp, bottom: Dp){
    HorizontalDivider(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = start, end = end, top = top, bottom = bottom),
        thickness = 1.dp,
        color = colorDang
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChoiceSegButton(options: List<String>, checkedOption: Int, changeCheckedOpt: (Int) -> Unit) {
    MultiChoiceSegmentedButtonRow {
        options.forEachIndexed { index, label ->
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = options.size
                ),
                icon = {},
                colors = SegmentedButtonDefaults.colors(
                    activeContainerColor = colorDong,
                    activeContentColor = Color.White,
                    inactiveContainerColor = colorBack,
                    inactiveContentColor = Color.White,
                    activeBorderColor = colorDong,
                    inactiveBorderColor = colorDang,
                ),
                onCheckedChange = {
                    if (label == options[0]) {
                        changeCheckedOpt(0)
                    } else {
                        changeCheckedOpt(1)

                    }
                },
                checked = index == checkedOption,
                modifier = Modifier.size(62.dp,32.dp)
            ) {
                if (checkedOption == index) {
                    androidx.compose.material.Text(label, color = colorBack, fontSize = 16.sp)
                } else {
                    androidx.compose.material.Text(label, color = colorDang, fontSize = 16.sp)
                }
            }
        }
    }
}
