package dk.zlatan.flotmand.util

import android.content.Context
import android.content.ContextWrapper
import android.os.Build
import java.util.Locale
import android.content.res.Configuration

object LocaleContextWrapper {
    fun wrap(context: Context, locale: Locale): ContextWrapper {
        var ctx = context
        val config = Configuration(ctx.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            ctx = ctx.createConfigurationContext(config)
        } else {
            config.locale = locale
            ctx.resources.updateConfiguration(config, ctx.resources.displayMetrics)
        }
        return ContextWrapper(ctx)
    }
}
