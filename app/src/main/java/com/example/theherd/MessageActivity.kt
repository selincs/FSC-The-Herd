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
    private lateinit var myId: String
    private lateinit var friendId: String
    private lateinit var convoId: String

    private lateinit var recyclerView: RecyclerView
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: FloatingActionButton
    private lateinit var chatAdapter: MessageAdapter

    private val messages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_messaging)

        myId = SessionManager.requireUserId()
//        friendId = intent.getStringExtra("FRIEND_ID") ?: return
        friendId = intent.getStringExtra("FRIEND_ID") ?: run {
            println("ERROR: FRIEND_ID is null")
            finish()
            return
        }

        println("myID = $myId")
        println("friendId = $friendId")

        convoId = MessageRepository.getConversationId(myId, friendId)

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
//        recyclerView.layoutManager = LinearLayoutManager(this).apply {
//            stackFromEnd = true
//        }
        val layoutManager = LinearLayoutManager(this)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = false

        recyclerView.layoutManager = layoutManager

        recyclerView.adapter = chatAdapter

        sendButton.setOnClickListener {
            val text = messageInput.text.toString().trim()
            println("Sending message $text")
            if (text.isNotEmpty()) {
                val msg = Message(
                    senderId = myId,
                    receiverId = friendId,
                    text = text
                )

                MessageRepository.sendMessage(convoId, msg)

                messages.forEach {
                    println("MSG: ${it.text} | ${it.timestamp}")
                }

                messageInput.text?.clear()
            }
        }

        backButton.setOnClickListener { finish() }

        btnViewProfile.setOnClickListener {
            // will open friend profile activity
        }

        MessageRepository.listenForMessages(convoId) { newMessages ->

            println("MESSAGES SIZE: ${newMessages.size}")

            messages.clear()
            messages.addAll(newMessages)

            chatAdapter.notifyDataSetChanged()

            if (messages.isNotEmpty()) {
                recyclerView.post {
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
        }
    }
}