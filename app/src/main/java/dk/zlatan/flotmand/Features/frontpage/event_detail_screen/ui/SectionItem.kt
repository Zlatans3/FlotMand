package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import androidx.compose.animation.SizeTransform

@Composable
fun SectionItem(
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector,
    trailingIcon: ImageVector? = null,
    title: String,
    onClick: () -> Unit = { },
    onLongClick: () -> Unit = { },
    iconTint: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = {
                    // Example: Open calendar app (placeholder, implement actual logic as needed)
                    onLongClick()
                }
            )
            .padding(vertical = 16.dp, horizontal = 20.dp)
        ,
        verticalAlignment = Alignment.CenterVertically

    ) {
        Icon(
            imageVector = leadingIcon,
            contentDescription = null,
            tint = iconTint
        )
        HSpacer(15.dp)

        // Split the title into prefix, number, and suffix. Only animate the number.
        val numberRegex = Regex("(\\d+)")
        val match = numberRegex.find(title)
        if (match != null) {
            val prefix = title.substring(0, match.range.first)
            val number = match.value
            val suffix = title.substring(match.range.last + 1)

            Text(
                text = prefix,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
            )
            AnimatedContent(
                targetState = number,
                transitionSpec = {
                    ((slideInVertically { fullHeight -> fullHeight / 2 } + fadeIn()) togetherWith
                    (slideOutVertically { fullHeight -> fullHeight / 2 } + fadeOut()))
                        .using(SizeTransform(clip = false))
                }
            ) { animatedNumber ->
                Text(
                    text = animatedNumber,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            Text(
                text = suffix,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge,
            )
        } else {
            // Fallback: animate whole title if no digits are present
            AnimatedContent(
                targetState = title,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { animatedTitle ->
                Text(
                    text = animatedTitle,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (trailingIcon != null){
            Icon(
                imageVector = trailingIcon,
                contentDescription = null,
                tint = iconTint
            )
        }
    }
}