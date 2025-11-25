package dk.zlatan.flotmand.util

import dk.zlatan.flotmand.util.NetworkErrorMessageList.messages

object NetworkErrorMessageList {
    val messages = listOf(
        "Bro tænd dit net",
        "Bro er i Sahara uden wifi",
        "Flot mand serveren er nede",
        "Flot mand, men grim forbindelse",
        "middag uden netværk",
        "netus er nedus",

    )
}

fun getRandomOfflineMessage(): String =
    messages.random()
