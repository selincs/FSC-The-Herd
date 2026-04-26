package com.example.theherd

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class StatusAdapter(private val posts: List<StatusPost>) :
    RecyclerView.Adapter<StatusAdapter.StatusViewHolder>() {

    class StatusViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val content: TextView = view.findViewById(R.id.tvStatusContent)
        val time: TextView = view.findViewById(R.id.tvStatusTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StatusViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_status_post, parent, false)
        return StatusViewHolder(view)
    }

    override fun onBindViewHolder(holder: StatusViewHolder, position: Int) {
        val post = posts[position]
        holder.content.text = post.content
        holder.time.text = post.timestamp
    }

    override fun getItemCount() = posts.size
}