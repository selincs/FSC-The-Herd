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
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)

        // buttons
        val loginButton = findViewById<Button>(R.id.loginButton)
        val signUpButton = findViewById<Button>(R.id.signUpButton)

        // validation field
        val validationField = findViewById<TextView>(R.id.validationMessage)

        // login button onclick listener
        loginButton.setOnClickListener {
            val email = emailField.text.toString()
            val password = passwordField.text.toString()

            println("in loginButton event listener")
            when {
                email.isEmpty() || password.isEmpty() -> {
                    validationField.text = "Error: Please enter a username and password"
                }
                !validLogin(email, password) -> {
                    validationField.text = "Error: Invalid username or password"
                }
                else -> {
                    validationField.text = ""
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                    //Selin Entry
                    // > Get the user from FakeUserDatabase
                    val user = FakeUserDatabase.findUserByEmail(email)
                    println("User found in Login DB : " + user.toString())

                    // > Get the user's profile using their userID
                    val profile = user?.let {
                        FakeUserDatabase.getProfileByUserId(it.getUserID())
                    }
                    println("Logging in to User profile : " + profile.toString())

                    // > Only login if both profile and class exist, otherwise null issue somewhere
                    if (user != null && profile != null) {
                        SessionManager.login(user, profile)
                        //Retrieving user info from Session Manager
                        println("Logging in to : " + SessionManager.getUser().toString())
                        println("Logged in User full name : " + SessionManager.getProfile().toString())
                    }

                    //End selin entry

                    println("before creating intent:")
                    val intent = Intent(this, MainActivity::class.java)
                    println("before startActivity:")
                    startActivity(intent)
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

    /**
     * validEmail: checks if the user entered a valid email
     */
    fun validLogin(email: String, password: String): Boolean {
        // if (email.isEmpty() || )
//        return true
        return FakeUserDatabase.validateLogin(email, password) //Selin entry- temp
    }

}
