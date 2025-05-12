package com.example.guardcare

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class FirstPageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_firstpage)

        val signupButton = findViewById<Button>(R.id.btn_register_firstpage)
        val loginButton = findViewById<Button>(R.id.btn_login_firstpage)

        signupButton.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            finish()
        }

        loginButton.setOnClickListener {
            startActivity(Intent(this, LoginEmailActivity::class.java))
            finish()
        }
    }
}