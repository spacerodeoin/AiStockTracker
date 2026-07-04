package com.aitracker.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.aitracker.app.data.remote.realtime.ConnectionState
import com.aitracker.app.ui.theme.GreenAccent
import com.aitracker.app.ui.theme.RedAccent

/**
 * Compact live-status pill showing the real-time [ConnectionState] with a colored dot. Tapping it
 * pauses/resumes the stream — the user-facing half of the two-way WebSocket demonstration.
 */
@Composable
fun LiveStatusPill(
    state: ConnectionState,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val amber = Color(0xFFF5A623)
    val grey = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
    val (dotColor, label) = when (state) {
        ConnectionState.Connecting -> amber to "Connecting…"
        ConnectionState.LiveReal -> GreenAccent to "Live"
        ConnectionState.LiveDemo -> GreenAccent to "Live · Demo"
        ConnectionState.Paused -> grey to "Paused"
        ConnectionState.Offline -> RedAccent to "Offline"
    }

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .clickable(onClick = onToggle)
            .background(dotColor.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.size(8.dp)) {
            drawCircle(color = dotColor)
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = dotColor,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
