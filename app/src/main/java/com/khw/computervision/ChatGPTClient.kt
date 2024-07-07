import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException

@Serializable
data class ChatGPTRequest(val model: String, val messages: List<ChatGPTMessage>)

@Serializable
data class ChatGPTMessage(val role: String, val content: String)

@Serializable
data class ChatGPTResponse(val choices: List<Choice>)

@Serializable
data class Choice(val message: ChatGPTMessage)
val jsonFormat = Json { ignoreUnknownKeys = true }

suspend fun sendChatGPTRequest(apiKey: String, messages: List<ChatGPTMessage>): ChatGPTResponse {
    return withContext(Dispatchers.IO) {
        val client = OkHttpClient()
        val requestObj = ChatGPTRequest(model = "gpt-3.5-turbo", messages = messages)
        val json = jsonFormat.encodeToString(requestObj)
        val requestBody = json.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            val responseBody = response.body?.string()
            Log.d("ChatGPTResponse", "Response Body: $responseBody")
            if (responseBody != null) {
                jsonFormat.decodeFromString(responseBody)
            } else {
                throw IOException("Empty response body")
            }
        }
    }
}