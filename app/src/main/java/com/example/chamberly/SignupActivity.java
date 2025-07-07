package com.example.chamberly;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.*;

import androidx.activity.ComponentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends ComponentActivity {

    EditText etEmail, etPassword, etUsername;
    RadioGroup roleGroup;
    RadioButton rbUser, rbListener;
    Button btnSignup;
    TextView tvLogin;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etUsername = findViewById(R.id.etUsername);
        roleGroup = findViewById(R.id.roleGroup);
        rbUser = findViewById(R.id.rbUser);
        rbListener = findViewById(R.id.rbListener);
        btnSignup = findViewById(R.id.btnSignup);
        tvLogin = findViewById(R.id.tvLogin);
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);

        mAuth = FirebaseAuth.getInstance();

        btnSignup.setOnClickListener(v -> registerUser());

        ivTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        tvLogin.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, MainActivity.class));
            finish();
        });
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String username = etUsername.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return;
        }
        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Enter username");
            return;
        }

        int selectedRoleId = roleGroup.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        String role = rbUser.isChecked() ? "user" : "listener";

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            String uid = firebaseUser.getUid();

                            // ✅ Save to Firebase Realtime Database
                            DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
                            AppUser newUser = new AppUser(username, role);
                            userRef.child(uid).setValue(newUser)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(SignupActivity.this, "Signup successful!", Toast.LENGTH_SHORT).show();

                                        // Open correct home screen
                                        if (role.equals("user")) {
                                            startActivity(new Intent(SignupActivity.this, UserHomeActivity.class));
                                        } else {
                                            startActivity(new Intent(SignupActivity.this, ListenerHomeActivity.class));
                                        }
                                        finish();
                                    })
                                    .addOnFailureListener(e ->
                                            Toast.makeText(SignupActivity.this, "Failed to save user: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                    );
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }
}
