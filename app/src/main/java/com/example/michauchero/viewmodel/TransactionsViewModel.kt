package com.example.michauchero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.michauchero.data.TransactionEntity
import com.example.michauchero.repository.FinanceRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.YearMonth

class TransactionsViewModel(private val repo: FinanceRepository) : ViewModel() {
    private val _selectedMonth = MutableStateFlow(YearMonth.now())
    val selectedMonth: StateFlow<YearMonth> = _selectedMonth

    val transactions: StateFlow<List<TransactionEntity>> = _selectedMonth
        .flatMapLatest { repo.transactionsForMonth(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setMonth(month: YearMonth) { _selectedMonth.value = month }

    fun add(title: String, amount: Double, isIncome: Boolean, category: String, dateEpoch: Long, notes: String?) {
        viewModelScope.launch {
            repo.addTransaction(TransactionEntity(title = title, amount = amount, isIncome = isIncome, category = category, dateEpoch = dateEpoch, notes = notes))
        }
    }

    fun delete(tx: TransactionEntity) { viewModelScope.launch { repo.deleteTransaction(tx) } }
}

class TransactionsViewModelFactory(private val repo: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TransactionsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}