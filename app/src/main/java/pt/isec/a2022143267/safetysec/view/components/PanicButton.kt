package pt.isec.a2022143267.safetysec.view.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pt.isec.a2022143267.safetysec.R

@Composable
fun PanicButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showConfirmDialog by remember { mutableStateOf(false) }

    // Pulsating animation
    val infiniteTransition = rememberInfiniteTransition(label = "panic_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "panic_scale"
    )

    FloatingActionButton(
        onClick = { showConfirmDialog = true },
        modifier = modifier.scale(scale),
        containerColor = Color.Red,
        contentColor = Color.White
    ) {
        Icon(
            Icons.Default.Warning,
            contentDescription = stringResource(R.string.panic_button),
            modifier = Modifier.size(32.dp)
        )
    }

    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text(stringResource(R.string.panic_alert_e)) },
            text = { Text(stringResource(R.string.panic_alert_confirmation)) },
            confirmButton = {
                Button(
                    onClick = {
                        onClick()
                        showConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red
                    )
                ) {
                    Text(stringResource(R.string.send_alert))
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }
}

