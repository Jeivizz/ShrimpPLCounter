package com.embasa.plcounter.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.embasa.plcounter.ui.theme.IAquaBlue
import com.embasa.plcounter.ui.theme.IAquaChip
import com.embasa.plcounter.ui.theme.IAquaTipCard
import com.embasa.plcounter.ui.theme.IAquaTitleBlue


@Composable
fun IAquaHeader(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp) {
        Row(
            modifier = Modifier
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "IAqua",
                color = IAquaBlue,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
            )
        }
    }
}

@Composable
fun TipsCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = IAquaTipCard),
        border = BorderStroke(1.dp, IAquaChip),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "\uD83D\uDCA1 DICAS PARA MELHOR PRECISÃO",
                style = MaterialTheme.typography.titleSmall,
                color = IAquaTitleBlue,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TipColumn(
                    title = "ILUMINAÇÃO E ÂNGULO",
                    tips = listOf(
                        "1. Use boa iluminação, evitando sombras sobre a amostra",
                        "2. Ângulo de 90° para evitar distorção",
                    ),
                    modifier = Modifier.weight(1f),
                )
                TipColumn(
                    title = "MÉTODO DE AMOSTRA",
                    tips = listOf(
                        "1. Pequena quantidade para melhor leitura (Ex: 100 ml)",
                        "2. Recipiente de fundo branco",
                    ),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun TipColumn(title: String, tips: List<String>, modifier: Modifier = Modifier) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = IAquaTitleBlue,
            fontWeight = FontWeight.Bold,
        )
        tips.forEach { tip ->
            Text(
                text = tip,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}