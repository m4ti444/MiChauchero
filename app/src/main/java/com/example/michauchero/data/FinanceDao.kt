package com.example.michauchero.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tx: TransactionEntity)

    @Update
    suspend fun update(tx: TransactionEntity)

    @Delete
    suspend fun delete(tx: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE dateEpoch BETWEEN :start AND :end ORDER BY dateEpoch DESC")
    fun transactionsInRange(start: Long, end: Long): Flow<List<TransactionEntity>>

    @Query("SELECT SUM(CASE WHEN isIncome = 1 THEN amount ELSE 0 END) FROM transactions WHERE dateEpoch BETWEEN :start AND :end")
    fun totalIncomeInRange(start: Long, end: Long): Flow<Double?>

    @Query("SELECT SUM(CASE WHEN isIncome = 0 THEN amount ELSE 0 END) FROM transactions WHERE dateEpoch BETWEEN :start AND :end")
    fun totalExpenseInRange(start: Long, end: Long): Flow<Double?>
}

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Query("SELECT * FROM budgets WHERE monthStartEpoch = :monthStart LIMIT 1")
    fun budgetForMonth(monthStart: Long): Flow<BudgetEntity?>
}

@Dao
interface BillDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(bill: BillEntity)

    @Update
    suspend fun update(bill: BillEntity)

    @Delete
    suspend fun delete(bill: BillEntity)

    @Query("SELECT * FROM bills WHERE isPaid = 0 ORDER BY dueEpoch ASC")
    fun upcomingBills(): Flow<List<BillEntity>>

    @Query("UPDATE bills SET isPaid = 1 WHERE id = :id")
    suspend fun markPaid(id: Int)
}