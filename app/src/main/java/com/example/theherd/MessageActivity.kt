package com.example.theherd

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class MessageActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: FloatingActionButton
    private lateinit var chatAdapter: MessageAdapter

    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging)

        val friendName = intent.getStringExtra("FRIEND_NAME") ?: "Jane Doe"
        val friendStatus = intent.getStringExtra("ONLINE_STATUS") ?: "Online"

        val tvFriendName = findViewById<TextView>(R.id.tvChatFriendName)
        val tvStatus = findViewById<TextView>(R.id.tvChatOnlineStatus)
        val imgAvatar = findViewById<ImageView>(R.id.imgChatAvatar)
        val backButton = findViewById<ImageButton>(R.id.btnBack)
        val btnViewProfile = findViewById<ImageButton>(R.id.btnViewProfile)

        tvFriendName.text = friendName
        tvStatus.text = friendStatus
        imgAvatar.setImageResource(R.drawable.ic_account_circle_24)

        recyclerView = findViewById(R.id.messagesRecyclerView)
        messageInput = findViewById(R.id.etMessageInput)
        sendButton = findViewById(R.id.btnSendMessage)

        chatAdapter = MessageAdapter(messages)
        recyclerView.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        recyclerView.adapter = chatAdapter

        // mock message
        addMessage(Message("1", "friend", "Hey! Ready for the Hackathon?", isFromMe = false))

        sendButton.setOnClickListener {
            val text = messageInput.text.toString().trim()
            if (text.isNotEmpty()) {
                handleUserMessage(text)
            }
        }

        backButton.setOnClickListener { finish() }

        btnViewProfile.setOnClickListener {
            // will open friend profile activity
        }
    }

    private fun handleUserMessage(text: String) {
        val userMsg = Message(
            senderId = "me",
            receiverId = "friend",
            text = text,
            isFromMe = true
        )
        addMessage(userMsg)
        messageInput.text?.clear()

        Handler(Looper.getMainLooper()).postDelayed({
            val reply = Message(
                senderId = "friend",
                receiverId = "me",
                text = "Messaging is working!.",
                isFromMe = false
            )
            addMessage(reply)
        }, 1500)
    }

    private fun addMessage(chatMessage: Message) {
        messages.add(chatMessage)
        chatAdapter.notifyItemInserted(messages.size - 1)
        recyclerView.smoothScrollToPosition(messages.size - 1)
    }
}