"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
exports.onPollCreated = exports.onEventCreated = void 0;
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
async function fanOut(users, type, title, body, referenceId) {
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
    users.forEach(({ uid }) => {
        const ref = db
            .collection("notifications")
            .doc(uid)
            .collection("items")
            .doc();
        batch.set(ref, {
            type,
            referenceId,
            title,
            body,
            isRead: false,
            createdAtMillis: now,
        });
    });
    await batch.commit();
    functions.logger.info(`Wrote ${users.length} notification docs`);
}
// ── Trigger: new event ────────────────────────────────────────────────────────
exports.onEventCreated = functions.firestore
    .document("dinnerEvents/{eventId}")
    .onCreate(async (snapshot) => {
    var _a, _b, _c;
    const data = snapshot.data();
    const publisherId = (_a = data.publisherId) !== null && _a !== void 0 ? _a : "";
    const eventName = (_b = data.eventName) !== null && _b !== void 0 ? _b : "Nyt event";
    const [publisherDoc, users] = await Promise.all([
        db.collection("users").doc(publisherId).get(),
        getUsersExcluding(publisherId),
    ]);
    const publisherName = ((_c = publisherDoc.data()) === null || _c === void 0 ? void 0 : _c.displayName) || "En bruger";
    await fanOut(users, "event", `🍽️ ${publisherName} har oprettet et nyt event`, eventName, snapshot.id);
});
// ── Trigger: new poll ─────────────────────────────────────────────────────────
exports.onPollCreated = functions.firestore
    .document("dateVotings/{votingId}")
    .onCreate(async (snapshot) => {
    var _a, _b, _c, _d, _e;
    const data = snapshot.data();
    const creatorId = (_b = (_a = data.creatorId) !== null && _a !== void 0 ? _a : data.publisherId) !== null && _b !== void 0 ? _b : "";
    const title = (_d = (_c = data.title) !== null && _c !== void 0 ? _c : data.name) !== null && _d !== void 0 ? _d : "Ny afstemning";
    const [creatorDoc, users] = await Promise.all([
        db.collection("users").doc(creatorId).get(),
        getUsersExcluding(creatorId),
    ]);
    const creatorName = ((_e = creatorDoc.data()) === null || _e === void 0 ? void 0 : _e.displayName) || "En bruger";
    await fanOut(users, "poll", `🗳️ ${creatorName} har oprettet en ny afstemning`, title, snapshot.id);
});
//# sourceMappingURL=index.js.map