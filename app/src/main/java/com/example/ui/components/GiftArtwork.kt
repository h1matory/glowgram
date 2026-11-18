package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CardGiftcard
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.example.model.NftGift

@Composable
fun GiftPatternBackdrop(
  startColor: Color,
  endColor: Color,
  accentColor: Color,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier.fillMaxSize()) {
    drawRect(
      brush =
        Brush.radialGradient(
          colors = listOf(startColor, endColor),
          center = Offset(size.width * 0.5f, size.height * 0.42f),
          radius = size.maxDimension * 0.85f,
        )
    )
    val cols = 5
    val rows = 4
    val stepX = size.width / cols
    val stepY = size.height / rows
    for (r in 0 until rows) {
      for (c in 0 until cols) {
        val cx = stepX * (c + 0.5f) + if (r % 2 == 1) stepX * 0.2f else 0f
        val cy = stepY * (r + 0.5f)
        val rDot = size.minDimension * 0.025f
        if ((r + c) % 2 == 0) {
          drawCircle(color = Color.White.copy(alpha = 0.08f), radius = rDot, center = Offset(cx, cy))
        } else {
          val diamond =
            Path().apply {
              moveTo(cx, cy - rDot * 1.6f)
              lineTo(cx + rDot * 1.4f, cy)
              lineTo(cx, cy + rDot * 1.6f)
              lineTo(cx - rDot * 1.4f, cy)
              close()
            }
          drawPath(diamond, color = accentColor.copy(alpha = 0.14f))
        }
      }
    }
  }
}

@Composable
fun TelegramGiftImage(
  gift: NftGift,
  useLarge: Boolean = false,
  contentScale: ContentScale = ContentScale.Crop,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val targetUrl = if (useLarge) gift.largeImageUrl else gift.imageUrl

  SubcomposeAsyncImage(
    model =
      ImageRequest.Builder(context)
        .data(targetUrl)
        .crossfade(true)
        .build(),
    contentDescription = gift.fullTitle,
    contentScale = contentScale,
    modifier = modifier,
    loading = {
      Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(
          modifier = Modifier.size(20.dp),
          color = gift.accentColor,
          strokeWidth = 2.dp,
        )
      }
    },
    error = {
      // Fallback to medium preview or clean icon if offline
      SubcomposeAsyncImage(
        model = ImageRequest.Builder(context).data(gift.stickerWebpUrl).crossfade(true).build(),
        contentDescription = gift.fullTitle,
        contentScale = contentScale,
        modifier = Modifier.fillMaxSize(),
        error = {
          Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Rounded.CardGiftcard,
              contentDescription = gift.fullTitle,
              tint = Color.White.copy(alpha = 0.85f),
              modifier = Modifier.size(32.dp),
            )
          }
        },
      )
    },
  )
}
