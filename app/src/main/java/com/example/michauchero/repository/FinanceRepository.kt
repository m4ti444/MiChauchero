package com.example.michauchero.repository

import android.content.Context
import com.example.michauchero.data.AppDatabase
import com.example.michauchero.data.BillDao
import com.example.michauchero.data.BillEntity
import com.example.michauchero.data.BudgetDao
import com.example.michauchero.data.BudgetEntity
import com.example.michauchero.data.TransactionDao
import com.example.michauchero.data.TransactionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

data class MonthlySummary(
    val income: Double,
    val expenses: Double,
    val balance: Double,
    val budgetAmount: Double?
)

class FinanceRepository(
    private val transactionDao: TransactionDao,
    private val budgetDao: BudgetDao,
    private val billDao: BillDao
) {
    companion object {
        fun build(context: Context): FinanceRepository {
            val db = AppDatabase.getInstance(context)
            return FinanceRepository(db.transactionDao(), db.budgetDao(), db.billDao())
        }
    }

    suspend fun addTransaction(tx: TransactionEntity) = transactionDao.insert(tx)
    suspend fun updateTransaction(tx: TransactionEntity) = transactionDao.update(tx)
    suspend fun deleteTransaction(tx: TransactionEntity) = transactionDao.delete(tx)

    fun transactionsForMonth(yearMonth: YearMonth): Flow<List<TransactionEntity>> {
        val (start, end) = monthBounds(yearMonth)
        return transactionDao.transactionsInRange(start, end)
    }

    fun monthlySummary(yearMonth: YearMonth): Flow<MonthlySummary> {
        val (start, end) = monthBounds(yearMonth)
        val incomeFlow = transactionDao.totalIncomeInRange(start, end)
        val expenseFlow = transactionDao.totalExpenseInRange(start, end)
        val budgetFlow = budgetDao.budgetForMonth(monthStartEpoch(yearMonth))
        return combine3(incomeFlow, expenseFlow, budgetFlow) { inc, exp, budget ->
            val income = inc ?: 0.0
            val expenses = exp ?: 0.0
            MonthlySummary(
                income = income,
                expenses = expenses,
                balance = income - expenses,
                budgetAmount = budget?.amount
            )
        }
    }

    suspend fun setBudgetForMonth(yearMonth: YearMonth, amount: Double) {
        budgetDao.upsert(BudgetEntity(monthStartEpoch(yearMonth), amount))
    }

    fun budgetForMonth(yearMonth: YearMonth): Flow<Double?> =
        budgetDao.budgetForMonth(monthStartEpoch(yearMonth)).map { it?.amount }

    fun upcomingBills(): Flow<List<BillEntity>> = billDao.upcomingBills()

    suspend fun addBill(bill: BillEntity) = billDao.upsert(bill)
    suspend fun markBillPaid(id: Int) = billDao.markPaid(id)
    suspend fun deleteBill(bill: BillEntity) = billDao.delete(bill)

    private fun monthBounds(yearMonth: YearMonth): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()
        val startEpoch = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endEpoch = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1
        return startEpoch to endEpoch
    }

    private fun monthStartEpoch(yearMonth: YearMonth): Long {
        val zone = ZoneId.systemDefault()
        return yearMonth.atDay(1).atStartOfDay(zone).toInstant().toEpochMilli()
    }
}

// Simple combine3 since we avoid external Flow combine of 3 without overload
private fun <A, B, C, R> combine3(
    a: Flow<A>,
    b: Flow<B>,
    c: Flow<C>,
    transform: suspend (A, B, C) -> R
): Flow<R> = kotlinx.coroutines.flow.combine(a, kotlinx.coroutines.flow.combine(b, c) { bVal, cVal -> bVal to cVal }) { aVal, bc ->
    transform(aVal, bc.first, bc.second)
}