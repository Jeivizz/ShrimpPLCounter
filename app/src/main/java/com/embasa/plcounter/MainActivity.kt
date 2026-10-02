package com.embasa.plcounter

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.embasa.plcounter.ui.BatchViewModel
import com.embasa.plcounter.ui.BatchViewModelFactory
import com.embasa.plcounter.ui.IAquaApp
import com.embasa.plcounter.ui.theme.IAquaTheme

class MainActivity : ComponentActivity() {

    private val viewModel: BatchViewModel by viewModels { BatchViewModelFactory(application) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            IAquaTheme {
                IAquaApp(viewModel)
            }
        }
    }
}