package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatMessageDoc
import com.example.model.ChatThreadDoc
import com.example.model.NftGift
import com.example.model.SampleNftCatalog
import com.example.model.UserProfileDoc
import com.example.ui.components.ChatWallpaperCanvas
import com.example.ui.components.GiftPatternBackdrop
import com.example.ui.components.TelegramGiftImage
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgBorder
import com.example.ui.theme.TgCard
import com.example.ui.theme.TgCardElevated
import com.example.ui.theme.TgDivider
import com.example.ui.theme.TgSurface
import com.example.ui.theme.TgTextPrimary
import com.example.ui.theme.TgTextSecondary
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun ChatDetailScreen(
  profile: UserProfileDoc,
  chat: ChatThreadDoc,
  messages: List<ChatMessageDoc>,
  ownedGifts: List<NftGift>,
  showGiftPicker: Boolean,
  onBack: () -> Unit,
  onOpenAppearance: () -> Unit,
  onToggleGiftPicker: (Boolean) -> Unit,
  onSendMessage: (text: String, gift: NftGift?) -> Unit,
  onGiftClick: (NftGift) -> Unit,
) {
  BackHandler { onBack() }

  var draftText by remember { mutableStateOf("") }
  val listState = rememberLazyListState()

  val allKnownGifts =
    remember(ownedGifts) {
      ownedGifts + SampleNftCatalog.initialOwnedGifts + SampleNftCatalog.marketGifts
    }

  LaunchedEffect(messages.size) {
    if (messages.isNotEmpty()) {
      listState.animateScrollToItem(messages.size - 1)
    }
  }

  Box(modifier = Modifier.fillMaxSize()) {
    // Customizable ExteraGram dark wallpaper + pattern canvas
    ChatWallpaperCanvas(
      backdrop = profile.backdropEnum,
      pattern = profile.patternEnum,
      accent = profile.accentEnum,
    )

    Scaffold(
      containerColor = Color.Transparent,
      contentWindowInsets = WindowInsets.safeDrawing,
      topBar = {
        Column(modifier = Modifier.fillMaxWidth().background(TgSurface.copy(alpha = 0.94f))) {
          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = onBack,
              modifier = Modifier.size(48.dp).testTag("chat_back_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Назад к чатам",
                tint = TgTextPrimary,
              )
            }

            Box(
              modifier =
                Modifier.size(40.dp)
                  .clip(CircleShape)
                  .background(
                    Brush.linearGradient(
                      listOf(profile.accentEnum.color, Color(0xFF6366F1))
                    )
                  ),
              contentAlignment = Alignment.Center,
            ) {
              Text(
                text = chat.avatarText.ifEmpty { chat.title.take(1).uppercase() },
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
              )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = chat.title,
                style = MaterialTheme.typography.titleMedium,
                color = TgTextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = chat.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = profile.accentEnum.color,
              )
            }

            IconButton(
              onClick = onOpenAppearance,
              modifier = Modifier.size(48.dp).testTag("chat_detail_appearance_button"),
            ) {
              Icon(
                imageVector = Icons.Rounded.Palette,
                contentDescription = "Оформление чата",
                tint = TgTextPrimary,
              )
            }
          }
          HorizontalDivider(color = TgDivider, thickness = 1.dp)
        }
      },
      bottomBar = {
        Column(
          modifier =
            Modifier.fillMaxWidth()
              .background(TgSurface.copy(alpha = 0.96f))
              .navigationBarsPadding()
              .imePadding(),
        ) {
          HorizontalDivider(color = TgDivider, thickness = 1.dp)

          AnimatedVisibility(visible = showGiftPicker) {
            Row(
              modifier =
                Modifier.fillMaxWidth()
                  .horizontalScroll(rememberScrollState())
                  .padding(horizontal = 12.dp, vertical = 10.dp),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
              ownedGifts.forEach { gift ->
                Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier =
                    Modifier.width(78.dp)
                      .clip(RoundedCornerShape(14.dp))
                      .background(TgCard)
                      .border(1.dp, gift.accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                      .clickable {
                        onSendMessage(draftText, gift)
                        draftText = ""
                        onToggleGiftPicker(false)
                      }
                      .padding(6.dp)
                      .testTag("chat_gift_pick_${gift.id}"),
                ) {
                  Box(
                    modifier = Modifier.size(54.dp).clip(RoundedCornerShape(10.dp)),
                  ) {
                    GiftPatternBackdrop(
                      startColor = gift.bgStartColor,
                      endColor = gift.bgEndColor,
                      accentColor = gift.accentColor,
                    )
                    TelegramGiftImage(gift = gift, modifier = Modifier.fillMaxSize())
                  }
                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = gift.name,
                    fontSize = 11.sp,
                    color = TgTextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                  )
                }
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
          ) {
            IconButton(
              onClick = { onToggleGiftPicker(!showGiftPicker) },
              modifier = Modifier.size(48.dp).testTag("toggle_chat_gift_picker"),
            ) {
              Icon(
                imageVector = Icons.Rounded.CardGiftcard,
                contentDescription = "Отправить NFT подарок",
                tint = if (showGiftPicker) profile.accentEnum.color else TgTextSecondary,
              )
            }

            OutlinedTextField(
              value = draftText,
              onValueChange = { draftText = it },
              placeholder = { Text("Сообщение...", color = TgTextSecondary) },
              maxLines = 4,
              shape = RoundedCornerShape(22.dp),
              colors =
                OutlinedTextFieldDefaults.colors(
                  focusedBorderColor = profile.accentEnum.color,
                  unfocusedBorderColor = TgBorder,
                  focusedTextColor = TgTextPrimary,
                  unfocusedTextColor = TgTextPrimary,
                  focusedContainerColor = TgCard,
                  unfocusedContainerColor = TgCard,
                ),
              modifier = Modifier.weight(1f).testTag("chat_message_input"),
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
              onClick = {
                if (draftText.isNotBlank()) {
                  onSendMessage(draftText, null)
                  draftText = ""
                }
              },
              enabled = draftText.isNotBlank(),
              modifier = Modifier.size(48.dp).testTag("send_message_button"),
            ) {
              Icon(
                imageVector = Icons.AutoMirrored.Rounded.Send,
                contentDescription = "Отправить",
                tint = if (draftText.isNotBlank()) profile.accentEnum.color else TgTextSecondary,
              )
            }
          }
        }
      },
    ) { innerPadding ->
      Box(
        modifier = Modifier.fillMaxSize().padding(innerPadding),
        contentAlignment = Alignment.TopCenter,
      ) {
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize().widthIn(max = 600.dp),
          contentPadding = PaddingValues(horizontal = 14.dp, vertical = 14.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          item {
            Box(
              modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
              contentAlignment = Alignment.Center,
            ) {
              Box(
                modifier =
                  Modifier.clip(RoundedCornerShape(50))
                    .background(Color.Black.copy(alpha = 0.36f))
                    .padding(horizontal = 14.dp, vertical = 5.dp),
              ) {
                Text(
                  text = "Сегодня",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color.White.copy(alpha = 0.85f),
                )
              }
            }
          }

          items(items = messages, key = { it.id }) { msg ->
            val embeddedGift =
              remember(msg.giftSlug, allKnownGifts) {
                msg.giftSlug?.let { slug ->
                  allKnownGifts.firstOrNull { it.fragmentSlug == slug }
                    ?: run {
                      val palette = SampleNftCatalog.paletteForSlug(slug)
                      NftGift(
                        id = slug,
                        fragmentSlug = slug,
                        number = 1,
                        name = msg.giftTitle?.substringBefore(" #") ?: "Telegram Gift",
                        modelName = "Collectible",
                        modelRarityPercent = "1.5%",
                        backgroundName = "Sapphire",
                        backgroundRarityPercent = "1.8%",
                        patternName = "Telegram",
                        patternRarityPercent = "3.0%",
                        editionText = "NFT Подарок",
                        owner = profile.displayName,
                        priceRub = 5000,
                        priceCrystals = 100,
                        bgStartColor = palette.first,
                        bgEndColor = palette.second,
                        accentColor = palette.third,
                      )
                    }
                }
              }

            ChatBubbleItem(
              message = msg,
              bubbleColor =
                if (msg.outgoing) profile.accentEnum.bubbleBg
                else TgCardElevated.copy(alpha = 0.94f),
              embeddedGift = embeddedGift,
              onGiftClick = onGiftClick,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun ChatBubbleItem(
  message: ChatMessageDoc,
  bubbleColor: Color,
  embeddedGift: NftGift?,
  onGiftClick: (NftGift) -> Unit,
) {
  val timeText =
    remember(message.createdAt) {
      val date = message.createdAt?.toDate()
      if (date != null) SimpleDateFormat("HH:mm", Locale.getDefault()).format(date) else "сейчас"
    }

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (message.outgoing) Alignment.End else Alignment.Start,
  ) {
    Surface(
      shape =
        RoundedCornerShape(
          topStart = 18.dp,
          topEnd = 18.dp,
          bottomStart = if (message.outgoing) 18.dp else 4.dp,
          bottomEnd = if (message.outgoing) 4.dp else 18.dp,
        ),
      color = bubbleColor,
      modifier =
        Modifier.widthIn(max = 310.dp)
          .border(
            1.dp,
            Color.White.copy(alpha = 0.08f),
            RoundedCornerShape(
              topStart = 18.dp,
              topEnd = 18.dp,
              bottomStart = if (message.outgoing) 18.dp else 4.dp,
              bottomEnd = if (message.outgoing) 4.dp else 18.dp,
            ),
          ),
    ) {
      Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp)) {
        if (embeddedGift != null) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
              Modifier.fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.24f))
                .clickable { onGiftClick(embeddedGift) }
                .padding(10.dp),
          ) {
            Box(
              modifier = Modifier.size(104.dp).clip(RoundedCornerShape(14.dp)),
            ) {
              GiftPatternBackdrop(
                startColor = embeddedGift.bgStartColor,
                endColor = embeddedGift.bgEndColor,
                accentColor = embeddedGift.accentColor,
              )
              TelegramGiftImage(gift = embeddedGift, modifier = Modifier.fillMaxSize())
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = embeddedGift.fullTitle,
              style = MaterialTheme.typography.labelMedium,
              color = Color.White,
              fontWeight = FontWeight.Bold,
            )
            Text(
              text = "Нажмите, чтобы открыть NFT",
              fontSize = 11.sp,
              color = Color.White.copy(alpha = 0.72f),
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
        }

        Text(
          text = message.text,
          style = MaterialTheme.typography.bodyMedium,
          color = TgTextPrimary,
        )

        Spacer(modifier = Modifier.height(3.dp))

        Row(
          modifier = Modifier.align(Alignment.End),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
          Text(
            text = timeText,
            fontSize = 11.sp,
            color = Color.White.copy(alpha = 0.65f),
          )
          if (message.outgoing) {
            Icon(
              imageVector = Icons.Rounded.DoneAll,
              contentDescription = "Прочитано",
              tint = Color(0xFF7DD3FC),
              modifier = Modifier.size(14.dp),
            )
          }
        }
      }
    }
  }
}
