package com.example

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlin.system.exitProcess

class TufiDeskApplication : Application() {

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
