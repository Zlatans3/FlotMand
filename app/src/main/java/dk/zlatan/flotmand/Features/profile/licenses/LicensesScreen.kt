package dk.zlatan.flotmand.Features.profile.licenses

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import dk.zlatan.flotmand.R
import dk.zlatan.flotmand.design_system.componenets.topappbar.FmTopAppBar
import dk.zlatan.flotmand.design_system.icon.FmIcons

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LicensesScreen(
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            FmTopAppBar(
                textContent = {
                    Text(
                        text = stringResource(R.string.open_source_licenses_title),
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
        LibrariesContainer(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = paddingValues.calculateTopPadding()),
        )
    }
}
