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

        //Selin entry - Hope this works avnoor sorry
        // Display uploaded image if exists, else default drawable
        val imageUriString = topic.getImageUriString()
        if (imageUriString != null && imageUriString != "default") {
            //Accommodate hard coded sample topics - Remove when backend works starting w/ this comment
            if (imageUriString == R.drawable.gym.toString()) {
                holder.image.setImageResource(R.drawable.gym)
            } else if (imageUriString == R.drawable.chess.toString()) {
                holder.image.setImageResource(R.drawable.chess)
            } else if (imageUriString == R.drawable.hiking.toString()) {
                holder.image.setImageResource(R.drawable.hiking)
            } else if (imageUriString == R.drawable.food.toString()) {
                holder.image.setImageResource(R.drawable.food)
            }
            else {  //When the above if/else ifs are removed, leave this as the only code in the block
                //Remove up to here and the corresponding closing bracket for the else statement

                // User uploaded an image -> show it
                try {
                    holder.image.setImageURI(Uri.parse(imageUriString))
                    println("User image set")
                }
                catch (e: Exception) {
                    println("URI failed to load")
                    holder.image.setImageResource(R.drawable.marquee_logo)
                }
            }
        }

        else {
            // No image uploaded -> show default logo
            println("Default set in Topics Adapter else block")
            holder.image.setImageResource(R.drawable.marquee_logo)
        }

        holder.joinButton.setOnClickListener {
            topic.incrementMembers()
            holder.members.text = "${topic.memberCount} members"
            holder.joinButton.text = "Joined"
            Toast.makeText(holder.itemView.context,
                "You joined ${topic.topicName}!", Toast.LENGTH_SHORT).show()
        }

        holder.joinButton.setBackgroundColor(
            android.graphics.Color.parseColor("#2F442F") //green btn
        )
        holder.joinButton.setTextColor(android.graphics.Color.WHITE)
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