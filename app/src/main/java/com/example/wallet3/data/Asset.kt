package com.example.wallet.data

/**
 * Актив в кошельке (строка таблицы assets).
 * amount — количество единиц (например, 0.0015 BTC), rateToRub — цена одной единицы в рублях.
 */
data class Asset(
    val id: Long = 0,
    val symbol: String,
    val name: String,
    val amount: Double,
    val rateToRub: Double
) {
    /** Стоимость актива в рублях. */
    val valueRub: Double get() = amount * rateToRub
}

/** Результат симуляции покупки. */
sealed class BuyResult {
    data class Success(val newBalance: Double, val cost: Double) : BuyResult()
    data class NotEnoughMoney(val balance: Double) : BuyResult()
    object Invalid : BuyResult()
}
