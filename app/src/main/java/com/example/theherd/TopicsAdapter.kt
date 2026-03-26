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
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TopicViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.topic_item, parent, false)
        return TopicViewHolder(view)
    }

    override fun onBindViewHolder(holder: TopicViewHolder, position: Int) {
        val topic = topics[position]
        holder.name.text = topic.topicName
        holder.description.text = topic.topicDesc
        holder.members.text = "${topic.memberCount} members"

        // Display uploaded image if exists, else default drawable
        if (topic.getImageUriString() != null) {
            holder.image.setImageURI(Uri.parse(topic.getImageUriString()))
        } else {
            holder.image.setImageResource(topic.getImageResId())
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