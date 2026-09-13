package com.talksyapp.chat.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.talksyapp.chat.R;
import com.talksyapp.chat.repositories.AuthRepository;
import com.talksyapp.chat.utils.FirebaseErrorMapper;
import com.talksyapp.chat.utils.ValidationUtils;

/** Uses Firebase's real sendPasswordResetEmail() flow — no fake reset logic. */
public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private MaterialButton btnSendReset;
    private View progressBar;
    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        etEmail = findViewById(R.id.etEmail);
        btnSendReset = findViewById(R.id.btnSendReset);
        progressBar = findViewById(R.id.progressBar);

        btnSendReset.setOnClickListener(v -> sendReset());
    }

    private void sendReset() {
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        if (!ValidationUtils.isValidEmail(email)) {
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        btnSendReset.setEnabled(false);

        authRepository.sendPasswordReset(email, new AuthRepository.Callback() {
            @Override
            public void onSuccess() {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(ForgotPasswordActivity.this,
                        "Password reset link sent to " + email, Toast.LENGTH_LONG).show();
                finish();
            }

            @Override
            public void onError(Exception e) {
                progressBar.setVisibility(View.GONE);
                btnSendReset.setEnabled(true);
                Toast.makeText(ForgotPasswordActivity.this, FirebaseErrorMapper.map(e), Toast.LENGTH_LONG).show();
            }
        });
    }
}
