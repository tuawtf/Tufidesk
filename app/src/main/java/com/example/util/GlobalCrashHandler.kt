package com.example.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.ui.CrashActivity
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GlobalCrashHandler(
    private val context: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            Log.e("GlobalCrashHandler", "Uncaught exception in thread ${thread.name}", throwable)

            val sw = StringWriter()
            val pw = PrintWriter(sw)
            throwable.printStackTrace(pw)
            val stackTrace = sw.toString()

            val logReport = buildString {
                appendLine("=== TUFIDESK ÇÖKME / HATA RAPORU ===")
                appendLine("Tarih / Saat: ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
                appendLine("Cihaz: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})")
                appendLine("İş Parçacığı (Thread): ${thread.name}")
                appendLine("Hata Sınıfı: ${throwable.javaClass.name}")
                appendLine("Hata Mesajı: ${throwable.message ?: "Detaylı mesaj yok"}")
                appendLine()
                appendLine("--- STACKTRACE ---")
                appendLine(stackTrace)
            }

            // Save to SharedPreferences for permanent retrieval
            val prefs = context.getSharedPreferences("tufidesk_crash_logs", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("last_crash_log", logReport)
                .putLong("last_crash_timestamp", System.currentTimeMillis())
                .commit()

            // Launch CrashActivity to display logs and copy button
            val intent = Intent(context, CrashActivity::class.java).apply {
                putExtra("crash_log", logReport)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            }
            context.startActivity(intent)

            android.os.Process.killProcess(android.os.Process.myPid())
            System.exit(10)
        } catch (e: Throwable) {
            Log.e("GlobalCrashHandler", "Failed inside crash handler", e)
            defaultHandler?.uncaughtException(thread, throwable)
        }
    }

    companion object {
        fun install(context: Context) {
            val currentHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (currentHandler !is GlobalCrashHandler) {
                Thread.setDefaultUncaughtExceptionHandler(
                    GlobalCrashHandler(context.applicationContext, currentHandler)
                )
            }
        }

        fun getLastCrashLog(context: Context): String? {
            val prefs = context.getSharedPreferences("tufidesk_crash_logs", Context.MODE_PRIVATE)
            return prefs.getString("last_crash_log", null)
        }

        fun saveManualError(context: Context, tag: String, throwable: Throwable) {
            try {
                val sw = StringWriter()
                val pw = PrintWriter(sw)
                throwable.printStackTrace(pw)
                val stackTrace = sw.toString()

                val logReport = buildString {
                    appendLine("=== TUFIDESK UYGULAMA İÇİ HATA RAPORU ($tag) ===")
                    appendLine("Tarih / Saat: ${SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault()).format(Date())}")
                    appendLine("Cihaz: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE}, API ${Build.VERSION.SDK_INT})")
                    appendLine("Hata Sınıfı: ${throwable.javaClass.name}")
                    appendLine("Hata Mesajı: ${throwable.message ?: "Detaylı mesaj yok"}")
                    appendLine()
                    appendLine("--- STACKTRACE ---")
                    appendLine(stackTrace)
                }

                val prefs = context.getSharedPreferences("tufidesk_crash_logs", Context.MODE_PRIVATE)
                prefs.edit()
                    .putString("last_crash_log", logReport)
                    .putLong("last_crash_timestamp", System.currentTimeMillis())
                    .apply()
            } catch (_: Exception) {}
        }
    }
}
