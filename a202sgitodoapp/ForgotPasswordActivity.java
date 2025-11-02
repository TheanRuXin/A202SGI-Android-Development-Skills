package com.example.a202sgitodoapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.regex.Pattern;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText newPasswordEditText;
    private Button sendCodeButton;
    private Button resetPasswordButton;
    private Button backToLogin;
    private FirebaseFirestore db;

    private QueryDocumentSnapshot foundUserDocument;

    // Password regex: minimum 8 chars, at least one letter and one number
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextUsername = findViewById(R.id.editTextUsername);
        newPasswordEditText = findViewById(R.id.newPasswordEditText);
        sendCodeButton = findViewById(R.id.sendCodeButton);
        resetPasswordButton = findViewById(R.id.resetPasswordButton);
        backToLogin = findViewById(R.id.backToLoginButton);

        db = FirebaseFirestore.getInstance();

        // hide new password UI initially
        newPasswordEditText.setVisibility(View.GONE);
        resetPasswordButton.setVisibility(View.GONE);

        // 1) Find user by username
        sendCodeButton.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();

            if (TextUtils.isEmpty(username)) {
                Toast.makeText(this, "Please enter your username", Toast.LENGTH_SHORT).show();
                return;
            }

            db.collection("users")
                    .whereEqualTo("username", username)
                    .limit(1)
                    .get()
                    .addOnSuccessListener((QuerySnapshot querySnapshot) -> {
                        if (!querySnapshot.isEmpty()) {
                            foundUserDocument = (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                            newPasswordEditText.setVisibility(View.VISIBLE);
                            resetPasswordButton.setVisibility(View.VISIBLE);
                            Toast.makeText(this, "Username found. Enter new password below.", Toast.LENGTH_SHORT).show();
                        } else {
                            foundUserDocument = null;
                            Toast.makeText(this, "Username not found.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        foundUserDocument = null;
                        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        // 2) Reset password with bcrypt + validation
        resetPasswordButton.setOnClickListener(v -> {
            if (foundUserDocument == null) {
                Toast.makeText(this, "No user selected. Search a username first.", Toast.LENGTH_SHORT).show();
                return;
            }

            String newPassword = newPasswordEditText.getText().toString().trim();
            if (TextUtils.isEmpty(newPassword)) {
                Toast.makeText(this, "Please enter a new password.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Validate password pattern
            if (!PASSWORD_PATTERN.matcher(newPassword).matches()) {
                Toast.makeText(this, "Password must be at least 8 characters long and include letters and numbers.", Toast.LENGTH_LONG).show();
                return;
            }

            // Hash password using bcrypt
            String hashedPassword = BCrypt.withDefaults().hashToString(12, newPassword.toCharArray());

            foundUserDocument.getReference()
                    .update("passwordHash", hashedPassword) // save hashed password
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Password updated successfully.", Toast.LENGTH_LONG).show();
                        newPasswordEditText.setText("");
                        newPasswordEditText.setVisibility(View.GONE);
                        resetPasswordButton.setVisibility(View.GONE);

                        // Go back to login
                        Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Failed to update password: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    });
        });

        backToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}
