package dk.zlatan.flotmand.design_system.componenets

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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun ProfileImage(
    modifier: Modifier = Modifier,
    @DrawableRes profilePic: Int? = null,
    profileSize: Dp = 40.dp,
    userName: String = "Zlatan Stadler",
    ) {
    if (profilePic != null) {
        Image(
            modifier = modifier.size(profileSize),
            painter = painterResource(id = profilePic),
            contentDescription = null
        )

    }  else {
        Box(
            modifier = Modifier
                .size(profileSize)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.inversePrimary),
            contentAlignment = Alignment.Center
        ){
            Text(
                text = getInitials(userName),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.tertiary,
                fontSize = (profileSize.value / 2).sp
            )
        }
    }
}

@Preview
@Composable
private fun ProfileImagePreview() {
    ProfileImage(modifier = Modifier)
}

// Helper function to extract initials from userName
private fun getInitials(userName: String): String {
    val parts = userName.trim().split(" ").filter { it.isNotEmpty() }
    return when {
        parts.size >= 2 -> (parts[0].firstOrNull()?.toString() ?: "") + (parts[1].firstOrNull()?.toString() ?: "")
        parts.size == 1 && parts[0].length >= 2 -> parts[0].substring(0, 2)
        parts.size == 1 && parts[0].isNotEmpty() -> parts[0].substring(0, 1)
        else -> "--"
    }.uppercase()
}
