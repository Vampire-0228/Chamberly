package com.example.chamberly;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.*;

import androidx.activity.ComponentActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends ComponentActivity {

    EditText etEmail, etPassword;
    RadioGroup roleGroup;
    RadioButton rbUser, rbListener;
    Button btnLogin;
    TextView tvSignup;

    FirebaseAuth mAuth;

    ImageView ivTogglePassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login); // Connect to the layout

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        roleGroup = findViewById(R.id.roleGroup);
        rbUser = findViewById(R.id.rbUser);
        rbListener = findViewById(R.id.rbListener);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignup = findViewById(R.id.tvSignup);
        ImageView ivTogglePassword = findViewById(R.id.ivTogglePassword);

        mAuth = FirebaseAuth.getInstance();

        btnLogin.setOnClickListener(v -> loginUser());

        ivTogglePassword.setOnClickListener(v -> {
            if (etPassword.getInputType() == (InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD)) {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye);
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ivTogglePassword.setImageResource(R.drawable.ic_eye_off);
            }
            etPassword.setSelection(etPassword.getText().length()); // keep cursor at end
        });





        tvSignup.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);

        tvForgotPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Enter your email first");
                etEmail.requestFocus();
                return;
            }

            mAuth.sendPasswordResetEmail(email)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(MainActivity.this, "Reset link sent to your email", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(MainActivity.this, "Error: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
        });




    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Enter password");
            return;
        }

        int selectedRoleId = roleGroup.getCheckedRadioButtonId();
        if (selectedRoleId == -1) {
            Toast.makeText(this, "Select a role", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (rbUser.isChecked()) {
                            startActivity(new Intent(MainActivity.this, UserHomeActivity.class));
                        } else if (rbListener.isChecked()) {
                            startActivity(new Intent(MainActivity.this, ListenerHomeActivity.class));
                        }
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Login failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }
}
