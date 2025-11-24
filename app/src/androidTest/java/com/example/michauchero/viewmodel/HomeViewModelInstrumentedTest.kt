package com.example.michauchero.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.michauchero.repository.FinanceRepository
import com.example.michauchero.MainDispatcherRule
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelInstrumentedTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @Test
    fun quickAddTransaction_delega_en_repo() = runTest {
        val repo = mockk<FinanceRepository>(relaxed = true)
        val vm = HomeViewModel(repo)

        vm.quickAddTransaction("Ingreso", 123.45, true, "Salario")
        advanceUntilIdle()
        coVerify { repo.addTransaction(match { it.title == "Ingreso" && it.amount == 123.45 && it.isIncome && it.category == "Salario" }) }
    }
}
