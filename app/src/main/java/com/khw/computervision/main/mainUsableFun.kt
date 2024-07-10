package com.khw.computervision.main

import androidx.activity.result.ActivityResultLauncher
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.khw.computervision.ClosetViewModel
import com.khw.computervision.FunButton
import com.khw.computervision.UserIDManager

fun startImagePicker(imageCropLauncher: ActivityResultLauncher<CropImageContractOptions>) {
    val cropOption = CropImageContractOptions(
        null, // 초기값 설정
        CropImageOptions()
    )
    imageCropLauncher.launch(cropOption)
}

@Composable
fun ExpandedImageDialog(
    ref: StorageReference,
    url: String,
    closetViewModel: ClosetViewModel,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .background(Color.White)
        ) {
            // Adjusting image size to fill more of the dialog space
            LoadImageFromUrl(
                url = url,
                modifier = Modifier
                    .align(Alignment.Center)
                    .fillMaxSize(fraction = 0.7f) // Set the image to fill 90% of the dialog size
            )
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
            }
            FunButton("삭제", null) {
                ref.delete()
                    .addOnSuccessListener {
                        closetViewModel.getItemsFromFirebase(
                            Firebase.storage.reference.child(
                                UserIDManager.userID.value
                            )
                        )
                        onDismiss()
                    }
                    .addOnFailureListener {
                    }
            }


        }
    }
}

@Composable
private fun LoadImageFromUrl(url: String, modifier: Modifier = Modifier) {
    Image(
        painter = rememberAsyncImagePainter(model = url),
        contentDescription = null,
        modifier = modifier
    )
}


fun filterProducts(
    productData: Map<String, Map<String, String>>?,
    categoryOption: String,
    searchText: String
): Map<String, Map<String, String>>? {
    val categoryFiltered = productData?.filter { (_, value) ->
        value["category"] == categoryOption
    }
    return if (searchText.isNotEmpty()) {
        categoryFiltered?.filter { (_, value) ->
            value["name"]?.contains(searchText, ignoreCase = true) == true
        }
    } else {
        categoryFiltered
    }
}

fun sortProducts(
    searchedProductData: Map<String, Map<String, String>>?,
    productFavoriteData: Map<String, Map<String, String>>?,
    sortOption: String
): List<String> {
    return if (sortOption == "liked" && productFavoriteData != null) {
        productFavoriteData.entries.sortedByDescending { it.value[sortOption] }
            .map { it.key }
    } else {
        searchedProductData?.keys?.toList() ?: emptyList()
    }
}

fun incrementViewCount(key: String, totalLiked: Map<String, String>?) {
    Firebase.firestore
        .collection("favoriteProduct")
        .document(key)
        .update("viewCount", (totalLiked?.get("viewCount")?.plus(1)) ?: 1)
}

@Composable
fun ImageItem(
    url: String,
    ref: StorageReference,
    category: String,
    onImageClick: (StorageReference, String, String) -> Unit
) {
    Column {
        val painter = rememberAsyncImagePainter(url)
        Image(
            painter = painter,
            contentDescription = "Image",
            modifier = Modifier
                .size(80.dp)
                .clickable {
                    onImageClick(ref, url, category)
                },
            contentScale = ContentScale.FillBounds
        )
    }
}
