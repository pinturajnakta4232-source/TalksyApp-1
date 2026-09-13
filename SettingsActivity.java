package com.talksyapp.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.talksyapp.chat.R;
import com.talksyapp.chat.repositories.AuthRepository;

/** Real logout via FirebaseAuth.getInstance().signOut(). */
public class SettingsActivity extends AppCompatActivity {

    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        findViewById(R.id.rowEditProfile).setOnClickListener(v ->
                startActivity(new Intent(this, EditProfileActivity.class)));
        findViewById(R.id.rowNotifications).setOnClickListener(v ->
                Toast.makeText(this, "Notifications are managed by your device settings", Toast.LENGTH_SHORT).show());
        findViewById(R.id.rowPrivacy).setOnClickListener(v ->
                Toast.makeText(this, "Your conversations are private and access-controlled", Toast.LENGTH_SHORT).show());
        findViewById(R.id.rowAbout).setOnClickListener(v ->
                startActivity(new Intent(this, AboutActivity.class)));
        findViewById(R.id.rowLogout).setOnClickListener(v -> logout());
    }

    private void logout() {
        try {
            authRepository.logout();
        } finally {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }
    }
}
