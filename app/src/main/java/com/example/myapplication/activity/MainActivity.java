package com.example.myapplication.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import com.example.myapplication.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // Make sure this line is correct

        // Remove EdgeToEdge if not needed
        // EdgeToEdge.enable(this);

        // Initialize buttons
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Click listener for login button (Replace with actual login logic)
        btnLogin.setOnClickListener(view -> {
            // Example: Navigate to HomeActivity after login
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        // Click listener for create account button (Navigate to RegisterActivity)
        btnCreateAccount.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
            startActivity(intent);
        });
    }
}