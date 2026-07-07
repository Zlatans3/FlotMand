"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.onPollCreated = exports.onEventRsvp = exports.onEventPriceSet = exports.onEventCreated = void 0;
const functions = require("firebase-functions/v1");
const admin = require("firebase-admin");
admin.initializeApp();
const db = admin.firestore();
/** Fetch all users except [excludeUid], returning their uid and FCM token. */
async function getUsersExcluding(excludeUid) {
    const snapshot = await db.collection("users").get();
    const rows = [];
    snapshot.forEach((doc) => {
        if (doc.id !== excludeUid) {
            rows.push({ uid: doc.id, fcmToken: doc.data().fcmToken });
        }
    });
    return rows;
}
/** Send a multicast FCM push and write in-app notification documents. */
async function fanOut(users, type, title, body, referenceId, senderPhotoUrl = "", senderDisplayName = "") {
    // ── 1. FCM push ────────────────────────────────────────────────────────────
    const tokens = users.map((u) => { var _a; return (_a = u.fcmToken) !== null && _a !== void 0 ? _a : ""; }).filter(Boolean);
    if (tokens.length > 0) {
        const message = {
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
        functions.logger.info(`FCM: ${response.successCount} ok, ${response.failureCount} failed`);
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
exports.onEventCreated = functions.firestore
    .document("dinnerEvents/{eventId}")
    .onCreate(async (snapshot) => {
    var _a, _b, _c, _d;
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
exports.onEventPriceSet = functions.firestore
    .document("dinnerEvents/{eventId}")
    .onUpdate(async (change, context) => {
    var _a, _b, _c, _d, _e;
    const before = change.before.data();
    const after = change.after.data();
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
    const formatted = pricePerPerson % 1 === 0
        ? `${Math.round(pricePerPerson)}`
        : pricePerPerson.toFixed(2);
    const [publisherDoc, ...recipientDocs] = await Promise.all([
        db.collection("users").doc(publisherId).get(),
        ...recipientIds.map((uid) => db.collection("users").doc(uid).get()),
    ]);
    const publisherName = ((_d = publisherDoc.data()) === null || _d === void 0 ? void 0 : _d.displayName) || "En bruger";
    const publisherPhotoUrl = ((_e = publisherDoc.data()) === null || _e === void 0 ? void 0 : _e.photoUrl) || "";
    const users = recipientDocs
        .filter((doc) => doc.exists)
        .map((doc) => { var _a; return ({ uid: doc.id, fcmToken: (_a = doc.data()) === null || _a === void 0 ? void 0 : _a.fcmToken }); });
    await fanOut(users, "event", eventName, `Prisen er sat til ${formatted} kr. pr. person`, context.params.eventId, publisherPhotoUrl, publisherName);
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
exports.onEventRsvp = functions
    .runWith({ timeoutSeconds: 60 })
    .firestore.document("dinnerEvents/{eventId}")
    .onUpdate(async (change, context) => {
    var _a, _b, _c, _d, _e, _f;
    const before = change.before.data();
    const after = change.after.data();
    const eventId = context.params.eventId;
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
        const body = attending
            ? `✅ ${userName} deltager`
            : `❌ ${userName} deltager ikke`;
        await fanOut([{ uid: publisherId, fcmToken: hostToken }], "event", eventName, body, eventId, userPhotoUrl, userName);
    }));
});
// ── Trigger: new poll ─────────────────────────────────────────────────────────
exports.onPollCreated = functions.firestore
    .document("dateVotings/{votingId}")
    .onCreate(async (snapshot) => {
    var _a, _b, _c, _d, _e, _f;
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
//# sourceMappingURL=index.js.map