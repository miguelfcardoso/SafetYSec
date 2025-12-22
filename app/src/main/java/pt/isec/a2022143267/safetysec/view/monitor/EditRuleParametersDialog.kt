package pt.isec.a2022143267.safetysec.view.monitor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pt.isec.a2022143267.safetysec.model.Rule
import pt.isec.a2022143267.safetysec.model.RuleParameters
import pt.isec.a2022143267.safetysec.model.RuleType

@Composable
fun EditRuleParametersDialog(
    rule: Rule,
    onDismiss: () -> Unit,
    onSave: (RuleParameters) -> Unit
) {
    var maxSpeed by remember { mutableStateOf(rule.parameters.maxSpeed.toString()) }
    var radius by remember { mutableStateOf(rule.parameters.radius.toString()) }
    var inactivityMinutes by remember { mutableStateOf(rule.parameters.inactivityMinutes.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Rule Parameters") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (rule.ruleType) {
                    RuleType.SPEED_CONTROL -> {
                        Text(
                            text = "Speed Control Settings",
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = maxSpeed,
                            onValueChange = { maxSpeed = it },
                            label = { Text("Max Speed (km/h)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text(
                            text = "Alert will trigger if speed exceeds this value",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    RuleType.GEOFENCING -> {
                        Text(
                            text = "Geofencing Settings",
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = radius,
                            onValueChange = { radius = it },
                            label = { Text("Radius (meters)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text(
                            text = "Alert will trigger if user leaves this area",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = "Note: Location must be set on the protected user's device",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    RuleType.INACTIVITY -> {
                        Text(
                            text = "Inactivity Settings",
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = inactivityMinutes,
                            onValueChange = { inactivityMinutes = it },
                            label = { Text("Inactivity Duration (minutes)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text(
                            text = "Alert will trigger after this period of no movement",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    else -> {
                        Text("This rule type has no configurable parameters")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newParameters = when (rule.ruleType) {
                        RuleType.SPEED_CONTROL -> {
                            RuleParameters(
                                maxSpeed = maxSpeed.toDoubleOrNull() ?: rule.parameters.maxSpeed,
                                radius = rule.parameters.radius,
                                inactivityMinutes = rule.parameters.inactivityMinutes,
                                geoPoint = rule.parameters.geoPoint
                            )
                        }
                        RuleType.GEOFENCING -> {
                            RuleParameters(
                                maxSpeed = rule.parameters.maxSpeed,
                                radius = radius.toDoubleOrNull() ?: rule.parameters.radius,
                                inactivityMinutes = rule.parameters.inactivityMinutes,
                                geoPoint = rule.parameters.geoPoint
                            )
                        }
                        RuleType.INACTIVITY -> {
                            RuleParameters(
                                maxSpeed = rule.parameters.maxSpeed,
                                radius = rule.parameters.radius,
                                inactivityMinutes = inactivityMinutes.toIntOrNull() ?: rule.parameters.inactivityMinutes,
                                geoPoint = rule.parameters.geoPoint
                            )
                        }
                        else -> rule.parameters
                    }
                    onSave(newParameters)
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

