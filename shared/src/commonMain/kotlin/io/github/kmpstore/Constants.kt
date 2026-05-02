package io.github.kmpstore

import androidx.compose.ui.unit.dp

const val SERVER_PORT = 8080
val STORE_NAME = BuildKonfig.STORE_NAME

const val DATABASE_NAME = "test.db"

val IMAGE_LOADING_ERROR = { name: String?, url: String?, error: String? ->
    "AN ERROR OCCURRED LOADING IMAGE\n" +
            "IMAGE NAME: $name\n" +
            "IMAGE URL: $url\n" +
            "ERROR: $error"
}

val pad = 16.dp