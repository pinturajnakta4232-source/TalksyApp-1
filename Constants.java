package com.talksyapp.chat.utils;

public class Constants {
    // Firestore collections
    public static final String COLLECTION_USERS = "users";
    public static final String COLLECTION_CONVERSATIONS = "conversations";
    public static final String COLLECTION_MESSAGES = "messages";

    // Firestore fields
    public static final String FIELD_PARTICIPANTS = "participants";
    public static final String FIELD_USERNAME_LOWER = "usernameLower";
    public static final String FIELD_FULL_NAME = "fullName";
    public static final String FIELD_IS_ONLINE = "isOnline";
    public static final String FIELD_LAST_SEEN = "lastSeen";
    public static final String FIELD_TIMESTAMP = "timestamp";
    public static final String FIELD_LAST_MESSAGE_TIME = "lastMessageTime";

    // Storage paths
    public static final String STORAGE_PROFILE_PHOTOS = "profile_photos";

    // Intent extras
    public static final String EXTRA_RECEIVER_ID = "extra_receiver_id";
    public static final String EXTRA_RECEIVER_NAME = "extra_receiver_name";
    public static final String EXTRA_RECEIVER_PHOTO = "extra_receiver_photo";

    // SharedPreferences
    public static final String PREFS_NAME = "talksyapp_prefs";

    private Constants() {}
}
