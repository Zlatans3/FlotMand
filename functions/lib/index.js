"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.onAuthUserDeleted = exports.onPollCreated = exports.onEventRsvp = exports.onEventPriceSet = exports.onEventCreated = void 0;
const v2_1 = require("firebase-functions/v2");
const firestore_1 = require("firebase-functions/v2/firestore");
const functionsV1 = require("firebase-functions/v1");
const logger = require("firebase-functions/logger");
const admin = require("firebase-admin");
admin.initializeApp();
const db = admin.firestore();
// As close as Cloud Functions v2 gets to the Firestore database in
// europe-north2 (Stockholm): europe-north2 is not a supported Functions
// region yet, so europe-north1 (Finland) keeps trigger delivery and all
// reads/writes intra-Nordic instead of hopping to us-central1.
(0, v2_1.setGlobalOptions)({ region: "europe-north1" });
/** Fetch all users except [excludeUid], returning their uid and FCM token. */
async function getUsersExcluding(excludeUid) {
    // select() limits the payload to the fields we need; the read count is
    // the same but we stop shipping every profile field over the wire.
    const snapshot = await db
        .collection("users")
        .select("fcmToken", "isGhostUser")
        .get();
    const rows = [];
    snapshot.forEach((doc) => {
        // Ghost users are rotation placeholders created by hosts — they can never
        // sign in, so notification docs written for them are pure waste.
        if (doc.id !== excludeUid && doc.data().isGhostUser !== true) {
            rows.push({ uid: doc.id, fcmToken: doc.data().fcmToken });
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
async function pruneDeadTokens(users, responses) {
    const batch = db.batch();
    let pruneCount = 0;
    responses.forEach((res, i) => {
        var _a;
        const code = (_a = res.error) === null || _a === void 0 ? void 0 : _a.code;
        if (code === "messaging/registration-token-not-registered" ||
            code === "messaging/invalid-argument") {
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
async function fanOut(users, type, title, body, referenceId, senderPhotoUrl = "", senderDisplayName = "") {
    // ── 1. FCM push ────────────────────────────────────────────────────────────
    const withTokens = users.filter((u) => u.fcmToken);
    if (withTokens.length > 0) {
        const message = {
            tokens: withTokens.map((u) => u.fcmToken),
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
        logger.info(`FCM: ${response.successCount} ok, ${response.failureCount} failed`);
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
        expiresAt: admin.firestore.Timestamp.fromMillis(now + NOTIFICATION_TTL_DAYS * 24 * 60 * 60 * 1000),
    };
    logger.info("Notification payload", payload);
    users.forEach(({ uid }) => {
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
exports.onEventCreated = (0, firestore_1.onDocumentCreated)("dinnerEvents/{eventId}", async (event) => {
    var _a, _b, _c, _d;
    const snapshot = event.data;
    if (!snapshot)
        return;
    const data = snapshot.data();
    const publisherId = (_a = data.publisherId) !== null && _a !== void 0 ? _a : "";
    const eventName = (_b = data.eventName) !== null && _b !== void 0 ? _b : "Nyt event";
    const [publisherDoc, users] = await Promise.all([
        db.collection("users").doc(publisherId).get(),
        getUsersExcluding(publisherId),
    ]);
    const publisherName = ((_c = publisherDoc.data()) === null || _c === void 0 ? void 0 : _c.displayName) || "En bruger";
    const publisherPhotoUrl = ((_d = publisherDoc.data()) === null || _d === void 0 ? void 0 : _d.photoUrl) || "";
    await fanOut(users, "event", `🍽️ ${publisherName} har oprettet et nyt event`, eventName, snapshot.id, publisherPhotoUrl, publisherName);
});
// ── Trigger: price set or updated on an event ────────────────────────────────
exports.onEventPriceSet = (0, firestore_1.onDocumentUpdated)("dinnerEvents/{eventId}", async (event) => {
    var _a, _b, _c, _d, _e;
    if (!event.data)
        return;
    const before = event.data.before.data();
    const after = event.data.after.data();
    const priceBefore = before.totalPrice;
    const priceAfter = after.totalPrice;
    // Only fire when totalPrice is newly added or changed
    if (priceAfter == null || priceAfter === priceBefore)
        return;
    const publisherId = (_a = after.publisherId) !== null && _a !== void 0 ? _a : "";
    const participantIds = (_b = after.participantIds) !== null && _b !== void 0 ? _b : [];
    const eventName = (_c = after.eventName) !== null && _c !== void 0 ? _c : "Middag";
    const recipientIds = participantIds.filter((id) => id !== publisherId);
    if (recipientIds.length === 0)
        return;
    const participantCount = participantIds.length > 0 ? participantIds.length : 1;
    const pricePerPerson = priceAfter / participantCount;
    const formatted = pricePerPerson % 1 === 0 ?
        `${Math.round(pricePerPerson)}` :
        pricePerPerson.toFixed(2);
    const [publisherDoc, ...recipientDocs] = await Promise.all([
        db.collection("users").doc(publisherId).get(),
        ...recipientIds.map((uid) => db.collection("users").doc(uid).get()),
    ]);
    const publisherName = ((_d = publisherDoc.data()) === null || _d === void 0 ? void 0 : _d.displayName) || "En bruger";
    const publisherPhotoUrl = ((_e = publisherDoc.data()) === null || _e === void 0 ? void 0 : _e.photoUrl) || "";
    const users = recipientDocs
        .filter((doc) => { var _a; return doc.exists && ((_a = doc.data()) === null || _a === void 0 ? void 0 : _a.isGhostUser) !== true; })
        .map((doc) => { var _a; return ({ uid: doc.id, fcmToken: (_a = doc.data()) === null || _a === void 0 ? void 0 : _a.fcmToken }); });
    await fanOut(users, "event", eventName, `Prisen er sat til ${formatted} kr. pr. person`, event.params.eventId, publisherPhotoUrl, publisherName);
});
// ── Trigger: RSVP changed on an event ────────────────────────────────────────
/**
 * Trailing debounce window for RSVP notifications. A user toggling their
 * answer back and forth produces one Firestore update per tap; only the
 * invocation that is still the latest after this delay notifies the host,
 * and it reads the event fresh so the message reflects the final answer.
 */
const RSVP_DEBOUNCE_MS = 20000;
const sleep = (ms) => new Promise((resolve) => setTimeout(resolve, ms));
/** IDs present in exactly one of the two arrays (added or removed). */
function symmetricDiff(before, after) {
    const beforeSet = new Set(before);
    const afterSet = new Set(after);
    const changed = new Set();
    afterSet.forEach((id) => {
        if (!beforeSet.has(id))
            changed.add(id);
    });
    beforeSet.forEach((id) => {
        if (!afterSet.has(id))
            changed.add(id);
    });
    return changed;
}
exports.onEventRsvp = (0, firestore_1.onDocumentUpdated)({ document: "dinnerEvents/{eventId}", timeoutSeconds: 60 }, async (event) => {
    var _a, _b, _c, _d, _e, _f;
    if (!event.data)
        return;
    const before = event.data.before.data();
    const after = event.data.after.data();
    const eventId = event.params.eventId;
    const changedIds = new Set([
        ...symmetricDiff((_a = before.participantIds) !== null && _a !== void 0 ? _a : [], (_b = after.participantIds) !== null && _b !== void 0 ? _b : []),
        ...symmetricDiff((_c = before.declinedIds) !== null && _c !== void 0 ? _c : [], (_d = after.declinedIds) !== null && _d !== void 0 ? _d : []),
    ]);
    const publisherId = (_e = after.publisherId) !== null && _e !== void 0 ? _e : "";
    changedIds.delete(publisherId);
    if (changedIds.size === 0)
        return;
    const hostDoc = await db.collection("users").doc(publisherId).get();
    const hostToken = (_f = hostDoc.data()) === null || _f === void 0 ? void 0 : _f.fcmToken;
    if (!hostDoc.exists)
        return;
    await Promise.all([...changedIds].map(async (uid) => {
        var _a, _b, _c, _d, _e, _f, _g, _h;
        const userDoc = await db.collection("users").doc(uid).get();
        if (!userDoc.exists)
            return;
        // Ghost users are added/removed by the host themself — no notification.
        if (((_a = userDoc.data()) === null || _a === void 0 ? void 0 : _a.isGhostUser) === true)
            return;
        const userName = ((_b = userDoc.data()) === null || _b === void 0 ? void 0 : _b.displayName) || "En bruger";
        const userPhotoUrl = ((_c = userDoc.data()) === null || _c === void 0 ? void 0 : _c.photoUrl) || "";
        // Trailing debounce: claim the (event, user) slot, wait, and bail out
        // if a newer RSVP change reclaimed it in the meantime.
        const debounceRef = db
            .collection("rsvpDebounce")
            .doc(`${eventId}_${uid}`);
        const claim = Date.now();
        await debounceRef.set({ pendingAtMillis: claim });
        await sleep(RSVP_DEBOUNCE_MS);
        const latest = await debounceRef.get();
        if (((_d = latest.data()) === null || _d === void 0 ? void 0 : _d.pendingAtMillis) !== claim)
            return;
        await debounceRef.delete();
        // Read the event fresh so the message matches the user's final answer.
        const freshDoc = await db.collection("dinnerEvents").doc(eventId).get();
        if (!freshDoc.exists)
            return;
        const fresh = (_e = freshDoc.data()) !== null && _e !== void 0 ? _e : {};
        const attending = ((_f = fresh.participantIds) !== null && _f !== void 0 ? _f : []).includes(uid);
        const declined = ((_g = fresh.declinedIds) !== null && _g !== void 0 ? _g : []).includes(uid);
        if (!attending && !declined)
            return; // RSVP was withdrawn entirely
        const eventName = (_h = fresh.eventName) !== null && _h !== void 0 ? _h : "Middag";
        const body = attending ?
            `✅ ${userName} deltager` :
            `❌ ${userName} deltager ikke`;
        await fanOut([{ uid: publisherId, fcmToken: hostToken }], "event", eventName, body, eventId, userPhotoUrl, userName);
    }));
});
// ── Trigger: new poll ─────────────────────────────────────────────────────────
exports.onPollCreated = (0, firestore_1.onDocumentCreated)("dateVotings/{votingId}", async (event) => {
    var _a, _b, _c, _d, _e, _f;
    const snapshot = event.data;
    if (!snapshot)
        return;
    const data = snapshot.data();
    const creatorId = (_b = (_a = data.creatorId) !== null && _a !== void 0 ? _a : data.publisherId) !== null && _b !== void 0 ? _b : "";
    const title = (_d = (_c = data.title) !== null && _c !== void 0 ? _c : data.name) !== null && _d !== void 0 ? _d : "Ny afstemning";
    const [creatorDoc, users] = await Promise.all([
        db.collection("users").doc(creatorId).get(),
        getUsersExcluding(creatorId),
    ]);
    const creatorName = ((_e = creatorDoc.data()) === null || _e === void 0 ? void 0 : _e.displayName) || "En bruger";
    const creatorPhotoUrl = ((_f = creatorDoc.data()) === null || _f === void 0 ? void 0 : _f.photoUrl) || "";
    await fanOut(users, "poll", `🗳️ ${creatorName} har oprettet en ny afstemning`, title, snapshot.id, creatorPhotoUrl, creatorName);
});
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
exports.onAuthUserDeleted = functionsV1
    .region("europe-west1")
    .auth.user()
    .onDelete(async (user) => {
    const uid = user.uid;
    // recursiveDelete clears the notifications/{uid}/items subcollection even
    // though the parent doc never existed.
    await admin.firestore().recursiveDelete(db.collection("notifications").doc(uid));
    await db.collection("users").doc(uid).delete();
    logger.info(`Cleaned up Firestore data for deleted user ${uid}`);
});
//# sourceMappingURL=index.js.map