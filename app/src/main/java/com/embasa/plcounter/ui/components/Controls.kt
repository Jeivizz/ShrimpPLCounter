package com.embasa.plcounter.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.embasa.plcounter.ui.theme.Alvorada
import com.embasa.plcounter.ui.theme.AlvoradaTint
import com.embasa.plcounter.ui.theme.Cobalto
import com.embasa.plcounter.ui.theme.Linha
import com.embasa.plcounter.ui.theme.Manrope
import com.embasa.plcounter.ui.theme.Mare
import com.embasa.plcounter.ui.theme.TextoSuave

// ---------- Barra superior: só o logo, sem caixa ----------

@Composable
fun AppTopBar(
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        IAquaLogo()
        trailing?.invoke()
    }
}

// ---------- Botões: formato de pílula, uma ação principal por tela ----------

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: IconKind? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(58.dp),
        enabled = enabled,
        shape = RoundedCornerShape(29.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Cobalto,
            contentColor = Color.White,
            disabledContainerColor = Color(0xFFC3CFE3),
            disabledContentColor = Color.White,
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp,
            disabledElevation = 0.dp,
        ),
    ) {
        if (icon != null) {
            IAquaIcon(icon, iconSize = 22.dp)
            Spacer(Modifier.width(10.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold))
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: IconKind? = null,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        enabled = enabled,
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.5.dp, if (enabled) Cobalto.copy(alpha = 0.35f) else Linha),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = Cobalto,
            disabledContentColor = TextoSuave.copy(alpha = 0.5f),
        ),
    ) {
        if (icon != null) {
            IAquaIcon(icon, iconSize = 20.dp)
            Spacer(Modifier.width(8.dp))
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

// ---------- Campos numéricos ----------

/** Campo numérico com unidade fixa à direita. [big] = leitura grande (volumes do lote). */
@Composable
fun NumberField(
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    modifier: Modifier = Modifier,
    big: Boolean = false,
    leading: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        textStyle = TextStyle(
            fontFamily = Manrope,
            fontWeight = FontWeight.ExtraBold,
            fontSize = if (big) 28.sp else 18.sp,
            color = Mare,
        ),
        leadingIcon = leading,
        suffix = {
            Text(
                text = unit,
                style = if (big) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                color = TextoSuave,
            )
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(if (big) 20.dp else 16.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = Cobalto,
            unfocusedBorderColor = Linha,
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            cursorColor = Cobalto,
        ),
    )
}

/** Campo grande com rótulo acima (sem rótulo flutuante: o rótulo fica sempre legível). */
@Composable
fun ReadoutField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String,
    icon: IconKind,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleSmall, color = Mare)
        NumberField(
            value = value,
            onValueChange = onValueChange,
            unit = unit,
            modifier = Modifier.fillMaxWidth(),
            big = true,
            leading = { IAquaIcon(icon, tint = Cobalto, iconSize = 24.dp) },
        )
    }
}

// ---------- Seção que abre e fecha ----------

/**
 * Linha que se expande ao toque. A abertura anima altura e transparência juntas e a seta gira
 * na mesma curva; a rotação é lida só ao desenhar, para não recompor o conteúdo a cada quadro.
 */
@Composable
fun Disclosure(
    title: String,
    modifier: Modifier = Modifier,
    summary: String? = null,
    startExpanded: Boolean = false,
    container: Color = Color.White,
    borderColor: Color? = Linha,
    leading: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    var expanded by rememberSaveable { mutableStateOf(startExpanded) }
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 180f else 0f,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "seta",
    )
    val shape = RoundedCornerShape(20.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(container)
            .then(if (borderColor != null) Modifier.border(1.dp, borderColor, shape) else Modifier)
            .clickable { expanded = !expanded }
            .padding(horizontal = 18.dp, vertical = 16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (leading != null) {
                leading()
                Spacer(Modifier.width(12.dp))
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleSmall, color = Mare)
                if (summary != null && !expanded) {
                    Text(summary, style = MaterialTheme.typography.bodySmall, color = TextoSuave)
                }
            }
            IAquaIcon(
                kind = IconKind.Chevron,
                tint = TextoSuave,
                iconSize = 22.dp,
                modifier = Modifier.graphicsLayer { rotationZ = rotation },
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                    fadeIn(animationSpec = tween(200, delayMillis = 50)),
            exit = shrinkVertically(animationSpec = tween(250, easing = FastOutSlowInEasing)) +
                    fadeOut(animationSpec = tween(150)),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                content = content,
            )
        }
    }
}

/** Dicas de captura: tom quente de alvorada, para se distinguir de dados e resultados. */
@Composable
fun CaptureTips(modifier: Modifier = Modifier, startExpanded: Boolean = false) {
    Disclosure(
        title = "Dicas para uma boa foto",
        modifier = modifier,
        summary = "Luz, ângulo e fundo",
        startExpanded = startExpanded,
        container = AlvoradaTint,
        borderColor = null,
        leading = { IAquaIcon(IconKind.Bulb, tint = Alvorada, iconSize = 24.dp) },
    ) {
        TipLine("Boa iluminação, sem sombra sobre a amostra.")
        TipLine("Câmera a 90° em relação ao recipiente, para não distorcer.")
        TipLine("Pouca quantidade por foto lê melhor (ex.: 100 ml).")
        TipLine("Recipiente de fundo branco.")
    }
}

@Composable
private fun TipLine(text: String) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .padding(top = 7.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(Alvorada),
        )
        Spacer(Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Mare)
    }
}

// ---------- Situação (cor + texto, nunca só cor) ----------

@Composable
fun StatusPill(
    text: String,
    dotColor: Color,
    modifier: Modifier = Modifier,
    onDark: Boolean = false,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(if (onDark) Color.White else dotColor.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(dotColor))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold), color = Mare)
    }
}

// ---------- Etapas (aqui a numeração é real: são 3 amostras em sequência) ----------

@Composable
fun StepTrack(
    total: Int,
    current: Int,
    done: List<Boolean>,
    modifier: Modifier = Modifier,
) {
    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        repeat(total) { i ->
            val isDone = done.getOrElse(i) { false }
            val isCurrent = i == current

            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isDone -> Cobalto
                            isCurrent -> Color.White
                            else -> Color(0xFFE6ECF5)
                        },
                    )
                    .then(if (isCurrent && !isDone) Modifier.border(2.dp, Cobalto, CircleShape) else Modifier),
                contentAlignment = Alignment.Center,
            ) {
                if (isDone) {
                    IAquaIcon(IconKind.Check, tint = Color.White, iconSize = 18.dp)
                } else {
                    Text(
                        text = "${i + 1}",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isCurrent) Cobalto else TextoSuave,
                    )
                }
            }

            if (i < total - 1) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (isDone) Cobalto else Color(0xFFE0E7F1)),
                )
            }
        }
    }
}

/** Botão redondo translúcido, para usar sobre fotos e câmera. */
@Composable
fun RoundIconButton(
    icon: IconKind,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        IAquaIcon(icon, tint = Color.White, iconSize = 22.dp)
    }
}