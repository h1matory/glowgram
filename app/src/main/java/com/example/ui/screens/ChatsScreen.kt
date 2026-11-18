package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatThreadDoc
import com.example.model.ProfileTab
import com.example.model.UserProfileDoc
import com.example.ui.components.AnimatedAvatarWithFrame
import com.example.ui.theme.TgBgDeep
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgBlueBright
import com.example.ui.theme.TgBorder
import com.example.ui.theme.TgCard
import com.example.ui.theme.TgCardElevated
import com.example.ui.theme.TgCyan
import com.example.ui.theme.TgDivider
import com.example.ui.theme.TgSurface
import com.example.ui.theme.TgTextPrimary
import com.example.ui.theme.TgTextSecondary
import kotlinx.coroutines.launch

@Composable
fun ChatsScreen(
  profile: UserProfileDoc,
  chats: List<ChatThreadDoc>,
  giftCount: Int,
  showNewChatDialog: Boolean,
  snackbarHostState: SnackbarHostState,
  onOpenChat: (String) -> Unit,
  onOpenProfile: (ProfileTab) -> Unit,
  onOpenAppearance: () -> Unit,
  onSetNewChatDialogVisible: (Boolean) -> Unit,
  onCreateChat: (title: String, subtitle: String) -> Unit,
  onSignOut: () -> Unit,
) {
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  var searchQuery by remember { mutableStateOf("") }

  val filteredChats =
    remember(chats, searchQuery) {
      val q = searchQuery.trim()
      if (q.isEmpty()) chats
      else
        chats.filter {
          it.title.contains(q, ignoreCase = true) || it.lastMessage.contains(q, ignoreCase = true)
        }
    }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier.widthIn(max = 304.dp),
        drawerContainerColor = TgSurface,
        drawerContentColor = TgTextPrimary,
      ) {
        ExteraDrawerContent(
          profile = profile,
          giftCount = giftCount,
          onOpenProfile = { tab ->
            scope.launch { drawerState.close() }
            onOpenProfile(tab)
          },
          onCloseDrawer = { scope.launch { drawerState.close() } },
          onOpenAppearance = {
            scope.launch { drawerState.close() }
            onOpenAppearance()
          },
          onSignOut = {
            scope.launch { drawerState.close() }
            onSignOut()
          },
        )
      }
    },
  ) {
    Scaffold(
      containerColor = TgBgDeep,
      contentWindowInsets = WindowInsets.safeDrawing,
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      floatingActionButton = {
        FloatingActionButton(
          onClick = { onSetNewChatDialogVisible(true) },
          containerColor = profile.accentEnum.color,
          contentColor = Color.White,
          modifier = Modifier.testTag("new_chat_fab"),
        ) {
          Icon(imageVector = Icons.Rounded.Add, contentDescription = "Новый чат")
        }
      },
    ) { innerPadding ->
      Box(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentAlignment = Alignment.TopCenter,
      ) {
        Column(modifier = Modifier.fillMaxSize().widthIn(max = 600.dp)) {
          // Top ExteraGram bar
          Row(
            modifier =
              Modifier.fillMaxWidth()
                .background(TgSurface)
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = { scope.launch { drawerState.open() } },
              modifier = Modifier.size(48.dp).testTag("open_drawer_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Menu,
                contentDescription = "Меню",
                tint = TgTextPrimary,
              )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "ExteraGram",
                style = MaterialTheme.typography.titleLarge,
                color = TgTextPrimary,
                fontWeight = FontWeight.Bold,
              )
              Text(
                text = "${chats.size} чатов • в сети",
                style = MaterialTheme.typography.labelSmall,
                color = profile.accentEnum.color,
              )
            }

            // Crystal pill shortcut -> Market
            Row(
              modifier =
                Modifier.clip(RoundedCornerShape(50))
                  .background(TgCard)
                  .border(1.dp, TgBorder, RoundedCornerShape(50))
                  .clickable { onOpenProfile(ProfileTab.MARKET) }
                  .padding(horizontal = 10.dp, vertical = 6.dp)
                  .testTag("chats_crystal_chip"),
              verticalAlignment = Alignment.CenterVertically,
            ) {
              Icon(
                imageVector = Icons.Rounded.Diamond,
                contentDescription = null,
                tint = TgCyan,
                modifier = Modifier.size(14.dp),
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text(
                text = profile.crystals.toString(),
                style = MaterialTheme.typography.labelMedium,
                color = TgTextPrimary,
                fontWeight = FontWeight.Bold,
              )
            }

            IconButton(
              onClick = onOpenAppearance,
              modifier = Modifier.size(48.dp).testTag("chats_appearance_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = "Оформление чатов",
                tint = TgTextPrimary,
              )
            }

            // Quick profile avatar button
            Box(
              modifier =
                Modifier.size(48.dp)
                  .clip(CircleShape)
                  .clickable { onOpenProfile(ProfileTab.GIFTS) }
                  .testTag("chats_profile_shortcut"),
              contentAlignment = Alignment.Center,
            ) {
              Box(
                modifier =
                  Modifier.size(34.dp)
                    .clip(CircleShape)
                    .background(
                      Brush.linearGradient(
                        listOf(profile.frameEnum.primaryColor, profile.frameEnum.secondaryColor)
                      )
                    )
                    .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape),
                contentAlignment = Alignment.Center,
              ) {
                Text(
                  text = profile.displayName.take(1).uppercase(),
                  color = Color.White,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                )
              }
            }
          }

          HorizontalDivider(color = TgDivider, thickness = 1.dp)

          // Search bar
          OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Поиск по чатам...", color = TgTextSecondary) },
            leadingIcon = {
              Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                tint = TgTextSecondary,
              )
            },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            colors =
              OutlinedTextFieldDefaults.colors(
                focusedBorderColor = profile.accentEnum.color,
                unfocusedBorderColor = TgBorder,
                focusedTextColor = TgTextPrimary,
                unfocusedTextColor = TgTextPrimary,
                focusedContainerColor = TgCard,
                unfocusedContainerColor = TgCard,
              ),
            modifier =
              Modifier.fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
                .testTag("chats_search_input"),
          )

          if (filteredChats.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxSize().padding(24.dp),
              contentAlignment = Alignment.Center,
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
              ) {
                Icon(
                  imageVector = Icons.Rounded.ChatBubbleOutline,
                  contentDescription = null,
                  tint = TgTextSecondary,
                  modifier = Modifier.size(44.dp),
                )
                Text(
                  text = if (chats.isEmpty()) "Чатов пока нет" else "Ничего не найдено",
                  style = MaterialTheme.typography.titleMedium,
                  color = TgTextPrimary,
                )
                Text(
                  text = "Нажмите «+», чтобы создать новый чат и отправить NFT-подарок.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = TgTextSecondary,
                )
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
              items(items = filteredChats, key = { it.id }) { chat ->
                ChatThreadRow(
                  chat = chat,
                  accentColor = profile.accentEnum.color,
                  onClick = { onOpenChat(chat.id) },
                )
              }
              item { Spacer(modifier = Modifier.height(76.dp)) }
            }
          }
        }
      }
    }
  }

  if (showNewChatDialog) {
    NewChatDialog(
      onConfirm = onCreateChat,
      onDismiss = { onSetNewChatDialogVisible(false) },
    )
  }
}

@Composable
private fun ChatThreadRow(
  chat: ChatThreadDoc,
  accentColor: Color,
  onClick: () -> Unit,
) {
  Surface(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(16.dp))
        .border(1.dp, TgBorder, RoundedCornerShape(16.dp))
        .clickable(onClick = onClick)
        .testTag("chat_row_${chat.id}"),
    shape = RoundedCornerShape(16.dp),
    color = TgSurface,
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier.size(50.dp)
            .clip(CircleShape)
            .background(
              if (chat.avatarText == "★") {
                Brush.linearGradient(listOf(TgBlue, TgCyan))
              } else {
                Brush.linearGradient(listOf(accentColor, Color(0xFF6366F1)))
              }
            ),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = chat.avatarText.ifEmpty { chat.title.take(1).uppercase() },
          color = Color.White,
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp,
        )
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically,
        ) {
          Text(
            text = chat.title,
            style = MaterialTheme.typography.titleMedium,
            color = TgTextPrimary,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
          )
          if (chat.pinned) {
            Icon(
              imageVector = Icons.Rounded.PushPin,
              contentDescription = "Закреплён",
              tint = TgTextSecondary,
              modifier = Modifier.size(15.dp),
            )
          }
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text(
          text = chat.lastMessage.ifBlank { chat.subtitle },
          style = MaterialTheme.typography.bodyMedium,
          color = TgTextSecondary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun ExteraDrawerContent(
  profile: UserProfileDoc,
  giftCount: Int,
  onOpenProfile: (ProfileTab) -> Unit,
  onCloseDrawer: () -> Unit,
  onOpenAppearance: () -> Unit,
  onSignOut: () -> Unit,
) {
  Column(modifier = Modifier.fillMaxSize()) {
    Box(
      modifier =
        Modifier.fillMaxWidth()
          .background(
            Brush.verticalGradient(
              listOf(
                profile.frameEnum.primaryColor.copy(alpha = 0.28f),
                TgCardElevated,
              )
            )
          )
          .padding(horizontal = 20.dp, vertical = 22.dp),
    ) {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        AnimatedAvatarWithFrame(
          initials = profile.displayName.take(1).uppercase(),
          frameStyle = profile.frameEnum,
          avatarSize = 60.dp,
        )
        Text(
          text = profile.displayName,
          style = MaterialTheme.typography.titleLarge,
          color = TgTextPrimary,
          fontWeight = FontWeight.Bold,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = profile.username,
            style = MaterialTheme.typography.bodyMedium,
            color = TgBlueBright,
          )
          Spacer(modifier = Modifier.width(10.dp))
          Icon(
            imageVector = Icons.Rounded.Diamond,
            contentDescription = null,
            tint = TgCyan,
            modifier = Modifier.size(14.dp),
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = "${profile.crystals}",
            style = MaterialTheme.typography.labelMedium,
            color = TgTextPrimary,
            fontWeight = FontWeight.Bold,
          )
        }
      }
    }

    HorizontalDivider(color = TgDivider, thickness = 1.dp)
    Spacer(modifier = Modifier.height(8.dp))

    DrawerRowItem(
      icon = Icons.Rounded.Person,
      label = "Мой профиль ($giftCount NFT)",
      selected = false,
      testTag = "drawer_profile_item",
      onClick = { onOpenProfile(ProfileTab.GIFTS) },
    )
    DrawerRowItem(
      icon = Icons.Rounded.ChatBubbleOutline,
      label = "Чаты",
      selected = true,
      testTag = "drawer_chats_item",
      onClick = onCloseDrawer,
    )
    DrawerRowItem(
      icon = Icons.Rounded.Storefront,
      label = "NFT Маркет Telegram",
      selected = false,
      testTag = "drawer_market_item",
      onClick = { onOpenProfile(ProfileTab.MARKET) },
    )
    DrawerRowItem(
      icon = Icons.Rounded.AutoAwesome,
      label = "Неоновые рамки",
      selected = false,
      testTag = "drawer_frames_item",
      onClick = { onOpenProfile(ProfileTab.FRAMES) },
    )
    DrawerRowItem(
      icon = Icons.Rounded.Palette,
      label = "Оформление чатов",
      selected = false,
      testTag = "drawer_appearance_item",
      onClick = onOpenAppearance,
    )

    Spacer(modifier = Modifier.weight(1f))
    HorizontalDivider(color = TgDivider, thickness = 1.dp)

    DrawerRowItem(
      icon = Icons.AutoMirrored.Rounded.Logout,
      label = "Выйти из аккаунта",
      selected = false,
      testTag = "drawer_sign_out_item",
      tint = Color(0xFFF87171),
      onClick = onSignOut,
    )
    Spacer(modifier = Modifier.height(12.dp))
  }
}

@Composable
private fun DrawerRowItem(
  icon: ImageVector,
  label: String,
  selected: Boolean,
  testTag: String,
  tint: Color = TgTextPrimary,
  onClick: () -> Unit,
) {
  NavigationDrawerItem(
    icon = {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = if (selected) TgBlueBright else tint,
      )
    },
    label = {
      Text(
        text = label,
        color = if (selected) TgBlueBright else tint,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      )
    },
    selected = selected,
    onClick = onClick,
    colors =
      NavigationDrawerItemDefaults.colors(
        selectedContainerColor = TgBlue.copy(alpha = 0.16f),
        unselectedContainerColor = Color.Transparent,
      ),
    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp).testTag(testTag),
  )
}

@Composable
private fun NewChatDialog(
  onConfirm: (title: String, subtitle: String) -> Unit,
  onDismiss: () -> Unit,
) {
  var title by remember { mutableStateOf("") }
  var subtitle by remember { mutableStateOf("в сети") }

  AlertDialog(
    onDismissRequest = onDismiss,
    containerColor = TgCard,
    titleContentColor = TgTextPrimary,
    textContentColor = TgTextSecondary,
    title = { Text("Новый чат", fontWeight = FontWeight.Bold) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = title,
          onValueChange = { title = it },
          label = { Text("Название или имя") },
          singleLine = true,
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TgBlueBright,
              unfocusedBorderColor = TgBorder,
              focusedTextColor = TgTextPrimary,
              unfocusedTextColor = TgTextPrimary,
            ),
          modifier = Modifier.fillMaxWidth().testTag("new_chat_title_input"),
        )
        OutlinedTextField(
          value = subtitle,
          onValueChange = { subtitle = it },
          label = { Text("Подпись (например: в сети)") },
          singleLine = true,
          colors =
            OutlinedTextFieldDefaults.colors(
              focusedBorderColor = TgBlueBright,
              unfocusedBorderColor = TgBorder,
              focusedTextColor = TgTextPrimary,
              unfocusedTextColor = TgTextPrimary,
            ),
          modifier = Modifier.fillMaxWidth().testTag("new_chat_subtitle_input"),
        )
      }
    },
    confirmButton = {
      Button(
        onClick = { onConfirm(title.ifBlank { "Новый контакт" }, subtitle) },
        colors = ButtonDefaults.buttonColors(containerColor = TgBlue, contentColor = Color.White),
        modifier = Modifier.testTag("confirm_new_chat_button"),
      ) {
        Text("Создать")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Отмена", color = TgTextSecondary)
      }
    },
  )
}
