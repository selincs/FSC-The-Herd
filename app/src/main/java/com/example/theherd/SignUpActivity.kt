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
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.core.content.ContextCompat

class SignUpActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up) // connects XML

        findViewById<ConstraintLayout>(R.id.signUpLayout).setOnClickListener {
            println("The BACKGROUND was clicked")
        }

        val reqLength = findViewById<TextView>(R.id.reqLength)
        val reqUpper = findViewById<TextView>(R.id.reqUpper)
        val reqLower = findViewById<TextView>(R.id.reqLower)
        val reqDigit = findViewById<TextView>(R.id.reqDigit)
        val reqMatch = findViewById<TextView>(R.id.reqMatch)

        val green = ContextCompat.getColor(this, android.R.color.holo_green_dark)
        val gray = ContextCompat.getColor(this, android.R.color.darker_gray)

        // text fields
        val firstNameField = findViewById<EditText>(R.id.firstNameField)
        val lastNameField = findViewById<EditText>(R.id.lastNameField)
        val emailUsernameField = findViewById<EditText>(R.id.emailUsernameField)
        val passwordField = findViewById<EditText>(R.id.passwordField)
        val confirmedPasswordField = findViewById<EditText>(R.id.confirmPasswordField)

        // Graduation spinners
        val graduationSeasonSpinner = findViewById<Spinner>(R.id.graduationSeasonSpinner)
        val graduationYearSpinner = findViewById<Spinner>(R.id.graduationYearSpinner)

        // Seasons with placeholder
        val seasons = listOf("Season", "Fall", "Spring", "Summer", "Winter")
        val seasonAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, seasons)
        seasonAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        graduationSeasonSpinner.adapter = seasonAdapter
        graduationSeasonSpinner.setSelection(0) // show placeholder by default

        // Years with placeholder
        val years = listOf("Year") + (2026..2030).map { it.toString() }
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        graduationYearSpinner.adapter = yearAdapter
        graduationYearSpinner.setSelection(0) // show placeholder by default

        // buttons
        val createAccountButton = findViewById<Button>(R.id.createAccountButton)
        val loginButton = findViewById<Button>(R.id.loginButton)

        // validation message
        val validationMessage: TextView = findViewById(R.id.validationMessage)

        fun updateChecklist() {
            val pass = passwordField.text.toString()
            val confirm = confirmedPasswordField.text.toString()

            // Length check
            reqLength.setTextColor(if (pass.length >= 8) green else gray)

            // Uppercase check
            reqUpper.setTextColor(if (pass.any { it.isUpperCase() }) green else gray)

            // Lowercase check
            reqLower.setTextColor(if (pass.any { it.isLowerCase() }) green else gray)

            // Digit check
            reqDigit.setTextColor(if (pass.any { it.isDigit() }) green else gray)

            // Match check
            reqMatch.setTextColor(if (pass.isNotEmpty() && pass == confirm) green else gray)
        }

        val passwordWatcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                updateChecklist()
            }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }

        passwordField.addTextChangedListener(passwordWatcher)
        confirmedPasswordField.addTextChangedListener(passwordWatcher)

        // create account button onclick listener
        createAccountButton.setOnClickListener {
            println("in create account button onclick listener")
            val firstName = firstNameField.text.toString()
            val lastName = lastNameField.text.toString()
            val emailUsername = emailUsernameField.text.toString().trim()
            val password = passwordField.text.toString()
            val confirmPassword = confirmedPasswordField.text.toString()
            val selectedSeason = graduationSeasonSpinner.selectedItem.toString()
            val selectedYear = graduationYearSpinner.selectedItem.toString()
            val validPass = validPassword(password, confirmPassword)

            // Combine username with fixed domain
            val fullEmail = "$emailUsername@farmingdale.edu"

            when {
                // if any fields are empty, notify user
                firstName.isEmpty() || lastName.isEmpty() ||  emailUsername.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() -> {
                    validationMessage.text = "Please fill out all fields."
                }
                !validEmail(fullEmail) -> {
                    validationMessage.text = "Email is not valid. Please enter your Farmingdale email username."
                }
                // if password is not valid, notify user
                !validPass-> {
                    validationMessage.text = "Password is invalid."
                }
                //If season placeholder selected
                selectedSeason == "Season" -> {
                    validationMessage.text = "Please select your expected graduation season."
                }
                //If year placeholder selected
                selectedYear == "Year" -> {
                    validationMessage.text = "Please select your expected graduation year."
                }
                else -> {
                    println("account created!!!")
                    validationMessage.text = ""

                    val graduationDate = "$selectedSeason $selectedYear"

                    //Calls register() in user repo with the below parameters in constructor
                    UserRepository.register(
                        firstName = firstName,
                        lastName = lastName,
                        email = fullEmail,
                        password = password,
                        graduationDate = graduationDate
                    ) { success ->

                        if (success) {

                            //Remove PrefMgr saves here once Firestore can do this job
                            PreferencesManager.saveFullName(this, firstName, lastName)
                            PreferencesManager.saveUsername(this, emailUsername)
                            PreferencesManager.saveGradYear(this, selectedYear)

                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()

                            startActivity(Intent(this, LoginActivity::class.java))
                            finish()

                        } else {
                            validationMessage.text = "Failed to create account."
                        }
                    }
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
        return email.endsWith("@farmingdale.edu")
    }

    /**
     * valid password: checks if password is valid or not
     */
    fun validPassword(pass: String, confirm: String): Boolean {
        return pass.length >= 8 &&
                pass.any { it.isUpperCase() } &&
                pass.any { it.isLowerCase() } &&
                pass.any { it.isDigit() } &&
                pass == confirm
    }

    /**
     * hasUpperCase: checks if the password contains any uppercase letters
     */
    private fun hasUpperCase(str: String): Boolean {
        return str.any { it.isUpperCase() }
    }

    /**
     * hasLowerCase: checks if the password contains any lowercase letters
     */
    private fun hasLowerCase(str: String): Boolean {
        return str.any { it.isLowerCase() }
    }

    /**
     * hasDigit: checks if the password contains any digits
     */
    private fun hasDigit(str: String): Boolean {
        return str.any { it.isDigit() }
    }

    private fun hasSpecialChar(str: String): Boolean {
        return str.any { it in """"!\"#\$%&'()*+,-./:;<=>?@[\\]^_`{|}~""""}
    }
}