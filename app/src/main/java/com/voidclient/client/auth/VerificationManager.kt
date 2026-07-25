package com.voidclient.client.auth

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsIntent
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URLEncoder
import java.util.*

object VerificationManager {
    private const val TAG = "VerifyNet"
    private const val PREFS = "wclient_prefs"
    private const val KEY_WCLIENT_ID = "wclient_id"

    private const val BASE_VERIFY_URL = "https://retrivedmods.online/task/verify.php"

    private val client: OkHttpClient = OkHttpClient.Builder()
        .followRedirects(true)
        .followSslRedirects(true)
        .addInterceptor { chain ->
            val req = chain.request()
            Log.d(TAG, "REQ: ${req.method} ${req.url}")
            val resp = chain.proceed(req)
            val loc = resp.header("Location")
            Log.d(TAG, "RESP: ${resp.code} ${resp.message} -> ${resp.request.url}")
            if (loc != null) Log.d(TAG, "Redirect Location: $loc")
            resp
        }
        .build()

    private val jsonMedia = "application/json; charset=utf-8".toMediaType()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    fun getWClientId(ctx: Context): String {
        val p = prefs(ctx)
        var id = p.getString(KEY_WCLIENT_ID, null)
        if (id.isNullOrBlank()) {
            id = "WC-" + UUID.randomUUID().toString().uppercase().replace("-", "").substring(0, 16)
            p.edit().putString(KEY_WCLIENT_ID, id).apply()
        }
        return id
    }

    suspend fun isWhitelisted(ctx: Context, wclientId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_VERIFY_URL?action=check_whitelist&wclient_id=${URLEncoder.encode(wclientId, "utf-8")}"
                val req = Request.Builder().url(url).get().build()

                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext false
                    val body = resp.body?.string() ?: return@withContext false
                    val j = JSONObject(body)
                    return@withContext j.optBoolean("whitelisted", false)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error checking whitelist", t)
                return@withContext false
            }
        }

    suspend fun isVerified(ctx: Context, wclientId: String): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val url = "$BASE_VERIFY_URL?action=check_verified&wclient_id=${URLEncoder.encode(wclientId, "utf-8")}"
                val req = Request.Builder().url(url).get().build()

                client.newCall(req).execute().use { resp ->
                    if (!resp.isSuccessful) return@withContext false
                    val body = resp.body?.string() ?: return@withContext false
                    val j = JSONObject(body)
                    return@withContext j.optBoolean("verified", false)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Error checking verification", t)
                return@withContext false
            }
        }

    suspend fun requestVerification(ctx: Context, wclientId: String): String =
        withContext(Dispatchers.IO) {
            val payload = JSONObject().put("wclient_id", wclientId).toString()
            val reqBody = payload.toRequestBody(jsonMedia)
            val url = "$BASE_VERIFY_URL?action=request&short=1"
            val req = Request.Builder().url(url).post(reqBody).build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) throw Exception("Server ${resp.code}")
                val body = resp.body?.string() ?: throw Exception("Empty response")
                val j = JSONObject(body)
                val verifyUrl = j.optString("verify_url", "")

                if (verifyUrl.isBlank()) throw Exception("Invalid response")
                return@withContext verifyUrl
            }
        }

    fun openInAppBrowser(activity: Activity, verifyUrl: String) {
        try {
            if (verifyUrl.isBlank()) {
                Log.w(TAG, "openInAppBrowser: empty URL")
                return
            }
            if (!verifyUrl.startsWith("http://") && !verifyUrl.startsWith("https://")) {
                Log.w(TAG, "openInAppBrowser: non-http URL, falling back to external: $verifyUrl")
                openInExternalBrowser(activity, verifyUrl)
                return
            }
            val builder = CustomTabsIntent.Builder()
            builder.setShowTitle(true)
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(activity, Uri.parse(verifyUrl))
            Log.d(TAG, "Launched CustomTab for $verifyUrl")
        } catch (e: ActivityNotFoundException) {
            Log.w(TAG, "CustomTab Activity not found - fallback to external", e)
            openInExternalBrowser(activity, verifyUrl)
        } catch (t: Throwable) {
            Log.w(TAG, "CustomTab failed - fallback to external", t)
            openInExternalBrowser(activity, verifyUrl)
        }
    }

    fun openInExternalBrowser(activity: Activity, url: String) {
        try {
            val i = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            activity.startActivity(Intent.createChooser(i, "Open link"))
            Log.d(TAG, "Launched external browser for $url")
        } catch (e: ActivityNotFoundException) {
            Log.e(TAG, "No browser to open url", e)
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to open external browser", t)
        }
    }

    fun pollVerificationStatus(ctx: Context, wclientId: String, onComplete: (Boolean, String?) -> Unit) {
        scope.launch {
            try {
                val pollIntervalMs = 3000L
                val start = System.currentTimeMillis()
                val maxDurationMs = 4L * 60L * 60L * 1000L + 10_000L

                while (true) {
                    val url = "$BASE_VERIFY_URL?action=check_verified&wclient_id=${URLEncoder.encode(wclientId, "utf-8")}"
                    val req = Request.Builder().url(url).get().build()

                    client.newCall(req).execute().use { resp ->
                        if (!resp.isSuccessful) {
                            Log.d(TAG, "Status call returned ${resp.code}; will retry")
                        } else {
                            val s = resp.body?.string() ?: ""
                            val j = JSONObject(s)
                            val verified = j.optBoolean("verified", false)

                            if (verified) {
                                withContext(Dispatchers.Main) { onComplete(true, null) }
                                return@launch
                            }
                        }
                    }

                    if (System.currentTimeMillis() - start > maxDurationMs) {
                        withContext(Dispatchers.Main) { onComplete(false, "timed out") }
                        return@launch
                    }
                    delay(pollIntervalMs)
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Polling error", t)
                withContext(Dispatchers.Main) { onComplete(false, t.message ?: "error") }
            }
        }
    }

    fun cancelAll() {
        scope.cancel()
    }
}