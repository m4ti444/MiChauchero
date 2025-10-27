package com.example.michauchero.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.michauchero.data.BillEntity
import com.example.michauchero.reminders.ReminderScheduler
import com.example.michauchero.repository.FinanceRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BillsViewModel(private val repo: FinanceRepository) : ViewModel() {
    val upcoming: StateFlow<List<BillEntity>> = repo.upcomingBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(context: android.content.Context, title: String, amount: Double, dueEpoch: Long, recurrence: String) {
        viewModelScope.launch {
            val bill = BillEntity(title = title, amount = amount, dueEpoch = dueEpoch, recurrence = recurrence)
            repo.addBill(bill)
            ReminderScheduler.scheduleBillReminder(context, title, amount, dueEpoch)
        }
    }

    fun markPaid(id: Int) { viewModelScope.launch { repo.markBillPaid(id) } }
    fun delete(bill: BillEntity) { viewModelScope.launch { repo.deleteBill(bill) } }
}

class BillsViewModelFactory(private val repo: FinanceRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillsViewModel(repo) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}