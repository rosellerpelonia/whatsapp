package com.example.myapplication.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class CreateAccountActivity extends AppCompatActivity {

    private EditText etEmail, etUsername, etPassword, etConfirmPassword;
    private Button btnCreateAccount;
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        // Reference to Firebase Database
//        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        databaseReference = FirebaseDatabase.getInstance("https://whatsapp-7c561-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("Users");

        // Initialize UI elements
        etEmail = findViewById(R.id.etEmail);
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);

        // Handle button click
        btnCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createAccount();
            }
        });
    }

    private void createAccount() {
        String email = etEmail.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        // Validate input fields
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(username) || TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
            Toast.makeText(this, "All fields are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user with Firebase Authentication
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Get user ID from Firebase Authentication
                            String userId = mAuth.getCurrentUser().getUid();

                            // Store user data in Firebase Database
                            HashMap<String, String> userMap = new HashMap<>();
                            userMap.put("userId", userId);
                            userMap.put("username", username);
                            userMap.put("email", email);
                            userMap.put("password", password);

                            // Save data in Firebase Realtime Database with Debug Logging
                            databaseReference.child(userId).setValue(userMap)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                Toast.makeText(CreateAccountActivity.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                                                System.out.println("Database write successful for user: " + userId);
                                            } else {
                                                Toast.makeText(CreateAccountActivity.this, "Failed to save data!", Toast.LENGTH_SHORT).show();
                                                System.out.println("Database write failed: " + task.getException().getMessage());
                                            }
                                        }
                                    });
                        } else {
                            Exception e = task.getException();
                            Toast.makeText(CreateAccountActivity.this, "Registration failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            e.printStackTrace(); // Log error in Logcat
                        }
                    }
                });
    }

}