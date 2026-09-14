package com.talksyapp.chat.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.talksyapp.chat.R;
import com.talksyapp.chat.repositories.AuthRepository;
import com.talksyapp.chat.repositories.StorageRepository;
import com.talksyapp.chat.utils.FirebaseErrorMapper;
import com.talksyapp.chat.utils.ValidationUtils;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Real account creation: writes to Firebase Authentication, then to
 * Firestore users/{uid}. No fake/demo accounts are ever created here.
 */
public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etUsername, etEmail, etPassword, etConfirmPassword, etBio;
    private MaterialButton btnCreateAccount;
    private CircleImageView ivProfilePhoto;
    private View progressBar;

    private final AuthRepository authRepository = new AuthRepository();
    private final StorageRepository storageRepository = new StorageRepository();

    private Uri selectedPhotoUri;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedPhotoUri = uri;
                    Glide.with(this).load(uri).into(ivProfilePhoto);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        etBio = findViewById(R.id.etBio);
        btnCreateAccount = findViewById(R.id.btnCreateAccount);
        ivProfilePhoto = findViewById(R.id.ivProfilePhoto);
        progressBar = findViewById(R.id.progressBar);

        ivProfilePhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        findViewById(R.id.tvAddPhoto).setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        btnCreateAccount.setOnClickListener(v -> validateAndRegister());
        findViewById(R.id.tvLogin).setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }

    private void validateAndRegister() {
        String fullName = text(etFullName);
        String username = text(etUsername);
        String email = text(etEmail);
        String password = text(etPassword);
        String confirmPassword = text(etConfirmPassword);
        String bio = text(etBio);

        if (!ValidationUtils.isNotEmpty(fullName) || !ValidationUtils.isNotEmpty(username)
                || !ValidationUtils.isNotEmpty(email) || !ValidationUtils.isNotEmpty(password)
                || !ValidationUtils.isNotEmpty(confirmPassword)) {
            Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationUtils.isValidUsername(username)) {
            Toast.makeText(this, "Username must be 3-20 characters (letters, numbers, _ or .)", Toast.LENGTH_LONG).show();
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationUtils.isValidPassword(password)) {
            Toast.makeText(this, R.string.error_password_length, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, R.string.error_password_mismatch, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        registerAccount(fullName, username, email, password, bio);
    }

    private void registerAccount(String fullName, String username, String email,
                                  String password, String bio) {
        authRepository.register(fullName, username, email, password, bio, "", new AuthRepository.Callback() {
            @Override
            public void onSuccess() {
                if (selectedPhotoUri != null) {
                    uploadPhotoThenContinue();
                } else {
                    goToHome();
                }
            }

            @Override
            public void onError(Exception e) {
                setLoading(false);
                Toast.makeText(RegisterActivity.this, FirebaseErrorMapper.map(e), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void uploadPhotoThenContinue() {
        String uid = authRepository.getCurrentUser() != null ? authRepository.getCurrentUser().getUid() : null;
        if (uid == null) {
            goToHome();
            return;
        }
        storageRepository.uploadProfilePhoto(uid, selectedPhotoUri, new StorageRepository.UploadCallback() {
            @Override
            public void onSuccess(String downloadUrl) {
                new com.talksyapp.chat.repositories.UserRepository().updateProfile(
                        uid, java.util.Collections.singletonMap("photoUrl", downloadUrl),
                        new com.talksyapp.chat.repositories.UserRepository.SimpleCallback() {
                            @Override
                            public void onSuccess() { goToHome(); }

                            @Override
                            public void onError(Exception e) { goToHome(); }
                        });
            }

            @Override
            public void onError(Exception e) {
                goToHome();
            }
        });
    }

    private void goToHome() {
        setLoading(false);
        // Don't sign the user into the app yet — Firebase Auth already
        // created and signed in the account, but we sign them back out
        // here so they can't use the app until the email is verified.
        authRepository.logout();
        Toast.makeText(this,
                "Account created! We've sent a verification link to your email — "
                        + "please verify it, then log in.",
                Toast.LENGTH_LONG).show();
        startActivity(new Intent(this, LoginActivity.class));
        finishAffinity();
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnCreateAccount.setEnabled(!loading);
    }

    private String text(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
