package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login) // connects XML

        // text fields
        val emailUsernameField  = findViewById<EditText>(R.id.emailUsernameField)
        val passwordField = findViewById<EditText>(R.id.passwordField)

        // buttons
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)

        // validation field
        val validationField = findViewById<TextView>(R.id.validationMessage)

        // login button onclick listener
        loginButton.setOnClickListener {
            val emailUsername = emailUsernameField.text.toString()
            val password = passwordField.text.toString()

            // build full FSC email
            val email = "$emailUsername@farmingdale.edu"

            println("in loginButton event listener")
            when {
                emailUsername.isEmpty() || password.isEmpty() -> {
                    validationField.text = "Error: Please enter a username and password"
                }
                else -> {
                    validationField.text = ""

                    UserRepository.login(email, password) { success ->
                        if (success) {
                            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                            TopicRepository.initializeTestTopic() //Remove

                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            validationField.text = "Error: Invalid username or password"
                        }
                    }
                }
            }
        }

        // sign up button onclick listener
        signUpButton.setOnClickListener {
            println("in signUpButton event listener")
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

    }
}
