package com.example.guardcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SignupActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        val dbHelper = DatabaseHelper(this)

        val emailEditText = findViewById<EditText>(R.id.etEmailEditText)
        val phoneEditText = findViewById<EditText>(R.id.etPhoneEditText)
        val passwordEditText = findViewById<EditText>(R.id.etPasswordSignup)
        val signupButton = findViewById<Button>(R.id.btnSignup)
        val loginTextView = findViewById<TextView>(R.id.tvLogin)

        signupButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val phone = phoneEditText.text.toString()
            val password = passwordEditText.text.toString()

            when {
                email.isEmpty() -> {
                    Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                phone.isEmpty() -> {
                    Toast.makeText(this, "Please enter phone number", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.isEmpty() -> {
                    Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                password.length < 6 -> {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }
            // Insert the new user into the database
            val result = dbHelper.insertUser(email, phone, password)

            if (result != -1L) {
                // Account created successfully
                Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()

                // Navigate to MainActivity
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                finish()
            } else {
                // Error creating account (likely due to duplicate email or phone)
                Toast.makeText(this, "Error: Could not create account. Email or phone may already exist.", Toast.LENGTH_SHORT).show()
            }
        }
        loginTextView.setOnClickListener {
            val intent = Intent(this, LoginEmailActivity::class.java)
            startActivity(intent)
        }
    }
}
