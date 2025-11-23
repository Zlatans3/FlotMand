package dk.zlatan.flotmand.Features.frontpage.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
internal fun ProfileImage(
    modifier: Modifier = Modifier,
    @DrawableRes profilePic: Int? = null,
    userNameInitials: String = "ZS",
    ) {
    if (profilePic != null) {
        Image(
            modifier = modifier.size(40.dp),
            painter = androidx.compose.ui.res.painterResource(id = profilePic),
            contentDescription = null
        )

    }  else {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.inversePrimary),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = userNameInitials,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier,
            )
        }
    }
}

@Preview
@Composable
private fun ProfileImagePreview() {
    ProfileImage(modifier = Modifier)
}