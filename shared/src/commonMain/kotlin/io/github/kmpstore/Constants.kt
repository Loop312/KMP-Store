package io.github.kmpstore

const val SERVER_PORT = 8080
val STORE_NAME = BuildKonfig.STORE_NAME

val IMAGE_LOADING_ERROR = { name: String?, url: String?, error: String? ->
    "AN ERROR OCCURRED LOADING IMAGE\n" +
            "IMAGE NAME: $name\n" +
            "IMAGE URL: $url\n" +
            "ERROR: $error"
}