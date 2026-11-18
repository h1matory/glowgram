package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.model.AvatarFrameStyle
import com.example.model.NftGift
import com.example.model.ProfileTab
import com.example.model.UserProfileDoc
import com.example.ui.components.AnimatedAvatarWithFrame
import com.example.ui.components.AvatarWithOrbitingGifts
import com.example.ui.components.GiftPatternBackdrop
import com.example.ui.components.TelegramGiftImage
import com.example.ui.theme.TgBadgeBg
import com.example.ui.theme.TgBadgeText
import com.example.ui.theme.TgBgDeep
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgBlueBright
import com.example.ui.theme.TgBorder
import com.example.ui.theme.TgCard
import com.example.ui.theme.TgCardElevated
import com.example.ui.theme.TgCyan
import com.example.ui.theme.TgDivider
import com.example.ui.theme.TgMint
import com.example.ui.theme.TgSurface
import com.example.ui.theme.TgTextPrimary
import com.example.ui.theme.TgTextSecondary
import com.example.viewmodel.UiControlsState

@Composable
fun ProfileScreen(
  profile: UserProfileDoc,
  ownedGifts: List<NftGift>,
  controls: UiControlsState,
  snackbarHostState: SnackbarHostState,
  onBackToChats: () -> Unit,
  onTabSelect: (ProfileTab) -> Unit,
  onGiftClick: (NftGift) -> Unit,
  onSelectFrame: (AvatarFrameStyle) -> Unit,
  onBuyGift: (NftGift) -> Unit,
  onOpenUsernameModal: () -> Unit,
  onToggleMusic: () -> Unit,
) {
  BackHandler { onBackToChats() }

  Scaffold(
    containerColor = TgBgDeep,
    contentWindowInsets = WindowInsets.safeDrawing,
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
  ) { innerPadding ->
    Box(
      modifier = Modifier.fillMaxSize().padding(innerPadding),
      contentAlignment = Alignment.TopCenter,
    ) {
      LazyColumn(
        modifier = Modifier.fillMaxSize().widthIn(max = 600.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        item {
          TopProfileBar(
            crystals = profile.crystals.toInt(),
            onBackToChats = onBackToChats,
            onEditFramesClick = { onTabSelect(ProfileTab.FRAMES) },
          )
        }

        item {
          ProfileHeroCard(
            profile = profile,
            ownedGifts = ownedGifts,
            controls = controls,
            onGiftClick = onGiftClick,
            onToggleMusic = onToggleMusic,
          )
        }

        item {
          ProfileInfoSection(
            profile = profile,
            controls = controls,
            onCollectibleClick = onOpenUsernameModal,
          )
        }

        item {
          ProfileTabBar(
            activeTab = controls.profileTab,
            giftCount = ownedGifts.size,
            onTabSelect = onTabSelect,
          )
        }

        when (controls.profileTab) {
          ProfileTab.GIFTS -> {
            item {
              if (ownedGifts.isEmpty()) {
                EmptyGiftsCard(onOpenMarket = { onTabSelect(ProfileTab.MARKET) })
              } else {
                NftGiftGrid(
                  gifts = ownedGifts,
                  onGiftClick = onGiftClick,
                )
              }
            }
          }
          ProfileTab.FRAMES -> {
            item {
              FramesGrid(
                initialLetter = profile.displayName.take(1).ifEmpty { "B" }.uppercase(),
                selectedFrame = profile.frameEnum,
                onSelectFrame = onSelectFrame,
              )
            }
          }
          ProfileTab.MARKET -> {
            item {
              MarketGrid(
                marketGifts = controls.marketGifts,
                onBuyGift = onBuyGift,
              )
            }
          }
        }

        item { Spacer(modifier = Modifier.navigationBarsPadding().height(8.dp)) }
      }
    }
  }
}

@Composable
private fun TopProfileBar(
  crystals: Int,
  onBackToChats: () -> Unit,
  onEditFramesClick: () -> Unit,
) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Row(verticalAlignment = Alignment.CenterVertically) {
      IconButton(
        onClick = onBackToChats,
        modifier = Modifier.size(48.dp).testTag("profile_back_to_chats"),
      ) {
        Icon(
          imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
          contentDescription = "Назад к чатам",
          tint = TgTextPrimary,
        )
      }
      Text(
        text = stringResource(R.string.profile_title),
        style = MaterialTheme.typography.titleLarge,
        color = TgTextPrimary,
        fontWeight = FontWeight.Bold,
      )
    }

    Row(
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Row(
        modifier =
          Modifier.clip(RoundedCornerShape(50))
            .background(TgCard)
            .border(1.dp, TgBorder, RoundedCornerShape(50))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Rounded.Diamond,
          contentDescription = null,
          tint = TgCyan,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(5.dp))
        Text(
          text = crystals.toString(),
          style = MaterialTheme.typography.labelMedium,
          color = TgTextPrimary,
          fontWeight = FontWeight.Bold,
        )
      }

      Row(
        modifier =
          Modifier.clip(RoundedCornerShape(50))
            .background(TgCardElevated)
            .border(1.dp, TgBorder, RoundedCornerShape(50))
            .clickable(onClick = onEditFramesClick)
            .padding(horizontal = 14.dp, vertical = 9.dp)
            .testTag("edit_frame_button"),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          imageVector = Icons.Rounded.Edit,
          contentDescription = null,
          tint = TgTextPrimary,
          modifier = Modifier.size(15.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text = stringResource(R.string.edit_button),
          style = MaterialTheme.typography.labelMedium,
          color = TgTextPrimary,
        )
      }
    }
  }
}

@Composable
private fun ProfileHeroCard(
  profile: UserProfileDoc,
  ownedGifts: List<NftGift>,
  controls: UiControlsState,
  onGiftClick: (NftGift) -> Unit,
  onToggleMusic: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().border(1.dp, TgBorder, RoundedCornerShape(22.dp)),
    shape = RoundedCornerShape(22.dp),
    color = TgSurface,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .background(
            Brush.verticalGradient(
              listOf(
                profile.frameEnum.primaryColor.copy(alpha = 0.14f),
                TgSurface,
              )
            )
          )
          .padding(horizontal = 16.dp, vertical = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
    ) {
      Box(
        modifier =
          Modifier.clip(RoundedCornerShape(50))
            .background(Color.Black.copy(alpha = 0.28f))
            .padding(horizontal = 12.dp, vertical = 5.dp),
      ) {
        Text(
          text = stringResource(R.string.profile_preview_hint),
          style = MaterialTheme.typography.labelSmall,
          color = TgTextSecondary,
        )
      }

      Spacer(modifier = Modifier.height(6.dp))

      AvatarWithOrbitingGifts(
        initials = profile.displayName.take(1).ifEmpty { "B" }.uppercase(),
        frameStyle = profile.frameEnum,
        gifts = ownedGifts,
        onGiftClick = onGiftClick,
      )

      Spacer(modifier = Modifier.height(6.dp))

      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = profile.displayName,
          style = MaterialTheme.typography.headlineMedium,
          color = TgTextPrimary,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier =
            Modifier.clip(RoundedCornerShape(8.dp))
              .background(TgCardElevated)
              .border(1.dp, TgBorder, RoundedCornerShape(8.dp))
              .padding(horizontal = 7.dp, vertical = 2.dp),
        ) {
          Text(
            text = (40 + ownedGifts.size).toString(),
            style = MaterialTheme.typography.labelSmall,
            color = TgTextSecondary,
            fontWeight = FontWeight.Bold,
          )
        }
      }

      Text(
        text = stringResource(R.string.status_online),
        style = MaterialTheme.typography.bodyMedium,
        color = TgBlueBright,
      )

      Spacer(modifier = Modifier.height(14.dp))

      MusicPlayerPill(
        isPlaying = controls.isPlayingMusic,
        seconds = controls.musicSeconds,
        durationSeconds = controls.musicDurationSeconds,
        onToggle = onToggleMusic,
      )
    }
  }
}

@Composable
private fun MusicPlayerPill(
  isPlaying: Boolean,
  seconds: Int,
  durationSeconds: Int,
  onToggle: () -> Unit,
) {
  val formattedTime =
    remember(seconds) {
      val mins = seconds / 60
      val secs = seconds % 60
      "%d:%02d".format(mins, secs)
    }

  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(TgCard)
        .border(1.dp, TgBorder, RoundedCornerShape(14.dp))
        .padding(horizontal = 10.dp, vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    IconButton(
      onClick = onToggle,
      modifier = Modifier.size(48.dp).testTag("music_toggle_button"),
    ) {
      Box(
        modifier = Modifier.size(34.dp).clip(CircleShape).background(TgBlue),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
          contentDescription = stringResource(R.string.play_pause_cd),
          tint = Color.White,
          modifier = Modifier.size(20.dp),
        )
      }
    }

    Spacer(modifier = Modifier.width(6.dp))

    Column(modifier = Modifier.weight(1f)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          imageVector = Icons.Rounded.MusicNote,
          contentDescription = null,
          tint = TgBlueBright,
          modifier = Modifier.size(14.dp),
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
          text = "назад в кресты • dabbacwood",
          style = MaterialTheme.typography.bodyMedium,
          color = TgTextPrimary,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
      AnimatedVisibility(visible = isPlaying, enter = fadeIn(), exit = fadeOut()) {
        LinearProgressIndicator(
          progress = { seconds.toFloat() / durationSeconds.toFloat() },
          modifier = Modifier.fillMaxWidth().padding(top = 6.dp).height(3.dp).clip(CircleShape),
          color = TgBlueBright,
          trackColor = TgBorder,
        )
      }
    }

    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = formattedTime,
      style = MaterialTheme.typography.labelMedium,
      color = TgTextSecondary,
    )
  }
}

@Composable
private fun ProfileInfoSection(
  profile: UserProfileDoc,
  controls: UiControlsState,
  onCollectibleClick: () -> Unit,
) {
  Surface(
    modifier = Modifier.fillMaxWidth().border(1.dp, TgBorder, RoundedCornerShape(18.dp)),
    shape = RoundedCornerShape(18.dp),
    color = TgCard,
  ) {
    Column(modifier = Modifier.fillMaxWidth()) {
      Column(
        modifier =
          Modifier.fillMaxWidth()
            .clickable(onClick = onCollectibleClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag("collectible_username_row"),
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = profile.username,
            style = MaterialTheme.typography.bodyLarge,
            color = TgTextPrimary,
            fontWeight = FontWeight.Medium,
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${stringResource(R.string.also_prefix)} ${controls.collectibleUsername.username}",
            style = MaterialTheme.typography.bodyMedium,
            color = TgBlueBright,
            fontWeight = FontWeight.SemiBold,
          )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = stringResource(R.string.username_label),
          style = MaterialTheme.typography.labelMedium,
          color = TgTextSecondary,
        )
      }

      HorizontalDivider(color = TgDivider, thickness = 1.dp)

      Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
          text = profile.bio.ifBlank { "—" },
          style = MaterialTheme.typography.bodyLarge,
          color = TgTextPrimary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = stringResource(R.string.bio_label),
          style = MaterialTheme.typography.labelMedium,
          color = TgTextSecondary,
        )
      }

      HorizontalDivider(color = TgDivider, thickness = 1.dp)

      Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text(
          text = profile.birthday.ifBlank { "Не указан" },
          style = MaterialTheme.typography.bodyLarge,
          color = TgTextPrimary,
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = stringResource(R.string.birthday_label),
          style = MaterialTheme.typography.labelMedium,
          color = TgTextSecondary,
        )
      }
    }
  }
}

@Composable
private fun ProfileTabBar(
  activeTab: ProfileTab,
  giftCount: Int,
  onTabSelect: (ProfileTab) -> Unit,
) {
  Row(
    modifier =
      Modifier.fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(TgCard)
        .border(1.dp, TgBorder, RoundedCornerShape(14.dp))
        .padding(4.dp),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    ProfileTab.entries.forEach { tab ->
      val selected = tab == activeTab
      val label =
        if (tab == ProfileTab.GIFTS) "${tab.title} ($giftCount)" else tab.title

      Box(
        modifier =
          Modifier.weight(1f)
            .height(48.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(if (selected) TgBlue else Color.Transparent)
            .clickable { onTabSelect(tab) }
            .testTag("tab_${tab.name.lowercase()}"),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = label,
          style = MaterialTheme.typography.labelMedium,
          color = if (selected) Color.White else TgTextSecondary,
          fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
        )
      }
    }
  }
}

@Composable
private fun EmptyGiftsCard(onOpenMarket: () -> Unit) {
  Surface(
    modifier =
      Modifier.fillMaxWidth()
        .border(1.dp, TgBorder, RoundedCornerShape(18.dp))
        .clickable(onClick = onOpenMarket),
    shape = RoundedCornerShape(18.dp),
    color = TgCard,
  ) {
    Column(
      modifier = Modifier.fillMaxWidth().padding(20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Text(
        text = "Подарки загружаются или коллекция пуста",
        style = MaterialTheme.typography.titleMedium,
        color = TgTextPrimary,
      )
      Text(
        text = "Откройте вкладку «Маркет», чтобы приобрести новый NFT-подарок из Telegram.",
        style = MaterialTheme.typography.bodyMedium,
        color = TgTextSecondary,
      )
    }
  }
}

@Composable
private fun NftGiftGrid(
  gifts: List<NftGift>,
  onGiftClick: (NftGift) -> Unit,
) {
  BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
    val columns = if (maxWidth >= 480.dp) 3 else 2
    val rows = gifts.chunked(columns)
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      rows.forEach { rowGifts ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          rowGifts.forEach { gift ->
            NftGiftCard(
              gift = gift,
              onClick = { onGiftClick(gift) },
              modifier = Modifier.weight(1f),
            )
          }
          repeat(columns - rowGifts.size) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}

@Composable
private fun NftGiftCard(
  gift: NftGift,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Surface(
    modifier =
      modifier
        .aspectRatio(0.92f)
        .clip(RoundedCornerShape(18.dp))
        .border(1.dp, gift.accentColor.copy(alpha = 0.35f), RoundedCornerShape(18.dp))
        .clickable(onClick = onClick)
        .testTag("gift_card_${gift.id}"),
    shape = RoundedCornerShape(18.dp),
    color = TgCard,
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      GiftPatternBackdrop(
        startColor = gift.bgStartColor,
        endColor = gift.bgEndColor,
        accentColor = gift.accentColor,
      )
      TelegramGiftImage(
        gift = gift,
        useLarge = false,
        modifier = Modifier.fillMaxSize(),
      )
      Box(
        modifier =
          Modifier.fillMaxSize()
            .background(
              Brush.verticalGradient(
                listOf(
                  Color.Transparent,
                  Color.Transparent,
                  Color.Black.copy(alpha = 0.72f),
                )
              )
            )
      )

      Box(
        modifier =
          Modifier.align(Alignment.TopEnd)
            .padding(8.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.48f))
            .padding(horizontal = 7.dp, vertical = 3.dp),
      ) {
        Text(
          text = "#${gift.number}",
          style = MaterialTheme.typography.labelSmall,
          color = Color.White,
          fontWeight = FontWeight.Bold,
        )
      }

      if (gift.isUpgraded) {
        Icon(
          imageVector = Icons.Rounded.AutoAwesome,
          contentDescription = null,
          tint = Color(0xFFFDE047),
          modifier = Modifier.align(Alignment.TopStart).padding(9.dp).size(16.dp),
        )
      }

      Column(
        modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().padding(10.dp),
      ) {
        Text(
          text = gift.name,
          style = MaterialTheme.typography.titleMedium,
          color = Color.White,
          fontSize = 13.sp,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        Text(
          text = gift.modelName,
          style = MaterialTheme.typography.labelSmall,
          color = Color.White.copy(alpha = 0.78f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun FramesGrid(
  initialLetter: String,
  selectedFrame: AvatarFrameStyle,
  onSelectFrame: (AvatarFrameStyle) -> Unit,
) {
  val rows = AvatarFrameStyle.entries.chunked(2)
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    rows.forEach { rowFrames ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        rowFrames.forEach { frame ->
          val isSelected = frame == selectedFrame
          Surface(
            modifier =
              Modifier.weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .border(
                  width = if (isSelected) 2.dp else 1.dp,
                  color = if (isSelected) frame.primaryColor else TgBorder,
                  shape = RoundedCornerShape(18.dp),
                )
                .clickable { onSelectFrame(frame) }
                .testTag("frame_card_${frame.name.lowercase()}"),
            shape = RoundedCornerShape(18.dp),
            color = TgCard,
          ) {
            Column(
              modifier = Modifier.fillMaxWidth().padding(14.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
            ) {
              Box(modifier = Modifier.fillMaxWidth()) {
                if (isSelected) {
                  Icon(
                    imageVector = Icons.Rounded.CheckCircle,
                    contentDescription = null,
                    tint = TgMint,
                    modifier = Modifier.align(Alignment.TopEnd).size(18.dp),
                  )
                }
                AnimatedAvatarWithFrame(
                  initials = initialLetter,
                  frameStyle = frame,
                  avatarSize = 54.dp,
                  modifier = Modifier.align(Alignment.Center),
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = frame.title,
                style = MaterialTheme.typography.titleMedium,
                color = TgTextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
              Text(
                text = frame.subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = TgTextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun MarketGrid(
  marketGifts: List<NftGift>,
  onBuyGift: (NftGift) -> Unit,
) {
  val rows = marketGifts.chunked(2)
  Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
    rows.forEach { rowGifts ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        rowGifts.forEach { gift ->
          Surface(
            modifier =
              Modifier.weight(1f)
                .clip(RoundedCornerShape(18.dp))
                .border(1.dp, TgBorder, RoundedCornerShape(18.dp))
                .clickable { onBuyGift(gift) }
                .testTag("market_card_${gift.id}"),
            shape = RoundedCornerShape(18.dp),
            color = TgCard,
          ) {
            Column(modifier = Modifier.fillMaxWidth()) {
              Box(modifier = Modifier.fillMaxWidth().height(126.dp)) {
                GiftPatternBackdrop(
                  startColor = gift.bgStartColor,
                  endColor = gift.bgEndColor,
                  accentColor = gift.accentColor,
                )
                TelegramGiftImage(
                  gift = gift,
                  useLarge = false,
                  modifier = Modifier.fillMaxSize(),
                )
              }
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = gift.name,
                  style = MaterialTheme.typography.titleMedium,
                  color = TgTextPrimary,
                  maxLines = 1,
                  overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                  modifier =
                    Modifier.fillMaxWidth()
                      .height(36.dp)
                      .clip(RoundedCornerShape(10.dp))
                      .background(TgBadgeBg),
                  horizontalArrangement = Arrangement.Center,
                  verticalAlignment = Alignment.CenterVertically,
                ) {
                  Icon(
                    imageVector = Icons.Rounded.ShoppingBag,
                    contentDescription = null,
                    tint = TgBadgeText,
                    modifier = Modifier.size(14.dp),
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Icon(
                    imageVector = Icons.Rounded.Diamond,
                    contentDescription = null,
                    tint = TgCyan,
                    modifier = Modifier.size(13.dp),
                  )
                  Spacer(modifier = Modifier.width(3.dp))
                  Text(
                    text = "${gift.priceCrystals}",
                    style = MaterialTheme.typography.labelMedium,
                    color = TgBadgeText,
                    fontWeight = FontWeight.Bold,
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
