package com.example.theherd

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import Model.Post

class PostAdapter(private val posts: List<Post>, private val communityName: String) :
    RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val title: TextView = view.findViewById(R.id.postTitleText)
        val content: TextView = view.findViewById(R.id.postContentText)
        val user: TextView = view.findViewById(R.id.postUserHandle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = posts[position]
        holder.title.text = post.postTitle
        holder.content.text = post.postContents
        holder.user.text = "Posted by: ${post.postedByUID}"

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PostDetailActivity::class.java)

            intent.putExtra("POST_ID", post.postID)
            intent.putExtra("COMMUNITY_NAME", communityName)

            context.startActivity(intent)
        }
    }

    override fun getItemCount() = posts.size
}