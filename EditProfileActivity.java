package com.talksyapp.chat.activities;

import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.talksyapp.chat.R;
import com.talksyapp.chat.models.User;
import com.talksyapp.chat.repositories.AuthRepository;
import com.talksyapp.chat.repositories.StorageRepository;
import com.talksyapp.chat.repositories.UserRepository;
import com.talksyapp.chat.utils.FirebaseErrorMapper;
import com.talksyapp.chat.utils.ValidationUtils;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/** Real profile edits: writes go to Firestore; photos go to Firebase Storage. */
public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etUsername, etBio;
    private CircleImageView ivPhoto;
    private MaterialButton btnSave;
    private View progressBar;

    private final UserRepository userRepository = new UserRepository();
    private final AuthRepository authRepository = new AuthRepository();
    private final StorageRepository storageRepository = new StorageRepository();

    private String uid;
    private Uri newPhotoUri;

    private final ActivityResultLauncher<String> pickImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    newPhotoUri = uri;
                    Glide.with(this).load(uri).into(ivPhoto);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) { finish(); return; }

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        etFullName = findViewById(R.id.etFullName);
        etUsername = findViewById(R.id.etUsername);
        etBio = findViewById(R.id.etBio);
        ivPhoto = findViewById(R.id.ivPhoto);
        btnSave = findViewById(R.id.btnSave);
        progressBar = findViewById(R.id.progressBar);

        ivPhoto.setOnClickListener(v -> pickImageLauncher.launch("image/*"));
        findViewById(R.id.tvChangePhoto).setOnClickListener(v -> pickImageLauncher.launch("image/*"));

        loadCurrentProfile();
        btnSave.setOnClickListener(v -> saveChanges());
    }

    private void loadCurrentProfile() {
        userRepository.getUser(uid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;
                etFullName.setText(user.getFullName());
                etUsername.setText(user.getUsername());
                etBio.setText(user.getBio());
                Glide.with(EditProfileActivity.this).load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_person_placeholder)
                        .into(ivPhoto);
            }

            @Override
            public void onError(Exception e) { /* keep blank */ }
        });
    }

    private void saveChanges() {
        String fullName = text(etFullName);
        String username = text(etUsername);
        String bio = text(etBio);

        if (!ValidationUtils.isNotEmpty(fullName) || !ValidationUtils.isValidUsername(username)) {
            Toast.makeText(this, "Enter a valid name and username", Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        if (newPhotoUri != null) {
            storageRepository.uploadProfilePhoto(uid, newPhotoUri, new StorageRepository.UploadCallback() {
                @Override
                public void onSuccess(String downloadUrl) {
                    persistProfile(fullName, username, bio, downloadUrl);
                }

                @Override
                public void onError(Exception e) {
                    setLoading(false);
                    Toast.makeText(EditProfileActivity.this, "Photo upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            persistProfile(fullName, username, bio, null);
        }
    }

    private void persistProfile(String fullName, String username, String bio, String photoUrl) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("fullName", fullName);
        updates.put("username", username);
        updates.put("usernameLower", username.toLowerCase());
        updates.put("bio", bio);
        if (photoUrl != null) updates.put("photoUrl", photoUrl);

        userRepository.updateProfile(uid, updates, new UserRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, "Profile updated", Toast.LENGTH_SHORT).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                setLoading(false);
                Toast.makeText(EditProfileActivity.this, FirebaseErrorMapper.map(e), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnSave.setEnabled(!loading);
    }

    private String text(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
