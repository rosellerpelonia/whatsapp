package com.example.myapplication.activity;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;

import com.example.myapplication.R;

public class CreateAccountActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account); // Correct layout reference

        // Remove EdgeToEdge (not needed)
        // EdgeToEdge.enable(this);

        // Correct reference to the root layout (if needed for insets)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.create_account_layout), (v, insets) -> {
            return insets;
        });
    }
}