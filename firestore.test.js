const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated users cannot read or write profiles, gifts, or chats", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("chats").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Hacker",
      username: "@hacker",
      crystals: 100,
      selectedFrame: "NEON_PULSE",
      chatBackdrop: "SLATE",
      chatPattern: "PLUSES",
      chatAccent: "BLUE",
      createdAt: new Date(Date.now() - 1000),
      updatedAt: new Date(Date.now() - 1000),
    })
  );
});

test("Owner can create and update profile, gifts, chats, and send messages", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Benzo",
      username: "@Benzo",
      bio: "Молодой Миллионер",
      birthday: "15 июня 2004 г.",
      crystals: 97,
      selectedFrame: "NEON_PULSE",
      chatBackdrop: "SLATE",
      chatPattern: "PLUSES",
      chatAccent: "BLUE",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Partial update on profile
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).update({
      selectedFrame: "CYBER_HALO",
      chatPattern: "WAVES",
      updatedAt: new Date(Date.now() - 1000),
    })
  );

  // Create gift (no @DocumentId `id` key)
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("gifts").doc("cigar_1").set({
      userId: ALICE_UID,
      fragmentSlug: "vintagecigar-1",
      number: 1,
      name: "Vintage Cigar",
      modelName: "Spectral Smoke",
      modelRarityPercent: "2%",
      backgroundName: "Battleship Grey",
      backgroundRarityPercent: "1.37%",
      patternName: "Viking",
      patternRarityPercent: "8.33%",
      editionText: "1/450 выпущено",
      owner: "Benzo",
      priceRub: 3400,
      priceCrystals: 180,
      upgraded: false,
      createdAt: created,
      updatedAt: created,
    })
  );

  // Create chat thread and send message
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("chats").doc("chat_support").set({
      userId: ALICE_UID,
      title: "Поддержка • exest",
      subtitle: "в сети",
      avatarText: "E",
      lastMessage: "Привет!",
      pinned: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("chats").doc("chat_support").collection("messages").doc("msg_1").set({
      userId: ALICE_UID,
      chatId: "chat_support",
      senderName: "Benzo",
      text: "Привет!",
      outgoing: true,
      createdAt: created,
    })
  );

  // List queries succeed
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("gifts").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("chats").get());
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("chats").doc("chat_support").collection("messages").get());
});

test("Cross-user isolation and ghost fields are strictly blocked", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  const created = new Date(Date.now() - 5000);

  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Benzo",
      username: "@Benzo",
      crystals: 97,
      selectedFrame: "NEON_PULSE",
      chatBackdrop: "SLATE",
      chatPattern: "PLUSES",
      chatAccent: "BLUE",
      createdAt: created,
      updatedAt: created,
    })
  );

  // Bob cannot read or write Alice's profile or chats
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).collection("chats").get());
  await assertFails(bobDb.collection("users").doc(ALICE_UID).update({ displayName: "Hacked" }));

  // Ghost field & future timestamp rejected
  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      username: "@bob",
      crystals: 97,
      selectedFrame: "NEON_PULSE",
      chatBackdrop: "SLATE",
      chatPattern: "PLUSES",
      chatAccent: "BLUE",
      isAdmin: true,
      createdAt: created,
      updatedAt: created,
    })
  );

  await assertFails(
    bobDb.collection("users").doc(BOB_UID).set({
      userId: BOB_UID,
      displayName: "Bob",
      username: "@bob",
      crystals: 97,
      selectedFrame: "NEON_PULSE",
      chatBackdrop: "SLATE",
      chatPattern: "PLUSES",
      chatAccent: "BLUE",
      createdAt: new Date(Date.now() + 60000),
      updatedAt: created,
    })
  );
});
