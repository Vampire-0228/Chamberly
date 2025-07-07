package com.example.chamberly;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class UserHomeActivity extends ComponentActivity {

    TextView tvWelcome;
    ImageButton btnLogout;
    FirebaseAuth mAuth;
    private String currentUserId;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        Button btnChat = findViewById(R.id.btnChat);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            currentUserId = currentUser.getUid();
            tvWelcome.setText("Welcome to Chamberly");
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        btnChat.setOnClickListener(v -> {
            Toast.makeText(this, "Searching for a listener...", Toast.LENGTH_SHORT).show();
            startUserChatFlow();
        });

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }

    private void startUserChatFlow() {
        DatabaseReference connRef = FirebaseDatabase.getInstance().getReference("connections").child(currentUserId);

        connRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String listenerId = snapshot.getValue(String.class);
                    Log.d("UserHome", "Already connected to listener: " + listenerId);
                    openChat(currentUserId, listenerId);
                } else {
                    findAvailableListener(currentUserId);
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(UserHomeActivity.this, "Connection check failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void findAvailableListener(String userId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("users");

        usersRef.orderByChild("role").equalTo("listener")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot snapshot) {
                        boolean[] found = {false};

                        for (DataSnapshot listenerSnap : snapshot.getChildren()) {
                            String listenerId = listenerSnap.getKey();
                            Log.d("Match", "Checking listener: " + listenerId);

                            DatabaseReference listenerConnRef = FirebaseDatabase.getInstance()
                                    .getReference("connections")
                                    .child(listenerId);

                            listenerConnRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot connSnap) {
                                    if (!connSnap.exists() && !found[0]) {
                                        found[0] = true;
                                        Log.d("Match", "✅ Found available listener: " + listenerId);

                                        DatabaseReference connRef = FirebaseDatabase.getInstance().getReference("connections");
                                        connRef.child(userId).setValue(listenerId);
                                        connRef.child(listenerId).setValue(userId);

                                        Toast.makeText(UserHomeActivity.this, "Connected to listener!", Toast.LENGTH_SHORT).show();
                                        openChat(userId, listenerId);
                                    } else {
                                        Log.d("Match", "⛔ Listener already connected: " + listenerId);
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError error) {
                                    Log.e("Match", "Error checking listener connection: " + error.getMessage());
                                }
                            });
                        }

                        handler.postDelayed(() -> {
                            if (!found[0]) {
                                Toast.makeText(UserHomeActivity.this, "No available listeners right now.", Toast.LENGTH_LONG).show();
                                Log.d("Match", "❌ No free listeners found.");
                            }
                        }, 1000); // allow Firebase async check to complete
                    }

                    @Override
                    public void onCancelled(DatabaseError error) {
                        Toast.makeText(UserHomeActivity.this, "Failed to find listener", Toast.LENGTH_SHORT).show();
                        Log.e("Match", "Firebase error: " + error.getMessage());
                    }
                });
    }

    private void openChat(String userId, String listenerId) {
        String chatId = userId.compareTo(listenerId) < 0
                ? userId + "_" + listenerId
                : listenerId + "_" + userId;

        Log.d("UserHome", "Opening chat with ID: " + chatId);
        Intent intent = new Intent(UserHomeActivity.this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("receiverId", listenerId);
        intent.putExtra("senderRole", "user");
        startActivity(intent);
    }
}
