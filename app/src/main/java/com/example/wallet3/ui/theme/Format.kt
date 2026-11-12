package com.example.wallet.ui

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

private fun format(pattern: String, value: Double): String {
    val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ' ' }
    return DecimalFormat(pattern, symbols).format(value)
}

/** 12 345.6 -> «12 345.6 ₽» */
fun formatRub(value: Double): String = format("#,##0.##", value) + " ₽"

/** Количество актива: до 8 знаков после точки без лишних нулей. */
fun formatQty(value: Double): String = format("#,##0.########", value)

/** Случайный курс в рублях (10.00 .. 5000.00). Временная заглушка до подключения API в ПР 4. */
fun randomRate(): Double = kotlin.random.Random.nextInt(1_000, 500_000) / 100.0
