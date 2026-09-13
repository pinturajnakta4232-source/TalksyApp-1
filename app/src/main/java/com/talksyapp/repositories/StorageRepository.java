package com.talksyapp.chat.repositories;

import android.net.Uri;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.talksyapp.chat.utils.Constants;

import java.util.UUID;

/**
 * Handles real profile photo uploads to Firebase Storage. The returned
 * download URL is what gets stored in the user's Firestore document.
 */
public class StorageRepository {

    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    public interface UploadCallback {
        void onSuccess(String downloadUrl);
        void onError(Exception e);
    }

    public void uploadProfilePhoto(String uid, Uri localImageUri, UploadCallback callback) {
        String fileName = uid + "_" + UUID.randomUUID() + ".jpg";
        StorageReference ref = storage.getReference()
                .child(Constants.STORAGE_PROFILE_PHOTOS)
                .child(fileName);

        ref.putFile(localImageUri)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl()
                        .addOnSuccessListener(uri -> callback.onSuccess(uri.toString()))
                        .addOnFailureListener(callback::onError))
                .addOnFailureListener(callback::onError);
    }
}
