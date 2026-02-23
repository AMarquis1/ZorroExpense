package com.marquis.zorroexpense

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Store application context for services that need it
        appContext = this

        setContent {
            App()
        }
    }

    companion object {
        var appContext: android.content.Context? = null
    }
}

@Preview
@Composable
fun appAndroidPreview() {
    App()
}
