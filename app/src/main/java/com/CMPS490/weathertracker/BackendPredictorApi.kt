package com.CMPS490.weathertracker

import android.content.Context
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

data class BackendPredictionResult(
    val stormProbability: Double,
    val stormLevel: String,
    val alertState: String,
    val alertAction: String,
    val alertReason: String,
)

class BackendPredictorApi(context: Context) {
    private val prefs = context.getSharedPreferences("weathertracker_backend", Context.MODE_PRIVATE)

    companion object {
        private const val PREF_USER_ID = "user_id"
        private const val PREF_DEVICE_TOKEN = "device_token"
    }

    suspend fun predictLive(
        userLocation: LatLng,
    ): BackendPredictionResult = withContext(Dispatchers.IO) {
        val userId = getOrRegisterUser(userLocation)
        val payload = JSONObject().apply {
            put("latitude", userLocation.latitude)
            put("longitude", userLocation.longitude)
        }
        val json = postJson("${BuildConfig.BACKEND_BASE_URL}/users/$userId/ml/predict-auto", payload)
        BackendPredictionResult(
            stormProbability = json.optDouble("storm_probability", 0.0),
            stormLevel = json.optString("storm_level", "unknown"),
            alertState = json.optString("alert_state", "off"),
            alertAction = json.optString("alert_action", "none"),
            alertReason = json.optString("alert_reason", "unknown"),
        )
    }

    private fun getOrRegisterUser(userLocation: LatLng): Int {
        val stored = prefs.getInt(PREF_USER_ID, -1)
        if (stored > 0) return stored

        val payload = JSONObject().apply {
            put("device_token", getDeviceToken())
            put("lat", userLocation.latitude)
            put("lon", userLocation.longitude)
        }
        val json = postJson("${BuildConfig.BACKEND_BASE_URL}/users/register", payload)
        val userId = json.getInt("user_id")
        prefs.edit().putInt(PREF_USER_ID, userId).apply()
        return userId
    }

    private fun getDeviceToken(): String {
        val existing = prefs.getString(PREF_DEVICE_TOKEN, null)
        if (!existing.isNullOrBlank()) return existing

        val generated = "android-" + UUID.randomUUID().toString()
        prefs.edit().putString(PREF_DEVICE_TOKEN, generated).apply()
        return generated
    }

    private fun postJson(url: String, payload: JSONObject): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(payload.toString())
            }

            val stream = if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = stream?.use {
                BufferedReader(InputStreamReader(it)).readText()
            }.orEmpty()

            if (connection.responseCode !in 200..299) {
                throw IllegalStateException("Backend request failed (${connection.responseCode}): $body")
            }
            JSONObject(body)
        } finally {
            connection.disconnect()
        }
    }
}
