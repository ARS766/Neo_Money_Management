package com.example

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.ui.screens.MainAppContainer
import com.example.ui.theme.MyApplicationTheme

class MainActivity : FragmentActivity() {
  companion object {
    var isMultiWindowModeRealtime by mutableStateOf(false)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Capture initial state
    isMultiWindowModeRealtime = isInMultiWindowMode

    window.setFlags(
      WindowManager.LayoutParams.FLAG_SECURE,
      WindowManager.LayoutParams.FLAG_SECURE
    )

    // Listen to Window Insets immediately to detect split-screen layout transitions in real time
    window.decorView.setOnApplyWindowInsetsListener { view, insets ->
      isMultiWindowModeRealtime = isInMultiWindowMode
      view.onApplyWindowInsets(insets)
    }

    enableEdgeToEdge()
    setContent {
      MainAppContainer()
    }
  }

  override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
    super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
    isMultiWindowModeRealtime = isInMultiWindowMode
  }

  override fun onConfigurationChanged(newConfig: Configuration) {
    super.onConfigurationChanged(newConfig)
    isMultiWindowModeRealtime = isInMultiWindowMode
  }

  override fun onStart() {
    super.onStart()
    isMultiWindowModeRealtime = isInMultiWindowMode
  }

  override fun onResume() {
    super.onResume()
    isMultiWindowModeRealtime = isInMultiWindowMode
  }

  override fun onPause() {
    super.onPause()
    isMultiWindowModeRealtime = isInMultiWindowMode
  }
}

