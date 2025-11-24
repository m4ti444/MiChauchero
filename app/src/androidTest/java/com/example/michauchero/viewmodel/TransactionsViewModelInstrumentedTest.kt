package com.example.michauchero.viewmodel

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.michauchero.data.TransactionEntity
import com.example.michauchero.repository.FinanceRepository
import com.example.michauchero.MainDispatcherRule
import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.launch
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
@OptIn(ExperimentalCoroutinesApi::class)
class TransactionsViewModelInstrumentedTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    @Test
    fun setMonth_actualiza_estado_y_lista() = runTest {
        val repo = mockk<FinanceRepository>(relaxed = true)
        val vm = TransactionsViewModel(repo)

        val nov = YearMonth.of(2025, 11)
        val dic = YearMonth.of(2025, 12)
        val novList = listOf(TransactionEntity(title = "Noviembre", amount = 10.0, isIncome = true, category = "Cat", dateEpoch = 1L))
        val dicList = listOf(TransactionEntity(title = "Diciembre", amount = 20.0, isIncome = false, category = "Cat", dateEpoch = 2L))
        every { repo.transactionsForMonth(nov) } returns flowOf(novList)
        every { repo.transactionsForMonth(dic) } returns flowOf(dicList)

        var latest: List<TransactionEntity>? = null
        val job = launch { vm.transactions.collect { latest = it } }

        vm.setMonth(nov)
        advanceUntilIdle()
        vm.selectedMonth.value shouldBe nov
        latest shouldBe novList

        vm.setMonth(dic)
        advanceUntilIdle()
        vm.selectedMonth.value shouldBe dic
        latest shouldBe dicList

        job.cancel()
    }

    @Test
    fun add_y_delete_delegan_en_repo() = runTest {
        val repo = mockk<FinanceRepository>(relaxed = true)
        val vm = TransactionsViewModel(repo)
        vm.add("T", 5.0, true, "C", 123L, "N")
        advanceUntilIdle()
        coVerify { repo.addTransaction(match { it.title == "T" && it.amount == 5.0 && it.isIncome && it.category == "C" && it.dateEpoch == 123L && it.notes == "N" }) }

        val tx = TransactionEntity(title = "X", amount = 1.0, isIncome = false, category = "C", dateEpoch = 2L)
        vm.delete(tx)
        advanceUntilIdle()
        coVerify { repo.deleteTransaction(tx) }
    }
}
