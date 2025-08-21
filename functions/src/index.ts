import * as functions from "firebase-functions/v2";
import {setGlobalOptions} from "firebase-functions/v2/options";
import {HttpsError, CallableRequest} from "firebase-functions/v2/https";
import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

// Use your preferred region — I kept your original
setGlobalOptions({region: "europe-west1"});

interface ProjectInvitationData {
  recipientUserId: string;
  projectId: string;
  projectName: string;
  fromUserId: string;
  fromUserName: string;
  invitationId: string;
}

interface FriendRequestData {
  recipientUserId: string;
  fromUserId: string;
  fromUserName: string;
  requestId: string;
}

/**
 * Handles project invitation notifications
 */
export const sendProjectInvitationNotification = functions.https.onCall(
  async (request: CallableRequest<ProjectInvitationData>) => {
    try {
      if (!request.auth) {
        functions.logger.error("Unauthenticated request");
        throw new HttpsError("unauthenticated", "Authentication required");
      }

      const {
        recipientUserId,
        projectId,
        projectName,
        fromUserId,
        fromUserName,
        invitationId,
      } = request.data;

      // Validate all required fields
      if (!recipientUserId || !projectId || !fromUserId || !invitationId) {
        throw new HttpsError("invalid-argument", "Missing required fields");
      }

      if (request.auth.uid !== fromUserId) {
        functions.logger.error(`UID mismatch: ${request.auth.uid} vs ${fromUserId}`);
        throw new HttpsError("permission-denied", "You can only send notifications as yourself");
      }

      // USE ADMIN SDK TO BYPASS SECURITY RULES
      const userDoc = await admin.firestore().collection("users").doc(recipientUserId).get();

      if (!userDoc.exists) {
        throw new HttpsError("not-found", "Recipient user not found");
      }

      const fcmToken = userDoc.data()?.fcmToken;
      if (!fcmToken) {
        functions.logger.warn(`No FCM token for user ${recipientUserId}`);
      }

      const notificationData = {
        type: "project_invitation",
        recipientUserId,
        projectId,
        projectName: projectName || "a project",
        fromUserId,
        fromUserName: fromUserName || "a user",
        invitationId,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        isRead: false,
      };

      // USE ADMIN SDK FOR ALL FIRESTORE OPERATIONS
      const notificationRef = admin.firestore()
        .collection("users")
        .doc(recipientUserId)
        .collection("notifications")
        .doc(invitationId);

      await notificationRef.set(notificationData);

      // Send FCM message
      if (fcmToken) {
        try {
          await admin.messaging().send({
            token: fcmToken,
            notification: {
              title: "Project Invitation",
              body: `${fromUserName} invited you to ${projectName}`,
            },
            data: {
              type: "project_invitation",
              projectId,
              invitationId,
              click_action: "OPEN_PROJECT_INVITES",
            },
            android: {
              priority: "high",
              notification: {
                channelId: "default_channel",
              },
            },
          });
          functions.logger.info(`FCM sent to ${recipientUserId}`);
        } catch (error) {
          functions.logger.error("FCM send error:", error);
        }
      }

      return {success: true};
    } catch (error) {
      functions.logger.error("Notification failed:", error);
      throw error;
    }
  }
);

/**
 * Handles friend request notifications
 */
export const sendFriendRequestNotification = functions.https.onCall(
  async (request: CallableRequest<any>) => {
    functions.logger.info("Function called with data:", request.data);
    functions.logger.info("Auth context:", request.auth);

    try {
      if (!request.auth) {
        functions.logger.error("Unauthenticated request");
        throw new functions.https.HttpsError('unauthenticated', 'Authentication required');
      }

      const { recipientUserId, fromUserId, fromUserName, requestId } = request.data;

      functions.logger.info("Parsed data:", {
        recipientUserId,
        fromUserId,
        fromUserName,
        requestId
      });

      // Validate all required fields
      if (!recipientUserId || !fromUserId || !requestId) {
        functions.logger.error("Missing required fields");
        throw new functions.https.HttpsError('invalid-argument', 'Missing required fields');
      }

      // Verify caller is the sender
      if (request.auth.uid !== fromUserId) {
        functions.logger.error(`UID mismatch: ${request.auth.uid} vs ${fromUserId}`);
        throw new functions.https.HttpsError('permission-denied', 'Permission denied');
      }

      // Check if recipient exists
      functions.logger.info("Checking recipient:", recipientUserId);
      const recipientDoc = await admin.firestore().collection('users').doc(recipientUserId).get();

      if (!recipientDoc.exists) {
        functions.logger.error("Recipient not found:", recipientUserId);
        throw new functions.https.HttpsError('not-found', 'Recipient not found');
      }

      // Create notification
      const notificationData = {
        type: "friend_request",
        recipientUserId,
        fromUserId,
        fromUserName: fromUserName || "Someone",
        requestId,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        isRead: false,
      };

      functions.logger.info("Creating notification:", notificationData);

      await admin.firestore()
        .collection('users')
        .doc(recipientUserId)
        .collection('notifications')
        .doc(requestId)
        .set(notificationData);

      functions.logger.info("Notification created successfully");

      // Try to send FCM notification (optional)
      try {
        const fcmToken = recipientDoc.data()?.fcmToken;
        if (fcmToken) {
          functions.logger.info("Sending FCM to token:", fcmToken);
          await admin.messaging().send({
            token: fcmToken,
            notification: {
              title: "Friend Request",
              body: `${fromUserName} wants to be your friend`,
            },
            data: {
              type: "friend_request",
              requestId: requestId,
            }
          });
          functions.logger.info("FCM sent successfully");
        } else {
          functions.logger.warn("No FCM token for user:", recipientUserId);
        }
      } catch (fcmError) {
        functions.logger.warn("FCM failed, continuing:", fcmError);
        // Don't throw - FCM failure shouldn't fail the whole operation
      }

      return { success: true, message: "Notification processed" };

    } catch (error) {
      functions.logger.error("FUNCTION ERROR DETAILS:", {
        error: error instanceof Error ? error.message : String(error),
        stack: error instanceof Error ? error.stack : undefined,
        data: request.data
      });

      // Re-throw with proper error type
      if (error instanceof functions.https.HttpsError) {
        throw error;
      }
      throw new functions.https.HttpsError('internal', 'Internal server error');
    }
  }
);

/**
 * Scheduled cleanup of old notifications
 */
export const cleanOldNotifications = functions.scheduler.onSchedule(
  "every day 03:00",
  async () => {
    const cutoffDate = new Date();
    cutoffDate.setDate(cutoffDate.getDate() - 30);

    const usersSnapshot = await db.collection("users").get();
    const batch = db.batch();

    for (const userDoc of usersSnapshot.docs) {
      const oldNotifications = await db
        .collection("users")
        .doc(userDoc.id)
        .collection("notifications")
        .where("timestamp", "<", cutoffDate)
        .get();

      oldNotifications.forEach((doc) => {
        batch.delete(doc.ref);
      });
    }

    await batch.commit();
    functions.logger.info("Cleaned up old notifications");
  }
);
