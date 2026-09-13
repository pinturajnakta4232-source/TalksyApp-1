package com.talksyapp.chat.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.talksyapp.chat.R;
import com.talksyapp.chat.activities.ChatActivity;
import com.talksyapp.chat.utils.Constants;

/**
 * Receives push notifications delivered via Firebase Cloud Messaging.
 *
 * IMPORTANT: This service only *receives* and displays notifications.
 * Actually *sending* a push to another device requires a trusted server
 * (e.g. a Cloud Function triggered on new-message writes) that holds the
 * Firebase Admin SDK service-account credentials. Those credentials must
 * NEVER be bundled inside this Android app or committed to GitHub — see
 * the /functions folder and README for the secure server-side approach.
 */
public class TalksyMessagingService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "talksyapp_messages";

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        String uid = FirebaseAuth.getInstance().getUid();
        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection(Constants.COLLECTION_USERS)
                    .document(uid)
                    .update("fcmToken", token);
        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String senderName = "TalksyApp";
        String messagePreview = "You have a new message";
        String senderId = null;

        if (remoteMessage.getData() != null && !remoteMessage.getData().isEmpty()) {
            senderName = remoteMessage.getData().getOrDefault("senderName", senderName);
            messagePreview = remoteMessage.getData().getOrDefault("messagePreview", messagePreview);
            senderId = remoteMessage.getData().get("senderId");
        } else if (remoteMessage.getNotification() != null) {
            senderName = remoteMessage.getNotification().getTitle();
            messagePreview = remoteMessage.getNotification().getBody();
        }

        showNotification(senderName, messagePreview, senderId);
    }

    private void showNotification(String title, String body, String senderId) {
        createChannelIfNeeded();

        Intent intent = new Intent(this, ChatActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (senderId != null) {
            intent.putExtra(Constants.EXTRA_RECEIVER_ID, senderId);
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify((int) System.currentTimeMillis(), builder.build());
        }
    }

    private void createChannelIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "Messages", NotificationManager.IMPORTANCE_HIGH);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }
}
