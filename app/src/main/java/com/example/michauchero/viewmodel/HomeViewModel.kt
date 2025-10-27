package com.example.michauchero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.michauchero.data.TransactionEntity
import com.example.michauchero.repository.FinanceRepository
import com.example.michauchero.repository.MonthlySummary
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

class HomeViewModel(private val repo: FinanceRepository) : ViewModel() {
    private val currentMonth = YearMonth.now()

    val summary: StateFlow<MonthlySummary> = repo.monthlySummary(currentMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), MonthlySummary(0.0, 0.0, 0.0, null))

    val recentTransactions = repo.transactionsForMonth(currentMonth)
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun quickAddTransaction(title: String, amount: Double, isIncome: Boolean, category: String) {
        val now = System.currentTimeMillis()
        viewModelScope.launch {
            repo.addTransaction(
                TransactionEntity(
                    title = title,
                    amount = amount,
                    isIncome = isIncome,
                    category = category,
                    dateEpoch = now
                )
            )
        }
    }
}

class HomeViewModelFactory(private val repo: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}