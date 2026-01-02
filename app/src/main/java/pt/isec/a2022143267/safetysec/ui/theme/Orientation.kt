package pt.isec.a2022143267.safetysec.ui.theme

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Utility to detect device orientation
 */
@Composable
fun isLandscape(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
}

@Composable
fun isPortrait(): Boolean {
    val configuration = LocalConfiguration.current
    return configuration.orientation == Configuration.ORIENTATION_PORTRAIT
}

/**
 * Get screen width in dp
 */
@Composable
fun getScreenWidthDp(): Int {
    return LocalConfiguration.current.screenWidthDp
}

/**
 * Get screen height in dp
 */
@Composable
fun getScreenHeightDp(): Int {
    return LocalConfiguration.current.screenHeightDp
}

