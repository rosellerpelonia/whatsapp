package com.example.myapplication.activity;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.myapplication.R;

public class ChatActivity extends AppCompatActivity {

    private TextView usernameTextView;
    private String userId, username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        usernameTextView = findViewById(R.id.usernameTextView);
        userId = getIntent().getStringExtra("userId");
        username = getIntent().getStringExtra("username");

        usernameTextView.setText(username);
    }
}