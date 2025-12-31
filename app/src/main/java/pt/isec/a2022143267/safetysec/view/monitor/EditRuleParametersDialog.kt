package pt.isec.a2022143267.safetysec.view.monitor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import pt.isec.a2022143267.safetysec.R
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
        title = { Text(stringResource(R.string.edit_rule_parameters)) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when (rule.ruleType) {
                    RuleType.SPEED_CONTROL -> {
                        Text(
                            text = stringResource(R.string.speed_control_settings),
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = maxSpeed,
                            onValueChange = { maxSpeed = it },
                            label = { Text(stringResource(R.string.max_speed)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text(
                            text = stringResource(R.string.speed_exceeds_alert),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    RuleType.GEOFENCING -> {
                        Text(
                            text = stringResource(R.string.geofencing_settings),
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = radius,
                            onValueChange = { radius = it },
                            label = { Text(stringResource(R.string.radius)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text(
                            text = stringResource(R.string.left_area_alert),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Text(
                            text = stringResource(R.string.set_location_note),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    RuleType.INACTIVITY -> {
                        Text(
                            text = stringResource(R.string.inactivity_settings),
                            style = MaterialTheme.typography.titleMedium
                        )

                        OutlinedTextField(
                            value = inactivityMinutes,
                            onValueChange = { inactivityMinutes = it },
                            label = { Text(stringResource(R.string.inactivity_time)) },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Text(
                            text = stringResource(R.string.inactivity_alert),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    else -> {
                        Text(stringResource(R.string.no_parameters))
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
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

