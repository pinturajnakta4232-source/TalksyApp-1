package com.talksyapp.chat.repositories;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;
import com.talksyapp.chat.models.User;
import com.talksyapp.chat.utils.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * All reads/writes against users/{uid} in Firestore — real backend only.
 */
public class UserRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface UserCallback {
        void onSuccess(User user);
        void onError(Exception e);
    }

    public interface UsersCallback {
        void onSuccess(List<User> users);
        void onError(Exception e);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception e);
    }

    public void getUser(String uid, UserCallback callback) {
        firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    User user = doc.toObject(User.class);
                    if (user != null) user.setUid(doc.getId());
                    callback.onSuccess(user);
                })
                .addOnFailureListener(callback::onError);
    }

    public ListenerRegistration listenToUser(String uid, UserCallback callback) {
        return firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .addSnapshotListener((doc, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    if (doc != null && doc.exists()) {
                        User user = doc.toObject(User.class);
                        if (user != null) user.setUid(doc.getId());
                        callback.onSuccess(user);
                    }
                });
    }

    /** Searches by username or full name (case-insensitive prefix match). */
    public void searchUsers(String query, String excludeUid, UsersCallback callback) {
        String lower = query.toLowerCase().trim();
        if (lower.isEmpty()) {
            callback.onSuccess(new ArrayList<>());
            return;
        }
        firestore.collection(Constants.COLLECTION_USERS)
                .orderBy(Constants.FIELD_USERNAME_LOWER)
                .startAt(lower)
                .endAt(lower + "\uf8ff")
                .limit(25)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<User> results = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        if (doc.getId().equals(excludeUid)) continue;
                        User user = doc.toObject(User.class);
                        if (user != null) {
                            user.setUid(doc.getId());
                            results.add(user);
                        }
                    }
                    callback.onSuccess(results);
                })
                .addOnFailureListener(callback::onError);
    }

    public void updateProfile(String uid, Map<String, Object> updates, SimpleCallback callback) {
        firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .update(updates)
                .addOnSuccessListener(unused -> callback.onSuccess())
                .addOnFailureListener(callback::onError);
    }

    public void setOnlineStatus(String uid, boolean isOnline) {
        if (uid == null) return;
        firestore.collection(Constants.COLLECTION_USERS).document(uid)
                .update(
                        Constants.FIELD_IS_ONLINE, isOnline,
                        Constants.FIELD_LAST_SEEN, FieldValue.serverTimestamp()
                );
    }
}
