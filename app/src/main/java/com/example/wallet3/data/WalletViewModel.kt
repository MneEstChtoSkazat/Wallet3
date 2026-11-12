package com.example.wallet.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/** Всё, что показывает UI: список активов и рублёвый баланс. */
data class WalletUiState(
    val items: List<Asset> = emptyList(),
    val balance: Double = 0.0
) {
    /** Сумма стоимости всех активов в рублях. */
    val totalAssets: Double get() = items.sumOf { it.valueRub }
}

/** Связывает экраны с базой: операции идут в фоновом потоке, после каждой состояние перечитывается. */
class WalletViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = WalletRepository(WalletDbHelper(app))

    private val _state = MutableStateFlow(WalletUiState())
    val state: StateFlow<WalletUiState> = _state

    init {
        viewModelScope.launch(Dispatchers.IO) { reload() }
    }

    private fun reload() {
        _state.value = WalletUiState(items = repo.getAll(), balance = repo.getBalance())
    }

    /** Update существующей записи (при id == 0 добавляет новую без списания баланса). */
    fun save(asset: Asset, onDone: () -> Unit) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                if (asset.id == 0L) repo.insert(asset) else repo.update(asset)
                reload()
            }
            onDone()
        }
    }

    /** Покупка новой валюты (Create). Баланс уменьшается, активы растут. */
    fun buyNew(name: String, amount: Double, rate: Double, onResult: (BuyResult) -> Unit) {
        viewModelScope.launch {
            val result = withContext(Dispatchers.IO) {
                repo.buyNew(name, amount, rate).also { reload() }
            }
            onResult(result)
        }
    }

    /** Продажа актива (Delete): рубли возвращаются на баланс. null — актив не найден. */
    fun sell(assetId: Long, onResult: (Double?) -> Unit) {
        viewModelScope.launch {
            val proceeds = withContext(Dispatchers.IO) {
                repo.sell(assetId).also { reload() }
            }
            onResult(proceeds)
        }
    }
}
