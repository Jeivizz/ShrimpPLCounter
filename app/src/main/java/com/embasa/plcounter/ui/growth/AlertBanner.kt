package com.embasa.plcounter.ui.growth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.embasa.plcounter.domain.growth.AlertSeverity
import com.embasa.plcounter.domain.growth.GrowthAlert
import com.embasa.plcounter.ui.components.IAquaIcon
import com.embasa.plcounter.ui.components.IconKind
import com.embasa.plcounter.ui.theme.Mare
import com.embasa.plcounter.ui.theme.TextoSuave

@Composable
fun AlertBanner(alerts: List<GrowthAlert>, modifier: Modifier = Modifier) {
    if (alerts.isEmpty()) return
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        alerts.forEach { alert ->
            val color = severityColor(alert.severity)
            val text = alertText(alert)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(color.copy(alpha = if (alert.severity == AlertSeverity.Info) 0.10f else 0.13f))
                    .padding(16.dp),
                verticalAlignment = Alignment.Top,
            ) {
                IAquaIcon(IconKind.Info, tint = color, iconSize = 24.dp)
                Spacer(Modifier.width(12.dp))
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(severityLabel(alert.severity), style = MaterialTheme.typography.labelLarge, color = color)
                        Text(text.title, style = MaterialTheme.typography.titleSmall, color = Mare)
                    }
                    Text(text.body, style = MaterialTheme.typography.bodyMedium, color = TextoSuave)
                }
            }
        }
    }
}