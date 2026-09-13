package com.talksyapp.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.talksyapp.chat.R;
import com.talksyapp.chat.models.User;
import com.talksyapp.chat.repositories.UserRepository;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity {

    private final UserRepository userRepository = new UserRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        String uid = FirebaseAuth.getInstance().getUid();
        if (uid == null) { finish(); return; }

        TextView tvFullName = findViewById(R.id.tvFullName);
        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView tvStatus = findViewById(R.id.tvStatus);
        TextView tvBio = findViewById(R.id.tvBio);
        TextView tvEmail = findViewById(R.id.tvEmail);
        CircleImageView ivPhoto = findViewById(R.id.ivPhoto);

        userRepository.getUser(uid, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                if (user == null) return;
                tvFullName.setText(user.getFullName());
                tvUsername.setText("@" + user.getUsername());
                tvStatus.setText(user.isOnline() ? getString(R.string.online) : getString(R.string.offline));
                tvBio.setText(user.getBio() == null || user.getBio().isEmpty() ? "No bio yet" : user.getBio());
                tvEmail.setText(user.getEmail());
                Glide.with(ProfileActivity.this).load(user.getPhotoUrl())
                        .placeholder(R.drawable.ic_person_placeholder)
                        .error(R.drawable.ic_person_placeholder)
                        .into(ivPhoto);
            }

            @Override
            public void onError(Exception e) { /* keep defaults */ }
        });

        findViewById(R.id.btnEditProfile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));
    }
}
