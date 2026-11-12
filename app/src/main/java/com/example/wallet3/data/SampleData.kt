package com.example.wallet.data

/**
 * Метка пункта обмена на карте.
 * fx и fy — положение метки как доля ширины и высоты экрана (0..1).
 * В практической работе 4 вместо них появятся настоящие координаты.
 */
data class ExchangePoint(
    val fx: Float,
    val fy: Float
)

/** Статические данные, которые пока не хранятся в БД (карта и настройки). */
object SampleData {
    val exchangePoints = listOf(
        ExchangePoint(fx = 0.265f, fy = 0.456f),
        ExchangePoint(fx = 0.755f, fy = 0.415f),
        ExchangePoint(fx = 0.230f, fy = 0.623f)
    )

    val mainCurrencies = listOf("RUB", "USD", "EUR")
}
