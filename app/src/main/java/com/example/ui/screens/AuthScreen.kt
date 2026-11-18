package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Login
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.auth.attemptAutoSignIn
import com.example.auth.onGoogleSignInClicked
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
fun AuthScreen(
  onAuthSuccess: () -> Unit = {},
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val credentialManager = remember(context) { CredentialManager.create(context) }

  var isCheckingSilentAuth by remember { mutableStateOf(true) }
  var isSigningIn by remember { mutableStateOf(false) }
  var errorMessage by remember { mutableStateOf<String?>(null) }

  LaunchedEffect(Unit) {
    attemptAutoSignIn(
      context = context,
      credentialManager = credentialManager,
      onAuthSuccess = {
        isCheckingSilentAuth = false
        onAuthSuccess()
      },
      onUnauthenticated = {
        isCheckingSilentAuth = false
      },
      scope = scope,
    )
  }

  Box(
    modifier =
      Modifier.fillMaxSize()
        .background(
          Brush.verticalGradient(
            listOf(Color(0xFF0E1420), TgBgDeep, Color(0xFF0C0F14))
          )
        )
        .windowInsetsPadding(WindowInsets.safeDrawing)
        .padding(20.dp),
    contentAlignment = Alignment.Center,
  ) {
    Surface(
      modifier =
        Modifier.fillMaxWidth()
          .widthIn(max = 460.dp)
          .border(1.dp, TgBorder, RoundedCornerShape(26.dp)),
      shape = RoundedCornerShape(26.dp),
      color = TgSurface,
    ) {
      Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(14.dp),
      ) {
        AnimatedAvatarWithFrame(
          initials = "E",
          frameStyle = AvatarFrameStyle.NEON_PULSE,
          avatarSize = 96.dp,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "ExteraNFT",
          style = MaterialTheme.typography.headlineMedium,
          color = TgTextPrimary,
          fontWeight = FontWeight.ExtraBold,
        )

        Text(
          text = "Telegram-чаты, кастомные обои с узорами и коллекция реальных NFT-подарков из маркета.",
          style = MaterialTheme.typography.bodyMedium,
          color = TgTextSecondary,
          textAlign = TextAlign.Center,
        )

        Column(
          modifier =
            Modifier.fillMaxWidth()
              .clip(RoundedCornerShape(16.dp))
              .background(TgCard)
              .border(1.dp, TgBorder, RoundedCornerShape(16.dp))
              .padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
          FeatureBullet("Личные и новые чаты с синхронизацией в облаке")
          FeatureBullet("Оформление чата: тёмные обои, узоры и акценты")
          FeatureBullet("Анимированные неоновые рамки и NFT-подарки")
        }

        if (errorMessage != null) {
          Text(
            text = errorMessage ?: "",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFF87171),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
          )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Button(
          onClick = {
            if (isSigningIn) return@Button
            errorMessage = null
            isSigningIn = true
            onGoogleSignInClicked(
              context = context,
              credentialManager = credentialManager,
              onAuthSuccess = {
                isSigningIn = false
                onAuthSuccess()
              },
              onAuthError = { msg ->
                isSigningIn = false
                errorMessage = msg
              },
              onAuthCancelled = {
                isSigningIn = false
              },
              scope = scope,
            )
          },
          enabled = !isCheckingSilentAuth && !isSigningIn,
          modifier =
            Modifier.fillMaxWidth()
              .height(52.dp)
              .testTag("google_sign_in_button"),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TgBlue, contentColor = Color.White),
        ) {
          if (isCheckingSilentAuth || isSigningIn) {
            CircularProgressIndicator(
              modifier = Modifier.size(20.dp),
              color = Color.White,
              strokeWidth = 2.dp,
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = if (isCheckingSilentAuth) "Проверка входа..." else "Входим...",
              fontWeight = FontWeight.SemiBold,
            )
          } else {
            Icon(
              imageVector = Icons.AutoMirrored.Rounded.Login,
              contentDescription = null,
              modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
              text = "Войти через Google",
              fontWeight = FontWeight.Bold,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun FeatureBullet(text: String) {
  androidx.compose.foundation.layout.Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
    Icon(
      imageVector = Icons.Rounded.AutoAwesome,
      contentDescription = null,
      tint = TgBlueBright,
      modifier = Modifier.size(16.dp),
    )
    Text(
      text = text,
      style = MaterialTheme.typography.bodyMedium,
      color = TgTextPrimary,
    )
  }
}
