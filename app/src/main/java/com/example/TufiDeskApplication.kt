package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import com.example.data.cloud.R2AuthInterceptor
import com.example.data.cloud.R2UriMapper
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class TufiDeskApplication : Application(), ImageLoaderFactory {

    override fun newImageLoader(): ImageLoader {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(R2AuthInterceptor(this))
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        return ImageLoader.Builder(this)
            .components {
                add(R2UriMapper(this@TufiDeskApplication))
            }
            .okHttpClient(okHttpClient)
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25)
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100L * 1024 * 1024) // 100 MB disk önbelleği (çevrimdışı erişim)
                    .build()
            }
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()

        val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()

        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            try {
                Log.e("TufiDeskApplication", "Uncaught exception in thread ${thread.name}: ${throwable.message}", throwable)

                val stackTrace = Log.getStackTraceString(throwable)
                val errMsg = throwable.message ?: throwable.localizedMessage ?: throwable.javaClass.simpleName

                // Çökme kaydını SharedPreferences'a acil olarak yaz
                try {
                    val prefs = getSharedPreferences("crash_recovery_prefs", Context.MODE_PRIVATE)
                    prefs.edit()
                        .putString("last_crash_error", errMsg)
                        .putString("last_crash_stacktrace", stackTrace)
                        .putLong("last_crash_time", System.currentTimeMillis())
                        .commit()
                } catch (_: Exception) {}

                val intent = Intent(this, CrashActivity::class.java).apply {
                    putExtra(CrashActivity.EXTRA_ERROR_MESSAGE, errMsg)
                    putExtra(CrashActivity.EXTRA_STACK_TRACE, stackTrace)
                    putExtra(CrashActivity.EXTRA_THREAD_NAME, thread.name)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)

                // Binder IPC çağrısının işletim sistemi tarafından tamamlanabilmesi için kısa bekleme
                try {
                    Thread.sleep(600)
                } catch (_: InterruptedException) {}
            } catch (e: Exception) {
                Log.e("TufiDeskApplication", "Failed to launch CrashActivity: ${e.message}", e)
                defaultHandler?.uncaughtException(thread, throwable)
            } finally {
                android.os.Process.killProcess(android.os.Process.myPid())
                exitProcess(10)
            }
        }
    }
}
