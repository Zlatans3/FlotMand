package dk.zlatan.flotmand.util

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dk.zlatan.flotmand.R

/*
 * This class is more like a joke to provide better type safety and readability
 * than just returning strings from the formatCreatedDateString function.
 */
sealed class CreatedDateLabel {
    object Today : CreatedDateLabel()
    object Yesterday : CreatedDateLabel()
    data class DaysAgo(val days: Int) : CreatedDateLabel()
    data class DateString(val date: String) : CreatedDateLabel()
    object Unknown : CreatedDateLabel()
}

internal fun formatCreatedDateString(dateString: String?): CreatedDateLabel {
    return if (dateString != null) {
        try {
            val date = java.time.LocalDateTime.parse(dateString)
            val now = java.time.LocalDateTime.now()
            val days = java.time.temporal.ChronoUnit.DAYS.between(
                date.toLocalDate(),
                now.toLocalDate(),
            ).toInt()
            when {
                days == 0 -> CreatedDateLabel.Today
                days == 1 -> CreatedDateLabel.Yesterday
                days < 7 -> CreatedDateLabel.DaysAgo(days)
                else -> CreatedDateLabel.DateString(date.toLocalDate().toString())
            }
        } catch (_: Exception) {
            CreatedDateLabel.Unknown
        }
    } else {
        CreatedDateLabel.Unknown
    }
}

@Composable
internal fun CreatedDateLabelText(label: CreatedDateLabel) {
    when (label) {
        is CreatedDateLabel.Today -> Text(
            text = stringResource(R.string.today),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is CreatedDateLabel.Yesterday -> Text(
            text = stringResource(R.string.yesterday),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is CreatedDateLabel.DaysAgo -> Text(
            text = stringResource(R.string.days_ago, label.days),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is CreatedDateLabel.DateString -> Text(
            text = label.date,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        is CreatedDateLabel.Unknown -> Text(
            text = stringResource(R.string.unknown_date),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}