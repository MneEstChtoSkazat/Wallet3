package com.example.wallet.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

/**
 * Локальная база данных SQLite.
 *  - assets  — активы кошелька (биток, юань, доллар ...);
 *  - profile — одна строка с рублёвым балансом пользователя.
 */
class WalletDbHelper(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE $T_ASSETS (
                $C_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $C_SYMBOL TEXT NOT NULL,
                $C_NAME TEXT NOT NULL,
                $C_AMOUNT REAL NOT NULL,
                $C_RATE REAL NOT NULL
            )
            """.trimIndent()
        )
        db.execSQL("CREATE TABLE $T_PROFILE ($C_ID INTEGER PRIMARY KEY, $C_BALANCE REAL NOT NULL)")

        // Стартовый баланс для симуляции покупок
        db.execSQL("INSERT INTO $T_PROFILE ($C_ID, $C_BALANCE) VALUES (1, $START_BALANCE)")

        // Стартовые активы, чтобы список не был пустым при первом запуске
        listOf(
            Asset(symbol = "B", name = "Bitcoin", amount = 0.0015, rateToRub = 8_000_000.0),
            Asset(symbol = "Ξ", name = "Ethereum", amount = 0.02, rateToRub = 250_000.0),
            Asset(symbol = "¥", name = "Юань", amount = 400.0, rateToRub = 11.0),
            Asset(symbol = "Ð", name = "Doggy Coin", amount = 20_000.0, rateToRub = 0.5),
            Asset(symbol = "$", name = "Доллар", amount = 12.5, rateToRub = 80.0)
        ).forEach { db.insert(T_ASSETS, null, it.toValues()) }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $T_ASSETS")
        db.execSQL("DROP TABLE IF EXISTS $T_PROFILE")
        onCreate(db)
    }

    companion object {
        const val DB_NAME = "wallet.db"
        const val DB_VERSION = 1
        const val START_BALANCE = 100_000.0

        const val T_ASSETS = "assets"
        const val T_PROFILE = "profile"
        const val C_ID = "id"
        const val C_SYMBOL = "symbol"
        const val C_NAME = "name"
        const val C_AMOUNT = "amount"
        const val C_RATE = "rate_to_rub"
        const val C_BALANCE = "balance"
    }
}

/** Превращает актив в набор значений для insert/update (id задаёт сама БД). */
fun Asset.toValues(): ContentValues = ContentValues().apply {
    put(WalletDbHelper.C_SYMBOL, symbol)
    put(WalletDbHelper.C_NAME, name)
    put(WalletDbHelper.C_AMOUNT, amount)
    put(WalletDbHelper.C_RATE, rateToRub)
}
