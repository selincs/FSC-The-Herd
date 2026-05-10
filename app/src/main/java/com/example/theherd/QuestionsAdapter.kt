package com.example.theherd

import Model.GuideQuestion
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class QuestionsAdapter(
    private var questionsList: List<GuideQuestion>,
    private val onQuestionClick: (GuideQuestion) -> Unit
) : RecyclerView.Adapter<QuestionsAdapter.QuestionViewHolder>() {

    class QuestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUser: TextView = itemView.findViewById(R.id.tvQuestionUser)
        val tvText: TextView = itemView.findViewById(R.id.tvQuestionText)
        val tvTime: TextView = itemView.findViewById(R.id.tvQuestionTime)

        val tvAnswer: TextView = itemView.findViewById(R.id.tvAnswerText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QuestionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_question, parent, false)
        return QuestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: QuestionViewHolder, position: Int) {
        val question = questionsList[position]

        holder.tvUser.text = question.username
        holder.tvText.text = question.questionText

        val relativeTime = DateUtils.getRelativeTimeSpanString(
            question.timestamp ?: System.currentTimeMillis(),
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS
        )

        holder.tvTime.text = relativeTime

        val reply = question.topAnswer

        if (!reply.isNullOrEmpty()) {
            holder.tvAnswer.text = "Top Answer: $reply"
            holder.tvAnswer.visibility = View.VISIBLE
        } else {
            holder.tvAnswer.visibility = View.GONE
        }



        holder.itemView.setOnClickListener {
            onQuestionClick(question)
        }
    }

    override fun getItemCount(): Int = questionsList.size

    fun updateData(newList: List<GuideQuestion>) {
        questionsList = newList
        notifyDataSetChanged()
    }
}