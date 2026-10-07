package com.embasa.plcounter.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Linha
import com.embasa.plcounter.ui.theme.TextoSuave

enum class AppTab { Count, Growth }

@Composable
fun AppBottomBar(selected: AppTab, onSelect: (AppTab) -> Unit, modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth().background(Color.White)) {
        HorizontalDivider(color = Linha)
        Row(
            modifier = Modifier.fillMaxWidth().navigationBarsPadding().height(66.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TabItem("Contagem", IconKind.Drop, selected == AppTab.Count, { onSelect(AppTab.Count) }, Modifier.weight(1f))
            TabItem("Crescimento", IconKind.Chart, selected == AppTab.Growth, { onSelect(AppTab.Growth) }, Modifier.weight(1f))
        }
    }
}

@Composable
private fun TabItem(label: String, icon: IconKind, active: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val color = if (active) Cobalto else TextoSuave
    Column(
        modifier = modifier.clickable(onClick = onClick).padding(vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(width = 58.dp, height = 30.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(if (active) MaterialTheme.colorScheme.primaryContainer else Color.Transparent),
            contentAlignment = Alignment.Center,
        ) {
            IAquaIcon(icon, tint = color, iconSize = 22.dp)
        }
        Text(label, style = MaterialTheme.typography.labelMedium, color = color)
    }
}