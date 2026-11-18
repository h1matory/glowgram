package com.example.ui.components

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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AvatarFrameStyle
import com.example.model.NftGift
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun AnimatedAvatarWithFrame(
  initials: String,
  frameStyle: AvatarFrameStyle,
  avatarSize: Dp = 104.dp,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "avatar_frame")
  val rotation by
    infiniteTransition.animateFloat(
      initialValue = 0f,
      targetValue = 360f,
      animationSpec = infiniteRepeatable(animation = tween(3600, easing = LinearEasing)),
      label = "frame_rotation",
    )
  val pulse by
    infiniteTransition.animateFloat(
      initialValue = 0.72f,
      targetValue = 1.0f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(1400, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "frame_pulse",
    )

  Box(modifier = modifier.size(avatarSize + 24.dp), contentAlignment = Alignment.Center) {
    // Animated neon glow + rotating ring frame
    Canvas(modifier = Modifier.fillMaxSize()) {
      val radius = size.minDimension * 0.44f
      val center = Offset(size.width * 0.5f, size.height * 0.5f)

      // Ambient outer neon aura
      drawCircle(
        brush =
          Brush.radialGradient(
            colors =
              listOf(
                frameStyle.primaryColor.copy(alpha = 0.42f * pulse),
                frameStyle.secondaryColor.copy(alpha = 0.16f),
                Color.Transparent,
              ),
            center = center,
            radius = size.minDimension * 0.52f,
          ),
        radius = size.minDimension * 0.52f,
        center = center,
      )

      // Rotating gradient neon ring
      rotate(degrees = rotation, pivot = center) {
        drawCircle(
          brush =
            Brush.sweepGradient(
              colors =
                listOf(
                  frameStyle.primaryColor,
                  frameStyle.secondaryColor,
                  Color.White.copy(alpha = 0.9f),
                  frameStyle.primaryColor,
                ),
              center = center,
            ),
          radius = radius,
          center = center,
          style = Stroke(width = 4.2.dp.toPx(), cap = StrokeCap.Round),
        )
      }

      // Frame-specific accents (spikes, petals, crystals, sparks)
      val orbitCount =
        when (frameStyle) {
          AvatarFrameStyle.SAKURA_BLOOM -> 6
          AvatarFrameStyle.ICE_CUBE -> 8
          AvatarFrameStyle.GOLD_LAUREL -> 10
          else -> 4
        }
      for (i in 0 until orbitCount) {
        val angleRad = Math.toRadians((rotation * 0.8 + i * (360.0 / orbitCount)))
        val ox = center.x + (cos(angleRad) * (radius + 3.dp.toPx())).toFloat()
        val oy = center.y + (sin(angleRad) * (radius + 3.dp.toPx())).toFloat()
        val dotColor = if (i % 2 == 0) frameStyle.primaryColor else frameStyle.secondaryColor
        drawCircle(
          color = dotColor.copy(alpha = 0.9f * pulse),
          radius = if (i % 2 == 0) 3.4.dp.toPx() else 2.2.dp.toPx(),
          center = Offset(ox, oy),
        )
      }
    }

    // Inner stylized avatar artwork
    Box(
      modifier =
        Modifier.size(avatarSize)
          .clip(CircleShape)
          .background(
            Brush.linearGradient(
              listOf(
                Color(0xFF1F2937),
                frameStyle.primaryColor.copy(alpha = 0.35f),
                Color(0xFF0F172A),
              )
            )
          )
          .border(2.dp, Color(0xFF111317), CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      // Stylized character/avatar silhouette inside
      Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        drawCircle(
          brush =
            Brush.radialGradient(
              listOf(frameStyle.secondaryColor.copy(alpha = 0.4f), Color.Transparent),
              center = Offset(w * 0.35f, h * 0.3f),
              radius = w * 0.65f,
            )
        )
      }
      Text(
        text = initials,
        style = MaterialTheme.typography.headlineMedium,
        fontSize = (avatarSize.value * 0.32f).sp,
        fontWeight = FontWeight.ExtraBold,
        color = Color.White,
      )
    }
  }
}

@Composable
fun AvatarWithOrbitingGifts(
  initials: String,
  frameStyle: AvatarFrameStyle,
  gifts: List<NftGift>,
  onGiftClick: (NftGift) -> Unit,
  modifier: Modifier = Modifier,
) {
  val infiniteTransition = rememberInfiniteTransition(label = "orbiting_gifts")
  val bobbing by
    infiniteTransition.animateFloat(
      initialValue = -4f,
      targetValue = 4f,
      animationSpec =
        infiniteRepeatable(
          animation = tween(1800, easing = LinearEasing),
          repeatMode = RepeatMode.Reverse,
        ),
      label = "gift_bobbing",
    )

  val orbitGifts = gifts.take(6)
  // Angles around the top & sides of the avatar (matching ExteraGram video layout)
  val anglesDeg = listOf(-158f, -124f, -90f, -56f, -22f, 18f)

  Box(modifier = modifier.size(width = 300.dp, height = 188.dp), contentAlignment = Alignment.BottomCenter) {
    AnimatedAvatarWithFrame(
      initials = initials,
      frameStyle = frameStyle,
      avatarSize = 104.dp,
      modifier = Modifier.padding(bottom = 8.dp),
    )

    orbitGifts.forEachIndexed { idx, gift ->
      val angle = Math.toRadians(anglesDeg.getOrElse(idx) { -90f }.toDouble())
      val rx = 114f
      val ry = 82f
      val xDp = (cos(angle) * rx).toFloat()
      val yDp = (sin(angle) * ry).toFloat() + (if (idx % 2 == 0) bobbing else -bobbing)

      Box(
        modifier =
          Modifier.align(Alignment.Center)
            .offset { IntOffset(xDp.dp.roundToPx(), (yDp + 18f).dp.roundToPx()) }
            .size(48.dp) // 48dp minimum interactive touch target
            .clip(CircleShape)
            .testTag("orbit_gift_${gift.id}")
            .semantics { contentDescription = "Открыть подарок ${gift.fullTitle}" }
            .clickable { onGiftClick(gift) },
        contentAlignment = Alignment.Center,
      ) {
        Box(
          modifier =
            Modifier.size(38.dp)
              .clip(CircleShape)
              .background(Brush.radialGradient(listOf(gift.bgStartColor, gift.bgEndColor)))
              .border(1.5.dp, gift.accentColor.copy(alpha = 0.8f), CircleShape),
          contentAlignment = Alignment.Center,
        ) {
          TelegramGiftImage(
            gift = gift,
            useLarge = false,
            modifier = Modifier.fillMaxSize().clip(CircleShape),
          )
        }
      }
    }
  }
}
