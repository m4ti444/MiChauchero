package com.example.michauchero.ui.screens

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.michauchero.viewmodel.BudgetViewModel

@Composable
fun BudgetScreen(viewModel: BudgetViewModel, padding: PaddingValues) {
    val amount by viewModel.amount.collectAsState()
    val input = remember { mutableStateOf(amount?.toString() ?: "") }

    androidx.compose.foundation.layout.Column(modifier = Modifier.padding(padding).padding(16.dp)) {
        Card(modifier = Modifier.fillMaxWidth()) {
            androidx.compose.foundation.layout.Column(modifier = Modifier.padding(16.dp)) {
                Text("Presupuesto mensual", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Text(text = amount?.let { "$${"%.2f".format(it)}" } ?: "No establecido")
            }
        }
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = input.value, onValueChange = { input.value = it }, label = { Text("Nuevo presupuesto") })
        Spacer(Modifier.height(8.dp))
        Button(onClick = { input.value.toDoubleOrNull()?.let { viewModel.setBudget(it) } }) { Text("Guardar") }
    }
}