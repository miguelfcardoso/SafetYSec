package pt.isec.a2022143267.safetysec.view.monitor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.GeoPoint
import pt.isec.a2022143267.safetysec.R
import pt.isec.a2022143267.safetysec.model.GeofenceArea
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

    // Multiple geofencing areas support
    var geofenceAreas by remember {
        mutableStateOf(
            if (rule.parameters.geoPoints.isNotEmpty()) {
                rule.parameters.geoPoints.toList()
            } else {
                emptyList<GeofenceArea>()
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_rule_parameters)) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    when (rule.ruleType) {
                        RuleType.SPEED_CONTROL -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        }

                        RuleType.GEOFENCING -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Multiple Geofencing Areas",
                                    style = MaterialTheme.typography.titleMedium
                                )

                                Text(
                                    text = "Define multiple safe zones. Alert triggers when outside ALL areas.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                OutlinedTextField(
                                    value = radius,
                                    onValueChange = { radius = it },
                                    label = { Text("Default Radius (meters)") },
                                    modifier = Modifier.fillMaxWidth(),
                                    singleLine = true
                                )
                            }
                        }

                        RuleType.INACTIVITY -> {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        }

                        else -> {
                            Text(stringResource(R.string.no_parameters))
                        }
                    }
                }

                // Geofencing areas list
                if (rule.ruleType == RuleType.GEOFENCING) {
                    item {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Defined Areas (${geofenceAreas.size})",
                                style = MaterialTheme.typography.titleSmall
                            )
                            IconButton(
                                onClick = {
                                    geofenceAreas = geofenceAreas + GeofenceArea(
                                        center = GeoPoint(0.0, 0.0),
                                        radius = radius.toDoubleOrNull() ?: 100.0,
                                        name = "Area ${geofenceAreas.size + 1}"
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Add, "Add Area")
                            }
                        }
                    }

                    items(geofenceAreas.size) { index ->
                        val area = geofenceAreas[index]
                        GeofenceAreaItem(
                            area = area,
                            onDelete = {
                                geofenceAreas = geofenceAreas.filterIndexed { i, _ -> i != index }
                            },
                            onUpdate = { updatedArea ->
                                geofenceAreas = geofenceAreas.mapIndexed { i, item ->
                                    if (i == index) updatedArea else item
                                }
                            }
                        )
                    }

                    if (geofenceAreas.isEmpty()) {
                        item {
                            Text(
                                "No areas defined. Add at least one area.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val newParameters = when (rule.ruleType) {
                        RuleType.SPEED_CONTROL -> {
                            rule.parameters.copy(
                                maxSpeed = maxSpeed.toDoubleOrNull() ?: rule.parameters.maxSpeed
                            )
                        }
                        RuleType.GEOFENCING -> {
                            rule.parameters.copy(
                                radius = radius.toDoubleOrNull() ?: rule.parameters.radius,
                                geoPoints = geofenceAreas
                            )
                        }
                        RuleType.INACTIVITY -> {
                            rule.parameters.copy(
                                inactivityMinutes = inactivityMinutes.toIntOrNull() ?: rule.parameters.inactivityMinutes
                            )
                        }
                        else -> rule.parameters
                    }
                    onSave(newParameters)
                },
                enabled = if (rule.ruleType == RuleType.GEOFENCING) {
                    geofenceAreas.isNotEmpty()
                } else {
                    true
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

@Composable
fun GeofenceAreaItem(
    area: GeofenceArea,
    onDelete: () -> Unit,
    onUpdate: (GeofenceArea) -> Unit
) {
    var name by remember { mutableStateOf(area.name) }
    var lat by remember { mutableStateOf(area.center.latitude.toString()) }
    var lon by remember { mutableStateOf(area.center.longitude.toString()) }
    var areaRadius by remember { mutableStateOf(area.radius.toString()) }
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    name.ifEmpty { "Unnamed Area" },
                    style = MaterialTheme.typography.titleSmall
                )
                Row {
                    TextButton(onClick = { expanded = !expanded }) {
                        Text(if (expanded) "Collapse" else "Expand")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, "Delete", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        onUpdate(area.copy(name = it))
                    },
                    label = { Text("Area Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = lat,
                        onValueChange = {
                            lat = it
                            it.toDoubleOrNull()?.let { newLat ->
                                onUpdate(area.copy(center = GeoPoint(newLat, area.center.longitude)))
                            }
                        },
                        label = { Text("Latitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = lon,
                        onValueChange = {
                            lon = it
                            it.toDoubleOrNull()?.let { newLon ->
                                onUpdate(area.copy(center = GeoPoint(area.center.latitude, newLon)))
                            }
                        },
                        label = { Text("Longitude") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = areaRadius,
                    onValueChange = {
                        areaRadius = it
                        it.toDoubleOrNull()?.let { newRadius ->
                            onUpdate(area.copy(radius = newRadius))
                        }
                    },
                    label = { Text("Radius (meters)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        }
    }
}

