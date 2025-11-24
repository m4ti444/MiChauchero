package com.example.michauchero.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.michauchero.data.TransactionEntity
import com.example.michauchero.viewmodel.TransactionsViewModel
import java.time.LocalDate
import java.time.ZoneId

@Composable
fun TransactionsScreen(viewModel: TransactionsViewModel, padding: PaddingValues) {
    val txs = viewModel.transactions.value
    val showDialog = remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.padding(padding),
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog.value = true }) { androidx.compose.material3.Icon(Icons.Filled.Add, contentDescription = "Agregar") }
        }
    ) { inner ->
        if (txs.isEmpty()) {
            androidx.compose.foundation.layout.Box(
                modifier = Modifier.padding(inner).fillMaxSize(),
                contentAlignment = androidx.compose.ui.Alignment.Center
            ) {
                androidx.compose.foundation.layout.Column(horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
                    Text("No hay transacciones aún", style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(12.dp))
                    androidx.compose.material3.Button(modifier = Modifier.testTag("tx_empty_add_button"), onClick = { showDialog.value = true }) { Text("Agregar transacción") }
                }
            }
        } else {
            LazyColumn(modifier = Modifier.padding(inner).padding(16.dp)) {
                items(txs) { tx ->
                    TransactionRow(tx)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        if (showDialog.value) {
            AddTransactionDialog(
                onDismiss = { showDialog.value = false },
                onSave = { title, amount, isIncome, category, dateStr, notes ->
                    val date = runCatching { LocalDate.parse(dateStr) }.getOrElse { LocalDate.now() }
                    val epoch = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
                    viewModel.add(title, amount, isIncome, category, epoch, notes)
                    showDialog.value = false
                }
            )
        }
    }
}

@Composable
fun TransactionRow(tx: TransactionEntity) {
    androidx.compose.material3.Card(modifier = Modifier.fillMaxWidth()) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween
        ) {
            androidx.compose.foundation.layout.Column {
                Text(tx.title, style = MaterialTheme.typography.titleMedium)
                Text(tx.category, style = MaterialTheme.typography.bodySmall)
            }
            Text(
                text = "$${"%.2f".format(tx.amount)}",
                color = if (tx.isIncome) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
            )
        }
    }
}

@Composable
fun AddTransactionDialog(onDismiss: () -> Unit, onSave: (String, Double, Boolean, String, String, String?) -> Unit) {
    val title = remember { mutableStateOf("") }
    val amountStr = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("General") }
    val isIncomeStr = remember { mutableStateOf("gasto") }
    val dateStr = remember { mutableStateOf(LocalDate.now().toString()) }
    val notes = remember { mutableStateOf("") }
    val titleError = remember { mutableStateOf<String?>(null) }
    val amountError = remember { mutableStateOf<String?>(null) }
    val typeError = remember { mutableStateOf<String?>(null) }
    val dateError = remember { mutableStateOf<String?>(null) }

    AlertDialog(
        modifier = Modifier.testTag("tx_add_dialog"),
        onDismissRequest = onDismiss,
        confirmButton = {
            val isValid = titleError.value == null && amountError.value == null && typeError.value == null && dateError.value == null &&
                    title.value.isNotBlank() && amountStr.value.toDoubleOrNull() != null
            TextButton(
                enabled = isValid,
                onClick = {
                    val amount = amountStr.value.toDoubleOrNull() ?: return@TextButton
                    val isIncome = isIncomeStr.value.lowercase().contains("ingreso")
                    onSave(title.value, amount, isIncome, category.value, dateStr.value, notes.value.takeIf { it.isNotBlank() })
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Agregar transacción") },
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
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { amountError.value?.let { Text(it) } }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = category.value, onValueChange = { category.value = it }, label = { Text("Categoría") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = isIncomeStr.value,
                    onValueChange = {
                        isIncomeStr.value = it
                        val v = it.lowercase()
                        typeError.value = if (v.contains("ingreso") || v.contains("gasto")) null else "Debe ser 'ingreso' o 'gasto'"
                    },
                    isError = typeError.value != null,
                    label = { Text("Tipo (ingreso/gasto)") },
                    supportingText = { typeError.value?.let { Text(it) } }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = dateStr.value,
                    onValueChange = {
                        dateStr.value = it
                        dateError.value = try {
                            LocalDate.parse(it)
                            null
                        } catch (_: Exception) {
                            "Fecha inválida"
                        }
                    },
                    isError = dateError.value != null,
                    label = { Text("Fecha (YYYY-MM-DD)") },
                    supportingText = { dateError.value?.let { Text(it) } }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = notes.value, onValueChange = { notes.value = it }, label = { Text("Notas (opcional)") })
            }
        }
    )
}
