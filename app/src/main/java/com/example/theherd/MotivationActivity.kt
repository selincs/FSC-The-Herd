package com.example.theherd

import Model.MentorshipRoles
import android.os.Bundle
import android.widget.TextView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import Model.Commitment
import com.google.android.material.button.MaterialButton

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
        val fakeMentors = mutableListOf(
            Mentor("Rachel Green", "Mentor"),
            Mentor("Ross Geller", "Mentor"),
            Mentor("Monica Geller", "Mentor"),
            Mentor("Chandler Bing", "Mentor"),
            Mentor("Joey Tribbiani", "Mentor"),
            Mentor("Phoebe Buffay", "Mentor")
        )

//        val fakeCommitments = mutableListOf(
//            Commitment("Go to Gym", "Chandler", 3, 1),
//            Commitment("Study", "Monica", 5, 1),
//            Commitment("Go for a hike", "Joey", 9, 1),
//            Commitment("Self Defense", "Rachel", 8, 1)
//        )

        val commitments = mutableListOf<Commitment>()



        findMentorCard.setOnClickListener {
            showMentorshipSignupDialog(MentorshipRoles.MENTEE)
        }

        becomeMentorCard.setOnClickListener {
            showMentorshipSignupDialog(MentorshipRoles.MENTOR)
        }

        mentorsRecyclerView.adapter = MentorAdapter(fakeMentors)

        lateinit var commitmentAdapter: CommitmentAdapter

        commitmentAdapter =
            CommitmentAdapter(commitments) { clickedCommitment ->

                val bottomSheet = BottomSheetDialog(this)

                bottomSheet.setContentView(
                    R.layout.bottom_sheet_commitment
                )

                val titleText =
                    bottomSheet.findViewById<TextView>(
                        R.id.detailActivityName
                    )

                val partnerText =
                    bottomSheet.findViewById<TextView>(
                        R.id.detailPartnerName
                    )

                val streakNum =
                    bottomSheet.findViewById<TextView>(
                        R.id.detailStreakNumber
                    )

                val levelText =
                    bottomSheet.findViewById<TextView>(
                        R.id.detailLevelText
                    )

                val completeBtn =
                    bottomSheet.findViewById<Button>(
                        R.id.btnMarkComplete
                    )

                titleText?.text =
                    clickedCommitment.activityName

                if (clickedCommitment.partnerName.isEmpty()) {

                    partnerText?.text = "Going solo"

                } else {

                    partnerText?.text =
                        "Shared commitment with ${clickedCommitment.partnerName}"
                }

                streakNum?.text =
                    "🔥 ${clickedCommitment.streak} Days"

                levelText?.text =
                    "Level ${clickedCommitment.level}"

                completeBtn?.setOnClickListener {

                    clickedCommitment.streak += 1

                    if (clickedCommitment.streak >= 10) {

                        clickedCommitment.streak = 0

                        clickedCommitment.level += 1
                    }

                    MotivationRepository.updateCommitment(

                        clickedCommitment,

                        onSuccess = {

                            streakNum?.text =
                                "🔥 ${clickedCommitment.streak} Days"

                            levelText?.text =
                                "Level ${clickedCommitment.level}"

                            commitmentAdapter.notifyDataSetChanged()

                            bottomSheet.dismiss()
                        },

                        onFailure = {

                            Toast.makeText(
                                this,
                                "Failed to update streak",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )
                }

                bottomSheet.show()
            }

        commitmentsRecyclerView.adapter = commitmentAdapter

        MotivationRepository.getUserCommitments(

            onSuccess = { loadedCommitments ->

                commitments.clear()

                commitments.addAll(loadedCommitments)

                commitmentAdapter.notifyDataSetChanged()
            },

            onFailure = {

                Toast.makeText(
                    this,
                    "Failed to load commitments",
                    Toast.LENGTH_SHORT
                ).show()
            }
        )


        val addCommitmentBtn = findViewById<MaterialButton>(R.id.fabAddCommitment)

        addCommitmentBtn.setOnClickListener {
            val newGoalSheet = BottomSheetDialog(this)
            newGoalSheet.setContentView(R.layout.bottom_sheet_new_commitment)

            val inputActivity = newGoalSheet.findViewById<EditText>(R.id.inputActivityName)
            val inputPartner = newGoalSheet.findViewById<EditText>(R.id.inputPartnerName)
            val btnSave = newGoalSheet.findViewById<Button>(R.id.btnSaveCommitment)

            btnSave?.setOnClickListener {

                val activityText = inputActivity?.text.toString()
                val partnerText = inputPartner?.text.toString().trim()

                if (activityText.isNotEmpty()) {

                    val newGoal = Commitment(

                        activityName = activityText,

                        partnerName = partnerText,

                        streak = 0,

                        level = 1
                    )

                    MotivationRepository.createCommitment(

                        newGoal,

                        onSuccess = { savedCommitment ->

                            commitments.add(savedCommitment)

                            commitmentAdapter.notifyDataSetChanged()

                            commitmentsRecyclerView.scrollToPosition(
                                commitments.size - 1
                            )

                            newGoalSheet.dismiss()
                        },

                        onFailure = {

                            Toast.makeText(
                                this,
                                "Failed to save commitment",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    )

                } else {
                    Toast.makeText(this, "Please name your new goal!", Toast.LENGTH_SHORT).show()
                }
            }

            newGoalSheet.show()
        }
    }

    private fun showMentorshipSignupDialog(role: String) {
        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(R.layout.bottom_sheet_mentorship_signup)

        //Set up Mentorship GUI fields
        val titleText = bottomSheet.findViewById<TextView>(R.id.signupTitle)
        val bioEditText = bottomSheet.findViewById<EditText>(R.id.bioEditText)
        val majorEditText = bottomSheet.findViewById<EditText>(R.id.majorEditText)
        val yearSpinner = bottomSheet.findViewById<Spinner>(R.id.yearSpinner)
        val chipGroup = bottomSheet.findViewById<ChipGroup>(R.id.topicChipGroup)
        val commuterCheckbox = bottomSheet.findViewById<CheckBox>(R.id.commuterCheckbox)
        val transferCheckbox = bottomSheet.findViewById<CheckBox>(R.id.transferCheckbox)
        val signupButton = bottomSheet.findViewById<Button>(R.id.signupButton)
        val cancelSignupButton = bottomSheet.findViewById<Button>(R.id.cancelSignupButton)

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

        val spinnerAdapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,years)
        yearSpinner?.adapter = spinnerAdapter

        // Load existing profile data into UI
        MotivationRepository.getMentorshipProfile(

            onSuccess = { existingProfile ->

                existingProfile?.let { profile ->

                    // Shared fields
                    majorEditText?.setText(profile.major)

                    commuterCheckbox?.isChecked =
                        profile.commuter

                    transferCheckbox?.isChecked =
                        profile.transferStudent

                    // Restore spinner selection
                    val yearPosition =
                        years.indexOf(profile.year)

                    if (yearPosition >= 0) {
                        yearSpinner?.setSelection(yearPosition)
                    }

                    // Role-specific bio/topics
                    val existingBio =
                        if (role == MentorshipRoles.MENTOR)
                            profile.mentorBio
                        else
                            profile.menteeBio

                    val existingTopics =
                        if (role == MentorshipRoles.MENTOR)
                            profile.mentorTopics
                        else
                            profile.menteeTopics

                    bioEditText?.setText(existingBio)

                    // Restore selected chips
                    for (i in 0 until chipGroup!!.childCount) {

                        val chip =
                            chipGroup.getChildAt(i) as? Chip

                        chip?.isChecked =
                            existingTopics.contains(
                                chip.text.toString()
                            )
                    }
                }
            },

            onFailure = {
                println("Failure in loading, user may not have profile yet. Verify showMentorshipSignupDialog")
                // Silent fail is okay here
                // User may simply not have a profile yet
            }
        )

        signupButton?.setOnClickListener {
            val selectedTopics = mutableListOf<String>()
            chipGroup?.checkedChipIds?.forEach { chipId ->
                val chip = chipGroup.findViewById<Chip>(chipId)
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
                    val updatedRoles =(existingProfile?.roles ?: emptyList()).toMutableSet()

                    // Add new role if not already present
                    updatedRoles.add(role)

                    // Create updated profile
                    val profile = MentorshipProfile(
                        username = username,
                        roles = updatedRoles.toList(),

                        mentorTopics =
                            if (role == MentorshipRoles.MENTOR)
                                selectedTopics
                            else
                                existingProfile?.mentorTopics ?: emptyList(),

                        menteeTopics =
                            if (role == MentorshipRoles.MENTEE)
                                selectedTopics
                            else
                                existingProfile?.menteeTopics ?: emptyList(),

                        mentorBio =
                            if (role == MentorshipRoles.MENTOR)
                                bioEditText?.text.toString()?.trim() ?: ""
                            else
                                existingProfile?.mentorBio ?: "",

                        menteeBio =
                            if (role == MentorshipRoles.MENTEE)
                                bioEditText?.text.toString()?.trim() ?: ""
                            else
                                existingProfile?.menteeBio ?: "",

                        major = majorEditText?.text.toString()?.trim() ?: "",
                        year = yearSpinner?.selectedItem.toString(),
                        commuter = commuterCheckbox?.isChecked ?: false,
                        transferStudent =transferCheckbox?.isChecked ?: false,
                        active = true
                    )

                    // Save updated profile
                    MotivationRepository.createOrUpdateMentorshipProfile(
                        profile,

                        onSuccess = {
                            Toast.makeText(this,"Mentorship profile created!",Toast.LENGTH_SHORT
                            ).show()
                            bottomSheet.dismiss()
                        },

                        onFailure = {
                            Toast.makeText(this,"Failed to create profile",Toast.LENGTH_SHORT).show()
                        }
                    )
                },

                onFailure = {
                    Toast.makeText(this,"Failed to load existing profile",Toast.LENGTH_SHORT).show()
                }
            )
        }

        //Clear out Mentorship Enrollment by Role
        cancelSignupButton?.setOnClickListener {
            MotivationRepository.getMentorshipProfile(

                onSuccess = { existingProfile ->
                    if (existingProfile == null) {
                        Toast.makeText(this,"No mentorship profile found",Toast.LENGTH_SHORT).show()
                        return@getMentorshipProfile
                    }

                    // Remove current role
                    val updatedRoles =existingProfile.roles.toMutableList()
                    updatedRoles.remove(role)

                    // Clear ONLY fields related to this role
                    val updatedProfile = existingProfile.copy(
                        roles = updatedRoles,

                        mentorTopics =
                            if (role == MentorshipRoles.MENTOR)
                                emptyList()
                            else
                                existingProfile.mentorTopics,

                        menteeTopics =
                            if (role == MentorshipRoles.MENTEE)
                                emptyList()
                            else
                                existingProfile.menteeTopics,

                        mentorBio =
                            if (role == MentorshipRoles.MENTOR)
                                ""
                            else
                                existingProfile.mentorBio,

                        menteeBio =
                            if (role == MentorshipRoles.MENTEE)
                                ""
                            else
                                existingProfile.menteeBio
                    )

                    MotivationRepository.createOrUpdateMentorshipProfile(
                        updatedProfile,
                        onSuccess = {
                            Toast.makeText( this, "Sign-up cancelled", Toast.LENGTH_SHORT).show()
                            bottomSheet.dismiss()
                        },

                        onFailure = {
                            Toast.makeText(this,"Failed to cancel sign-up",Toast.LENGTH_SHORT).show()
                        }
                    )
                },

                onFailure = {
                    Toast.makeText(this,"Failed to load profile",Toast.LENGTH_SHORT).show()
                }
            )
        }
        //Show mentorship bottom sheet
        bottomSheet.show()
    }
}
// data class Mentor(val name: String, val role: String) -> Only used for fake mentor displays currently