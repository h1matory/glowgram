package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.model.ChatAccent
import com.example.model.ChatBackdrop
import com.example.model.ChatPattern
import kotlin.math.sin

@Composable
fun ChatWallpaperCanvas(
  backdrop: ChatBackdrop,
  pattern: ChatPattern,
  accent: ChatAccent,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier.fillMaxSize()) {
    drawRect(color = backdrop.color)

    val tint = accent.color.copy(alpha = 0.14f)
    val step = 36.dp.toPx()

    when (pattern) {
      ChatPattern.NONE -> Unit
      ChatPattern.DOTS -> {
        var y = step * 0.5f
        var row = 0
        while (y < size.height) {
          var x = if (row % 2 == 0) step * 0.5f else step
          while (x < size.width) {
            drawCircle(color = tint, radius = 2.2.dp.toPx(), center = Offset(x, y))
            x += step
          }
          y += step
          row++
        }
      }
      ChatPattern.GRID -> {
        val stroke = 1.dp.toPx()
        var x = 0f
        while (x < size.width) {
          drawLine(color = tint, start = Offset(x, 0f), end = Offset(x, size.height), strokeWidth = stroke)
          x += step
        }
        var y = 0f
        while (y < size.height) {
          drawLine(color = tint, start = Offset(0f, y), end = Offset(size.width, y), strokeWidth = stroke)
          y += step
        }
      }
      ChatPattern.STROKES -> {
        val stroke = 1.6.dp.toPx()
        val len = 10.dp.toPx()
        var y = step * 0.5f
        while (y < size.height) {
          var x = step * 0.5f
          while (x < size.width) {
            drawLine(
              color = tint,
              start = Offset(x - len * 0.5f, y + len * 0.5f),
              end = Offset(x + len * 0.5f, y - len * 0.5f),
              strokeWidth = stroke,
              cap = StrokeCap.Round,
            )
            x += step
          }
          y += step
        }
      }
      ChatPattern.WAVES -> {
        val stroke = 1.4.dp.toPx()
        var y = step * 0.6f
        while (y < size.height) {
          val path = Path()
          var x = 0f
          path.moveTo(0f, y)
          while (x <= size.width) {
            val dy = sin(x / 24.dp.toPx()) * 6.dp.toPx()
            path.lineTo(x, y + dy)
            x += 8.dp.toPx()
          }
          drawPath(path, color = tint, style = Stroke(width = stroke, cap = StrokeCap.Round))
          y += step * 0.9f
        }
      }
      ChatPattern.PLUSES -> {
        val arm = 4.dp.toPx()
        val stroke = 1.6.dp.toPx()
        var y = step * 0.5f
        while (y < size.height) {
          var x = step * 0.5f
          while (x < size.width) {
            drawLine(
              color = tint,
              start = Offset(x - arm, y),
              end = Offset(x + arm, y),
              strokeWidth = stroke,
              cap = StrokeCap.Round,
            )
            drawLine(
              color = tint,
              start = Offset(x, y - arm),
              end = Offset(x, y + arm),
              strokeWidth = stroke,
              cap = StrokeCap.Round,
            )
            x += step
          }
          y += step
        }
      }
    }
  }
}
