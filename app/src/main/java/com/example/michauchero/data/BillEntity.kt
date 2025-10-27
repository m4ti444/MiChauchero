package com.example.michauchero.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class BillEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val amount: Double,
    val dueEpoch: Long,
    val recurrence: String = "NONE", // NONE, WEEKLY, MONTHLY
    val isPaid: Boolean = false
)