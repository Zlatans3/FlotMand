package dk.zlatan.flotmand.Features.frontpage.event_detail_screen.ui

import android.content.ActivityNotFoundException
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.buttons.FmPrimaryButton
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme

@Composable
internal fun PriceCard(
    isPublisher: Boolean,
    totalPrice: Double?,
    totalPriceInput: String,
    pricePerPerson: Double?,
    onTotalPriceChanged: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isEditing by remember { mutableStateOf(false) }

    AnimatedContent(
        targetState = when {
            isPublisher && totalPrice == null && !isEditing -> PriceCardState.HostEmpty
            isPublisher && isEditing                        -> PriceCardState.HostEdit
            isPublisher                                     -> PriceCardState.HostDisplay
            totalPrice != null                              -> PriceCardState.ParticipantDisplay
            else                                            -> PriceCardState.Hidden
        },
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        modifier = modifier,
    ) { state ->
        when (state) {
            PriceCardState.HostEmpty -> HostEmptyCard(
                onClick = { isEditing = true },
            )

            PriceCardState.HostEdit -> HostEditCard(
                totalPriceInput = totalPriceInput,
                pricePerPerson = pricePerPerson,
                onTotalPriceChanged = onTotalPriceChanged,
                onSave = {
                    onSave()
                    isEditing = false
                },
                onCancel = {
                    onTotalPriceChanged(totalPrice?.formatKr(withSuffix = false) ?: "")
                    isEditing = false
                },
            )

            PriceCardState.HostDisplay -> HostDisplayCard(
                totalPrice = totalPrice,
                pricePerPerson = pricePerPerson,
                onEditClick = { isEditing = true },
            )

            PriceCardState.ParticipantDisplay -> ParticipantPriceCard(
                pricePerPerson = pricePerPerson,
            )

            PriceCardState.Hidden -> Unit
        }
    }
}

private enum class PriceCardState {
    HostEmpty,
    HostEdit,
    HostDisplay,
    ParticipantDisplay,
    Hidden,
}

@Composable
private fun HostEmptyCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    Row(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                drawRoundRect(
                    color = outlineColor,
                    size = size,
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(
                        width = 1.5.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f),
                    ),
                )
            }
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = FmIcons.Add,
            contentDescription = stringResource(R.string.price_add_content_description),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        HSpacer(8.dp)
        Text(
            text = stringResource(R.string.price_add),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun HostEditCard(
    totalPriceInput: String,
    pricePerPerson: Double?,
    onTotalPriceChanged: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val focusManager = LocalFocusManager.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
    ) {
        Text(
            text = stringResource(R.string.price_label_upper),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        VSpacer(10.dp)
        OutlinedTextField(
            value = totalPriceInput,
            onValueChange = { input ->
                if (input.matches(Regex("[0-9]*\\.?[0-9]*"))) {
                    onTotalPriceChanged(input)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(text = stringResource(R.string.price_total_input_label)) },
            placeholder = { Text(text = stringResource(R.string.price_input_placeholder)) },
            suffix = { Text(text = stringResource(R.string.price_kr_suffix)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Decimal,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() },
            ),
            shape = RoundedCornerShape(8.dp),
        )
        VSpacer(6.dp)
        // Live per-person indicator
        AnimatedContent(
            targetState = pricePerPerson,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
        ) { perPerson ->
            if (perPerson != null) {
                Text(
                    text = "${stringResource(R.string.price_becomes_prefix)} ${perPerson.formatKr()} ${stringResource(R.string.price_per_person_suffix)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            } else {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
        VSpacer(14.dp)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onCancel) {
                Text(
                    text = stringResource(R.string.price_cancel),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HSpacer(8.dp)
            FmPrimaryButton(
                text = stringResource(R.string.price_save),
                onClick = {
                    focusManager.clearFocus()
                    onSave()
                },
                isLoading = false,
                isAffirmed = false,
            )
        }
    }
}

@Composable
private fun HostDisplayCard(
    totalPrice: Double?,
    pricePerPerson: Double?,
    onEditClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .clickable(onClick = onEditClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Payments,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.price_label_upper),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (totalPrice != null) {
                Text(
                    text = "${totalPrice.formatKr()} ${stringResource(R.string.price_total_suffix)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            if (pricePerPerson != null) {
                Text(
                    text = "${pricePerPerson.formatKr()} ${stringResource(R.string.price_per_person_suffix)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Icon(
            imageVector = Icons.Rounded.Edit,
            contentDescription = stringResource(R.string.price_edit_content_description),
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

@Composable
private fun ParticipantPriceCard(
    pricePerPerson: Double?,
    modifier: Modifier = Modifier,
) {
    if (pricePerPerson == null) return
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(12.dp),
            )
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.Payments,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stringResource(R.string.price_your_share_label_upper),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = pricePerPerson.formatKr(),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        FilledIconButton(
            onClick = { launchMobilePay(context) },
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ),
        ) {
            Icon(
                imageVector = Icons.Filled.AccountBalanceWallet,
                contentDescription = stringResource(R.string.mobilepay_open_content_description),
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

private fun launchMobilePay(context: Context) {
    try {
        val intent = context.packageManager
            .getLaunchIntentForPackage(MOBILEPAY_PACKAGE)
            ?: throw ActivityNotFoundException()
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(
            context,
            context.getString(R.string.mobilepay_not_found),
            Toast.LENGTH_SHORT,
        ).show()
    }
}

private const val MOBILEPAY_PACKAGE = "dk.danskebank.mobilepay"

private fun Double.formatKr(withSuffix: Boolean = true): String {
    val amount = if (this % 1.0 == 0.0) "${this.toLong()}" else "%.2f".format(this)
    return if (withSuffix) "$amount kr." else amount
}

// ── Previews ─────────────────────────────────────────────────────────────────

@Preview(showBackground = true)
@Composable
private fun HostEmptyPreview() {
    FlotMandTheme {
        HostEmptyCard(onClick = {}, modifier = Modifier.padding(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
private fun HostEditPreview() {
    FlotMandTheme {
        HostEditCard(
            totalPriceInput = "300",
            pricePerPerson = 60.0,
            onTotalPriceChanged = {},
            onSave = {},
            onCancel = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun HostDisplayPreview() {
    FlotMandTheme {
        HostDisplayCard(
            totalPrice = 300.0,
            pricePerPerson = 60.0,
            onEditClick = {},
            modifier = Modifier.padding(16.dp),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ParticipantPreview() {
    FlotMandTheme {
        ParticipantPriceCard(pricePerPerson = 60.0, modifier = Modifier.padding(16.dp))
    }
}
