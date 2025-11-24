package com.example.michauchero.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import com.example.michauchero.ui.screens.BudgetScreen
import com.example.michauchero.viewmodel.BudgetViewModel
import com.example.michauchero.repository.FinanceRepository
import io.mockk.every
import io.mockk.mockk
import io.mockk.coVerify
import kotlinx.coroutines.flow.flowOf
import com.example.michauchero.ui.theme.MiChaucheroTheme

class BudgetScreenUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun muestra_presupuesto_y_valida_input() {
        val repo = mockk<FinanceRepository>(relaxed = true)
        every { repo.budgetForMonth(any()) } returns flowOf(100.0)
        val vm = BudgetViewModel(repo)

        composeTestRule.setContent {
            MiChaucheroTheme {
                BudgetScreen(vm, PaddingValues())
            }
        }

        composeTestRule.onNodeWithText("Presupuesto mensual").assertIsDisplayed()
        composeTestRule.onNodeWithText("$100.00").assertIsDisplayed()

        composeTestRule.onNodeWithText("Nuevo presupuesto").performTextInput("abc")
        composeTestRule.onNodeWithText("El monto debe ser numérico").assertIsDisplayed()

        composeTestRule.onNodeWithText("Nuevo presupuesto").performTextClearance()
        composeTestRule.onNodeWithText("Nuevo presupuesto").performTextInput("200")
        composeTestRule.onNodeWithText("Guardar").performClick()
        coVerify(timeout = 1500) { repo.setBudgetForMonth(any(), 200.0) }
    }
}
