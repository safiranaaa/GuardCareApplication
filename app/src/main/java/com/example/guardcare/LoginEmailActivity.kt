package com.example.guardcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginEmailActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginemail)

        val dbHelper = DatabaseHelper(this)

        val emailOrPhoneEditText = findViewById<EditText>(R.id.etLoginEmail)
        val passwordEditText = findViewById<EditText>(R.id.etPasswordLoginEmail)
        val loginButton = findViewById<Button>(R.id.btnLoginEmail)
        val usePhoneButton = findViewById<Button>(R.id.tvUsePhone)
        val signupTextView = findViewById<TextView>(R.id.signupLink)

        // Handle Login Button Click
        loginButton.setOnClickListener {
            val emailOrPhone = emailOrPhoneEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (emailOrPhone.isNotEmpty() && password.isNotEmpty()) {
                val isValidUser = if (emailOrPhone.contains("@")) {
                    // Login with Email
                    dbHelper.checkEmailLogin(emailOrPhone, password)
                } else {
                    // Login with Phone
                    dbHelper.checkPhoneLogin(emailOrPhone, password)
                }

                if (isValidUser) {
                    // Successful login, navigate to MainActivity
                    Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // Close the Login Screen
                } else {
                    // Invalid credentials
                    Toast.makeText(this, "Invalid credentials, please try again", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter both email/phone and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to Phone Login
        usePhoneButton.setOnClickListener {
            val intent = Intent(this, LoginPhoneActivity::class.java)
            startActivity(intent)
        }

        signupTextView.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}
