package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.model.AvatarFrameStyle
import com.example.ui.components.AnimatedAvatarWithFrame
import com.example.ui.theme.TgBgDeep
import com.example.ui.theme.TgBlue
import com.example.ui.theme.TgBlueBright
import com.example.ui.theme.TgBorder
import com.example.ui.theme.TgCard
import com.example.ui.theme.TgSurface
import com.example.ui.theme.TgTextPrimary
import com.example.ui.theme.TgTextSecondary

@Composable
fun ProfileSetupScreen(
  initialGoogleName: String?,
  isSubmitting: Boolean,
  onSubmit: (
    displayName: String,
    username: String,
    bio: String,
    birthday: String,
    frame: AvatarFrameStyle,
  ) -> Unit,
  onSignOut: () -> Unit,
) {
  val suggestedName = initialGoogleName?.trim()?.takeIf { it.isNotEmpty() } ?: "Benzo"
  val suggestedHandle =
    "@" +
      suggestedName
        .lowercase()
        .replace(Regex("[^a-z0-9_]"), "")
        .ifEmpty { "benzo" }

  var displayName by remember { mutableStateOf(suggestedName) }
  var username by remember { mutableStateOf(suggestedHandle) }
  var bio by remember { mutableStateOf("Молодой Миллионер") }
  var birthday by remember { mutableStateOf("15 июня 2004 г.") }
  var selectedFrame by remember { mutableStateOf(AvatarFrameStyle.NEON_PULSE) }

  val fieldColors =
    OutlinedTextFieldDefaults.colors(
      focusedBorderColor = TgBlueBright,
      unfocusedBorderColor = TgBorder,
      focusedTextColor = TgTextPrimary,
      unfocusedTextColor = TgTextPrimary,
      focusedLabelColor = TgBlueBright,
      unfocusedLabelColor = TgTextSecondary,
      cursorColor = TgBlueBright,
      focusedContainerColor = TgCard,
      unfocusedContainerColor = TgCard,
    )

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(TgBgDeep)
        .windowInsetsPadding(WindowInsets.safeDrawing),
    contentAlignment = Alignment.TopCenter,
  ) {
    Column(
      modifier =
        Modifier.fillMaxSize()
          .widthIn(max = 560.dp)
          .verticalScroll(rememberScrollState())
          .padding(horizontal = 18.dp, vertical = 20.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
      Text(
        text = "Регистрация профиля",
        style = MaterialTheme.typography.headlineMedium,
        color = TgTextPrimary,
        fontWeight = FontWeight.Bold,
      )
      Text(
        text = "Настрой имя, юзернейм и стартовую неоновую рамку — сразу откроются чаты и коллекция NFT.",
        style = MaterialTheme.typography.bodyMedium,
        color = TgTextSecondary,
      )

      Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, TgBorder, RoundedCornerShape(22.dp)),
        shape = RoundedCornerShape(22.dp),
        color = TgSurface,
      ) {
        Column(
          modifier = Modifier.fillMaxWidth().padding(18.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
          AnimatedAvatarWithFrame(
            initials = displayName.trim().take(1).ifEmpty { "B" }.uppercase(),
            frameStyle = selectedFrame,
            avatarSize = 90.dp,
          )

          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
          ) {
            AvatarFrameStyle.entries.forEach { frame ->
              val isSelected = frame == selectedFrame
              Box(
                modifier =
                  Modifier.clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) frame.primaryColor.copy(alpha = 0.22f) else TgCard)
                    .border(
                      width = if (isSelected) 1.5.dp else 1.dp,
                      color = if (isSelected) frame.primaryColor else TgBorder,
                      shape = RoundedCornerShape(12.dp),
                    )
                    .clickable { selectedFrame = frame }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
              ) {
                Text(
                  text = frame.title,
                  style = MaterialTheme.typography.labelMedium,
                  color = if (isSelected) Color.White else TgTextSecondary,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                )
              }
            }
          }

          OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Имя в профиле") },
            singleLine = true,
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("reg_name_input"),
          )

          OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Юзернейм (например @benzo)") },
            singleLine = true,
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("reg_username_input"),
          )

          OutlinedTextField(
            value = bio,
            onValueChange = { bio = it },
            label = { Text("О себе") },
            singleLine = true,
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("reg_bio_input"),
          )

          OutlinedTextField(
            value = birthday,
            onValueChange = { birthday = it },
            label = { Text("День рождения") },
            singleLine = true,
            colors = fieldColors,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth().testTag("reg_birthday_input"),
          )
        }
      }

      Button(
        onClick = {
          if (!isSubmitting) {
            onSubmit(displayName, username, bio, birthday, selectedFrame)
          }
        },
        enabled = !isSubmitting && displayName.isNotBlank(),
        modifier =
          Modifier.fillMaxWidth()
            .height(52.dp)
            .testTag("create_account_button"),
        shape = RoundedCornerShape(14.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TgBlue, contentColor = Color.White),
      ) {
        if (isSubmitting) {
          CircularProgressIndicator(
            modifier = Modifier.size(20.dp),
            color = Color.White,
            strokeWidth = 2.dp,
          )
          Spacer(modifier = Modifier.width(10.dp))
          Text("Создаём аккаунт...", fontWeight = FontWeight.SemiBold)
        } else {
          Text("Создать аккаунт и начать", fontWeight = FontWeight.Bold)
        }
      }

      TextButton(
        onClick = onSignOut,
        modifier = Modifier.height(48.dp).testTag("reg_sign_out_button"),
      ) {
        Text(
          text = "Сменить Google-аккаунт",
          color = TgTextSecondary,
        )
      }
    }
  }
}
