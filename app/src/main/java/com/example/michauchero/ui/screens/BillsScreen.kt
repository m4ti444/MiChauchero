package com.example.michauchero.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.michauchero.data.BillEntity
import com.example.michauchero.viewmodel.BillsViewModel
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun BillsScreen(viewModel: BillsViewModel, padding: PaddingValues) {
    val bills by viewModel.upcoming.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    Scaffold(
        modifier = Modifier.padding(padding),
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog.value = true }) { androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = "Agregar") }
        }
    ) { inner ->
        if (bills.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(inner).fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("No tienes pagos programados", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { showDialog.value = true }) { Text("Agregar recordatorio") }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(inner).padding(16.dp)) {
                items(bills) { bill ->
                    BillRow(bill, onMarkPaid = { viewModel.markPaid(bill.id) }, onDelete = { viewModel.delete(bill) })
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
        if (showDialog.value) {
            AddBillDialog(onDismiss = { showDialog.value = false }, onSave = { title, amount, dueStr, recurrence ->
                val dueDate = runCatching { LocalDate.parse(dueStr) }.getOrElse { LocalDate.now() }
                val epoch = dueDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                viewModel.add(context, title, amount, epoch, recurrence)
                showDialog.value = false
            })
        }
    }
}

@Composable
fun BillRow(bill: BillEntity, onMarkPaid: () -> Unit, onDelete: () -> Unit) {
    androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(modifier = Modifier.padding(12.dp), horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween) {
            androidx.compose.foundation.layout.Column {
                Text(bill.title, style = MaterialTheme.typography.titleMedium)
                Text("Vence: " + java.time.Instant.ofEpochMilli(bill.dueEpoch).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toString(), style = MaterialTheme.typography.bodySmall)
            }
            androidx.compose.foundation.layout.Row {
                TextButton(onClick = onMarkPaid) { Text("Pagado") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onDelete) { Text("Eliminar") }
            }
        }
    }
}

@Composable
fun AddBillDialog(onDismiss: () -> Unit, onSave: (String, Double, String, String) -> Unit) {
    val title = remember { mutableStateOf("") }
    val amountStr = remember { mutableStateOf("") }
    val dueStr = remember { mutableStateOf(LocalDate.now().toString()) }
    val recurrence = remember { mutableStateOf("NONE") }
    val titleError = remember { mutableStateOf<String?>(null) }
    val amountError = remember { mutableStateOf<String?>(null) }
    val dateError = remember { mutableStateOf<String?>(null) }
    val recurrenceError = remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val isValid = titleError.value == null && amountError.value == null && dateError.value == null && recurrenceError.value == null &&
                    title.value.isNotBlank() && amountStr.value.toDoubleOrNull() != null
            TextButton(
                enabled = isValid,
                onClick = {
                    val amount = amountStr.value.toDoubleOrNull() ?: return@TextButton
                    onSave(title.value, amount, dueStr.value, recurrence.value)
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Agregar pago/recordatorio") },
        text = {
            androidx.compose.foundation.layout.Column {
                OutlinedTextField(
                    value = title.value,
                    onValueChange = {
                        title.value = it
                        titleError.value = if (it.isBlank()) "Ingresa un título" else null
                    },
                    isError = titleError.value != null,
                    label = { Text("Título") },
                    supportingText = { titleError.value?.let { Text(it) } }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = amountStr.value,
                    onValueChange = {
                        amountStr.value = it
                        val v = it.toDoubleOrNull()
                        amountError.value = when {
                            it.isBlank() -> "Ingresa un monto"
                            v == null -> "El monto debe ser numérico"
                            v <= 0.0 -> "El monto debe ser mayor a 0"
                            else -> null
                        }
                    },
                    isError = amountError.value != null,
                    label = { Text("Monto") },
                    supportingText = { amountError.value?.let { Text(it) } }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dueStr.value,
                    onValueChange = {
                        dueStr.value = it
                        dateError.value = try {
                            LocalDate.parse(it)
                            null
                        } catch (_: Exception) {
                            "Fecha inválida"
                        }
                    },
                    isError = dateError.value != null,
                    label = { Text("Fecha de vencimiento (YYYY-MM-DD)") },
                    supportingText = { dateError.value?.let { Text(it) } }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = recurrence.value,
                    onValueChange = {
                        recurrence.value = it
                        val v = it.uppercase()
                        recurrenceError.value = if (v in listOf("NONE", "WEEKLY", "MONTHLY")) null else "Valores: NONE, WEEKLY o MONTHLY"
                    },
                    isError = recurrenceError.value != null,
                    label = { Text("Recurrencia (NONE/WEEKLY/MONTHLY)") },
                    supportingText = { recurrenceError.value?.let { Text(it) } }
                )
            }
        }
    )
}
