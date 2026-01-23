package dk.zlatan.flotmand.Features.profile.switchlanguage

import android.app.Activity
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.topappbar.FmTopAppBar
import dk.zlatan.flotmand.design_system.icon.FmIcons
import dk.zlatan.flotmand.util.LocaleHelper

data class LanguageItem(
    val code: String,
    val nativeName: String,
    val englishName: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwitchLanguageScreen(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
    viewModel: SwitchLanguageViewModel = hiltViewModel(),
) {
    // Fix multiline expression warning for supportedLanguages
    val supportedLanguages =
        listOf(
            LanguageItem("da", "Dansk", "Danish"),
            LanguageItem("bs", "Bosanski", "Bosnian"),
            LanguageItem("pt", "Português", "Portuguese"),
            LanguageItem("fo", "Føroyskt", "Faroese"),
        )
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    val selectedLanguage = uiState.value.selectedLanguage
    val loading = uiState.value.loading
    val context = LocalContext.current
    val activity = context as? Activity
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            FmTopAppBar(
                textContent = {
                    Text(
                        text = stringResource(R.string.language_screen_title),
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
        val toppadding = paddingValues.calculateTopPadding()
        SwitchLanguageScreenContent(
            modifier =
                Modifier
                    .padding(top = toppadding)
                    .fillMaxSize(),
            supportedLanguages = supportedLanguages,
            selectedLanguage = selectedLanguage,
            onLanguageSelected = { languageCode ->
                viewModel.onLanguageSelected(languageCode)
                LocaleHelper.setLocale(context, languageCode)
                activity?.recreate()
            },
        )
        if (loading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
    }
}

@Composable
private fun SwitchLanguageScreenContent(
    modifier: Modifier = Modifier,
    supportedLanguages: List<LanguageItem>,
    selectedLanguage: String?,
    onLanguageSelected: (String) -> Unit,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Fix multiline expression warning for modifier = Modifier
        LazyColumn(
            modifier =
                Modifier.fillMaxWidth(),
        ) {
            items(supportedLanguages) { language ->
                val isSelected = language.code == selectedLanguage
                val animatedColor = animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    animationSpec = tween(durationMillis = 350),
                    label = "LanguageTextColor"
                ).value
                val animatedAlpha = animateFloatAsState(
                    targetValue = if (isSelected) 1f else 0.7f,
                    animationSpec = tween(durationMillis = 350),
                    label = "LanguageAlpha"
                ).value
                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .clickable { onLanguageSelected(language.code) }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                    ) {
                        Text(
                            text = language.nativeName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = animatedColor,
                        )
                        Text(
                            text = language.englishName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = animatedAlpha),
                        )
                    }
                    // Fix multiline expression warning for colors = RadioButtonDefaults.colors
                    RadioButton(
                        selected = isSelected,
                        onClick = { onLanguageSelected(language.code) },
                        colors =
                            RadioButtonDefaults.colors(
                                selectedColor = MaterialTheme.colorScheme.primary,
                            ),
                        modifier = Modifier,
                        // Animate the radio button selection with a fade
                        // (Material3 RadioButton already animates selection, but we can add a fade for extra smoothness)
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SwitchLanguageScreenContentPreview() {
    // Fix multiline expression warning for supportedLanguages in preview
    val supportedLanguages =
        listOf(
            LanguageItem("da", "Dansk", "Danish"),
            LanguageItem("bs", "Bosanski", "Bosnian"),
            LanguageItem("pt", "Português", "Portuguese"),
            LanguageItem("fo", "Føroyskt", "Faroese"),
        )
    SwitchLanguageScreenContent(
        supportedLanguages = supportedLanguages,
        selectedLanguage = "da",
        onLanguageSelected = {},
    )
}
