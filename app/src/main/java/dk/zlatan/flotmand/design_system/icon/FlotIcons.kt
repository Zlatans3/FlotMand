package dk.zlatan.flotmand.design_system.icon

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.AccessibleForward
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ArrowRight
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.ShortText
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Bookmarks
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Upcoming
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DinnerDining
import androidx.compose.material.icons.rounded.Grid3x3
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.HowToVote
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PinDrop
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Upcoming
import androidx.compose.material.icons.rounded.ViewDay
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Now in Android icons. Material icons are [ImageVector]s, custom icons are drawable resource IDs.
 */
object FmIcons {
    val Add = Icons.Rounded.Add
    val ArrowBack = Icons.AutoMirrored.Rounded.ArrowBack
    val Bookmark = Icons.Rounded.Bookmark
    val BookmarkBorder = Icons.Rounded.BookmarkBorder
    val Bookmarks = Icons.Rounded.Bookmarks
    val BookmarksBorder = Icons.Outlined.Bookmarks
    val Check = Icons.Rounded.Check
    val info = Icons.Rounded.Info
    val Close = Icons.Rounded.Close
    val Grid3x3 = Icons.Rounded.Grid3x3
    val MoreVert = Icons.Default.MoreVert
    val Search = Icons.Rounded.Search
    val globe = Icons.Rounded.Public
    val Settings = Icons.Rounded.Settings
    val ShortText = Icons.AutoMirrored.Rounded.ShortText
    val Upcoming = Icons.Rounded.Upcoming
    val UpcomingBorder = Icons.Outlined.Upcoming
    val ViewDay = Icons.Rounded.ViewDay


    val logout = Icons.AutoMirrored.Rounded.Logout

    val mapPin = Icons.Rounded.PinDrop
    val chevronRight = Icons.AutoMirrored.Rounded.KeyboardArrowRight

        // top level
    val Home = Icons.Rounded.Home
    val homeBorder = Icons.Outlined.Home

    val Calendar = Icons.Rounded.CalendarMonth
    val CalendarBorder = Icons.Outlined.CalendarMonth

    val Person = Icons.Rounded.Person
    val PersonBorder = Icons.Outlined.Person

    // Notifications
    val Bell = Icons.Rounded.Notifications
    val BellOutline = Icons.Outlined.NotificationsNone

    // Notification type icons
    val NotificationEvent = Icons.Rounded.DinnerDining
    val NotificationPoll = Icons.Rounded.HowToVote
}