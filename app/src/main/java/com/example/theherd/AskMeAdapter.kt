package com.example.theherd

import android.app.AlertDialog
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//For the Ask me About section on the profile page
class AskMeAdapter(
    private val context: Context,
    private val topics: MutableList<String>
) : RecyclerView.Adapter<AskMeAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val topicText: TextView = view.findViewById(R.id.topicText)
        val editButton: ImageButton = view.findViewById(R.id.editTopic)
        val deleteButton: ImageButton = view.findViewById(R.id.deleteTopic)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_ask_me_topic, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.topicText.text = topics[position]

        // Edit topic
        holder.editButton.setOnClickListener {
            val editText = EditText(context)
            editText.inputType = InputType.TYPE_CLASS_TEXT
            editText.setText(topics[position])

            AlertDialog.Builder(context)
                .setTitle("Edit Topic")
                .setView(editText)
                .setPositiveButton("Save") { _, _ ->
                    val newTopic = editText.text.toString().trim()
                    if (newTopic.isNotEmpty()) {
                        topics[position] = newTopic
                        notifyItemChanged(position)
                    }
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        // Delete topic
        holder.deleteButton.setOnClickListener {
            topics.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, topics.size)
        }
    }

    override fun getItemCount(): Int = topics.size
}