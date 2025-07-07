package com.example.chamberly;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.*;
import androidx.activity.ComponentActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;

import java.util.Objects;

public class ChatActivity extends ComponentActivity {

    private TextView tvChatHistory;
    private EditText etMessage;
    private ImageButton btnSend;
    private Button btnEndSession;

    private String chatId;
    private String receiverId;
    private String senderId;

    private DatabaseReference chatRef;
    private DatabaseReference connRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Get intent extras
        chatId = getIntent().getStringExtra("chatId");
        receiverId = getIntent().getStringExtra("receiverId");
        String senderRole = getIntent().getStringExtra("senderRole");

        if (chatId == null || receiverId == null || senderRole == null) {
            Toast.makeText(this, "Missing chat data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        tvChatHistory = findViewById(R.id.tvChatHistory);
        etMessage = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        btnEndSession = findViewById(R.id.btnEndSession);

        // Get current user
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        senderId = currentUser.getUid();

        // Firebase references
        chatRef = FirebaseDatabase.getInstance().getReference("chats").child(chatId);
        connRef = FirebaseDatabase.getInstance().getReference("connections");

        // Load messages
        chatRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                StringBuilder chatBuilder = new StringBuilder();

                for (DataSnapshot msgSnap : snapshot.getChildren()) {
                    String sender = String.valueOf(msgSnap.child("sender").getValue());
                    String msg = String.valueOf(msgSnap.child("message").getValue());

                    if (sender.equals(senderId)) {
                        chatBuilder.append("You: ").append(msg).append("\n");
                    } else {
                        chatBuilder.append("Partner: ").append(msg).append("\n");
                    }
                }

                tvChatHistory.setText(chatBuilder.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(ChatActivity.this, "Failed to load chat", Toast.LENGTH_SHORT).show();
            }
        });

        // Send message
        btnSend.setOnClickListener(v -> {
            String message = etMessage.getText().toString().trim();

            if (TextUtils.isEmpty(message)) return;

            if (chatRef == null || senderId == null) {
                Toast.makeText(this, "Chat not ready. Try again.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                DatabaseReference msgRef = chatRef.push();
                msgRef.child("sender").setValue(senderId);
                msgRef.child("message").setValue(message);
                etMessage.setText("");
            } catch (Exception e) {
                Log.e("ChatActivity", "Send failed: " + e.getMessage(), e);
                Toast.makeText(this, "Send failed", Toast.LENGTH_SHORT).show();
            }
        });

        // End session
        btnEndSession.setOnClickListener(v -> {
            if (connRef != null) {
                connRef.child(senderId).removeValue();
                connRef.child(receiverId).removeValue();
            }
            if (chatRef != null) {
                chatRef.removeValue();
            }
            Toast.makeText(this, "Session ended", Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
