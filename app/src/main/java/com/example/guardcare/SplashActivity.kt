package com.example.guardcare

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen

class SplashActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper

    companion object {
        private const val PREFS_NAME = "AppPrefs"
        private const val KEY_FIRST_TIME = "isFirstTime"
        private const val SPLASH_DELAY_MS = 2000L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install splash screen
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        // Initialize DatabaseHelper
        dbHelper = DatabaseHelper(this)

        // Keep splash screen visible
        splashScreen.setKeepOnScreenCondition { true }

        Log.d("SplashActivity", "SplashActivity created")

        val prefs: SharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val isFirstTime = prefs.getBoolean(KEY_FIRST_TIME, true)

        Log.d("SplashActivity", "isFirstTime: $isFirstTime")

        Handler(Looper.getMainLooper()).postDelayed({
            navigateToAppropriateScreen(prefs, isFirstTime)
        }, SPLASH_DELAY_MS)
    }

    private fun navigateToAppropriateScreen(prefs: SharedPreferences, isFirstTime: Boolean) {
        try {
            val intent = when {
                // If it's the first time opening the app
                isFirstTime -> {
                    prefs.edit().putBoolean(KEY_FIRST_TIME, false).apply()
                    Intent(this, FirstPageActivity::class.java)
                }
                // Check if any user exists in the database
                !dbHelper.checkIfUserExists() -> {
                    Intent(this, FirstPageActivity::class.java)
                }
                // If user exists, but not logged in, go to login
                else -> {
                    Intent(this, LoginEmailActivity::class.java)
                }
            }

            startActivity(intent)
            finish()
        } catch (e: Exception) {
            Log.e("SplashActivity", "Error during navigation", e)
            // Fallback to FirstPageActivity in case of any error
            startActivity(Intent(this, FirstPageActivity::class.java))
            finish()
        }
    }
}