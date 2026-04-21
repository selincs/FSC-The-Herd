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

class AskMeAdapter(
    private val context: Context,
    private val topics: MutableList<String>,
    private val isEditable: Boolean = true //was isEditing before merge
) : RecyclerView.Adapter<AskMeAdapter.ViewHolder>() {

    //merge comment out -- isEditable was isEditing previously
//    fun setEditMode(editing: Boolean) {
//        isEditing = editing
//        notifyDataSetChanged()
//    }

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
        //holder.topicText.text = topics[position]
        val currentPosition = holder.bindingAdapterPosition
        holder.topicText.text = topics[currentPosition]

        // ✅ Show/hide buttons based on edit mode
//        holder.editButton.visibility = if (isEditing) View.VISIBLE else View.GONE
//        holder.deleteButton.visibility = if (isEditing) View.VISIBLE else View.GONE
        // Edit topic
//        holder.editButton.setOnClickListener {
//            val editText = EditText(context)
//            editText.inputType = InputType.TYPE_CLASS_TEXT
//            editText.setText(topics[position]) .. AlertDialogBuilder(context).settitle...
// saving merge logic

        // read-only logic
        if (!isEditable) {
            holder.editButton.visibility = View.GONE
            holder.deleteButton.visibility = View.GONE
        } else {
            holder.editButton.visibility = View.VISIBLE
            holder.deleteButton.visibility = View.VISIBLE

            // Edit topic logic
            holder.editButton.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                val editText = EditText(context)
                editText.inputType = InputType.TYPE_CLASS_TEXT
                editText.setText(topics[pos])

                AlertDialog.Builder(context)
                    .setTitle("Edit Topic")
                    .setView(editText)
                    .setPositiveButton("Save") { _, _ ->
                        val newTopic = editText.text.toString().trim()
                        if (newTopic.isNotEmpty()) {
                            topics[pos] = newTopic
                            notifyItemChanged(pos)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            // Delete topic - Commented out during merging
//            holder.deleteButton.setOnClickListener {
//                topics.removeAt(position)
//                notifyItemRemoved(position)
//                notifyItemRangeChanged(position, topics.size)
//            }

            // Delete topic logic
            holder.deleteButton.setOnClickListener {
                val pos = holder.bindingAdapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    topics.removeAt(pos)
                    notifyItemRemoved(pos)
                    notifyItemRangeChanged(pos, topics.size)
                }
            }
        }
    }

    override fun getItemCount(): Int = topics.size
}