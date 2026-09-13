package com.talksyapp.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.talksyapp.chat.R;

/**
 * Checks the REAL Firebase Authentication session on startup.
 * No fake/local login-state logic is used here.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY_MS = 800;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler(Looper.getMainLooper()).postDelayed(this::routeUser, SPLASH_DELAY_MS);
    }

    private void routeUser() {
        boolean isLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        Intent intent = new Intent(this, isLoggedIn ? HomeActivity.class : LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
