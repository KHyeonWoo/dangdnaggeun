package com.khw.computervision

import android.graphics.Bitmap
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

//240702 김현우 - 서버통신을 위한 함수 UsableFun으로 이동
interface ApiService {
    @Multipart
    @POST("/infer")
    fun uploadImage(
        @Part image: MultipartBody.Part,
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>
    ): Call<ResponseBody>

    @Multipart
    @POST("/tryon")
    fun uploadList(
        @PartMap data: Map<String, @JvmSuppressWildcards RequestBody>
    ): Call<ResponseBody>
}

object RetrofitClient {
    private const val BASE_URL = "http://192.168.45.140:8080/"

    private val client = OkHttpClient.Builder()
        .readTimeout(120, TimeUnit.SECONDS)
        .connectTimeout(120, TimeUnit.SECONDS)
        .build()


    val instance: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }
}

fun sendImageToServer(
    bitmap: Bitmap,
    successEvent: (String) -> Unit
) {
    val image = bitmapToByteArray(bitmap) // 실제 이미지 파일 경로
    val requestFile = RequestBody.create("image/*".toMediaTypeOrNull(), image)
    val imagePart = MultipartBody.Part.createFormData(
        "image",
        "${UserIDManager.userID.value}.png",
        requestFile
    )
    val userIdPart =
        RequestBody.create("text/plain".toMediaTypeOrNull(), UserIDManager.userID.value)

    val dataMap = mapOf("userID" to userIdPart)

    RetrofitClient.instance.uploadImage(imagePart, dataMap)
        .enqueue(object : Callback<ResponseBody> {
            override fun onResponse(
                call: Call<ResponseBody>,
                response: Response<ResponseBody>
            ) {
                if (response.isSuccessful) {
                    successEvent("성공: ${response.body()}")
                } else {
                    successEvent("에러 메시지: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                successEvent("요청이 실패했습니다: ${t.message}")
            }
        })
}

