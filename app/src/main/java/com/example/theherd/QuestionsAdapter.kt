package com.example.theherd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.theherd.R
import android.text.format.DateUtils
import com.google.firebase.Timestamp

class QuestionsAdapter(
    private var questionsList: List<Map<String,Any>>,
    private val onQuestionClicked: (Map<String,Any>) -> Unit
) :
    RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder>() {


    class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUser: TextView = itemView.findViewById(R.id.tvQuestionUser)
        val tvText: TextView = itemView.findViewById(R.id.tvQuestionText)
        val tvTime: TextView = itemView.findViewById(R.id.tvQuestionTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view)
    }


    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val data = questionsList[position]

        holder.tvUser.text = data["username"]?.toString() ?: "Anonymous"
        holder.tvText.text = data["questionText"]?.toString() ?: ""


        val createdAt = data["createdAt"] as? Timestamp
        val ltimeMillis = createdAt?.toDate()?.time ?: System.currentTimeMillis()

        val relativeTime = DateUtils.getRelativeTimeSpanString(
            ltimeMillis,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )
        holder.tvTime.text = relativeTime
        holder.itemView.setOnClickListener {
            onQuestionClicked(data)
        }
    }

    override fun getItemCount(): Int = questionsList.size

    fun updateData(newList: List<Map<String, Any>>) {
        questionsList = newList
        notifyDataSetChanged()
    }
}