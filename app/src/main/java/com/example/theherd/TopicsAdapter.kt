package com.example.theherd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import Model.Topic
import android.net.Uri
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import android.widget.ImageButton
import androidx.core.net.toUri
import android.content.Intent

// to populate topic bubbles in topicActivity
class TopicsAdapter(private val allTopics: List<Topic>) :
    RecyclerView.Adapter<TopicsAdapter.TopicViewHolder>() {

    private var topics: List<Topic> = allTopics.toList()

    class TopicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.topicName)
        val description: TextView = itemView.findViewById(R.id.topicDescription)
        val members: TextView = itemView.findViewById(R.id.topicMembers)
        val image: ImageView = itemView.findViewById(R.id.topicImage)
        val joinButton: Button = itemView.findViewById(R.id.joinButton)
        val reportButton: ImageButton = itemView.findViewById(R.id.reportButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.topic_item, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        holder.reportButton.setOnClickListener {

            val context = holder.itemView.context
            val dialogView = LayoutInflater.from(context)
                .inflate(R.layout.dialog_report_topic, null)

            val spinner = dialogView.findViewById<Spinner>(R.id.reportReasonSpinner)
            val comment = dialogView.findViewById<EditText>(R.id.reportComment)

            // Report reasons
            val reasons = listOf("Spam", "Inappropriate Content", "Harassment", "Other")

            val adapter = ArrayAdapter(
                context,
                android.R.layout.simple_spinner_dropdown_item,
                reasons
            )
            spinner.adapter = adapter

            AlertDialog.Builder(context)
                .setTitle("Report Topic")
                .setView(dialogView)
                .setPositiveButton("Submit") { _, _ ->

                    val selectedReason = spinner.selectedItem.toString()
                    val userComment = comment.text.toString()

                    // write implementation for backend here (Topic Reporting)

                    Toast.makeText(
                        context,
                        "Topic reported",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        val topic = topics[position]
        holder.name.text = topic.topicName
        holder.description.text = topic.topicDesc
        holder.members.text = "${topic.memberCount} members"

        //TODO: This is merged from AvSpr5, check if needed
        //when a topic is clicked
//        holder.itemView.setOnClickListener {
//            val context = holder.itemView.context
//            val intent = Intent(context, TopicDetailActivity::class.java)
//
//            // Pass topic data
//            intent.putExtra("topicName", topic.topicName)
//            intent.putExtra("topicDesc", topic.topicDesc)
//            intent.putExtra("memberCount", topic.memberCount)
//
//            context.startActivity(intent)
//        }

        // Display uploaded imageUri if exists in Firestore, else default drawable
        val imageUriString = topic.getImageUriString()
        println("Topic imageUri from Firestore: $imageUriString")

        try {
            when {
                imageUriString == null || imageUriString == "default" -> {
                    // Default image for no selection (The Herd logo)
                    println("Default image used")
                    holder.image.setImageResource(R.drawable.marquee_logo)
                }

                // User uploaded an image -> show it
                println("User image set")
                holder.image.setImageURI(Uri.parse(imageUriString))
            }
        } else {
            // No image uploaded -> show default logo
            println("Default set in Topics Adapter else block")
                imageUriString.startsWith("content://") -> {
                    // Local image chosen by user was stored, load the local device image
                    val uri = imageUriString.toUri()
                    holder.image.setImageURI(uri)
                    println("Loaded local content URI image")
                }

                imageUriString.startsWith("http") -> {
                    // Firebase storage URL case, but this doesn't work unless we pay $$$ I think
                    val uri = imageUriString.toUri()
                    holder.image.setImageURI(uri)
                    println("Loaded remote image URL")
                }

                else -> {
                    println("Unknown image format - TopicsAdapter")
                    holder.image.setImageResource(R.drawable.marquee_logo)
                }
            }
        } catch (e: Exception) {
            println("Image loading failed-Topics Adapter: ${e.message}")
            holder.image.setImageResource(R.drawable.marquee_logo)
        }

        //Backend implementation for join button goes in here, at increment line
//        holder.joinButton.setOnClickListener {
//            TopicRepository.joinTopic(topic.topicID) { success ->
//                if (success) {
//                    topic.incrementMembers()
//                    holder.members.text = "${topic.memberCount} members"
//                    holder.joinButton.text = "Joined"
//
//                    Toast.makeText(holder.itemView.context,
//                        "You joined ${topic.topicName}!", Toast.LENGTH_SHORT).show()
//                } else {
//                    Toast.makeText(holder.itemView.context, "Failed to join", Toast.LENGTH_SHORT).show()
//                }
//            }
//        }

        // ---------------- JOIN / UNJOIN ----------------
        fun updateButtonUI() {
            if (topic.isJoined) {
                holder.joinButton.text = "Joined"
                holder.joinButton.setBackgroundColor(
                    android.graphics.Color.parseColor("#2F442F")
                )
            } else {
                holder.joinButton.text = "Join"
                holder.joinButton.setBackgroundColor(
                    android.graphics.Color.GRAY
                )
            }
            holder.joinButton.setTextColor(android.graphics.Color.WHITE)
        }

        // Set initial UI state
        updateButtonUI()

        holder.joinButton.setOnClickListener {
            if (!topic.isJoined) {
                topic.incrementMembers()
                topic.isJoined = true

                Toast.makeText(
                    holder.itemView.context,
                    "You joined ${topic.topicName}!",
                    Toast.LENGTH_SHORT
                ).show()

            } else {
                topic.decrementMembers()
                topic.isJoined = false

                Toast.makeText(
                    holder.itemView.context,
                    "You left ${topic.topicName}!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Update UI
            holder.members.text = "${topic.memberCount} members"
            updateButtonUI()
        }
    }


        override fun getItemCount(): Int = topics.size

    // Search filter
    fun filter(query: String) {
        topics = if (query.isEmpty()) {
            allTopics.toList()
        } else {
            allTopics.filter { it.topicName.contains(query, ignoreCase = true) }
        }
        notifyDataSetChanged()
    }

    fun updateList(newList: List<Topic>) {
        topics = newList
        notifyDataSetChanged()
    }
}