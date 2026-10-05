package com.embasa.plcounter.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.embasa.plcounter.ui.components.IAquaLogo
import com.embasa.plcounter.ui.components.WaveFill
import com.embasa.plcounter.ui.theme.AguaProfunda
import com.embasa.plcounter.ui.theme.Espuma
import com.embasa.plcounter.ui.theme.TextoSuave
import kotlinx.coroutines.delay
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.embasa.plcounter.R

private val WaveSky = Color(0xFF44A0EB)
private val WaveMid = Color(0xFF347DC2)
private val WaveDeep = Color(0xFF133A75)

/**
 * Único momento animado do app: quatro camadas de onda (as cores do seu splash) deslizando devagar.
 * Troque o texto central pelo logo vertical e o "EMBASA" pelo logo da EMBASA quando importar os SVGs.
 */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1800)
        onFinished()
    }

    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val logoAlpha by animateFloatAsState(
        targetValue = if (started) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "logo",
    )

    val transition = rememberInfiniteTransition(label = "ondas")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(durationMillis = 9000, easing = LinearEasing), RepeatMode.Restart),
        label = "deriva",
    )

    Box(modifier = Modifier.fillMaxSize().background(Espuma)) {
        val logo = painterResource(id = R.drawable.iaqua_logo_vertical)

        Image(
            painter = logo,
            contentDescription = "IAqua Logo Vertical",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.33f)
                .aspectRatio(logo.intrinsicSize.width /
                        logo.intrinsicSize.height)
        )

        Box(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(300.dp)) {
            WaveFill(WaveSky, Modifier.fillMaxSize(), amplitude = 14.dp, wavelength = 260.dp, phase = { drift })
            WaveFill(WaveMid, Modifier.fillMaxSize().padding(top = 42.dp), amplitude = 12.dp, wavelength = 220.dp, phase = { -drift * 1.3f + 1.2f })
            WaveFill(WaveDeep, Modifier.fillMaxSize().padding(top = 92.dp), amplitude = 10.dp, wavelength = 300.dp, phase = { drift + 2.4f })
            WaveFill(AguaProfunda, Modifier.fillMaxSize().padding(top = 140.dp), amplitude = 8.dp, wavelength = 240.dp, phase = { -drift * 0.8f + 3.6f })
            
            Image(
                painter = painterResource(id = R.drawable.powered_by_embasa),
                contentDescription = "powered by EMBASA",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 5.dp)
                    .fillMaxWidth(0.33f)
                    .aspectRatio(215.31f / 68.95f)
            )
        }
    }
}