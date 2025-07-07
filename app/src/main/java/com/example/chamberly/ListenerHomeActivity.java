package com.example.chamberly;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.ComponentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

public class ListenerHomeActivity extends ComponentActivity {

    FirebaseAuth mAuth;
    TextView tvWelcome;
    ImageButton btnLogout;
    Button btnChat;

    String currentListenerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listener_home);

        tvWelcome = findViewById(R.id.tvWelcome);
        btnLogout = findViewById(R.id.btnLogout);
        btnChat = findViewById(R.id.btnChatWithUser); // ✅ Make sure this ID exists in XML

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            currentListenerId = user.getUid();
            tvWelcome.setText("Welcome, Listener\n" + user.getEmail());
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        btnLogout.setOnClickListener(v -> {
            mAuth.signOut();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });

        btnChat.setOnClickListener(v -> checkUserConnection());
    }

    private void checkUserConnection() {
        DatabaseReference connRef = FirebaseDatabase.getInstance()
                .getReference("connections")
                .child(currentListenerId);

        connRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String userId = snapshot.getValue(String.class);

                    if (userId == null || userId.isEmpty()) {
                        Toast.makeText(ListenerHomeActivity.this, "Invalid user ID received", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    openChat(currentListenerId, userId);
                } else {
                    Toast.makeText(ListenerHomeActivity.this, "No user connected yet. Please wait for a request.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ListenerHomeActivity.this, "Failed to check connection.", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void openChat(String listenerId, String userId) {
        if (listenerId == null || userId == null) {
            Toast.makeText(this, "Cannot open chat: missing user info", Toast.LENGTH_SHORT).show();
            return; // 🚫 Prevent crash
        }

        String chatId = userId.compareTo(listenerId) < 0
                ? userId + "_" + listenerId
                : listenerId + "_" + userId;

        Intent intent = new Intent(ListenerHomeActivity.this, ChatActivity.class);
        intent.putExtra("chatId", chatId);
        intent.putExtra("receiverId", userId);
        intent.putExtra("senderRole", "listener");
        startActivity(intent);
    }

}
