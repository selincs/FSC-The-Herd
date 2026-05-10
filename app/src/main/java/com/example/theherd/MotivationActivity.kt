package com.example.theherd

import Model.MentorshipRoles
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup


class MotivationActivity : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_motivation) // connects XML
        setupNavigation() // sets up all buttons in the tool/nav bar

        val mentorsRecyclerView = findViewById<RecyclerView>(R.id.mentorsRecyclerView)
        val commitmentsRecyclerView = findViewById<RecyclerView>(R.id.commitmentsRecyclerView)
        val findMentorCard = findViewById<MaterialCardView>(R.id.findMentorCard)
        val becomeMentorCard = findViewById<MaterialCardView>(R.id.becomeMentorCard)

        mentorsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        commitmentsRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        //Fake Data -- Can remove when firebase is connected
        val fakeMentors = listOf(Mentor("Rachel Green", "Mentor"), Mentor("Ross Geller", "Mentor"), Mentor("Monica Geller", "Mentor"), Mentor("Chandler Bing", "Mentor"), Mentor("Joey Tribbiani", "Mentor"), Mentor("Phoebe Buffay", "Mentor"))
        val fakeCommitments = listOf(Commitment("Go to Gym", "Chandler", 3), Commitment("Study", "Monica", 5), Commitment("Go for a hike", "Joey", 9), Commitment("Self Defense", "Rachel", 15))

        findMentorCard.setOnClickListener {
            showMentorshipSignupDialog(MentorshipRoles.MENTEE)
        }

        becomeMentorCard.setOnClickListener {
            showMentorshipSignupDialog(MentorshipRoles.MENTOR)
        }

        mentorsRecyclerView.adapter = MentorAdapter(fakeMentors)

        commitmentsRecyclerView.adapter = CommitmentAdapter(fakeCommitments) { clickedCommitment ->
            val bottomSheet = BottomSheetDialog(this)
            bottomSheet.setContentView(R.layout.bottom_sheet_commitment)

            val titleText = bottomSheet.findViewById<TextView>(R.id.detailActivityName)
            val partnerText = bottomSheet.findViewById<TextView>(R.id.detailPartnerName)
            val streakNum = bottomSheet.findViewById<TextView>(R.id.detailStreakNumber)

            titleText?.text = clickedCommitment.activityName
            partnerText?.text = "Shared commitment with ${clickedCommitment.partnerName}"
            streakNum?.text = "🔥 ${clickedCommitment.streak} Days"

            bottomSheet.show()
        }

    }

    private fun showMentorshipSignupDialog(role: String) {
        val bottomSheet = BottomSheetDialog(this)

        bottomSheet.setContentView(R.layout.bottom_sheet_mentorship_signup)

        val titleText =
            bottomSheet.findViewById<TextView>(R.id.signupTitle)

        val bioEditText =
            bottomSheet.findViewById<EditText>(R.id.bioEditText)

        val majorEditText =
            bottomSheet.findViewById<EditText>(R.id.majorEditText)

        val yearSpinner =
            bottomSheet.findViewById<Spinner>(R.id.yearSpinner)

        val chipGroup =
            bottomSheet.findViewById<ChipGroup>(R.id.topicChipGroup)

        val commuterCheckbox =
            bottomSheet.findViewById<CheckBox>(R.id.commuterCheckbox)

        val transferCheckbox =
            bottomSheet.findViewById<CheckBox>(R.id.transferCheckbox)

        val signupButton =
            bottomSheet.findViewById<Button>(R.id.signupButton)

        // Dynamic title
        titleText?.text =
            if (role == MentorshipRoles.MENTOR)
                "Become a Mentor"
            else
                "Find a Mentor"

        // Spinner values
        val years = listOf(
            "Freshman",
            "Sophomore",
            "Junior",
            "Senior"
        )

        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            years
        )

        yearSpinner?.adapter = spinnerAdapter

        signupButton?.setOnClickListener {

            val selectedTopics = mutableListOf<String>()

            chipGroup?.checkedChipIds?.forEach { chipId ->

                val chip =
                    chipGroup.findViewById<Chip>(chipId)

                selectedTopics.add(chip.text.toString())
            }

            val username = FirestoreAuthManager.auth.currentUser
                ?.email
                ?.substringBefore("@")
                ?: "UnknownUser"

            // FIRST load existing profile
            MotivationRepository.getMentorshipProfile(

                onSuccess = { existingProfile ->

                    // Preserve old roles
                    val updatedRoles =
                        (existingProfile?.roles ?: emptyList())
                            .toMutableSet()

                    // Add new role if not already present
                    updatedRoles.add(role)

                    // Create updated profile
                    val profile = MentorshipProfile(

                        username = username,

                        roles = updatedRoles.toList(),

                        mentorshipTopics = selectedTopics,

                        bio = bioEditText?.text.toString()?.trim() ?: "",

                        major = majorEditText?.text.toString()?.trim() ?: "",

                        year = yearSpinner?.selectedItem.toString(),

                        commuter = commuterCheckbox?.isChecked ?: false,

                        transferStudent =
                            transferCheckbox?.isChecked ?: false,

                        active = true
                    )

                    // Save updated profile
                    MotivationRepository.createOrUpdateMentorshipProfile(
                        profile,

                        onSuccess = {

                            Toast.makeText(
                                this,
                                "Mentorship profile created!",
                                Toast.LENGTH_SHORT
                            ).show()

                            bottomSheet.dismiss()
                        },

                        onFailure = {

                            Toast.makeText(
                                this,
                                "Failed to create profile",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                },

                onFailure = {

                    Toast.makeText(
                        this,
                        "Failed to load existing profile",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }

        bottomSheet.show()
    }
}
// data class Mentor(val name: String, val role: String)