package com.embasa.plcounter.ui.splash

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds

import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.unit.dp

import kotlinx.coroutines.delay
import androidx.compose.ui.res.painterResource
import com.embasa.plcounter.R


/** Placeholder: troque o texto pelo logo (res/drawable) e a faixa azul pela imagem das ondas. */
@Composable
fun SplashScreen(onFinished: () -> Unit) {
    LaunchedEffect(Unit) {
        delay(1500)
        onFinished()
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
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

        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()

        ) {
            Image(
                painter = painterResource(id = R.drawable.waves_cropped),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )

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