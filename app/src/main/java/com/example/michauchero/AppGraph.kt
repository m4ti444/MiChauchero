package com.example.michauchero

import android.app.Application
import android.content.Context
import com.example.michauchero.repository.FinanceRepository

object AppGraph {
    lateinit var repository: FinanceRepository
        private set

    fun init(context: Context) {
        repository = FinanceRepository.build(context)
    }
}