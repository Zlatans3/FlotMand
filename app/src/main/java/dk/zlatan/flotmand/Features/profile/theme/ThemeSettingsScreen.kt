package dk.zlatan.flotmand.Features.profile.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.topappbar.FmTopAppBar
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.design_system.theme.FlotMandTheme
import dk.zlatan.flotmand.design_system.theme.ThemeMode

data class ThemeItem(
    val mode: ThemeMode,
    val name: String,
    val description: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: ThemeViewModel = hiltViewModel(),
) {
    val themeMode = viewModel.themeMode.collectAsStateWithLifecycle()

    val themeItems = listOf(
        ThemeItem(ThemeMode.SYSTEM, stringResource(R.string.theme_system), stringResource(R.string.theme_system_description)),
        ThemeItem(ThemeMode.LIGHT, stringResource(R.string.theme_light), stringResource(R.string.theme_light_description)),
        ThemeItem(ThemeMode.DARK, stringResource(R.string.theme_dark), stringResource(R.string.theme_dark_description)),
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            FmTopAppBar(
                textContent = {
                    Text(
                        text = stringResource(R.string.theme_settings_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                },
                leadingIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = FmIcons.ArrowBack,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
            )
        },
    ) { paddingValues ->
        ThemeSettingsContent(
            modifier = Modifier
                .padding(top = paddingValues.calculateTopPadding())
                .fillMaxSize(),
            themeItems = themeItems,
            selectedMode = themeMode.value,
            onThemeSelected = { viewModel.setThemeMode(it) },
        )
    }
}

@Composable
private fun ThemeSettingsContent(
    modifier: Modifier = Modifier,
    themeItems: List<ThemeItem>,
    selectedMode: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
) {
    Column(
        modifier = modifier.background(MaterialTheme.colorScheme.background),
    ) {
        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(themeItems) { item ->
                val isSelected = item.mode == selectedMode
                val animatedColor = animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    animationSpec = tween(durationMillis = 350),
                    label = "ThemeTextColor",
                ).value
                val animatedAlpha = animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.7f,
                    animationSpec = tween(durationMillis = 350),
                    label = "ThemeAlpha",
                ).value
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onThemeSelected(item.mode) }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = animatedColor,
                        )
                        Text(
                            text = item.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = animatedAlpha),
                        )
                    }
                    RadioButton(
                        selected = isSelected,
                        onClick = { onThemeSelected(item.mode) },
                        colors = RadioButtonDefaults.colors(
                            selectedColor = MaterialTheme.colorScheme.primary,
                        ),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ThemeSettingsPreview() {
    FlotMandTheme {
        ThemeSettingsContent(
            themeItems = listOf(
                ThemeItem(ThemeMode.SYSTEM, "System", "Følger enhedens indstilling"),
                ThemeItem(ThemeMode.LIGHT, "Lys", "Altid lys tilstand"),
                ThemeItem(ThemeMode.DARK, "Mørk", "Altid mørk tilstand"),
            ),
            selectedMode = ThemeMode.SYSTEM,
            onThemeSelected = {},
        )
    }
}
