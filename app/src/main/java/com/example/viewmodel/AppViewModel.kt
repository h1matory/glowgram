package com.example.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ExteraRepository
import com.example.model.AppSection
import com.example.model.AvatarFrameStyle
import com.example.model.ChatAccent
import com.example.model.ChatBackdrop
import com.example.model.ChatMessageDoc
import com.example.model.ChatPattern
import com.example.model.ChatThreadDoc
import com.example.model.CollectibleUsername
import com.example.model.NftGift
import com.example.model.ProfileTab
import com.example.model.SampleNftCatalog
import com.example.model.UserProfileDoc
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface AccountState {
  data object Loading : AccountState
  data object NeedsRegistration : AccountState
  data class Ready(val profile: UserProfileDoc) : AccountState
  data class Error(val message: String) : AccountState
}

data class UiControlsState(
  val section: AppSection = AppSection.CHATS,
  val activeChatId: String? = null,
  val profileTab: ProfileTab = ProfileTab.GIFTS,
  val isPlayingMusic: Boolean = false,
  val musicSeconds: Int = 8,
  val musicDurationSeconds: Int = 128,
  val selectedGift: NftGift? = null,
  val showUsernameModal: Boolean = false,
  val showAppearanceSheet: Boolean = false,
  val showNewChatDialog: Boolean = false,
  val showGiftPickerInChat: Boolean = false,
  val isSubmittingRegistration: Boolean = false,
  val confettiTriggerKey: Int = 0,
  val statusMessage: String? = null,
  val marketGifts: List<NftGift> = SampleNftCatalog.marketGifts,
  val collectibleUsername: CollectibleUsername = CollectibleUsername(),
)

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModel(
  private val repository: ExteraRepository,
  private val currentUserId: String,
) : ViewModel() {

  private val _controls = MutableStateFlow(UiControlsState())
  val controls: StateFlow<UiControlsState> = _controls.asStateFlow()

  private var musicJob: Job? = null

  val accountState: StateFlow<AccountState> =
    repository
      .observeProfile(currentUserId)
      .map { doc ->
        if (doc == null) AccountState.NeedsRegistration else AccountState.Ready(doc)
      }
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing profile", rootError)
        emit(AccountState.Error(rootError.localizedMessage ?: "Не удалось загрузить профиль"))
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = AccountState.Loading,
      )

  val ownedGifts: StateFlow<List<NftGift>> =
    repository
      .observeGifts(currentUserId)
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        Log.w(TAG, "Error observing gifts", firestoreError ?: error)
        emit(emptyList())
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList(),
      )

  val chats: StateFlow<List<ChatThreadDoc>> =
    repository
      .observeChats(currentUserId)
      .catch { error ->
        val firestoreError =
          generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        Log.w(TAG, "Error observing chats", firestoreError ?: error)
        emit(emptyList())
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList(),
      )

  val activeMessages: StateFlow<List<ChatMessageDoc>> =
    _controls
      .map { it.activeChatId }
      .flatMapLatest { chatId ->
        if (chatId.isNullOrBlank()) {
          flowOf(emptyList())
        } else {
          repository.observeMessages(currentUserId, chatId).catch { error ->
            val firestoreError =
              generateSequence(error) { it.cause }
                .filterIsInstance<FirebaseFirestoreException>()
                .firstOrNull()
            if (firestoreError == null && error is CancellationException) throw error
            Log.w(TAG, "Error observing messages", firestoreError ?: error)
            emit(emptyList())
          }
        }
      }
      .stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = emptyList(),
      )

  fun registerAccount(
    displayName: String,
    username: String,
    bio: String,
    birthday: String,
    frame: AvatarFrameStyle,
  ) {
    viewModelScope.launch {
      _controls.update { it.copy(isSubmittingRegistration = true) }
      val result =
        repository.registerAccount(
          userId = currentUserId,
          displayName = displayName,
          username = username,
          bio = bio,
          birthday = birthday,
          frame = frame,
        )
      _controls.update {
        it.copy(
          isSubmittingRegistration = false,
          statusMessage = result.exceptionOrNull()?.localizedMessage ?: "Аккаунт создан!",
        )
      }
    }
  }

  fun openSection(section: AppSection, profileTab: ProfileTab? = null) {
    _controls.update {
      it.copy(
        section = section,
        activeChatId = if (section == AppSection.PROFILE) null else it.activeChatId,
        profileTab = profileTab ?: it.profileTab,
      )
    }
  }

  fun openChat(chatId: String) {
    _controls.update { it.copy(section = AppSection.CHATS, activeChatId = chatId) }
  }

  fun closeActiveChat() {
    _controls.update { it.copy(activeChatId = null, showGiftPickerInChat = false) }
  }

  fun setNewChatDialogVisible(visible: Boolean) {
    _controls.update { it.copy(showNewChatDialog = visible) }
  }

  fun createNewChat(title: String, subtitle: String) {
    viewModelScope.launch {
      val result = repository.createChat(currentUserId, title, subtitle)
      result
        .onSuccess { newId ->
          _controls.update {
            it.copy(
              showNewChatDialog = false,
              activeChatId = newId,
              section = AppSection.CHATS,
            )
          }
        }
        .onFailure { e ->
          _controls.update { it.copy(statusMessage = e.localizedMessage ?: "Не удалось создать чат") }
        }
    }
  }

  fun sendMessage(chatId: String, senderName: String, text: String, gift: NftGift? = null) {
    viewModelScope.launch {
      val finalText =
        if (text.isBlank() && gift != null) "Отправил подарок ${gift.fullTitle}" else text
      val result = repository.sendMessage(currentUserId, chatId, senderName, finalText, gift)
      result.onFailure { e ->
        _controls.update { it.copy(statusMessage = e.localizedMessage ?: "Ошибка отправки") }
      }
    }
  }

  fun setGiftPickerInChatVisible(visible: Boolean) {
    _controls.update { it.copy(showGiftPickerInChat = visible) }
  }

  fun setAppearanceSheetVisible(visible: Boolean) {
    _controls.update { it.copy(showAppearanceSheet = visible) }
  }

  fun updateChatAppearance(backdrop: ChatBackdrop, pattern: ChatPattern, accent: ChatAccent) {
    viewModelScope.launch {
      repository.updateChatAppearance(currentUserId, backdrop, pattern, accent).onFailure { e ->
        _controls.update { it.copy(statusMessage = e.localizedMessage ?: "Не удалось сохранить тему") }
      }
    }
  }

  fun selectProfileTab(tab: ProfileTab) {
    _controls.update { it.copy(profileTab = tab) }
  }

  fun openGiftModal(gift: NftGift) {
    _controls.update { it.copy(selectedGift = gift) }
  }

  fun closeGiftModal() {
    _controls.update { it.copy(selectedGift = null) }
  }

  fun openUsernameModal() {
    _controls.update { it.copy(showUsernameModal = true) }
  }

  fun closeUsernameModal() {
    _controls.update { it.copy(showUsernameModal = false) }
  }

  fun selectFrame(frame: AvatarFrameStyle) {
    viewModelScope.launch {
      repository
        .updateSelectedFrame(currentUserId, frame)
        .onSuccess {
          _controls.update { it.copy(statusMessage = "Рамка «${frame.title}» применена") }
        }
        .onFailure { e ->
          _controls.update { it.copy(statusMessage = e.localizedMessage ?: "Ошибка сохранения рамки") }
        }
    }
  }

  fun upgradeSelectedGift() {
    val gift = _controls.value.selectedGift ?: return
    viewModelScope.launch {
      repository
        .upgradeGift(currentUserId, gift)
        .onSuccess {
          val updated =
            if (gift.isUpgraded) gift
            else gift.copy(isUpgraded = true, modelName = "${gift.modelName} ★", priceRub = gift.priceRub + 1500)
          _controls.update {
            it.copy(
              selectedGift = updated,
              confettiTriggerKey = it.confettiTriggerKey + 1,
            )
          }
        }
        .onFailure { e ->
          _controls.update { it.copy(statusMessage = e.localizedMessage ?: "Ошибка улучшения") }
        }
    }
  }

  fun buyMarketGift(ownerName: String, currentCrystals: Long, gift: NftGift) {
    if (currentCrystals < gift.priceCrystals) {
      _controls.update { it.copy(statusMessage = "Недостаточно кристаллов") }
      return
    }
    viewModelScope.launch {
      repository
        .buyMarketGift(currentUserId, ownerName, currentCrystals, gift)
        .onSuccess { bought ->
          _controls.update {
            it.copy(
              profileTab = ProfileTab.GIFTS,
              selectedGift = bought,
              confettiTriggerKey = it.confettiTriggerKey + 1,
              statusMessage = "Подарок ${bought.fullTitle} куплен!",
            )
          }
        }
        .onFailure { e ->
          _controls.update { it.copy(statusMessage = e.localizedMessage ?: "Ошибка покупки") }
        }
    }
  }

  fun toggleMusic() {
    val nextPlaying = !_controls.value.isPlayingMusic
    _controls.update { it.copy(isPlayingMusic = nextPlaying) }
    musicJob?.cancel()
    if (nextPlaying) {
      musicJob =
        viewModelScope.launch {
          while (true) {
            delay(1000L)
            _controls.update { state ->
              state.copy(musicSeconds = (state.musicSeconds + 1) % state.musicDurationSeconds)
            }
          }
        }
    }
  }

  fun clearStatusMessage() {
    _controls.update { it.copy(statusMessage = null) }
  }

  private companion object {
    const val TAG = "AppVM"
  }
}
