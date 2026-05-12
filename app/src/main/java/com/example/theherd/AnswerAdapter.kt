package com.example.theherd

import Model.GuideAnswer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AnswerAdapter(
    private var answersList: List<GuideAnswer>,
    private val onUpVoteClicked: (GuideAnswer) -> Unit,
    private val onDownVoteClicked: (GuideAnswer) -> Unit
) : RecyclerView.Adapter<AnswerAdapter.AnswerViewHolder>() {

    class AnswerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvUser: TextView = itemView.findViewById(R.id.tvAnswerUser)
        val tvContent: TextView = itemView.findViewById(R.id.tvAnswerContent)
        val btnUpVote: ImageButton = itemView.findViewById(R.id.btnUpVote)
        val btnDownVote: ImageButton = itemView.findViewById(R.id.btnDownVote)
        val tvUpCount: TextView = itemView.findViewById(R.id.tvUpCount)
        val tvDownCount: TextView = itemView.findViewById(R.id.tvDownCount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AnswerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_answer, parent, false)
        return AnswerViewHolder(view)
    }

    override fun onBindViewHolder(holder: AnswerViewHolder, position: Int) {
        val answer = answersList[position]

        holder.tvUser.text = answer.username
        holder.tvContent.text = answer.answerText
        holder.tvUpCount.text = answer.upvotes.toString()
        holder.tvDownCount.text = answer.downvotes.toString()

        when (answer.currentUserVote) {
            "up" -> {
                holder.btnUpVote.setColorFilter(android.graphics.Color.parseColor("#2E7D32"))
                holder.btnDownVote.setColorFilter(android.graphics.Color.parseColor("#555555"))
            }
            "down" -> {
                holder.btnUpVote.setColorFilter(android.graphics.Color.parseColor("#555555"))
                holder.btnDownVote.setColorFilter(android.graphics.Color.parseColor("#C62828"))
            }
            else -> {
                holder.btnUpVote.setColorFilter(android.graphics.Color.parseColor("#555555"))
                holder.btnDownVote.setColorFilter(android.graphics.Color.parseColor("#555555"))
            }
        }

        holder.btnUpVote.setOnClickListener {
            onUpVoteClicked(answer)
        }

        holder.btnDownVote.setOnClickListener {
            onDownVoteClicked(answer)
        }
    }

    override fun getItemCount(): Int = answersList.size

    fun updateData(newList: List<GuideAnswer>) {
        answersList = newList
        notifyDataSetChanged()
    }
}