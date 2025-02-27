package com.example.myapplication.fragments;

import android.app.AlertDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.InputType;
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
    private FirebaseAuth auth;
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

        auth = FirebaseAuth.getInstance();
        currentUserId = auth.getCurrentUser().getUid();

        contactsRef = FirebaseDatabase.getInstance("https://whatsapp-7c561-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users").child(currentUserId).child("contacts");

        usersRef = FirebaseDatabase.getInstance("https://whatsapp-7c561-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("Users");

        loadContacts();

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
        usersRef.orderByChild("phone_number").equalTo(phoneNumber)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                                String userId = userSnapshot.getKey();

                                // Add userId to current user's contacts
                                contactsRef.child(userId).setValue(true)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Contact added!", Toast.LENGTH_SHORT).show();
                                            loadContacts();
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add contact", Toast.LENGTH_SHORT).show());
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