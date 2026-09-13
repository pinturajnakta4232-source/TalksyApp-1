package com.talksyapp.chat.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.talksyapp.chat.models.User;
import com.talksyapp.chat.utils.Constants;

import java.util.HashMap;
import java.util.Map;

/**
 * Wraps real Firebase Authentication calls. There is no local/offline
 * fallback here — every account is created and verified against the
 * real Firebase Authentication backend.
 */
public class AuthRepository {

    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface Callback {
        void onSuccess();
        void onError(Exception e);
    }

    public interface UsernameCallback {
        void onResult(boolean isTaken);
        void onError(Exception e);
    }

    public FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    public boolean isLoggedIn() {
        return auth.getCurrentUser() != null;
    }

    public void checkUsernameTaken(String username, UsernameCallback callback) {
        firestore.collection(Constants.COLLECTION_USERS)
                .whereEqualTo(Constants.FIELD_USERNAME_LOWER, username.toLowerCase())
                .limit(1)
                .get()
                .addOnSuccessListener(snapshot -> callback.onResult(!snapshot.isEmpty()))
                .addOnFailureListener(callback::onError);
    }

    public void register(String fullName, String username, String email, String password,
                          String bio, String photoUrl, Callback callback) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser firebaseUser = result.getUser();
                    if (firebaseUser == null) {
                        callback.onError(new Exception("Registration failed. Please try again."));
                        return;
                    }
                    String uid = firebaseUser.getUid();
                    User user = new User(uid, fullName, username, email, photoUrl, bio);

                    Map<String, Object> data = user.toMap();
                    data.put("createdAt", com.google.firebase.firestore.FieldValue.serverTimestamp());
                    data.put("lastSeen", com.google.firebase.firestore.FieldValue.serverTimestamp());

                    firestore.collection(Constants.COLLECTION_USERS).document(uid)
                            .set(data)
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    public void login(String email, String password, Callback callback) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void sendPasswordReset(String email, Callback callback) {
        auth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void logout() {
        String uid = getCurrentUser() != null ? getCurrentUser().getUid() : null;
        if (uid != null) {
            Map<String, Object> offline = new HashMap<>();
            offline.put(Constants.FIELD_IS_ONLINE, false);
            offline.put(Constants.FIELD_LAST_SEEN, com.google.firebase.firestore.FieldValue.serverTimestamp());
            firestore.collection(Constants.COLLECTION_USERS).document(uid).update(offline);
        }
        auth.signOut();
    }
}
