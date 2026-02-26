package com.benjamin.autodishes

import android.util.Log
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.resume

object UniCloudConfig {
    const val SPACE_ID = "mp-334e6ddc-7a19-4d9e-ae02-03449157fb4e"
    const val SECRET = "Tnm/FGIn7ldYF5opB8JVXg=="
    // The '/client' path is for triggering Cloud Functions (uni-functions)
    const val ENDPOINT = "https://api.next.bspapp.com/client"
}

object UniCloudClient {
    private val client = OkHttpClient()
    private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()

    /**
     * Calls a UniCloud Function named 'recommendMeals'
     * which should be set up in your Ali Cloud console to talk to Qwen.
     */
    suspend fun getAIRecommendation(profile: ElderProfile): String {
        return suspendCancellableCoroutine { cont ->
            try {
                val json = JSONObject()
                json.put("action", "recommendMeals") // The name of your Cloud Function
                json.put("spaceId", UniCloudConfig.SPACE_ID)
                json.put("secret", UniCloudConfig.SECRET)

                // The data we pass to the AI
                val data = JSONObject()
                data.put("name", profile.name)
                data.put("health_info", profile.diseases)
                data.put("allergies", profile.allergies)
                data.put("likes", profile.preferences)
                json.put("data", data)

                val body = json.toString().toRequestBody(JSON_TYPE)
                val request = Request.Builder()
                    .url(UniCloudConfig.ENDPOINT)
                    .post(body)
                    .build()

                client.newCall(request).enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e("UniCloud", "Network Error: ${e.message}")
                        cont.resume("Error connecting to cloud.")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        val responseString = response.body?.string() ?: ""
                        Log.d("UniCloud", "AI Response: $responseString")

                        val resJson = JSONObject(responseString)
                        if (resJson.optBoolean("success")) {
                            // Assumes your cloud function returns { "data": { "recommendation": "..." } }
                            val result = resJson.optJSONObject("data")?.optString("recommendation")
                                ?: "AI returned success but no text."
                            cont.resume(result)
                        } else {
                            cont.resume("AI Error: ${resJson.optString("error")}")
                        }
                    }
                })
            } catch (e: Exception) {
                cont.resume("Exception: ${e.message}")
            }
        }
    }
}