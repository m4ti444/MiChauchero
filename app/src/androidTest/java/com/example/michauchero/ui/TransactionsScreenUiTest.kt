package com.example.michauchero.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import org.junit.Test
import com.example.michauchero.ui.screens.TransactionsScreen
import com.example.michauchero.viewmodel.TransactionsViewModel
import com.example.michauchero.repository.FinanceRepository
import io.mockk.mockk
import com.example.michauchero.ui.theme.MiChaucheroTheme

class TransactionsScreenUiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyState_muestra_mensaje_y_dialogo_al_agregar() {
        val repo = mockk<FinanceRepository>(relaxed = true)
        val vm = TransactionsViewModel(repo)

        composeTestRule.setContent {
            MiChaucheroTheme {
                TransactionsScreen(vm, PaddingValues())
            }
        }

        composeTestRule.onNodeWithText("No hay transacciones aún").assertIsDisplayed()
        composeTestRule.onNodeWithTag("tx_empty_add_button").performClick()
        composeTestRule.onNodeWithTag("tx_add_dialog").assertIsDisplayed()
        composeTestRule.onNodeWithText("Título").assertIsDisplayed()
    }
}
