package com.example.wallet.data

import android.database.Cursor
import com.example.wallet.data.WalletDbHelper.Companion.C_AMOUNT
import com.example.wallet.data.WalletDbHelper.Companion.C_BALANCE
import com.example.wallet.data.WalletDbHelper.Companion.C_ID
import com.example.wallet.data.WalletDbHelper.Companion.C_NAME
import com.example.wallet.data.WalletDbHelper.Companion.C_RATE
import com.example.wallet.data.WalletDbHelper.Companion.C_SYMBOL
import com.example.wallet.data.WalletDbHelper.Companion.T_ASSETS
import com.example.wallet.data.WalletDbHelper.Companion.T_PROFILE

/** CRUD-операции над активами и симуляция покупки. Все методы блокирующие — вызывать не из UI-потока. */
class WalletRepository(private val helper: WalletDbHelper) {

    // ---------- CREATE ----------
    fun insert(asset: Asset): Long =
        helper.writableDatabase.insert(T_ASSETS, null, asset.toValues())

    // ---------- READ ----------
    fun getAll(): List<Asset> {
        val result = mutableListOf<Asset>()
        helper.readableDatabase.query(T_ASSETS, null, null, null, null, null, "$C_ID ASC").use { c ->
            while (c.moveToNext()) result += c.toAsset()
        }
        return result
    }

    fun getById(id: Long): Asset? =
        helper.readableDatabase
            .query(T_ASSETS, null, "$C_ID = ?", arrayOf(id.toString()), null, null, null)
            .use { c -> if (c.moveToFirst()) c.toAsset() else null }

    fun getBalance(): Double =
        helper.readableDatabase
            .query(T_PROFILE, arrayOf(C_BALANCE), "$C_ID = 1", null, null, null, null)
            .use { c -> if (c.moveToFirst()) c.getDouble(0) else 0.0 }

    // ---------- UPDATE ----------
    fun update(asset: Asset): Int =
        helper.writableDatabase.update(T_ASSETS, asset.toValues(), "$C_ID = ?", arrayOf(asset.id.toString()))

    // ---------- DELETE ----------
    fun delete(id: Long): Int =
        helper.writableDatabase.delete(T_ASSETS, "$C_ID = ?", arrayOf(id.toString()))

    // ---------- Симуляция покупки и продажи ----------
    /**
     * Покупка валюты: баланс уменьшается на amount * rate, актив добавляется в кошелёк.
     * Новая валюта получает случайную иконку; если такая валюта уже есть, количество
     * увеличивается, а курс пересчитывается как средневзвешенный
     * (так «Активы» растут ровно на стоимость покупки).
     * Всё выполняется в одной транзакции.
     */
    fun buyNew(name: String, amount: Double, rate: Double): BuyResult {
        if (name.isBlank() || amount <= 0.0 || rate <= 0.0) return BuyResult.Invalid
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val cost = round2(amount * rate)
            val balance = getBalance()
            if (cost > balance) return BuyResult.NotEnoughMoney(balance)

            val existing = getAll().firstOrNull { it.name.equals(name.trim(), ignoreCase = true) }
            if (existing == null) {
                insert(Asset(symbol = RANDOM_ICONS.random(), name = name.trim(), amount = amount, rateToRub = rate))
            } else {
                val newAmount = existing.amount + amount
                val newRate = (existing.valueRub + amount * rate) / newAmount
                update(existing.copy(amount = newAmount, rateToRub = newRate))
            }

            val newBalance = balance - cost
            setBalance(newBalance)
            db.setTransactionSuccessful()
            return BuyResult.Success(newBalance = newBalance, cost = cost)
        } finally {
            db.endTransaction()
        }
    }

    /**
     * Продажа всего актива по текущему курсу: рубли возвращаются на баланс, запись удаляется.
     * Возвращает полученную сумму или null, если актива нет.
     */
    fun sell(assetId: Long): Double? {
        val db = helper.writableDatabase
        db.beginTransaction()
        try {
            val asset = getById(assetId) ?: return null
            val proceeds = round2(asset.valueRub)
            setBalance(getBalance() + proceeds)
            delete(assetId)
            db.setTransactionSuccessful()
            return proceeds
        } finally {
            db.endTransaction()
        }
    }

    private fun setBalance(value: Double) {
        helper.writableDatabase.execSQL(
            "UPDATE $T_PROFILE SET $C_BALANCE = ? WHERE $C_ID = 1", arrayOf<Any>(value)
        )
    }

    private fun round2(v: Double): Double = Math.round(v * 100.0) / 100.0

    private fun Cursor.toAsset() = Asset(
        id = getLong(getColumnIndexOrThrow(C_ID)),
        symbol = getString(getColumnIndexOrThrow(C_SYMBOL)),
        name = getString(getColumnIndexOrThrow(C_NAME)),
        amount = getDouble(getColumnIndexOrThrow(C_AMOUNT)),
        rateToRub = getDouble(getColumnIndexOrThrow(C_RATE))
    )

    companion object {
        /** Иконки, из которых выбирается случайная для новой валюты. */
        val RANDOM_ICONS = listOf("🚀", "💎", "🔥", "🌟", "🦄", "🐶", "🍀", "⚡", "🌙", "🎲", "🍉", "👑")
    }
}
