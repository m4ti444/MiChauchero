package com.example.michauchero.ui

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import com.example.michauchero.ui.screens.FinancialSummary
import com.example.michauchero.ui.screens.AddQuickTransactionDialog
import com.example.michauchero.ui.theme.MiChaucheroTheme

class HomeScreenUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun financialSummary_muestra_valores_y_presupuesto() {
        composeTestRule.setContent {
            MiChaucheroTheme {
                FinancialSummary(income = 1000.0, expenses = 250.0, balance = 750.0, budget = 800.0)
            }
        }

        composeTestRule.onNodeWithText("Balance Total").assertIsDisplayed()
        composeTestRule.onNodeWithText("$750.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Presupuesto del mes: $800.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Ingresos").assertIsDisplayed()
        composeTestRule.onNodeWithText("$1000.00").assertIsDisplayed()
        composeTestRule.onNodeWithText("Gastos").assertIsDisplayed()
        composeTestRule.onNodeWithText("$250.00").assertIsDisplayed()
    }

    @Test
    fun addQuickTransactionDialog_valida_y_guarda() {
        var saved: Quad? = null
        composeTestRule.setContent {
            MiChaucheroTheme {
                AddQuickTransactionDialog(onDismiss = {}, onSave = { t, a, i, c -> saved = Quad(t, a, i, c) })
            }
        }

        composeTestRule.onNodeWithText("Título").performTextClearance()
        composeTestRule.onNodeWithText("Título").performTextInput("Test")
        composeTestRule.onNodeWithText("Monto").performTextClearance()
        composeTestRule.onNodeWithText("Monto").performTextInput("10")
        composeTestRule.onNodeWithText("Categoría").performTextClearance()
        composeTestRule.onNodeWithText("Categoría").performTextInput("General")
        composeTestRule.onNodeWithText("Tipo (ingreso/gasto)").performTextClearance()
        composeTestRule.onNodeWithText("Tipo (ingreso/gasto)").performTextInput("ingreso")
        composeTestRule.onNodeWithText("Guardar").performClick()
        composeTestRule.waitUntil(2000) { saved != null }
        check(saved!!.title == "Test")
        check(saved!!.amount == 10.0)
        check(saved!!.isIncome)
        check(saved!!.category == "General")
    }

    private data class Quad(val title: String, val amount: Double, val isIncome: Boolean, val category: String)
}
