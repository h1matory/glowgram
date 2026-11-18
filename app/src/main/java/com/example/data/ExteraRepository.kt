package com.example.data

import android.content.Context
import com.example.R
import com.example.model.AvatarFrameStyle
import com.example.model.ChatAccent
import com.example.model.ChatBackdrop
import com.example.model.ChatMessageDoc
import com.example.model.ChatPattern
import com.example.model.ChatThreadDoc
import com.example.model.NftGift
import com.example.model.OwnedGiftDoc
import com.example.model.SampleNftCatalog
import com.example.model.UserProfileDoc
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class ExteraRepository(
  private val db: FirebaseFirestore,
  private val auth: FirebaseAuth = Firebase.auth,
) {
  constructor(context: Context) : this(
    FirebaseFirestore.getInstance(
      context.applicationContext.getString(R.string.firestore_database_id)
    )
  )

  private fun requireMatchingUid(userId: String): String {
    val uid =
      auth.currentUser?.uid
        ?: throw IllegalStateException("Требуется авторизация через Google")
    check(uid == userId) { "Идентификатор пользователя не совпадает" }
    return uid
  }

  fun observeProfile(userId: String): Flow<UserProfileDoc?> =
    db.collection("users")
      .document(userId)
      .snapshots()
      .map { snapshot ->
        if (snapshot.exists()) {
          snapshot.toObject(UserProfileDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        } else {
          null
        }
      }

  fun observeGifts(userId: String): Flow<List<NftGift>> =
    db.collection("users")
      .document(userId)
      .collection("gifts")
      .orderBy("createdAt", Query.Direction.DESCENDING)
      .snapshots()
      .map { query ->
        query.toObjects(OwnedGiftDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
          .map { it.toNftGift() }
      }

  fun observeChats(userId: String): Flow<List<ChatThreadDoc>> =
    db.collection("users")
      .document(userId)
      .collection("chats")
      .orderBy("updatedAt", Query.Direction.DESCENDING)
      .snapshots()
      .map { query ->
        query.toObjects(ChatThreadDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      }

  fun observeMessages(userId: String, chatId: String): Flow<List<ChatMessageDoc>> =
    db.collection("users")
      .document(userId)
      .collection("chats")
      .document(chatId)
      .collection("messages")
      .orderBy("createdAt", Query.Direction.ASCENDING)
      .snapshots()
      .map { query ->
        query.toObjects(ChatMessageDoc::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
      }

  suspend fun registerAccount(
    userId: String,
    displayName: String,
    username: String,
    bio: String,
    birthday: String,
    frame: AvatarFrameStyle,
  ): Result<Unit> {
    val path = "users/$userId"
    return try {
      val uid = requireMatchingUid(userId)
      val cleanName = displayName.trim().take(40).ifEmpty { "User" }
      val normalizedUser =
        username.trim().let { if (it.startsWith("@")) it else "@$it" }.take(35).ifBlank { "@user" }
      val cleanBio = bio.trim().take(160)
      val cleanBirthday = birthday.trim().take(40)

      val batch = db.batch()
      val userRef = db.collection("users").document(uid)
      val profilePayload =
        mapOf(
          "userId" to uid,
          "displayName" to cleanName,
          "username" to normalizedUser,
          "bio" to cleanBio,
          "birthday" to cleanBirthday,
          "crystals" to 97L,
          "selectedFrame" to frame.name,
          "chatBackdrop" to ChatBackdrop.SLATE.name,
          "chatPattern" to ChatPattern.PLUSES.name,
          "chatAccent" to ChatAccent.BLUE.name,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        )
      batch.set(userRef, profilePayload)

      // Seed starter Telegram Market NFT gifts
      SampleNftCatalog.initialOwnedGifts.forEach { gift ->
        val giftRef = userRef.collection("gifts").document(gift.id)
        batch.set(
          giftRef,
          mapOf(
            "userId" to uid,
            "fragmentSlug" to gift.fragmentSlug,
            "number" to gift.number.toLong(),
            "name" to gift.name,
            "modelName" to gift.modelName,
            "modelRarityPercent" to gift.modelRarityPercent,
            "backgroundName" to gift.backgroundName,
            "backgroundRarityPercent" to gift.backgroundRarityPercent,
            "patternName" to gift.patternName,
            "patternRarityPercent" to gift.patternRarityPercent,
            "editionText" to gift.editionText,
            "owner" to cleanName,
            "priceRub" to gift.priceRub.toLong(),
            "priceCrystals" to gift.priceCrystals.toLong(),
            "upgraded" to false,
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp(),
          ),
        )
      }

      // Seed starter ExteraGram-style chats
      val supportChatRef = userRef.collection("chats").document("chat_support")
      batch.set(
        supportChatRef,
        mapOf(
          "userId" to uid,
          "title" to "Поддержка • exest",
          "subtitle" to "в сети",
          "avatarText" to "E",
          "lastMessage" to "Добро пожаловать в ExteraNFT!",
          "pinned" to true,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        supportChatRef.collection("messages").document("msg_welcome_1"),
        mapOf(
          "userId" to uid,
          "chatId" to "chat_support",
          "senderName" to "Поддержка • exest",
          "text" to "Привет! Это обновлённый чат с кастомными обоями и узорами.",
          "outgoing" to false,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        supportChatRef.collection("messages").document("msg_welcome_2"),
        mapOf(
          "userId" to uid,
          "chatId" to "chat_support",
          "senderName" to cleanName,
          "text" to "Выглядит очень аккуратно!",
          "outgoing" to true,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )

      val savedChatRef = userRef.collection("chats").document("chat_saved")
      batch.set(
        savedChatRef,
        mapOf(
          "userId" to uid,
          "title" to "Избранное",
          "subtitle" to "личные заметки",
          "avatarText" to "★",
          "lastMessage" to "Коллекция NFT-подарков активирована",
          "pinned" to true,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        savedChatRef.collection("messages").document("msg_saved_1"),
        mapOf(
          "userId" to uid,
          "chatId" to "chat_saved",
          "senderName" to cleanName,
          "text" to "Коллекция NFT-подарков активирована",
          "outgoing" to true,
          "giftSlug" to "plushpepe-1",
          "giftTitle" to "Plush Pepe #1",
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )

      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun createChat(userId: String, title: String, subtitle: String): Result<String> {
    val uid = requireMatchingUid(userId)
    val chatId = "chat_${System.currentTimeMillis()}"
    val path = "users/$uid/chats/$chatId"
    return try {
      val cleanTitle = title.trim().take(60).ifEmpty { "Новый чат" }
      val cleanSubtitle = subtitle.trim().take(60).ifEmpty { "в сети" }
      val avatarLetter = cleanTitle.take(1).uppercase()

      val batch = db.batch()
      val chatRef = db.collection("users").document(uid).collection("chats").document(chatId)
      batch.set(
        chatRef,
        mapOf(
          "userId" to uid,
          "title" to cleanTitle,
          "subtitle" to cleanSubtitle,
          "avatarText" to avatarLetter,
          "lastMessage" to "Чат создан",
          "pinned" to false,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        chatRef.collection("messages").document("msg_init"),
        mapOf(
          "userId" to uid,
          "chatId" to chatId,
          "senderName" to cleanTitle,
          "text" to "Привет! Напиши сообщение или отправь NFT-подарок 🎁",
          "outgoing" to false,
          "createdAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
      Result.success(chatId)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.CREATE, path)
      Result.failure(e)
    }
  }

  suspend fun sendMessage(
    userId: String,
    chatId: String,
    senderName: String,
    text: String,
    gift: NftGift? = null,
  ): Result<Unit> {
    val path = "users/$userId/chats/$chatId/messages"
    return try {
      val uid = requireMatchingUid(userId)
      val cleanText = text.trim().take(1000)
      require(cleanText.isNotEmpty()) { "Сообщение не может быть пустым" }

      val batch = db.batch()
      val chatRef = db.collection("users").document(uid).collection("chats").document(chatId)
      val msgRef = chatRef.collection("messages").document()

      val msgPayload =
        mutableMapOf<String, Any>(
          "userId" to uid,
          "chatId" to chatId,
          "senderName" to senderName.take(60),
          "text" to cleanText,
          "outgoing" to true,
          "createdAt" to FieldValue.serverTimestamp(),
        )
      if (gift != null) {
        msgPayload["giftSlug"] = gift.fragmentSlug
        msgPayload["giftTitle"] = gift.fullTitle
      }

      batch.set(msgRef, msgPayload)
      batch.update(
        chatRef,
        mapOf(
          "lastMessage" to (if (gift != null) "🎁 ${gift.fullTitle}: $cleanText" else cleanText).take(500),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }

  suspend fun updateSelectedFrame(userId: String, frame: AvatarFrameStyle): Result<Unit> {
    val path = "users/$userId"
    return try {
      val uid = requireMatchingUid(userId)
      db.collection("users")
        .document(uid)
        .update(
          mapOf(
            "selectedFrame" to frame.name,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun updateChatAppearance(
    userId: String,
    backdrop: ChatBackdrop,
    pattern: ChatPattern,
    accent: ChatAccent,
  ): Result<Unit> {
    val path = "users/$userId"
    return try {
      val uid = requireMatchingUid(userId)
      db.collection("users")
        .document(uid)
        .update(
          mapOf(
            "chatBackdrop" to backdrop.name,
            "chatPattern" to pattern.name,
            "chatAccent" to accent.name,
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun upgradeGift(userId: String, gift: NftGift): Result<Unit> {
    val path = "users/$userId/gifts/${gift.id}"
    return try {
      val uid = requireMatchingUid(userId)
      if (gift.isUpgraded) return Result.success(Unit)
      db.collection("users")
        .document(uid)
        .collection("gifts")
        .document(gift.id)
        .update(
          mapOf(
            "upgraded" to true,
            "modelName" to "${gift.modelName} ★".take(60),
            "priceRub" to (gift.priceRub + 1500).toLong(),
            "updatedAt" to FieldValue.serverTimestamp(),
          )
        )
        .await()
      Result.success(Unit)
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.UPDATE, path)
      Result.failure(e)
    }
  }

  suspend fun buyMarketGift(
    userId: String,
    ownerName: String,
    currentCrystals: Long,
    gift: NftGift,
  ): Result<NftGift> {
    val docId = "${gift.id}_${System.currentTimeMillis()}"
    val path = "users/$userId/gifts/$docId"
    return try {
      val uid = requireMatchingUid(userId)
      require(currentCrystals >= gift.priceCrystals) { "Недостаточно кристаллов" }

      val newBalance = currentCrystals - gift.priceCrystals
      val batch = db.batch()
      val userRef = db.collection("users").document(uid)
      val giftRef = userRef.collection("gifts").document(docId)

      batch.update(
        userRef,
        mapOf(
          "crystals" to newBalance,
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.set(
        giftRef,
        mapOf(
          "userId" to uid,
          "fragmentSlug" to gift.fragmentSlug,
          "number" to gift.number.toLong(),
          "name" to gift.name,
          "modelName" to gift.modelName,
          "modelRarityPercent" to gift.modelRarityPercent,
          "backgroundName" to gift.backgroundName,
          "backgroundRarityPercent" to gift.backgroundRarityPercent,
          "patternName" to gift.patternName,
          "patternRarityPercent" to gift.patternRarityPercent,
          "editionText" to gift.editionText,
          "owner" to ownerName.take(40),
          "priceRub" to gift.priceRub.toLong(),
          "priceCrystals" to gift.priceCrystals.toLong(),
          "upgraded" to false,
          "createdAt" to FieldValue.serverTimestamp(),
          "updatedAt" to FieldValue.serverTimestamp(),
        ),
      )
      batch.commit().await()
      Result.success(gift.copy(id = docId, owner = ownerName))
    } catch (e: CancellationException) {
      throw e
    } catch (e: Exception) {
      handleFirestoreError(e, OperationType.WRITE, path)
      Result.failure(e)
    }
  }
}
