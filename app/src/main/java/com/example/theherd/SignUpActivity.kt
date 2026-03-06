package com.example.theherd

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import Model.User
import Model.Profile

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up) // connects XML

        findViewById<ConstraintLayout>(R.id.signUpLayout).setOnClickListener {
            println("The BACKGROUND was clicked")
        }

        // text fields
        val firstNameField = findViewById<EditText>(R.id.firstNameField)
        val lastNameField = findViewById<EditText>(R.id.lastNameField)
        val emailField = findViewById<EditText>(R.id.emailField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val confirmedPasswordField = findViewById<EditText>(R.id.confirmPasswordField)

        // buttons
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // validation message
        val validationMessage: TextView = findViewById(R.id.validationMessage)

        // create account button onclick listener
        createAccountButton.setOnClickListener {
            println("in create account button onclick listener")
            val firstName = firstNameField.text.toString()
            val lastName = lastNameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val confirmPassword = confirmedPasswordField.text.toString()

            when {
                // if any fields are empty, notify user
                firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    validationMessage.text = "Please fill out all fields."
                }
                !validEmail(email) -> {
                    validationMessage.text = "Email is not valid. Please enter your Farmingdale email address."
                }
                // if password is not valid, notify user
                !validPassword(password, confirmPassword) -> {
                    validationMessage.text = "Password is not valid."
                }
                else -> {
                    validationMessage.text = ""

                    //User creation code goes here?
                    //This is where Firestore code must be input, Firestore will create the User
                    //Email & Pw in new User, userID auto genned
                    val newUser = createUser(email, password)
                    //Create profile of newUser, a new profile uses auto genned userID, fName, lName
                    val newProfile = Profile(newUser.userID, firstName, lastName)
                    println("New User created with Email : " + email + "Password : " + password)
                    println("User Profile : " + firstName + " " + lastName + ", " + newProfile.userID)

                    //Save User Data in Fake Repo - Temp solution till Firestore saves this data
                    FakeUserDatabase.addUser(newUser, newProfile)

                    //Login -> should only happen on login, not here...
                    //SessionManager.login(newUser, newProfile)

                    Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)

                    startActivity(intent)
                }
            }
        }

        // login button event listener
        loginButton.setOnClickListener {
            println("in login button event listener")
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * Private func to create a User account - Temporary until Firestore integration
     */
    private fun createUser(email: String, password: String): User {
        //Profile?
        return User(email, password)
    }

    /**
     * valid email: checks if email is valid or not
     */
    fun validEmail(email: String): Boolean {
        if (!email.endsWith("@farmingdale.edu"))
            return false
        return true
    }

    /**
     * valid password: checks if password is valid or not
     */
    fun validPassword(pass: String, confirm: String): Boolean {
        if (pass != confirm)
            return false
        if (pass.length < 8)
            return false
        return true
    }
}