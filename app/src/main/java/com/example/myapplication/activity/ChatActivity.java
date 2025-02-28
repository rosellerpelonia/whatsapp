package com.example.myapplication.activity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Message;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.MessagesAdapter;
import com.example.myapplication.model.Messages;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private EditText messageEditText;
    private ImageView sendButton;
    private RecyclerView recyclerViewMessages;
    private MessagesAdapter messagesAdapter;
    private List<Messages> messageList;
    private DatabaseReference chatRef;
    private String userId, username, currentUserId, chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        usernameTextView = findViewById(R.id.usernameTextView);
        messageEditText = findViewById(R.id.messageEditText);
        sendButton = findViewById(R.id.sendButton);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        ImageView backButton = findViewById(R.id.backButton);

        userId = getIntent().getStringExtra("userId"); // Receiver's ID
        username = getIntent().getStringExtra("username");

        usernameTextView.setText(username);

        // Get current logged-in user ID from SharedPreferences
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        currentUserId = prefs.getString("currentUserId", null);

        if (currentUserId == null) {
            Toast.makeText(this, "Error: Not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Generate chatId (sorted combination of userId and currentUserId)
        chatId = (currentUserId.compareTo(userId) < 0) ? currentUserId + "_" + userId : userId + "_" + currentUserId;

        chatRef = FirebaseDatabase.getInstance("https://whatsapp-7c561-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Chats")
                .child(chatId);

        // Setup RecyclerView
        messageList = new ArrayList<>();
        messagesAdapter = new MessagesAdapter(this, messageList, currentUserId);
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messagesAdapter);

        loadMessages();

        sendButton.setOnClickListener(v -> sendMessage());

        backButton.setOnClickListener(v -> finish());
    }

    private void sendMessage() {
        String messageText = messageEditText.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }

        long timestamp = System.currentTimeMillis();
        String messageId = chatRef.push().getKey();

//        Message message = new Message(currentUserId, userId, messageText, timestamp);
        Messages message = new Messages(currentUserId, userId, messageText, timestamp);
        if (messageId != null) {
            chatRef.child(messageId).setValue(message)
                    .addOnSuccessListener(aVoid -> {
                        messageEditText.setText("");
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    });
        }
    }

    private void loadMessages() {
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messageList.clear();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Messages message = data.getValue(Messages.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }
                messagesAdapter.notifyDataSetChanged();
                recyclerViewMessages.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load messages", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
