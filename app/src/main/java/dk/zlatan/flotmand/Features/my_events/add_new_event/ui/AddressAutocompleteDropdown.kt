package dk.zlatan.flotmand.Features.my_events.add_new_event.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dk.zlatan.flotmand.design_system.componenets.spacers.HSpacer
import dk.zlatan.flotmand.design_system.componenets.spacers.VSpacer
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.model.AddressPrediction

/**
 * Address autocomplete dropdown showing predictions.
 */
@Composable
internal fun AddressAutocompleteDropdown(
    predictions: List<AddressPrediction>,
    isLoading: Boolean,
    onPredictionSelected: (AddressPrediction) -> Unit,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = predictions.isNotEmpty() || isLoading,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically(),
        modifier = modifier,
    ) {
        Card(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            shape = RoundedCornerShape(12.dp),
            colors =
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            elevation =
                CardDefaults.cardElevation(
                    defaultElevation = 4.dp,
                ),
        ) {
            if (isLoading) {
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp,
                    )
                }
            } else {
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 300.dp)
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 8.dp),
                ) {
                    predictions.forEach { prediction ->
                        AddressPredictionItem(
                            prediction = prediction,
                            onClick = { onPredictionSelected(prediction) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AddressPredictionItem(
    prediction: AddressPrediction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier =
            modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )

        HSpacer(12.dp)

        Column {
            Text(
                text = prediction.primaryText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (prediction.secondaryText != null) {
                VSpacer(2.dp)
                Text(
                    text = prediction.secondaryText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, name = "Autocomplete with Predictions")
@Composable
private fun AddressAutocompleteDropdownPreview() {
    FlotMandTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AddressAutocompleteDropdown(
                predictions = AddressPrediction.mockAddressPredictionList(5),
                isLoading = false,
                onPredictionSelected = { },
            )
        }
    }
}

@Preview(showBackground = true, name = "Autocomplete Loading")
@Composable
private fun AddressAutocompleteLoadingPreview() {
    FlotMandTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AddressAutocompleteDropdown(
                predictions = emptyList(),
                isLoading = true,
                onPredictionSelected = { },
            )
        }
    }
}

@Preview(showBackground = true, name = "Autocomplete Many Items")
@Composable
private fun AddressAutocompleteManyItemsPreview() {
    FlotMandTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AddressAutocompleteDropdown(
                predictions = AddressPrediction.mockAddressPredictionList(8),
                isLoading = false,
                onPredictionSelected = { },
            )
        }
    }
}

@Preview(showBackground = true, name = "Single Prediction Item")
@Composable
private fun AddressPredictionItemPreview() {
    FlotMandTheme {
        AddressPredictionItem(
            prediction = AddressPrediction.mockAddressPredictionList(1).first(),
            onClick = { },
        )
    }
}
