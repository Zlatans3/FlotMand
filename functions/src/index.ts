import {setGlobalOptions} from "firebase-functions/v2";
import {
  onDocumentCreated,
  onDocumentUpdated,
} from "firebase-functions/v2/firestore";
import * as functionsV1 from "firebase-functions/v1";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

admin.initializeApp();

const db = admin.firestore();

// As close as Cloud Functions v2 gets to the Firestore database in
// europe-north2 (Stockholm): europe-north2 is not a supported Functions
// region yet, so europe-north1 (Finland) keeps trigger delivery and all
// reads/writes intra-Nordic instead of hopping to us-central1.
setGlobalOptions({region: "europe-north1"});

interface UserRow {
  uid: string;
  fcmToken?: string;
}

/** Fetch all users except [excludeUid], returning their uid and FCM token. */
async function getUsersExcluding(excludeUid: string): Promise<UserRow[]> {
  // select() limits the payload to the fields we need; the read count is
  // the same but we stop shipping every profile field over the wire.
  const snapshot = await db
    .collection("users")
    .select("fcmToken", "isGhostUser")
    .get();
  const rows: UserRow[] = [];
  snapshot.forEach((doc) => {
    // Ghost users are rotation placeholders created by hosts — they can never
    // sign in, so notification docs written for them are pure waste.
    if (doc.id !== excludeUid && doc.data().isGhostUser !== true) {
      rows.push({uid: doc.id, fcmToken: doc.data().fcmToken});
    }
  });
  return rows;
}

/**
 * Notification docs carry an expiresAt timestamp so a Firestore TTL policy
 * (collection group "items", field "expiresAt") can sweep them for free.
 * Without this the per-user collections grow forever and every cold listener
 * attach on a client re-reads all of them.
 */
const NOTIFICATION_TTL_DAYS = 60;

/** Remove FCM tokens that FCM itself reports as permanently dead. */
async function pruneDeadTokens(
  users: UserRow[],
  responses: admin.messaging.SendResponse[],
): Promise<void> {
  const batch = db.batch();
  let pruneCount = 0;
  responses.forEach((res, i) => {
    const code = res.error?.code;
    if (
      code === "messaging/registration-token-not-registered" ||
      code === "messaging/invalid-argument"
    ) {
      batch.update(db.collection("users").doc(users[i].uid), {
        fcmToken: admin.firestore.FieldValue.delete(),
      });
      pruneCount++;
    }
  });
  if (pruneCount > 0) {
    await batch.commit();
    logger.info(`Pruned ${pruneCount} dead FCM token(s)`);
  }
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
  const withTokens = users.filter((u) => u.fcmToken);
  if (withTokens.length > 0) {
    const message: admin.messaging.MulticastMessage = {
      tokens: withTokens.map((u) => u.fcmToken as string),
      data: {title, body},
      notification: {title, body},
      android: {
        priority: "high",
        notification: {channelId: "flotmand_events"},
      },
      apns: {
        payload: {aps: {sound: "default"}},
      },
    };
    const response = await admin.messaging().sendEachForMulticast(message);
    logger.info(
      `FCM: ${response.successCount} ok, ${response.failureCount} failed`,
    );
    if (response.failureCount > 0) {
      await pruneDeadTokens(withTokens, response.responses);
    }
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
    expiresAt: admin.firestore.Timestamp.fromMillis(
      now + NOTIFICATION_TTL_DAYS * 24 * 60 * 60 * 1000,
    ),
  };
  logger.info("Notification payload", payload);
  users.forEach(({uid}) => {
    const ref = db
      .collection("notifications")
      .doc(uid)
      .collection("items")
      .doc();
    batch.set(ref, payload);
  });
  await batch.commit();
  logger.info(`Wrote ${users.length} notification docs`);
}

// ── Trigger: new event ────────────────────────────────────────────────────────

export const onEventCreated = onDocumentCreated(
  "dinnerEvents/{eventId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;
    const data = snapshot.data();
    const publisherId: string = data.publisherId ?? "";
    const eventName: string = data.eventName ?? "Nyt event";

    const [publisherDoc, users] = await Promise.all([
      db.collection("users").doc(publisherId).get(),
      getUsersExcluding(publisherId),
    ]);
    const publisherName: string =
      publisherDoc.data()?.displayName || "En bruger";
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
  },
);

// ── Trigger: price set or updated on an event ────────────────────────────────

export const onEventPriceSet = onDocumentUpdated(
  "dinnerEvents/{eventId}",
  async (event) => {
    if (!event.data) return;
    const before = event.data.before.data();
    const after = event.data.after.data();

    const priceBefore: number | undefined = before.totalPrice;
    const priceAfter: number | undefined = after.totalPrice;

    // Only fire when totalPrice is newly added or changed
    if (priceAfter == null || priceAfter === priceBefore) return;

    const publisherId: string = after.publisherId ?? "";
    const participantIds: string[] = after.participantIds ?? [];
    const eventName: string = after.eventName ?? "Middag";
    const recipientIds = participantIds.filter((id) => id !== publisherId);

    if (recipientIds.length === 0) return;

    const participantCount =
      participantIds.length > 0 ? participantIds.length : 1;
    const pricePerPerson = priceAfter / participantCount;
    const formatted =
      pricePerPerson % 1 === 0 ?
        `${Math.round(pricePerPerson)}` :
        pricePerPerson.toFixed(2);

    const [publisherDoc, ...recipientDocs] = await Promise.all([
      db.collection("users").doc(publisherId).get(),
      ...recipientIds.map((uid) => db.collection("users").doc(uid).get()),
    ]);
    const publisherName: string =
      publisherDoc.data()?.displayName || "En bruger";
    const publisherPhotoUrl: string = publisherDoc.data()?.photoUrl || "";
    const users: UserRow[] = recipientDocs
      .filter((doc) => doc.exists && doc.data()?.isGhostUser !== true)
      .map((doc) => ({uid: doc.id, fcmToken: doc.data()?.fcmToken}));

    await fanOut(
      users,
      "event",
      eventName,
      `Prisen er sat til ${formatted} kr. pr. person`,
      event.params.eventId,
      publisherPhotoUrl,
      publisherName,
    );
  },
);

// ── Trigger: RSVP changed on an event ────────────────────────────────────────

/**
 * Trailing debounce window for RSVP notifications. A user toggling their
 * answer back and forth produces one Firestore update per tap; only the
 * invocation that is still the latest after this delay notifies the host,
 * and it reads the event fresh so the message reflects the final answer.
 */
const RSVP_DEBOUNCE_MS = 20_000;

const sleep = (ms: number) => new Promise((resolve) => setTimeout(resolve, ms));

/** IDs present in exactly one of the two arrays (added or removed). */
function symmetricDiff(before: string[], after: string[]): Set<string> {
  const beforeSet = new Set(before);
  const afterSet = new Set(after);
  const changed = new Set<string>();
  afterSet.forEach((id) => {
    if (!beforeSet.has(id)) changed.add(id);
  });
  beforeSet.forEach((id) => {
    if (!afterSet.has(id)) changed.add(id);
  });
  return changed;
}

export const onEventRsvp = onDocumentUpdated(
  {document: "dinnerEvents/{eventId}", timeoutSeconds: 60},
  async (event) => {
    if (!event.data) return;
    const before = event.data.before.data();
    const after = event.data.after.data();
    const eventId: string = event.params.eventId;

    const changedIds = new Set([
      ...symmetricDiff(before.participantIds ?? [], after.participantIds ?? []),
      ...symmetricDiff(before.declinedIds ?? [], after.declinedIds ?? []),
    ]);
    const publisherId: string = after.publisherId ?? "";
    changedIds.delete(publisherId);
    if (changedIds.size === 0) return;

    const hostDoc = await db.collection("users").doc(publisherId).get();
    const hostToken: string | undefined = hostDoc.data()?.fcmToken;
    if (!hostDoc.exists) return;

    await Promise.all(
      [...changedIds].map(async (uid) => {
        const userDoc = await db.collection("users").doc(uid).get();
        if (!userDoc.exists) return;
        // Ghost users are added/removed by the host themself — no notification.
        if (userDoc.data()?.isGhostUser === true) return;
        const userName: string = userDoc.data()?.displayName || "En bruger";
        const userPhotoUrl: string = userDoc.data()?.photoUrl || "";

        // Trailing debounce: claim the (event, user) slot, wait, and bail out
        // if a newer RSVP change reclaimed it in the meantime.
        const debounceRef = db
          .collection("rsvpDebounce")
          .doc(`${eventId}_${uid}`);
        const claim = Date.now();
        await debounceRef.set({pendingAtMillis: claim});
        await sleep(RSVP_DEBOUNCE_MS);
        const latest = await debounceRef.get();
        if (latest.data()?.pendingAtMillis !== claim) return;
        await debounceRef.delete();

        // Read the event fresh so the message matches the user's final answer.
        const freshDoc = await db.collection("dinnerEvents").doc(eventId).get();
        if (!freshDoc.exists) return;
        const fresh = freshDoc.data() ?? {};
        const attending = (fresh.participantIds ?? []).includes(uid);
        const declined = (fresh.declinedIds ?? []).includes(uid);
        if (!attending && !declined) return; // RSVP was withdrawn entirely

        const eventName: string = fresh.eventName ?? "Middag";
        const body = attending ?
          `✅ ${userName} deltager` :
          `❌ ${userName} deltager ikke`;

        await fanOut(
          [{uid: publisherId, fcmToken: hostToken}],
          "event",
          eventName,
          body,
          eventId,
          userPhotoUrl,
          userName,
        );
      }),
    );
  },
);

// ── Trigger: new poll ─────────────────────────────────────────────────────────

export const onPollCreated = onDocumentCreated(
  "dateVotings/{votingId}",
  async (event) => {
    const snapshot = event.data;
    if (!snapshot) return;
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
  },
);

// ── Trigger: auth account deleted ─────────────────────────────────────────────

/**
 * Cleans up a user's Firestore footprint when their Auth account is deleted
 * (from the app or the console). Without this, orphaned users docs keep
 * receiving notification fan-outs forever.
 *
 * Auth onDelete triggers only exist in the v1 API, and v1 does not offer
 * europe-north1 — europe-west1 is fine since Auth events carry no region
 * affinity anyway.
 */
export const onAuthUserDeleted = functionsV1
  .region("europe-west1")
  .auth.user()
  .onDelete(async (user) => {
    const uid = user.uid;
    // recursiveDelete clears the notifications/{uid}/items subcollection even
    // though the parent doc never existed.
    await admin.firestore().recursiveDelete(
      db.collection("notifications").doc(uid),
    );
    await db.collection("users").doc(uid).delete();
    logger.info(`Cleaned up Firestore data for deleted user ${uid}`);
  });
