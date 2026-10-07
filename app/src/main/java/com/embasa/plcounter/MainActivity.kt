package com.embasa.plcounter

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.embasa.plcounter.ui.BatchViewModel
import com.embasa.plcounter.ui.BatchViewModelFactory
import com.embasa.plcounter.ui.IAquaApp
import com.embasa.plcounter.ui.growth.GrowthViewModel
import com.embasa.plcounter.ui.growth.GrowthViewModelFactory
import com.embasa.plcounter.ui.theme.IAquaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BatchViewModel by viewModels { BatchViewModelFactory(application) }
    private val growthViewModel: GrowthViewModel by viewModels { GrowthViewModelFactory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
        )
        setContent {
            IAquaTheme {
                IAquaApp(viewModel, growthViewModel)
            }
        }
    }
}