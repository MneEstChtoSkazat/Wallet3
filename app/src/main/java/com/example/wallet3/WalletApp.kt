package com.example.wallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.wallet.data.WalletViewModel
import com.example.wallet.ui.screens.EditorScreen
import com.example.wallet.ui.screens.MapScreen
import com.example.wallet.ui.screens.SettingsScreen
import com.example.wallet.ui.screens.WalletScreen
import com.example.wallet3.Routes

/** Маршруты экранов приложения (они же — стрелки в прототипе Figma). */

@Composable
fun WalletApp(viewModel: WalletViewModel) {
    val navController = rememberNavController()
    val state by viewModel.state.collectAsState()

    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            WalletScreen(
                state = state,
                onItemClick = { navController.navigate(Routes.editor(it.id)) },
                onAddClick = { navController.navigate(Routes.editor(-1L)) },
                onMapClick = { navController.navigate(Routes.MAP) },
                onSettingsClick = { navController.navigate(Routes.SETTINGS) }
            )
        }
        composable(
            route = Routes.EDITOR,
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) { entry ->
            EditorScreen(
                assetId = entry.arguments?.getLong("id") ?: -1L,
                state = state,
                onBack = { navController.popBackStack() },
                onSave = { asset -> viewModel.save(asset) { navController.popBackStack() } },
                onSell = { id, onResult -> viewModel.sell(id, onResult) },
                onBuy = { name, amount, rate, onResult -> viewModel.buyNew(name, amount, rate, onResult) }
            )
        }
        composable(Routes.MAP) {
            MapScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
