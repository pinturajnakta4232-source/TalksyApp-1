package com.talksyapp.chat.models;

import com.google.firebase.firestore.Exclude;
import java.util.Date;
import java.util.List;

/**
 * Represents a conversation document at conversations/{conversationId}.
 * The conversationId itself is deterministic: smallerUid_largerUid,
 * so the same two users always land on the same conversation thread.
 */
public class Conversation {

    private String conversationId;
    private List<String> participants;
    private String lastMessage;
    private Date lastMessageTime;
    private String lastMessageSenderId;

    // Fields below are populated client-side after joining with the
    // "other" user's profile — not stored directly in this document.
    @Exclude private String otherUserName;
    @Exclude private String otherUserPhotoUrl;
    @Exclude private String otherUserId;
    @Exclude private boolean otherUserOnline;
    @Exclude private int unreadCount;

    public Conversation() {
        // Required empty constructor for Firestore
    }

    public static String buildConversationId(String uidA, String uidB) {
        if (uidA == null || uidB == null) return null;
        return uidA.compareTo(uidB) < 0 ? uidA + "_" + uidB : uidB + "_" + uidA;
    }

    public String getConversationId() { return conversationId; }
    public void setConversationId(String conversationId) { this.conversationId = conversationId; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Date getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(Date lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public String getLastMessageSenderId() { return lastMessageSenderId; }
    public void setLastMessageSenderId(String lastMessageSenderId) { this.lastMessageSenderId = lastMessageSenderId; }

    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }

    public String getOtherUserPhotoUrl() { return otherUserPhotoUrl; }
    public void setOtherUserPhotoUrl(String otherUserPhotoUrl) { this.otherUserPhotoUrl = otherUserPhotoUrl; }

    public String getOtherUserId() { return otherUserId; }
    public void setOtherUserId(String otherUserId) { this.otherUserId = otherUserId; }

    public boolean isOtherUserOnline() { return otherUserOnline; }
    public void setOtherUserOnline(boolean otherUserOnline) { this.otherUserOnline = otherUserOnline; }

    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
}
