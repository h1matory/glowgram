package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChatAccent
import com.example.model.ChatBackdrop
import com.example.model.ChatPattern
import com.example.ui.theme.TgBorder
import com.example.ui.theme.TgCard
import com.example.ui.theme.TgCardElevated
import com.example.ui.theme.TgSurface
import com.example.ui.theme.TgTextPrimary
import com.example.ui.theme.TgTextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatAppearanceSheet(
  backdrop: ChatBackdrop,
  pattern: ChatPattern,
  accent: ChatAccent,
  onSelect: (ChatBackdrop, ChatPattern, ChatAccent) -> Unit,
  onDismiss: () -> Unit,
) {
  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = TgSurface,
    contentColor = TgTextPrimary,
  ) {
    Column(
      modifier =
        Modifier.fillMaxWidth()
          .padding(horizontal = 18.dp)
          .navigationBarsPadding()
          .padding(bottom = 22.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
      Text(
        text = "Оформление чата",
        style = MaterialTheme.typography.titleLarge,
        color = TgTextPrimary,
        fontWeight = FontWeight.Bold,
      )

      // Live Mini Chat Preview (like 00:35 in the video!)
      Box(
        modifier =
          Modifier.fillMaxWidth()
            .height(118.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, TgBorder, RoundedCornerShape(16.dp)),
      ) {
        ChatWallpaperCanvas(backdrop = backdrop, pattern = pattern, accent = accent)
        Column(
          modifier = Modifier.fillMaxSize().padding(12.dp),
          verticalArrangement = Arrangement.SpaceEvenly,
        ) {
          Box(
            modifier =
              Modifier.align(Alignment.Start)
                .clip(RoundedCornerShape(12.dp))
                .background(TgCardElevated)
                .padding(horizontal = 12.dp, vertical = 7.dp),
          ) {
            Text(text = "Как тебе новые обои?", style = MaterialTheme.typography.bodyMedium, color = TgTextPrimary)
          }
          Box(
            modifier =
              Modifier.align(Alignment.End)
                .clip(RoundedCornerShape(12.dp))
                .background(accent.bubbleBg)
                .padding(horizontal = 12.dp, vertical = 7.dp),
          ) {
            Text(text = "Смотрится аккуратно ✓✓", style = MaterialTheme.typography.bodyMedium, color = Color.White)
          }
        }
      }

      // Accent Colors
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Цвет акцента", style = MaterialTheme.typography.labelMedium, color = TgTextSecondary)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
        ) {
          ChatAccent.entries.forEach { item ->
            val selected = item == accent
            Box(
              modifier =
                Modifier.size(44.dp)
                  .clip(CircleShape)
                  .background(item.color)
                  .border(
                    width = if (selected) 3.dp else 0.dp,
                    color = if (selected) Color.White else Color.Transparent,
                    shape = CircleShape,
                  )
                  .clickable { onSelect(backdrop, pattern, item) }
                  .testTag("accent_${item.name.lowercase()}"),
              contentAlignment = Alignment.Center,
            ) {
              if (selected) {
                Icon(
                  imageVector = Icons.Rounded.Check,
                  contentDescription = item.title,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp),
                )
              }
            }
          }
        }
      }

      // Backdrops
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Обои чата", style = MaterialTheme.typography.labelMedium, color = TgTextSecondary)
        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          ChatBackdrop.entries.forEach { item ->
            val selected = item == backdrop
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier =
                Modifier.width(72.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { onSelect(item, pattern, accent) }
                  .padding(vertical = 4.dp)
                  .testTag("backdrop_${item.name.lowercase()}"),
            ) {
              Box(
                modifier =
                  Modifier.size(56.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.color)
                    .border(
                      width = if (selected) 2.dp else 1.dp,
                      color = if (selected) accent.color else TgBorder,
                      shape = RoundedCornerShape(12.dp),
                    ),
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = item.title,
                fontSize = 11.sp,
                color = if (selected) TgTextPrimary else TgTextSecondary,
                maxLines = 1,
              )
            }
          }
        }
      }

      // Patterns
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(text = "Узоры", style = MaterialTheme.typography.labelMedium, color = TgTextSecondary)
        Row(
          modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          ChatPattern.entries.forEach { item ->
            val selected = item == pattern
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier =
                Modifier.width(74.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { onSelect(backdrop, item, accent) }
                  .padding(vertical = 4.dp)
                  .testTag("pattern_${item.name.lowercase()}"),
            ) {
              Box(
                modifier =
                  Modifier.size(58.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .border(
                      width = if (selected) 2.dp else 1.dp,
                      color = if (selected) accent.color else TgBorder,
                      shape = RoundedCornerShape(12.dp),
                    ),
              ) {
                ChatWallpaperCanvas(backdrop = TgCard.let { backdrop }, pattern = item, accent = accent)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = item.title,
                fontSize = 11.sp,
                color = if (selected) TgTextPrimary else TgTextSecondary,
                maxLines = 1,
              )
            }
          }
        }
      }
    }
  }
}
