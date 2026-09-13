package com.talksyapp.chat.repositories;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.talksyapp.chat.models.Conversation;
import com.talksyapp.chat.models.Message;
import com.talksyapp.chat.utils.Constants;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Real-time chat backend layer. Every message is written to and read
 * from Cloud Firestore via snapshot listeners — no polling, no local
 * or mock message storage of any kind.
 */
public class ChatRepository {

    private final FirebaseFirestore firestore = FirebaseFirestore.getInstance();

    public interface MessagesCallback {
        void onMessagesChanged(List<Message> messages);
        void onError(Exception e);
    }

    public interface ConversationsCallback {
        void onConversationsChanged(List<Conversation> conversations);
        void onError(Exception e);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(Exception e);
    }

    /**
     * Ensures the conversation document exists (deterministic id) so the
     * two users always resolve to the exact same thread.
     */
    public void ensureConversation(String uidA, String uidB, SimpleCallback callback) {
        String conversationId = Conversation.buildConversationId(uidA, uidB);
        firestore.collection(Constants.COLLECTION_CONVERSATIONS).document(conversationId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        callback.onSuccess();
                        return;
                    }
                    Map<String, Object> data = new HashMap<>();
                    data.put(Constants.FIELD_PARTICIPANTS, Arrays.asList(uidA, uidB));
                    data.put("lastMessage", "");
                    data.put("lastMessageTime", FieldValue.serverTimestamp());
                    data.put("lastMessageSenderId", "");
                    firestore.collection(Constants.COLLECTION_CONVERSATIONS).document(conversationId)
                            .set(data)
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    /** Sends a real message and updates the parent conversation's preview fields. */
    public void sendMessage(String senderUid, String receiverUid, String text, SimpleCallback callback) {
        String conversationId = Conversation.buildConversationId(senderUid, receiverUid);
        Message message = new Message(senderUid, receiverUid, text);

        firestore.collection(Constants.COLLECTION_CONVERSATIONS).document(conversationId)
                .collection(Constants.COLLECTION_MESSAGES)
                .add(message.toMap())
                .addOnSuccessListener(docRef -> {
                    Map<String, Object> conversationUpdate = new HashMap<>();
                    conversationUpdate.put("lastMessage", text);
                    conversationUpdate.put("lastMessageTime", FieldValue.serverTimestamp());
                    conversationUpdate.put("lastMessageSenderId", senderUid);

                    firestore.collection(Constants.COLLECTION_CONVERSATIONS).document(conversationId)
                            .set(conversationUpdate, com.google.firebase.firestore.SetOptions.merge())
                            .addOnSuccessListener(unused -> callback.onSuccess())
                            .addOnFailureListener(callback::onError);
                })
                .addOnFailureListener(callback::onError);
    }

    /** Real-time listener for a single conversation's messages, oldest to newest. */
    public ListenerRegistration listenToMessages(String uidA, String uidB, MessagesCallback callback) {
        String conversationId = Conversation.buildConversationId(uidA, uidB);
        return firestore.collection(Constants.COLLECTION_CONVERSATIONS).document(conversationId)
                .collection(Constants.COLLECTION_MESSAGES)
                .orderBy(Constants.FIELD_TIMESTAMP, Query.Direction.ASCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    List<Message> messages = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Message message = doc.toObject(Message.class);
                            if (message != null) {
                                message.setMessageId(doc.getId());
                                messages.add(message);
                            }
                        }
                    }
                    callback.onMessagesChanged(messages);
                });
    }

    /** Real-time listener for the logged-in user's conversation list, newest first. */
    public ListenerRegistration listenToConversations(String currentUid, ConversationsCallback callback) {
        return firestore.collection(Constants.COLLECTION_CONVERSATIONS)
                .whereArrayContains(Constants.FIELD_PARTICIPANTS, currentUid)
                .orderBy(Constants.FIELD_LAST_MESSAGE_TIME, Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        callback.onError(error);
                        return;
                    }
                    List<Conversation> conversations = new ArrayList<>();
                    if (snapshot != null) {
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            Conversation conversation = doc.toObject(Conversation.class);
                            if (conversation != null) {
                                conversation.setConversationId(doc.getId());
                                conversations.add(conversation);
                            }
                        }
                    }
                    callback.onConversationsChanged(conversations);
                });
    }

    /** Marks all messages sent by the other user in this thread as read. */
    public void markMessagesAsRead(String currentUid, String otherUid) {
        String conversationId = Conversation.buildConversationId(currentUid, otherUid);
        firestore.collection(Constants.COLLECTION_CONVERSATIONS).document(conversationId)
                .collection(Constants.COLLECTION_MESSAGES)
                .whereEqualTo("receiverId", currentUid)
                .whereEqualTo("status", Message.STATUS_SENT)
                .get()
                .addOnSuccessListener((QuerySnapshot snapshot) -> {
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        doc.getReference().update(
                                "status", Message.STATUS_READ,
                                "readAt", FieldValue.serverTimestamp()
                        );
                    }
                });
    }
}
