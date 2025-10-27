package com.example.michauchero.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// Primary key is monthStartEpoch (start of the month at midnight in epoch millis)
@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey val monthStartEpoch: Long,
    val amount: Double
)