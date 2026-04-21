package com.example.theherd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.progressindicator.CircularProgressIndicator

class CommitmentAdapter(
    private val commitmentList: List<Commitment>,
    private val onCardClick: (Commitment) -> Unit
) : RecyclerView.Adapter<CommitmentAdapter.CommitmentViewHolder>() {

    class CommitmentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val activityTitle: TextView = itemView.findViewById(R.id.activityTitleText)
        val partnerName: TextView = itemView.findViewById(R.id.partnerNameText)
        val streakText: TextView = itemView.findViewById(R.id.streakText)
        val streakRing: CircularProgressIndicator = itemView.findViewById(R.id.streakRing)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommitmentViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_commitment_card, parent, false)
        return CommitmentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommitmentViewHolder, position: Int) {
        val currentItem = commitmentList[position]

        holder.activityTitle.text = currentItem.activityName
        holder.partnerName.text = "w/ ${currentItem.partnerName}"
        holder.streakText.text = "🔥 ${currentItem.streak}"
        holder.streakRing.progress = currentItem.streak

        holder.itemView.setOnClickListener {
            onCardClick(currentItem)
        }
    }

    override fun getItemCount() = commitmentList.size

}

data class Commitment(val activityName: String, val partnerName: String, val streak: Int)