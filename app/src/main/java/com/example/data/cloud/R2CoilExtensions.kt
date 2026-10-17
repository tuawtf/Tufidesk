package com.example.data.cloud

import android.content.Context
import coil.map.Mapper
import coil.request.Options
import okhttp3.Interceptor
import okhttp3.Response
import java.net.URI

/**
 * "r2://" ile başlayan görsel modellerini "https://{workerUrl}/files/{key}" adresine dönüştüren Coil Mapper.
 */
class R2UriMapper(private val context: Context) : Mapper<String, String> {
    override fun map(data: String, options: Options): String? {
        if (!data.startsWith("r2://")) return null

        val workerUrl = R2Settings.getWorkerUrl(context)
        if (workerUrl.isBlank()) return null

        val key = data.removePrefix("r2://").trimStart('/')
        val baseUrl = workerUrl.trimEnd('/')
        return "$baseUrl/files/$key"
    }
}

/**
 * Yalnızca Cloudflare Worker ana makinesine (host) giden isteklere
 * "Authorization: Bearer {token}" başlığı ekleyen OkHttp Interceptor.
 */
class R2AuthInterceptor(private val context: Context) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val workerUrl = R2Settings.getWorkerUrl(context)
        val token = R2Settings.getWorkerToken(context)

        if (workerUrl.isBlank() || token.isBlank()) {
            return chain.proceed(originalRequest)
        }

        val workerHost = try {
            URI(workerUrl).host
        } catch (_: Exception) {
            null
        }

        val requestHost = originalRequest.url.host
        val isTargetingWorker = workerHost != null && requestHost.equals(workerHost, ignoreCase = true)

        val newRequest = if (isTargetingWorker && originalRequest.header("Authorization") == null) {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        } else {
            originalRequest
        }

        return chain.proceed(newRequest)
    }
}
