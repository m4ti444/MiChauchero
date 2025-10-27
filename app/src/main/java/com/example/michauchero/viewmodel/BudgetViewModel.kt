package com.example.michauchero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.michauchero.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

class BudgetViewModel(private val repo: FinanceRepository) : ViewModel() {
    private val currentMonth = YearMonth.now()

    val amount: StateFlow<Double?> = repo.budgetForMonth(currentMonth)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setBudget(value: Double) {
        viewModelScope.launch { repo.setBudgetForMonth(currentMonth, value) }
    }
}

class BudgetViewModelFactory(private val repo: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}