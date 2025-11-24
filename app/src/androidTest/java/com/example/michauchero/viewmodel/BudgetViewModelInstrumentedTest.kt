package com.example.michauchero.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.michauchero.repository.FinanceRepository
import com.example.michauchero.MainDispatcherRule
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class BudgetViewModelInstrumentedTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @Test
    fun amount_refleja_repo() = runTest {
        val nowMonth = YearMonth.now()
        val repo = mockk<FinanceRepository>(relaxed = true)
        every { repo.budgetForMonth(nowMonth) } returns flowOf(100.0)
        val vm = BudgetViewModel(repo)
        val received = vm.amount.filterNotNull().first()
        received shouldBe 100.0
    }

    @Test
    fun setBudget_delega_en_repo() = runTest {
        val repo = mockk<FinanceRepository>(relaxed = true)
        val vm = BudgetViewModel(repo)
        val value = 250.0
        vm.setBudget(value)
        advanceUntilIdle()
        coVerify { repo.setBudgetForMonth(match { it == YearMonth.now() }, value) }
    }
}
