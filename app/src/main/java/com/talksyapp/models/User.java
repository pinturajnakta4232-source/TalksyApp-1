package com.talksyapp.chat.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a TalksyApp user profile stored at users/{uid} in Firestore.
 * Every field here is written only through real Firebase Authentication +
 * Firestore calls — there is no local/mock user data anywhere in this model.
 */
public class User {

    private String uid;
    private String fullName;
    private String username;
    private String usernameLower; // used for case-insensitive search
    private String email;
    private String photoUrl;
    private String bio;
    private boolean isOnline;
    @ServerTimestamp
    private Date lastSeen;
    @ServerTimestamp
    private Date createdAt;

    public User() {
        // Required empty constructor for Firestore deserialization
    }

    public User(String uid, String fullName, String username, String email,
                String photoUrl, String bio) {
        this.uid = uid;
        this.fullName = fullName;
        this.username = username;
        this.usernameLower = username != null ? username.toLowerCase() : null;
        this.email = email;
        this.photoUrl = photoUrl;
        this.bio = bio;
        this.isOnline = true;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("fullName", fullName);
        map.put("username", username);
        map.put("usernameLower", username != null ? username.toLowerCase() : null);
        map.put("email", email);
        map.put("photoUrl", photoUrl);
        map.put("bio", bio);
        map.put("isOnline", isOnline);
        return map;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username;
        this.usernameLower = username != null ? username.toLowerCase() : null;
    }

    public String getUsernameLower() { return usernameLower; }
    public void setUsernameLower(String usernameLower) { this.usernameLower = usernameLower; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhotoUrl() { return photoUrl; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }

    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }

    public Date getLastSeen() { return lastSeen; }
    public void setLastSeen(Date lastSeen) { this.lastSeen = lastSeen; }

    public Date getCreatedAt() { return createdAt; }
    public void setCreatedAt(Date createdAt) { this.createdAt = createdAt; }
}
