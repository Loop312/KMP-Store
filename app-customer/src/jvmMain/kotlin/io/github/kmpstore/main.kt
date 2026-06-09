package io.github.kmpstore

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "kmpstore",
        alwaysOnTop = true
    ) {
        App()
    }
}