package com.example.theherd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MentorAdapter(private val mentorList: List<Mentor>) :
    RecyclerView.Adapter<MentorAdapter.MentorViewHolder>() {

    class MentorViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.mentorName)
        val roleText: TextView = itemView.findViewById(R.id.mentorRole)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MentorViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mentor_card, parent, false)
        return MentorViewHolder(view)
    }

    override fun onBindViewHolder(holder: MentorViewHolder, position: Int) {
        val currentItem = mentorList[position]
        holder.nameText.text = currentItem.name
        holder.roleText.text = currentItem.role
    }

    override fun getItemCount() = mentorList.size
}