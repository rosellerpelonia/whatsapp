package com.example.myapplication.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;
import com.example.myapplication.adapters.ChatsAdapter;
import com.example.myapplication.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ChatsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ChatsAdapter chatsAdapter;
    private List<User> userList;
    private DatabaseReference contactsRef, usersRef;
    private String currentUserId;
    private FloatingActionButton fabNewChat;

    public ChatsFragment() {}

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewChats);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        userList = new ArrayList<>();
        chatsAdapter = new ChatsAdapter(getContext(), userList);
        recyclerView.setAdapter(chatsAdapter);

        fabNewChat = view.findViewById(R.id.fabNewChat);
        fabNewChat.setOnClickListener(v -> showAddContactDialog());

        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
            currentUserId = prefs.getString("currentUserId", null);
        }

        if (currentUserId != null) {
            Log.d("ChatsFragment", "Current User ID: " + currentUserId);

            contactsRef = FirebaseDatabase.getInstance("https://whatsapp-7c561-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Users")
                    .child(currentUserId)
                    .child("contacts");

            usersRef = FirebaseDatabase.getInstance("https://whatsapp-7c561-default-rtdb.asia-southeast1.firebasedatabase.app/")
                    .getReference("Users");

            loadContacts();
        } else {
            Log.e("ChatsFragment", "User ID is null! Possible login issue.");
            Toast.makeText(getContext(), "User ID is null. Please login again.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void loadContacts() {
        contactsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String userId = snapshot.getKey();
                    fetchUserDetails(userId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load contacts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDetails(String userId) {
        DatabaseReference userRef = usersRef.child(userId);
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null) {
                    userList.add(user);
                    chatsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error fetching user details", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showAddContactDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Add Contact");

        EditText input = new EditText(getContext());
        input.setHint("Enter phone number");
        input.setInputType(InputType.TYPE_CLASS_PHONE);
        builder.setView(input);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String phoneNumber = input.getText().toString().trim();
            if (!phoneNumber.isEmpty()) {
                findUserByPhoneNumber(phoneNumber);
            } else {
                Toast.makeText(getContext(), "Enter a valid phone number", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        builder.show();
    }

    private void findUserByPhoneNumber(String phoneNumber) {
        if (getActivity() == null) {
            Log.e("ChatsFragment", "Activity is null in findUserByPhoneNumber");
            return;
        }

        SharedPreferences prefs = getActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        String currentUserId = prefs.getString("currentUserId", null);

        if (currentUserId == null) {
            Toast.makeText(getContext(), "User not found. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        usersRef.orderByChild("phone_number").equalTo(phoneNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String foundUserId = userSnapshot.getKey();

                                Log.d("ChatsFragment", "Adding contact: " + foundUserId + " to user: " + currentUserId);

                                contactsRef.child(foundUserId).setValue(true)
                                        .addOnSuccessListener(aVoid -> {
                                            Log.d("ChatsFragment", "Contact added successfully!");
                                            Toast.makeText(getContext(), "Contact added!", Toast.LENGTH_SHORT).show();
                                            loadContacts();
                                        })
                                        .addOnFailureListener(e -> Log.e("ChatsFragment", "Failed to add contact", e));
                            }
                        } else {
                            Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(getContext(), "Error finding user", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}