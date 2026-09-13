package com.talksyapp.chat.utils;

import android.text.TextUtils;
import android.util.Patterns;

public class ValidationUtils {

    public static boolean isValidEmail(CharSequence email) {
        return !TextUtils.isEmpty(email) && Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    public static boolean isValidPassword(CharSequence password) {
        return password != null && password.length() >= 8;
    }

    public static boolean isValidUsername(CharSequence username) {
        return username != null && username.toString().trim().matches("^[a-zA-Z0-9_.]{3,20}$");
    }

    public static boolean isNotEmpty(CharSequence value) {
        return !TextUtils.isEmpty(value != null ? value.toString().trim() : null);
    }
}
