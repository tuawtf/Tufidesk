package com.example

import android.app.Application
import com.example.util.GlobalCrashHandler

class TufiDeskApp : Application() {

    override fun onCreate() {
        super.onCreate()
        // Install global crash handler at the earliest moment in the application lifecycle
        GlobalCrashHandler.install(this)
    }
}
