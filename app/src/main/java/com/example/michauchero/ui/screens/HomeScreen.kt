package com.example.michauchero.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.michauchero.data.TransactionEntity
import com.example.michauchero.viewmodel.HomeViewModel
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material.icons.filled.Add
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import android.util.Log
import android.app.Activity
import com.example.michauchero.repository.ReportRepository
import java.time.YearMonth
import kotlinx.coroutines.launch
 

@Composable
fun HomeScreen(viewModel: HomeViewModel, padding: androidx.compose.foundation.layout.PaddingValues) {
    val summary by viewModel.summary.collectAsState()
    val transactions by viewModel.recentTransactions.collectAsState()
    val showDialog = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.padding(padding),
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog.value = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Agregar")
            }
        }
    ) { inner ->
        Column(modifier = Modifier.padding(inner).padding(16.dp)) {
            FinancialSummary(income = summary.income, expenses = summary.expenses, balance = summary.balance, budget = summary.budgetAmount)
            Spacer(modifier = Modifier.height(8.dp))
            Button(onClick = {
                val ym = YearMonth.now()
                scope.launch {
                    try {
                        val uri = ReportRepository().generateAndSave(
                            context,
                            ym.year,
                            ym.monthValue,
                            summary.income,
                            summary.expenses,
                            summary.budgetAmount
                        )
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                            .setDataAndType(uri, "application/pdf")
                            .addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        val pm = context.packageManager
                        if (intent.resolveActivity(pm) != null) {
                            val activity = context as? Activity
                            if (activity != null) {
                                val chooser = android.content.Intent.createChooser(intent, "Abrir PDF")
                                activity.startActivity(chooser)
                            } else {
                                intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                val chooser = android.content.Intent.createChooser(intent, "Abrir PDF")
                                context.startActivity(chooser)
                            }
                        } else {
                            val market = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("market://search?q=pdf%20viewer"))
                            if (market.resolveActivity(pm) != null) {
                                val activity = context as? Activity
                                if (activity != null) activity.startActivity(market) else {
                                    market.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(market)
                                }
                            } else {
                                val web = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://play.google.com/store/search?q=pdf%20viewer&c=apps"))
                                val activity = context as? Activity
                                if (activity != null) activity.startActivity(web) else {
                                    web.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(web)
                                }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("PDF", "Error al generar PDF", e)
                        val msg = e.message ?: "Error desconocido"
                        Toast.makeText(context, "Error al generar PDF: $msg", Toast.LENGTH_LONG).show()
                    }
                }
            }) { Text("Generar PDF del mes") }
            Spacer(modifier = Modifier.height(24.dp))
            Text("Transacciones recientes", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            TransactionList(transactions)
        }

        if (showDialog.value) {
            AddQuickTransactionDialog(
                onDismiss = { showDialog.value = false },
                onSave = { title, amount, isIncome, category ->
                    viewModel.quickAddTransaction(title, amount, isIncome, category)
                    showDialog.value = false
                }
            )
        }
    }
}

@Composable
fun AddQuickTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (String, Double, Boolean, String) -> Unit
) {
    val title = remember { mutableStateOf("") }
    val amountStr = remember { mutableStateOf("") }
    val category = remember { mutableStateOf("General") }
    val typeStr = remember { mutableStateOf("gasto") }
    val titleError = remember { mutableStateOf<String?>(null) }
    val amountError = remember { mutableStateOf<String?>(null) }
    val typeError = remember { mutableStateOf<String?>(null) }
 

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            val isValid = titleError.value == null && amountError.value == null && typeError.value == null &&
                    title.value.isNotBlank() && amountStr.value.toDoubleOrNull() != null
            TextButton(
                enabled = isValid,
                onClick = {
                    val amount = amountStr.value.toDoubleOrNull() ?: return@TextButton
                    val isIncome = typeStr.value.lowercase().contains("ingreso")
                    onSave(title.value, amount, isIncome, category.value)
                }
            ) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        title = { Text("Agregar transacción") },
        text = {
            Column {
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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = { amountError.value?.let { Text(it) } }
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(value = category.value, onValueChange = { category.value = it }, label = { Text("Categoría") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = typeStr.value,
                    onValueChange = {
                        typeStr.value = it
                        val v = it.lowercase()
                        typeError.value = if (v.contains("ingreso") || v.contains("gasto")) null else "Debe ser 'ingreso' o 'gasto'"
                    },
                    isError = typeError.value != null,
                    label = { Text("Tipo (ingreso/gasto)") },
                    supportingText = { typeError.value?.let { Text(it) } }
                )
            }
        }
    )
}

@Composable
fun FinancialSummary(income: Double, expenses: Double, balance: Double, budget: Double?) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Balance Total", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(text = "$${"%.2f".format(balance)}", style = MaterialTheme.typography.displaySmall.copy(fontWeight = FontWeight.ExtraBold), color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (budget != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "Presupuesto del mes: $${"%.2f".format(budget)}", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            SummaryCard(title = "Ingresos", amount = income, icon = Icons.Filled.ArrowUpward, color = Color(0xFF00897B), modifier = Modifier.weight(1f))
            SummaryCard(title = "Gastos", amount = expenses, icon = Icons.Filled.ArrowDownward, color = Color(0xFFE53935), modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun SummaryCard(title: String, amount: Double, icon: ImageVector, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyMedium)
                Text(text = "$${"%.2f".format(amount)}", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
            }
        }
    }
}

@Composable
fun TransactionList(transactions: List<TransactionEntity>) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(transactions) { tx ->
            TransactionItem(tx)
        }
    }
}

@Composable
fun TransactionItem(tx: TransactionEntity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(12.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(tx.title, fontWeight = FontWeight.Bold)
                Text(tx.category, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(text = "$${"%.2f".format(tx.amount)}", color = if (tx.isIncome) Color(0xFF00897B) else Color(0xFFE53935), style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold))
        }
    }
}
