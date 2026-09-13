package com.talksyapp.chat.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.talksyapp.chat.R;
import com.talksyapp.chat.repositories.AuthRepository;
import com.talksyapp.chat.utils.FirebaseErrorMapper;
import com.talksyapp.chat.utils.ValidationUtils;

/**
 * Real login screen backed entirely by Firebase Authentication —
 * there is no offline/demo login path.
 */
public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnLogin;
    private View progressBar;
    private final AuthRepository authRepository = new AuthRepository();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        progressBar = findViewById(R.id.progressBar);

        btnLogin.setOnClickListener(v -> attemptLogin());
        findViewById(R.id.tvCreateAccount).setOnClickListener(v ->
                startActivity(new Intent(this, RegisterActivity.class)));
        findViewById(R.id.tvForgotPassword).setOnClickListener(v ->
                startActivity(new Intent(this, ForgotPasswordActivity.class)));
    }

    private void attemptLogin() {
        String email = getText(etEmail);
        String password = getText(etPassword);

        if (!ValidationUtils.isNotEmpty(email) || !ValidationUtils.isNotEmpty(password)) {
            Toast.makeText(this, R.string.error_field_required, Toast.LENGTH_SHORT).show();
            return;
        }
        if (!ValidationUtils.isValidEmail(email)) {
            Toast.makeText(this, R.string.error_invalid_email, Toast.LENGTH_SHORT).show();
            return;
        }

        setLoading(true);
        authRepository.login(email, password, new AuthRepository.Callback() {
            @Override
            public void onSuccess() {
                setLoading(false);
                startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                finish();
            }

            @Override
            public void onError(Exception e) {
                setLoading(false);
                Toast.makeText(LoginActivity.this, FirebaseErrorMapper.map(e), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoading(boolean loading) {
        progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!loading);
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }
}
