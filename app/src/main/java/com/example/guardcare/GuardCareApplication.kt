package com.example.guardcare

import android.app.Application

class GuardCareApplication : Application() {
    var splashActivity: SplashActivity? = null

    override fun onCreate() {
        super.onCreate()
        // Any global initialization can go here
    }
}