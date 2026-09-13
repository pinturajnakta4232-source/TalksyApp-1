/**
 * Secure server-side push notification trigger for TalksyApp.
 *
 * This is the ONLY place the Firebase Admin SDK / service-account
 * credentials should ever be used. Deploy this with the Firebase CLI
 * (`firebase deploy --only functions`) — never bundle these
 * credentials inside the Android app or commit them to GitHub.
 *
 * Trigger: fires whenever a new message document is created at
 * conversations/{conversationId}/messages/{messageId}, then looks up
 * the receiver's saved FCM token and sends a real push notification.
 */
const { onDocumentCreated } = require("firebase-functions/v2/firestore");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");
const { getMessaging } = require("firebase-admin/messaging");

initializeApp();
const db = getFirestore();
const messaging = getMessaging();

exports.sendMessageNotification = onDocumentCreated(
  "conversations/{conversationId}/messages/{messageId}",
  async (event) => {
    const message = event.data.data();
    if (!message) return;

    const { senderId, receiverId, text } = message;
    if (!receiverId) return;

    const receiverDoc = await db.collection("users").doc(receiverId).get();
    const receiver = receiverDoc.data();
    if (!receiver || !receiver.fcmToken) return;

    const senderDoc = await db.collection("users").doc(senderId).get();
    const senderName = senderDoc.exists ? senderDoc.data().fullName : "Someone";

    await messaging.send({
      token: receiver.fcmToken,
      data: {
        senderId,
        senderName,
        messagePreview: text || "",
      },
      android: { priority: "high" },
    });
  }
);
