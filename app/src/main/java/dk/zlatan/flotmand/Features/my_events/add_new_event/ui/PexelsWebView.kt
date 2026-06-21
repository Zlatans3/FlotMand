package dk.zlatan.flotmand.Features.my_events.add_new_event.ui

import android.content.Context
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import java.net.URLEncoder

fun openPexelsSearch(context: Context, query: String) {
    val encoded = URLEncoder.encode(query.trim().ifBlank { "food" }, "UTF-8")
    CustomTabsIntent.Builder().build()
        .launchUrl(context, "https://www.pexels.com/search/$encoded".toUri())
}
