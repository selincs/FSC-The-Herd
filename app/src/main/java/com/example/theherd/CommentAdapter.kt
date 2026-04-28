package com.example.theherd

import Model.Comment
import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import java.time.format.DateTimeFormatter

class CommentAdapter(
    private val context: Context,
    private val comments: List<Comment>,
    private val onLikeClicked: (Comment) -> Unit
) : BaseAdapter() {

    override fun getCount(): Int = comments.size

    override fun getItem(position: Int): Comment = comments[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_comment, parent, false)

        val comment = comments[position]
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        val tvUsername = view.findViewById<TextView>(R.id.tvUsername)
        val tvTime = view.findViewById<TextView>(R.id.tvTime)
        val tvContent = view.findViewById<TextView>(R.id.tvComment)
        val btnLike = view.findViewById<ImageView>(R.id.btnLike)
        val tvLikeCount = view.findViewById<TextView>(R.id.tvLikeCount)

        tvUsername.text = comment.commentedByUID
        tvContent.text = comment.commContents
        tvLikeCount.text = comment.likeCt.toString()

        val formatter = DateTimeFormatter.ofPattern("MMM d, h:mm a")
        tvTime.text = comment.createdAt.format(formatter)

        val isLiked = currentUserId != null &&
                comment.likedUserIds?.contains(currentUserId) == true

        if (isLiked) {
            btnLike.setImageResource(R.drawable.ic_heart)
            btnLike.setColorFilter(Color.RED)
        } else {
            btnLike.setImageResource(R.drawable.ic_heart)
            btnLike.setColorFilter(Color.GRAY)
        }

        btnLike.setOnClickListener {
            onLikeClicked(comment)
        }

        return view
    }
}