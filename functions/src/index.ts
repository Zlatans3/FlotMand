import * as functions from "firebase-functions/v1";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();

interface UserRow {
  uid: string;
  fcmToken?: string;
}

/** Fetch all users except [excludeUid], returning their uid and FCM token. */
async function getUsersExcluding(excludeUid: string): Promise<UserRow[]> {
  const snapshot = await db.collection("users").get();
  const rows: UserRow[] = [];
  snapshot.forEach((doc) => {
    if (doc.id !== excludeUid) {
      rows.push({ uid: doc.id, fcmToken: doc.data().fcmToken });
    }
  });
  return rows;
}

/** Send a multicast FCM push and write in-app notification documents. */
async function fanOut(
  users: UserRow[],
  type: "event" | "poll",
  title: string,
  body: string,
  referenceId: string,
  senderPhotoUrl: string = "",
  senderDisplayName: string = "",
): Promise<void> {
  // ── 1. FCM push ────────────────────────────────────────────────────────────
  const tokens = users.map((u) => u.fcmToken ?? "").filter(Boolean);
  if (tokens.length > 0) {
    const message: admin.messaging.MulticastMessage = {
      tokens,
      data: { title, body },
      notification: { title, body },
      android: {
        priority: "high",
        notification: { channelId: "flotmand_events" },
      },
      apns: {
        payload: { aps: { sound: "default" } },
      },
    };
    const response = await admin.messaging().sendEachForMulticast(message);
    functions.logger.info(
      `FCM: ${response.successCount} ok, ${response.failureCount} failed`,
    );
  }

  // ── 2. In-app notification documents ───────────────────────────────────────
  const batch = db.batch();
  const now = Date.now();
  const payload = {
    type,
    referenceId,
    title,
    body,
    isRead: false,
    createdAtMillis: now,
    senderPhotoUrl,
    senderDisplayName,
  };
  functions.logger.info("Notification payload", payload);
  users.forEach(({ uid }) => {
    const ref = db
      .collection("notifications")
      .doc(uid)
      .collection("items")
      .doc();
    batch.set(ref, payload);
  });
  await batch.commit();
  functions.logger.info(`Wrote ${users.length} notification docs`);
}

// ── Trigger: new event ────────────────────────────────────────────────────────

export const onEventCreated = functions.firestore
  .document("dinnerEvents/{eventId}")
  .onCreate(async (snapshot) => {
    const data = snapshot.data();
    const publisherId: string = data.publisherId ?? "";
    const eventName: string = data.eventName ?? "Nyt event";

    const [publisherDoc, users] = await Promise.all([
      db.collection("users").doc(publisherId).get(),
      getUsersExcluding(publisherId),
    ]);
    const publisherName: string = publisherDoc.data()?.displayName || "En bruger";
    const publisherPhotoUrl: string = publisherDoc.data()?.photoUrl || "";
    await fanOut(
      users,
      "event",
      `🍽️ ${publisherName} har oprettet et nyt event`,
      eventName,
      snapshot.id,
      publisherPhotoUrl,
      publisherName,
    );
  });

// ── Trigger: price set or updated on an event ────────────────────────────────

export const onEventPriceSet = functions.firestore
  .document("dinnerEvents/{eventId}")
  .onUpdate(async (change, context) => {
    const before = change.before.data();
    const after = change.after.data();

    const priceBefore: number | undefined = before.totalPrice;
    const priceAfter: number | undefined = after.totalPrice;

    // Only fire when totalPrice is newly added or changed
    if (priceAfter == null || priceAfter === priceBefore) return;

    const publisherId: string = after.publisherId ?? "";
    const participantIds: string[] = after.participantIds ?? [];
    const eventName: string = after.eventName ?? "Middag";
    const recipientIds = participantIds.filter((id) => id !== publisherId);

    if (recipientIds.length === 0) return;

    const participantCount = participantIds.length > 0 ? participantIds.length : 1;
    const pricePerPerson = priceAfter / participantCount;
    const formatted =
      pricePerPerson % 1 === 0
        ? `${Math.round(pricePerPerson)}`
        : pricePerPerson.toFixed(2);

    const [publisherDoc, ...recipientDocs] = await Promise.all([
      db.collection("users").doc(publisherId).get(),
      ...recipientIds.map((uid) => db.collection("users").doc(uid).get()),
    ]);
    const publisherName: string = publisherDoc.data()?.displayName || "En bruger";
    const publisherPhotoUrl: string = publisherDoc.data()?.photoUrl || "";
    const users: UserRow[] = recipientDocs
      .filter((doc) => doc.exists)
      .map((doc) => ({ uid: doc.id, fcmToken: doc.data()?.fcmToken }));

    await fanOut(
      users,
      "event",
      eventName,
      `Prisen er sat til ${formatted} kr. pr. person`,
      context.params.eventId,
      publisherPhotoUrl,
      publisherName,
    );
  });

// ── Trigger: new poll ─────────────────────────────────────────────────────────

export const onPollCreated = functions.firestore
  .document("dateVotings/{votingId}")
  .onCreate(async (snapshot) => {
    const data = snapshot.data();
    const creatorId: string = data.creatorId ?? data.publisherId ?? "";
    const title: string = data.title ?? data.name ?? "Ny afstemning";

    const [creatorDoc, users] = await Promise.all([
      db.collection("users").doc(creatorId).get(),
      getUsersExcluding(creatorId),
    ]);
    const creatorName: string = creatorDoc.data()?.displayName || "En bruger";
    const creatorPhotoUrl: string = creatorDoc.data()?.photoUrl || "";
    await fanOut(
      users,
      "poll",
      `🗳️ ${creatorName} har oprettet en ny afstemning`,
      title,
      snapshot.id,
      creatorPhotoUrl,
      creatorName,
    );
  });
