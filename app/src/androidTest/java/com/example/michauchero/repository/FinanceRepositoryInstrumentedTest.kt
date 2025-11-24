package com.example.michauchero.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.michauchero.data.BillDao
import com.example.michauchero.data.BudgetDao
import com.example.michauchero.data.BudgetEntity
import com.example.michauchero.data.TransactionDao
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.runner.RunWith
import java.time.YearMonth

@RunWith(AndroidJUnit4::class)
class FinanceRepositoryInstrumentedTest {
    @Test
    fun monthlySummary_combina_valores_correctamente() = runTest {
        val txn = mockk<TransactionDao>()
        val bud = mockk<BudgetDao>()
        val bill = mockk<BillDao>()
        val repo = FinanceRepository(txn, bud, bill)

        val ym = YearMonth.of(2025, 11)
        every { txn.totalIncomeInRange(any(), any()) } returns flowOf(100_000.0)
        every { txn.totalExpenseInRange(any(), any()) } returns flowOf(25_000.0)
        every { bud.budgetForMonth(any()) } returns flowOf(BudgetEntity(0L, 80_000.0))

        val first = repo.monthlySummary(ym).first()
        first.income shouldBe 100_000.0
        first.expenses shouldBe 25_000.0
        first.balance shouldBe 75_000.0
        first.budgetAmount shouldBe 80_000.0
    }

    @Test
    fun monthlySummary_maneja_nulos() = runTest {
        val txn = mockk<TransactionDao>()
        val bud = mockk<BudgetDao>()
        val bill = mockk<BillDao>()
        val repo = FinanceRepository(txn, bud, bill)

        val ym = YearMonth.of(2025, 11)
        every { txn.totalIncomeInRange(any(), any()) } returns flowOf(null)
        every { txn.totalExpenseInRange(any(), any()) } returns flowOf(null)
        every { bud.budgetForMonth(any()) } returns flowOf(null)

        val first = repo.monthlySummary(ym).first()
        first.income shouldBe 0.0
        first.expenses shouldBe 0.0
        first.balance shouldBe 0.0
        first.budgetAmount shouldBe null
    }
}
