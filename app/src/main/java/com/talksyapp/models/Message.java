package com.talksyapp.chat.models;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.ServerTimestamp;
import com.google.firebase.firestore.FieldValue;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents one message document at
 * conversations/{conversationId}/messages/{messageId}
 */
public class Message {

    public static final String STATUS_SENT = "sent";
    public static final String STATUS_DELIVERED = "delivered";
    public static final String STATUS_READ = "read";

    private String messageId;
    private String senderId;
    private String receiverId;
    private String text;
    @ServerTimestamp
    private Date timestamp;
    private String status;
    private Date readAt;

    public Message() {
        // Required empty constructor for Firestore
    }

    public Message(String senderId, String receiverId, String text) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.text = text;
        this.status = STATUS_SENT;
    }

    @Exclude
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("senderId", senderId);
        map.put("receiverId", receiverId);
        map.put("text", text);
        map.put("timestamp", FieldValue.serverTimestamp());
        map.put("status", status == null ? STATUS_SENT : status);
        return map;
    }

    public String getMessageId() { return messageId; }
    public void setMessageId(String messageId) { this.messageId = messageId; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getReceiverId() { return receiverId; }
    public void setReceiverId(String receiverId) { this.receiverId = receiverId; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public Date getTimestamp() { return timestamp; }
    public void setTimestamp(Date timestamp) { this.timestamp = timestamp; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Date getReadAt() { return readAt; }
    public void setReadAt(Date readAt) { this.readAt = readAt; }
}
