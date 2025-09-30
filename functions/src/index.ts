/** CLEANED: Consolidated file. Duplicate block removed. */
import * as functions from "firebase-functions/v2";
import {setGlobalOptions} from "firebase-functions/v2/options";
import {HttpsError, CallableRequest} from "firebase-functions/v2/https";
import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();

// Use your preferred region
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

interface GeneralNotificationData {
  userId: string;
  title: string;
  body: string;
  imageUrl?: string;
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
        functions.logger.error(
          `UID mismatch: ${request.auth.uid} vs ${fromUserId}`
        );
        throw new HttpsError(
          "permission-denied",
          "You can only send notifications as yourself"
        );
      }

      // USE ADMIN SDK TO BYPASS SECURITY RULES
      const userDoc = await admin.firestore()
        .collection("users")
        .doc(recipientUserId)
        .get();

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
  async (request: CallableRequest<FriendRequestData>) => {
    functions.logger.info("Function called with data:", request.data);
    functions.logger.info("Auth context:", request.auth);

    try {
      if (!request.auth) {
        functions.logger.error("Unauthenticated request");
        throw new functions.https.HttpsError(
          "unauthenticated",
          "Authentication required"
        );
      }

      const {recipientUserId, fromUserId, fromUserName, requestId} = request.data;

      functions.logger.info("Parsed data:", {
        recipientUserId,
        fromUserId,
        fromUserName,
        requestId,
      });

      // Validate all required fields
      if (!recipientUserId || !fromUserId || !requestId) {
        functions.logger.error("Missing required fields");
        throw new functions.https.HttpsError(
          "invalid-argument",
          "Missing required fields"
        );
      }

      // Verify caller is the sender
      if (request.auth.uid !== fromUserId) {
        functions.logger.error(
          `UID mismatch: ${request.auth.uid} vs ${fromUserId}`
        );
        throw new functions.https.HttpsError(
          "permission-denied",
          "Permission denied"
        );
      }

      // Check if recipient exists
      functions.logger.info("Checking recipient:", recipientUserId);
      const recipientDoc = await admin.firestore()
        .collection("users")
        .doc(recipientUserId)
        .get();

      if (!recipientDoc.exists) {
        functions.logger.error("Recipient not found:", recipientUserId);
        throw new functions.https.HttpsError("not-found", "Recipient not found");
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
        .collection("users")
        .doc(recipientUserId)
        .collection("notifications")
        .doc(requestId)
        .set(notificationData);

      functions.logger.info("Notification created successfully");

      // Try to send FCM notification (optional)
      try {
        const fcmToken = recipientDoc.data()?.fcmToken;
        if (fcmToken) {
          functions.logger.info(`Sending FCM to user ${recipientUserId}`);
          await admin.messaging().send({
            token: fcmToken,
            notification: {
              title: "Friend Request",
              body: `${fromUserName} wants to be your friend`,
            },
            data: {
              type: "friend_request",
              requestId: requestId,
            },
          });
          functions.logger.info("FCM sent successfully");
        } else {
          functions.logger.warn("No FCM token for user:", recipientUserId);
        }
      } catch (fcmError) {
        functions.logger.warn("FCM failed, continuing:", fcmError);
        // Don't throw - FCM failure shouldn't fail the whole operation
      }

      return {success: true, message: "Notification processed"};
    } catch (error) {
      functions.logger.error("FUNCTION ERROR DETAILS:", {
        error: error instanceof Error ? error.message : String(error),
        stack: error instanceof Error ? error.stack : undefined,
        data: request.data,
      });

      // Re-throw with proper error type
      if (error instanceof functions.https.HttpsError) {
        throw error;
      }
      throw new functions.https.HttpsError("internal", "Internal server error");
    }
  }
);

/**
 * General notification callable (used by app NotificationRepositoryImpl)
 */
export const sendGeneralNotification = functions.https.onCall(
  async (request: CallableRequest<GeneralNotificationData>) => {
    try {
      const {userId, title, body, imageUrl} = request.data || {};

      if (!userId || !title || !body) {
        throw new HttpsError("invalid-argument", "userId, title and body are required");
      }

      // Recipient doc
      const userDoc = await admin.firestore().collection("users").doc(userId).get();
      if (!userDoc.exists) {
        throw new HttpsError("not-found", "Recipient user not found");
      }

      const fcmToken = userDoc.data()?.fcmToken as string | undefined;

      // Persist to user notifications collection
      const notifRef = admin.firestore()
        .collection("users")
        .doc(userId)
        .collection("notifications")
        .doc();

      const notificationData: Record<string, unknown> = {
        type: "general",
        recipientUserId: userId,
        title,
        body,
        imageUrl: imageUrl || null,
        timestamp: admin.firestore.FieldValue.serverTimestamp(),
        isRead: false,
      };

      await notifRef.set(notificationData);

      // Send FCM if token available (best-effort)
      if (fcmToken) {
        try {
          await admin.messaging().send({
            token: fcmToken,
            notification: {title, body},
            data: {
              type: "general",
              notificationId: notifRef.id,
            },
            android: {
              priority: "high",
              notification: {channelId: "default_channel"},
            },
          });
        } catch (e) {
          functions.logger.warn("FCM send failed for general notification", e);
        }
      } else {
        functions.logger.info(`No FCM token for user ${userId}, stored only`);
      }

      return {success: true, id: notifRef.id};
    } catch (error) {
      if (error instanceof HttpsError) throw error;
      functions.logger.error("sendGeneralNotification failed:", error);
      throw new HttpsError("internal", "Internal server error");
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


export const dueDateReminders = functions.scheduler.onSchedule(
  "every 15 minutes",
  async () => {
    const now = new Date();
    const WINDOWS = [
      {key: "24h", minutesBefore: 24 * 60},
      {key: "1h", minutesBefore: 60},
      {key: "due", minutesBefore: 0},
      {key: "overdue", minutesBefore: -1},
    ] as const;

    const parseDue = (due: unknown): Date | null => {
      if (typeof due !== "string" || !due) return null;
      const withTime = new Date(due);
      if (!isNaN(withTime.getTime())) return withTime;
      if (/^\d{4}-\d{2}-\d{2}$/.test(due)) {
        const [y, m, d] = due.split("-").map((p) => parseInt(p, 10));
        return new Date(y, m - 1, d, 9, 0, 0, 0);
      }
      return null;
    };

    const projectsSnap = await db.collection("projects").get();
    for (const projectDoc of projectsSnap.docs) {
      const projectId = projectDoc.id;
      const tasksSnap = await db.collection("projects").doc(projectId).collection("tasks").where("isCompleted", "==", false).get();
      for (const taskDoc of tasksSnap.docs) {
        const data = taskDoc.data() as any;
        const assignedTo: string[] = Array.isArray(data.assignedTo) ? data.assignedTo : [];
        const title: string = typeof data.title === "string" ? data.title : "Task";
        const dueStr: string = typeof data.dueDate === "string" ? data.dueDate : "";
        const lastReminderSent: Record<string, any> = data.lastReminderSent || {};
        const dueAt = parseDue(dueStr);
        if (!dueAt) continue;
        const minutesUntilDue = Math.round((dueAt.getTime() - now.getTime()) / 60000);

        for (const w of WINDOWS) {
          let shouldSend = false;
          if (w.key === "overdue") {
            shouldSend = minutesUntilDue < 0;
          } else {
            const target = -w.minutesBefore;
            const delta = Math.abs(minutesUntilDue - target);
            shouldSend = delta <= 8;
          }
          if (!shouldSend) continue;

          const sentTs = lastReminderSent[w.key]?.toDate?.() || (lastReminderSent[w.key] instanceof Date ? lastReminderSent[w.key] : null);
          if (sentTs && (now.getTime() - new Date(sentTs).getTime()) < 12 * 60 * 60000) continue;

          if (assignedTo.length === 0) continue;

          const tokens: string[] = [];
          for (const uid of assignedTo) {
            const userDoc = await db.collection("users").doc(uid).get();
            if (!userDoc.exists) continue;
            const u = userDoc.data() as any;
            if (u?.fcmToken && typeof u.fcmToken === "string") tokens.push(u.fcmToken);
            if (u?.fcmTokens && typeof u.fcmTokens === "object") tokens.push(...(Object.values(u.fcmTokens) as string[]).filter(Boolean));
          }
          const uniqueTokens = Array.from(new Set(tokens)).filter(Boolean);
          if (uniqueTokens.length === 0) continue;

          const body = w.key === "24h" ? `Due tomorrow: ${title}` : w.key === "1h" ? `Due in 1 hour: ${title}` : w.key === "due" ? `Due now: ${title}` : `Overdue: ${title}`;

          await admin.messaging().sendEachForMulticast({
            tokens: uniqueTokens,
            notification: {title: "Task Reminder", body},
            data: {type: "due_reminder", when: w.key as string, projectId, taskId: taskDoc.id},
            android: {priority: "high", notification: {channelId: "default_notification_channel_id"}},
          });

          await taskDoc.ref.set({
            lastReminderSent: {...lastReminderSent, [w.key]: admin.firestore.FieldValue.serverTimestamp()},
          }, {merge: true});
        }
      }
    }
  }
);


/**
 * Security note about FCM server keys:
 * - Never put FCM server keys (legacy HTTP keys or any server credentials) in the Android client or Gradle files.
 * - This Cloud Functions backend authenticates with a service account via the Admin SDK; no server key is required for admin.messaging().
 * - If you must keep any notification-related secrets, store them server-side only:
 *   Option A) Firebase Functions runtime config
 *     - Set:  firebase functions:config:set notifications.key="<your-secret>"
 *     - Read: const notifKey = functions.config().notifications?.key as string | undefined;
 *   Option B) Google Cloud Secret Manager (recommended for higher security)
 *     - Create a secret and grant access to the Functions service account.
 *     - Access the secret at runtime using the Secret Manager client library.
 */
