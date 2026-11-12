package com.example.wallet.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallet.data.Asset
import com.example.wallet.data.BuyResult
import com.example.wallet.data.WalletUiState
import com.example.wallet.ui.AppColors
import com.example.wallet.ui.components.BackButton
import com.example.wallet.ui.formatQty
import com.example.wallet.ui.formatRub
import com.example.wallet.ui.randomRate

private val SellRed = Color(0xFFC62828)

private fun String.toNumber(): Double? = replace(',', '.').trim().toDoubleOrNull()

private fun Double.plain(): String = formatQty(this).replace(" ", "")

/**
 * Экран 03 «Редактор».
 * assetId == -1 — покупка новой валюты (курс случайный, иконка случайная, баланс уменьшается).
 * Иначе — редактирование существующей записи: «Сохранить» (Update) и «Продать» (Delete + возврат рублей на баланс).
 */
@Composable
fun EditorScreen(
    assetId: Long,
    state: WalletUiState,
    onBack: () -> Unit,
    onSave: (Asset) -> Unit,
    onSell: (Long, (Double?) -> Unit) -> Unit,
    onBuy: (String, Double, Double, (BuyResult) -> Unit) -> Unit
) {
    val context = LocalContext.current
    val isNew = assetId == -1L
    val existing = state.items.find { it.id == assetId }

    var currency by remember(assetId) { mutableStateOf(existing?.name ?: "") }
    var symbol by remember(assetId) { mutableStateOf(existing?.symbol ?: "") }
    var amount by remember(assetId) { mutableStateOf(existing?.amount?.plain() ?: "") }
    var rate by remember(assetId) {
        mutableStateOf(existing?.rateToRub?.plain() ?: randomRate().plain())
    }
    var showSellDialog by remember { mutableStateOf(false) }

    fun toast(text: String) = Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

    val parsedAmount = amount.toNumber()
    val parsedRate = rate.toNumber()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .verticalScroll(rememberScrollState())
    ) {
        BackButton(onClick = onBack)

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            FieldLabel("Валюта")
            FormTextField(value = currency, onValueChange = { currency = it }, placeholder = "Название")

            Spacer(modifier = Modifier.height(16.dp))

            if (!isNew) {
                FieldLabel("Символ (необязательно)")
                FormTextField(
                    value = symbol,
                    onValueChange = { symbol = it.take(2) },
                    placeholder = "Например, ¥"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            FieldLabel("Сумма (количество единиц)")
            FormTextField(
                value = amount,
                onValueChange = { amount = it },
                placeholder = "Количество",
                keyboardType = KeyboardType.Decimal
            )

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Курс, ₽ за 1 единицу")
            if (isNew) {
                // При покупке курс случайный (до подключения API в ПР 4)
                FormTextField(
                    value = rate,
                    onValueChange = {},
                    placeholder = "Курс",
                    readOnly = true
                )
                TextButton(onClick = { rate = randomRate().plain() }) {
                    Text("Обновить курс", fontSize = 13.sp, color = AppColors.ButtonDark)
                }
            } else {
                FormTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    placeholder = "Курс",
                    keyboardType = KeyboardType.Decimal
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            FieldLabel("Стоимость в валюте пользователя")
            RateBox(
                rate = parsedRate,
                amount = parsedAmount,
                balance = if (isNew) state.balance else null
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isNew) {
                // ---------- Купить: Create + списание баланса ----------
                DarkButton(text = "Купить") {
                    when {
                        currency.isBlank() -> toast("Введите название валюты")
                        parsedAmount == null || parsedAmount <= 0 -> toast("Введите корректную сумму")
                        parsedRate == null || parsedRate <= 0 -> toast("Некорректный курс")
                        else -> onBuy(currency, parsedAmount, parsedRate) { result ->
                            when (result) {
                                is BuyResult.Success -> {
                                    toast("Куплено за ${formatRub(result.cost)}. Баланс: ${formatRub(result.newBalance)}")
                                    onBack()
                                }
                                is BuyResult.NotEnoughMoney ->
                                    toast("Недостаточно средств. Баланс: ${formatRub(result.balance)}")
                                BuyResult.Invalid -> toast("Не удалось выполнить покупку")
                            }
                        }
                    }
                }
            } else {
                // ---------- Сохранить: Update ----------
                DarkButton(text = "Сохранить") {
                    when {
                        currency.isBlank() -> toast("Введите название валюты")
                        parsedAmount == null || parsedAmount < 0 -> toast("Введите корректную сумму")
                        parsedRate == null || parsedRate <= 0 -> toast("Введите корректный курс")
                        else -> onSave(
                            Asset(
                                id = assetId,
                                symbol = symbol.ifBlank { currency.trim().first().uppercase() },
                                name = currency.trim(),
                                amount = parsedAmount,
                                rateToRub = parsedRate
                            )
                        )
                    }
                }

                if (existing != null) {
                    Spacer(modifier = Modifier.height(12.dp))

                    // ---------- Продать: Delete + рубли на баланс ----------
                    OutlinedButton(
                        onClick = { showSellDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(text = "Продать", fontSize = 14.sp, color = SellRed)
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showSellDialog && existing != null) {
        AlertDialog(
            onDismissRequest = { showSellDialog = false },
            title = { Text("Продать «${existing.name}»?") },
            text = { Text("Вы получите ${formatRub(existing.valueRub)} на баланс.") },
            confirmButton = {
                TextButton(onClick = {
                    showSellDialog = false
                    onSell(existing.id) { proceeds ->
                        if (proceeds != null) toast("Продано за ${formatRub(proceeds)}")
                        onBack()
                    }
                }) { Text("Продать", color = SellRed) }
            },
            dismissButton = {
                TextButton(onClick = { showSellDialog = false }) { Text("Отмена") }
            }
        )
    }
}

@Composable
private fun DarkButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(44.dp),
        shape = RoundedCornerShape(6.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = AppColors.ButtonDark,
            contentColor = Color.White
        )
    ) {
        Text(text = text, fontSize = 14.sp)
    }
}

@Composable
private fun FieldLabel(text: String) {
    Text(
        text = text,
        modifier = Modifier.padding(bottom = 6.dp),
        fontSize = 13.sp
    )
}

@Composable
private fun FormTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(text = placeholder, color = AppColors.Hint, fontSize = 14.sp) },
        singleLine = true,
        readOnly = readOnly,
        shape = RoundedCornerShape(6.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = AppColors.FieldBorder,
            focusedBorderColor = AppColors.ButtonDark
        )
    )
}

/** Блок со стоимостью (и балансом при покупке). Курс пока случайный или ручной, в ПР 4 придёт из API. */
@Composable
private fun RateBox(rate: Double?, amount: Double?, balance: Double?) {
    val total = if (rate != null && amount != null) formatRub(rate * amount) else "—"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, AppColors.FieldBorder, RoundedCornerShape(6.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Курс: 1 ед. = ${if (rate != null) formatRub(rate) else "—"}",
            fontSize = 12.sp,
            color = AppColors.Hint
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(text = "Стоимость в RUB: $total", fontSize = 12.sp, color = AppColors.Hint)
        if (balance != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = "Ваш баланс: ${formatRub(balance)}", fontSize = 12.sp, color = AppColors.Hint)
        }
    }
}
