package com.example.myapplication.activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin, btnCreateAccount;
    private DatabaseReference databaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
        String currentUsername = prefs.getString("currentUsername", null);

        if (currentUsername != null) {
            // User is already logged in, go to HomeActivity
            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_main);

        databaseReference = FirebaseDatabase.getInstance("https://whatsapp-7c561-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users");

        // Initialize UI elements
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Handle login button click
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginUser();
            }
        });

        // Handle create account button click
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CreateAccountActivity.class);
                startActivity(intent);
            }
        });
    }
    private void loginUser() {
        String enteredUsername = etUsername.getText().toString().trim();
        String enteredPassword = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(enteredUsername) || TextUtils.isEmpty(enteredPassword)) {
            Toast.makeText(MainActivity.this, "Please enter username and password!", Toast.LENGTH_SHORT).show();
            return;
        }

        databaseReference.orderByChild("username").equalTo(enteredUsername)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                String storedPassword = snapshot.child("password").getValue(String.class);
                                if (storedPassword != null && storedPassword.equals(enteredPassword)) {
                                    // Get User ID from Database
                                    String userId = snapshot.child("userId").getValue(String.class);

                                    if (userId != null) {
                                        // Store userId & username in SharedPreferences
                                        SharedPreferences prefs = getSharedPreferences("MyAppPrefs", MODE_PRIVATE);
                                        SharedPreferences.Editor editor = prefs.edit();
                                        editor.putString("currentUserId", userId);
                                        editor.putString("currentUsername", enteredUsername);
                                        editor.apply();

                                        // Navigate to HomeActivity
                                        Toast.makeText(MainActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                                        Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                    return;
                                }
                            }
                            Toast.makeText(MainActivity.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Username not found!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(MainActivity.this, "Database error: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
