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
                !validPassword(password, confirmPassword) -> {
                    validationMessage.text = "Password is not valid."
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
                    validationMessage.text = ""

                    //User creation code goes here?
                    //This is where Firestore code must be input, Firestore will create the User
                    //Email & Pw in new User, userID auto genned

//                    val newUser = createUser(fullEmail, password)
//                    //Create profile of newUser, a new profile uses auto genned userID, fName, lName
//                    val newProfile = Profile(newUser.userID, firstName, lastName)
//                    println("New User created with Email : " + fullEmail + "Password : " + password)
//                    println("User Profile : " + firstName + " " + lastName + ", " + newProfile.userID)
//
//                    //Save User Data in Fake Repo - Temp solution till Firestore saves this data
//                    FakeUserDatabase.addUser(newUser, newProfile)
                    UserRepository.register(
                        firstName = firstName,
                        lastName = lastName,
                        email = fullEmail,
                        password = password
                    ) { success ->
                        if (success) {
                            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                        } else {
                            validationMessage.text = "Failed to create account."
                        }
                    }

                    //Login -> should only happen on login, not here...
                    //SessionManager.login(newUser, newProfile)



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