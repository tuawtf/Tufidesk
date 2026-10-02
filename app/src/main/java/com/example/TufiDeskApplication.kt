package com.example

import android.app.Application
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
                val intent = Intent(this, CrashActivity::class.java).apply {
                    putExtra(CrashActivity.EXTRA_ERROR_MESSAGE, throwable.message ?: throwable.localizedMessage ?: throwable.javaClass.simpleName)
                    putExtra(CrashActivity.EXTRA_STACK_TRACE, Log.getStackTraceString(throwable))
                    putExtra(CrashActivity.EXTRA_THREAD_NAME, thread.name)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                }
                startActivity(intent)
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
