package com.example.michauchero.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.michauchero.AppGraph
import com.example.michauchero.ui.screens.BudgetScreen
import com.example.michauchero.ui.screens.BillsScreen
import com.example.michauchero.ui.screens.HomeScreen
import com.example.michauchero.viewmodel.BillsViewModel
import com.example.michauchero.viewmodel.BillsViewModelFactory
import com.example.michauchero.viewmodel.BudgetViewModel
import com.example.michauchero.viewmodel.BudgetViewModelFactory
import com.example.michauchero.viewmodel.HomeViewModel
import com.example.michauchero.viewmodel.HomeViewModelFactory

sealed class Dest(val route: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    data object Home : Dest("home", Icons.Filled.Home)
    data object Budget : Dest("budget", Icons.Filled.AccountBalanceWallet)
    data object Bills : Dest("bills", Icons.Filled.Notifications)
}

@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    val navController = rememberNavController()
    val items = listOf(Dest.Home, Dest.Budget, Dest.Bills)

    Scaffold(
        bottomBar = {
            val backStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = backStackEntry?.destination?.route
            NavigationBar {
                items.forEach { dest ->
                    NavigationBarItem(
                        selected = currentRoute == dest.route,
                        onClick = {
                            navController.navigate(dest.route) {
                                popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(dest.icon, contentDescription = dest.route) },
                        label = null
                    )
                }
            }
        },
        modifier = modifier
    ) { padding ->
        NavHost(navController, startDestination = Dest.Home.route) {
            composable(Dest.Home.route) {
                val vm: HomeViewModel = viewModel(factory = HomeViewModelFactory(AppGraph.repository))
                HomeScreen(vm, padding)
            }
            composable(Dest.Budget.route) {
                val vm: BudgetViewModel = viewModel(factory = BudgetViewModelFactory(AppGraph.repository))
                BudgetScreen(vm, padding)
            }
            composable(Dest.Bills.route) {
                val vm: BillsViewModel = viewModel(factory = BillsViewModelFactory(AppGraph.repository))
                BillsScreen(vm, padding)
            }
        }
    }
}
