package com.example.guardcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginPhoneActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginphone)

        val dbHelper = DatabaseHelper(this)

        val phoneEditText = findViewById<EditText>(R.id.etLoginPhone)
        val passwordEditText = findViewById<EditText>(R.id.etPasswordLoginPhone)
        val loginButton = findViewById<Button>(R.id.btnLoginPhone)
        val useEmailButton = findViewById<Button>(R.id.tvUseEmail)
        val signupTextView = findViewById<TextView>(R.id.signupLink)

        // Handle Login Button Click
        loginButton.setOnClickListener {
            val phone = phoneEditText.text.toString()
            val password = passwordEditText.text.toString()

            if (phone.isNotEmpty() && password.isNotEmpty()) {
                val isValidUser = dbHelper.checkPhoneLogin(phone, password)

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
                Toast.makeText(this, "Please enter both phone number and password", Toast.LENGTH_SHORT).show()
            }
        }

        // Navigate to Email Login
        useEmailButton.setOnClickListener {
            val intent = Intent(this, LoginEmailActivity::class.java)
            startActivity(intent)
        }
        signupTextView.setOnClickListener {
            val intent = Intent(this, SignupActivity::class.java)
            startActivity(intent)
        }
    }
}
