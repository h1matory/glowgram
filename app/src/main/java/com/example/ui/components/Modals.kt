package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AlternateEmail
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.model.CollectibleUsername
import com.example.model.NftGift
import com.example.ui.theme.TgBadgeBg
import com.example.ui.theme.TgBadgeText
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgBlueBright
import com.example.ui.theme.TgBorder
import com.example.ui.theme.TgCard
import com.example.ui.theme.TgCardElevated
import com.example.ui.theme.TgCyan
import com.example.ui.theme.TgDivider
import com.example.ui.theme.TgGold
import com.example.ui.theme.TgPink
import com.example.ui.theme.TgPurple
import com.example.ui.theme.TgTextPrimary
import com.example.ui.theme.TgTextSecondary
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GiftDetailModal(
  gift: NftGift,
  confettiTriggerKey: Int,
  onUpgradeClick: () -> Unit,
  onDismiss: () -> Unit,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "gift_modal_float")
  val floatOffset by
    infiniteTransition.animateFloat(
      initialValue = -6f,
      targetValue = 6f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(1600, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "gift_float",
    )

  val confettiProgress = remember { Animatable(0f) }
  LaunchedEffect(confettiTriggerKey) {
    if (confettiTriggerKey > 0) {
      confettiProgress.snapTo(0f)
      confettiProgress.animateTo(1f, animationSpec = tween(900))
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 20.dp)
          .clip(RoundedCornerShape(22.dp))
          .border(1.dp, TgBorder, RoundedCornerShape(22.dp))
          .testTag("gift_detail_modal"),
      color = TgCard,
    ) {
      Column(modifier = Modifier.fillMaxWidth()) {
        // Patterned Colored Hero Header with Official Telegram Gift Artwork
        Box(modifier = Modifier.fillMaxWidth().height(236.dp)) {
          GiftPatternBackdrop(
            startColor = gift.bgStartColor,
            endColor = gift.bgEndColor,
            accentColor = gift.accentColor,
          )
          TelegramGiftImage(
            gift = gift,
            useLarge = true,
            modifier = Modifier.fillMaxSize().graphicsLayer { alpha = 0.92f },
          )
          // Subtle bottom gradient scrim so title & subtitle remain super crisp
          Box(
            modifier =
              Modifier.fillMaxSize()
                .background(
                  Brush.verticalGradient(
                    listOf(
                      Color.Transparent,
                      Color.Transparent,
                      Color.Black.copy(alpha = 0.68f),
                    )
                  )
                )
          )

          // Confetti celebration layer on upgrade
          if (confettiProgress.value in 0.01f..0.99f) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val t = confettiProgress.value
              val cx = size.width * 0.5f
              val cy = size.height * 0.45f
              val colors = listOf(TgCyan, TgPink, TgGold, TgPurple, Color.White)
              for (i in 0 until 24) {
                val angle = Math.toRadians(i * 15.0)
                val dist = (40f + (i % 5) * 28f) * t
                val px = cx + (cos(angle) * dist).toFloat()
                val py = cy + (sin(angle) * dist).toFloat() + (t * t * 35f)
                drawCircle(
                  color = colors[i % colors.size].copy(alpha = 1f - t),
                  radius = 5.dp.toPx(),
                  center = Offset(px, py),
                )
              }
            }
          }

          // Top Close Button
          IconButton(
            onClick = onDismiss,
            modifier =
              Modifier.align(Alignment.TopStart)
                .padding(8.dp)
                .size(48.dp)
                .testTag("gift_modal_close_button"),
          ) {
            Box(
              modifier = Modifier.size(32.dp).clip(CircleShape).background(Color.Black.copy(alpha = 0.38f)),
              contentAlignment = Alignment.Center,
            ) {
              Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = stringResource(R.string.close_cd),
                tint = Color.White,
                modifier = Modifier.size(18.dp),
              )
            }
          }

          // Bottom Title Overlay
          Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
          ) {
            Text(
              text = gift.fullTitle,
              style = MaterialTheme.typography.titleLarge,
              color = Color.White,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.graphicsLayer { translationY = floatOffset * 0.25f },
            )
            Text(
              text = gift.modelName,
              style = MaterialTheme.typography.bodyMedium,
              color = Color.White.copy(alpha = 0.85f),
            )
          }
        }

        // Specs Table (matching video's clean Telegram attribute list)
        Column(
          modifier =
            Modifier.fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 14.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(TgCardElevated)
              .border(1.dp, TgBorder, RoundedCornerShape(14.dp)),
        ) {
          SpecOwnerRow(owner = gift.owner)
          HorizontalDivider(color = TgDivider, thickness = 1.dp)
          SpecRarityRow(
            label = stringResource(R.string.model_label),
            value = gift.modelName,
            rarity = gift.modelRarityPercent,
          )
          HorizontalDivider(color = TgDivider, thickness = 1.dp)
          SpecRarityRow(
            label = stringResource(R.string.background_label),
            value = gift.backgroundName,
            rarity = gift.backgroundRarityPercent,
          )
          HorizontalDivider(color = TgDivider, thickness = 1.dp)
          SpecRarityRow(
            label = stringResource(R.string.pattern_label),
            value = gift.patternName,
            rarity = gift.patternRarityPercent,
          )
          HorizontalDivider(color = TgDivider, thickness = 1.dp)
          SpecPlainRow(label = stringResource(R.string.edition_label), value = gift.editionText)
          HorizontalDivider(color = TgDivider, thickness = 1.dp)
          SpecValueRow(priceRub = gift.priceRub, priceCrystals = gift.priceCrystals)
        }

        // Actions
        Row(
          modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          OutlinedButton(
            onClick = onUpgradeClick,
            modifier = Modifier.weight(1f).height(48.dp).testTag("gift_upgrade_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TgCyan),
          ) {
            Icon(
              imageVector = Icons.Rounded.AutoAwesome,
              contentDescription = null,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text =
                if (gift.isUpgraded) stringResource(R.string.upgraded_label)
                else stringResource(R.string.upgrade_gift_action),
              maxLines = 1,
              fontSize = 13.sp,
            )
          }

          Button(
            onClick = onDismiss,
            modifier = Modifier.weight(0.75f).height(48.dp).testTag("gift_ok_button"),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TgBlue, contentColor = Color.White),
          ) {
            Text(text = stringResource(R.string.ok_button), fontWeight = FontWeight.SemiBold)
          }
        }
      }
    }
  }
}

@Composable
private fun SpecOwnerRow(owner: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = stringResource(R.string.owner_label),
      style = MaterialTheme.typography.bodyMedium,
      color = TgTextSecondary,
      modifier = Modifier.width(96.dp),
    )
    Box(
      modifier =
        Modifier.size(22.dp)
          .clip(CircleShape)
          .background(Brush.linearGradient(listOf(TgCyan, TgPurple))),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = owner.take(1).uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = Color.White,
      )
    }
    Spacer(modifier = Modifier.width(8.dp))
    Text(
      text = owner,
      style = MaterialTheme.typography.bodyMedium,
      fontWeight = FontWeight.SemiBold,
      color = TgBadgeText,
    )
  }
}

@Composable
private fun SpecRarityRow(label: String, value: String, rarity: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = TgTextSecondary,
      modifier = Modifier.width(96.dp),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = TgTextPrimary,
      fontWeight = FontWeight.Medium,
    )
    Spacer(modifier = Modifier.width(8.dp))
    Box(
      modifier =
        Modifier.clip(RoundedCornerShape(6.dp))
          .background(TgBadgeBg)
          .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
      Text(
        text = rarity,
        style = MaterialTheme.typography.labelSmall,
        color = TgBadgeText,
        fontWeight = FontWeight.SemiBold,
      )
    }
  }
}

@Composable
private fun SpecPlainRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodyMedium,
      color = TgTextSecondary,
      modifier = Modifier.width(96.dp),
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodyMedium,
      color = TgTextPrimary,
    )
  }
}

@Composable
private fun SpecValueRow(priceRub: Int, priceCrystals: Int) {
  Row(
    modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Text(
      text = stringResource(R.string.value_label),
      style = MaterialTheme.typography.bodyMedium,
      color = TgTextSecondary,
      modifier = Modifier.width(96.dp),
    )
    Text(
      text = "~$priceRub ₽",
      style = MaterialTheme.typography.bodyMedium,
      color = TgTextPrimary,
      fontWeight = FontWeight.SemiBold,
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
      text = "$priceCrystals Кристаллов",
      style = MaterialTheme.typography.bodyMedium,
      color = TgTextSecondary,
    )
  }
}

@Composable
fun CollectibleUsernameModal(
  collectible: CollectibleUsername,
  onCopyLink: () -> Unit,
  onDismiss: () -> Unit,
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false),
  ) {
    Surface(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 24.dp)
          .clip(RoundedCornerShape(22.dp))
          .border(1.dp, TgBorder, RoundedCornerShape(22.dp))
          .testTag("username_modal"),
      color = TgCard,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
      ) {
        Box(modifier = Modifier.fillMaxWidth()) {
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.TopStart).size(48.dp).testTag("username_modal_close"),
          ) {
            Icon(
              imageVector = Icons.Rounded.Close,
              contentDescription = stringResource(R.string.close_cd),
              tint = TgTextSecondary,
            )
          }
          Box(
            modifier =
              Modifier.align(Alignment.Center)
                .size(72.dp)
                .clip(CircleShape)
                .background(TgBlue),
            contentAlignment = Alignment.Center,
          ) {
            Icon(
              imageVector = Icons.Rounded.AlternateEmail,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(38.dp),
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Text(
          text = "${collectible.username} — коллекционный юзернейм, который принадлежит ${collectible.owner}",
          style = MaterialTheme.typography.titleMedium,
          color = TgTextPrimary,
          textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text =
            "Юзернейм ${collectible.username} был приобретён на exest ${collectible.acquiredDate} за ${collectible.priceCrystals} кристаллов (~${collectible.priceRub} ₽)",
          style = MaterialTheme.typography.bodyMedium,
          color = TgTextSecondary,
          textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(18.dp))
        Button(
          onClick = onDismiss,
          modifier = Modifier.fillMaxWidth().height(48.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TgBlue, contentColor = Color.White),
        ) {
          Text(text = stringResource(R.string.collectible_username_more), fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
          modifier =
            Modifier.fillMaxWidth()
              .height(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .clickable(onClick = onCopyLink)
              .testTag("copy_username_button"),
          contentAlignment = Alignment.Center,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Rounded.ContentCopy,
              contentDescription = null,
              tint = TgBlueBright,
              modifier = Modifier.size(16.dp),
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = stringResource(R.string.collectible_username_copy),
              color = TgBlueBright,
              fontWeight = FontWeight.Medium,
            )
          }
        }
      }
    }
  }
}
