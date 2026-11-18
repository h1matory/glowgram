package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.auth.authStateFlow
import com.example.auth.signOut
import com.example.data.ExteraRepository
import com.example.model.AppSection
import com.example.ui.components.ChatAppearanceSheet
import com.example.ui.components.CollectibleUsernameModal
import com.example.ui.components.GiftDetailModal
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatDetailScreen
import com.example.ui.screens.ChatsScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ProfileSetupScreen
import com.example.ui.theme.ExteraNftTheme
import com.example.ui.theme.TgBgDeep
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgBlueBright
import com.example.ui.theme.TgTextPrimary
import com.example.ui.theme.TgTextSecondary
import com.example.viewmodel.AccountState
import com.example.viewmodel.AppViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      ExteraNftTheme {
        ExteraRoot()
      }
    }
  }
}

@Composable
private fun ExteraRoot() {
  val authState = remember { Firebase.auth.authStateFlow() }
  val currentUser by authState.collectAsStateWithLifecycle(initialValue = Firebase.auth.currentUser)

  val user = currentUser
  if (user == null) {
    AuthScreen()
  } else {
    key(user.uid) {
      AuthenticatedApp(user = user)
    }
  }
}

@Composable
private fun AuthenticatedApp(user: FirebaseUser) {
  val context = LocalContext.current
  val appContext = context.applicationContext
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  val viewModel: AppViewModel =
    viewModel(
      key = user.uid,
      factory =
        viewModelFactory {
          initializer {
            AppViewModel(
              repository = ExteraRepository(appContext),
              currentUserId = user.uid,
            )
          }
        },
    )

  val accountState by viewModel.accountState.collectAsStateWithLifecycle()
  val ownedGifts by viewModel.ownedGifts.collectAsStateWithLifecycle()
  val chats by viewModel.chats.collectAsStateWithLifecycle()
  val activeMessages by viewModel.activeMessages.collectAsStateWithLifecycle()
  val controls by viewModel.controls.collectAsStateWithLifecycle()

  val snackbarHostState = remember { SnackbarHostState() }
  val clipboardManager = LocalClipboardManager.current
  val copiedMessage = stringResource(R.string.link_copied_toast)

  LaunchedEffect(controls.statusMessage) {
    controls.statusMessage?.let { msg ->
      snackbarHostState.showSnackbar(msg)
      viewModel.clearStatusMessage()
    }
  }

  val handleSignOut: () -> Unit = {
    signOut(credentialManager = credentialManager, scope = scope)
  }

  when (val state = accountState) {
    AccountState.Loading -> {
      Box(
        modifier = Modifier.fillMaxSize().background(TgBgDeep),
        contentAlignment = Alignment.Center,
      ) {
        CircularProgressIndicator(color = TgBlueBright)
      }
    }

    is AccountState.Error -> {
      Box(
        modifier = Modifier.fillMaxSize().background(TgBgDeep).padding(24.dp),
        contentAlignment = Alignment.Center,
      ) {
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          Text(
            text = "Не удалось загрузить профиль",
            style = MaterialTheme.typography.titleLarge,
            color = TgTextPrimary,
          )
          Text(
            text = state.message,
            style = MaterialTheme.typography.bodyMedium,
            color = TgTextSecondary,
            textAlign = TextAlign.Center,
          )
          Button(
            onClick = handleSignOut,
            colors = ButtonDefaults.buttonColors(containerColor = TgBlue, contentColor = Color.White),
          ) {
            Text("Выйти и попробовать снова")
          }
        }
      }
    }

    AccountState.NeedsRegistration -> {
      ProfileSetupScreen(
        initialGoogleName = user.displayName,
        isSubmitting = controls.isSubmittingRegistration,
        onSubmit = viewModel::registerAccount,
        onSignOut = handleSignOut,
      )
    }

    is AccountState.Ready -> {
      val profile = state.profile
      val activeChat = chats.firstOrNull { it.id == controls.activeChatId }

      BackHandler(
        enabled =
          controls.selectedGift != null ||
            controls.showUsernameModal ||
            controls.showAppearanceSheet ||
            controls.showNewChatDialog ||
            activeChat != null ||
            controls.section == AppSection.PROFILE,
      ) {
        when {
          controls.selectedGift != null -> viewModel.closeGiftModal()
          controls.showUsernameModal -> viewModel.closeUsernameModal()
          controls.showAppearanceSheet -> viewModel.setAppearanceSheetVisible(false)
          controls.showNewChatDialog -> viewModel.setNewChatDialogVisible(false)
          activeChat != null -> viewModel.closeActiveChat()
          controls.section == AppSection.PROFILE -> viewModel.openSection(AppSection.CHATS)
        }
      }

      when {
        controls.section == AppSection.PROFILE -> {
          ProfileScreen(
            profile = profile,
            ownedGifts = ownedGifts,
            controls = controls,
            snackbarHostState = snackbarHostState,
            onBackToChats = { viewModel.openSection(AppSection.CHATS) },
            onTabSelect = viewModel::selectProfileTab,
            onGiftClick = viewModel::openGiftModal,
            onSelectFrame = viewModel::selectFrame,
            onBuyGift = { gift ->
              viewModel.buyMarketGift(
                ownerName = profile.displayName,
                currentCrystals = profile.crystals,
                gift = gift,
              )
            },
            onOpenUsernameModal = viewModel::openUsernameModal,
            onToggleMusic = viewModel::toggleMusic,
          )
        }

        activeChat != null -> {
          ChatDetailScreen(
            profile = profile,
            chat = activeChat,
            messages = activeMessages,
            ownedGifts = ownedGifts,
            showGiftPicker = controls.showGiftPickerInChat,
            onBack = viewModel::closeActiveChat,
            onOpenAppearance = { viewModel.setAppearanceSheetVisible(true) },
            onToggleGiftPicker = viewModel::setGiftPickerInChatVisible,
            onSendMessage = { text, gift ->
              viewModel.sendMessage(
                chatId = activeChat.id,
                senderName = profile.displayName,
                text = text,
                gift = gift,
              )
            },
            onGiftClick = viewModel::openGiftModal,
          )
        }

        else -> {
          ChatsScreen(
            profile = profile,
            chats = chats,
            giftCount = ownedGifts.size,
            showNewChatDialog = controls.showNewChatDialog,
            snackbarHostState = snackbarHostState,
            onOpenChat = viewModel::openChat,
            onOpenProfile = { tab -> viewModel.openSection(AppSection.PROFILE, tab) },
            onOpenAppearance = { viewModel.setAppearanceSheetVisible(true) },
            onSetNewChatDialogVisible = viewModel::setNewChatDialogVisible,
            onCreateChat = viewModel::createNewChat,
            onSignOut = handleSignOut,
          )
        }
      }

      if (controls.showAppearanceSheet) {
        ChatAppearanceSheet(
          backdrop = profile.backdropEnum,
          pattern = profile.patternEnum,
          accent = profile.accentEnum,
          onSelect = viewModel::updateChatAppearance,
          onDismiss = { viewModel.setAppearanceSheetVisible(false) },
        )
      }

      controls.selectedGift?.let { gift ->
        GiftDetailModal(
          gift = gift,
          confettiTriggerKey = controls.confettiTriggerKey,
          onUpgradeClick = viewModel::upgradeSelectedGift,
          onDismiss = viewModel::closeGiftModal,
        )
      }

      if (controls.showUsernameModal) {
        CollectibleUsernameModal(
          collectible = controls.collectibleUsername.copy(owner = profile.displayName),
          onCopyLink = {
            clipboardManager.setText(AnnotatedString("https://t.me/crow"))
            viewModel.closeUsernameModal()
            scope.launch { snackbarHostState.showSnackbar(copiedMessage) }
          },
          onDismiss = viewModel::closeUsernameModal,
        )
      }
    }
  }
}
