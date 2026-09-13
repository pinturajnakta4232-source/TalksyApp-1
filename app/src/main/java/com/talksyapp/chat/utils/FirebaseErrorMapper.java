package com.talksyapp.chat.utils;

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class FirebaseErrorMapper {

    public static String map(Exception e) {
        if (e == null) return "Something went wrong. Please try again.";

        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "Password is too weak. Use at least 8 characters.";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException) {
            return "Invalid email or password.";
        }
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "An account with this email already exists.";
        }
        if (e instanceof FirebaseAuthInvalidUserException) {
            return "No account found for this email.";
        }
        if (e instanceof FirebaseFirestoreException) {
            FirebaseFirestoreException fe = (FirebaseFirestoreException) e;
            switch (fe.getCode()) {
                case UNAVAILABLE:
                    return "Network unavailable. Check your connection.";
                case PERMISSION_DENIED:
                    return "You don't have permission to do that.";
                default:
                    return "Something went wrong. Please try again.";
            }
        }
        String msg = e.getMessage();
        return msg != null ? msg : "Something went wrong. Please try again.";
    }
}
