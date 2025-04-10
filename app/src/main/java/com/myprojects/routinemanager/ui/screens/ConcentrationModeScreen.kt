package com.myprojects.routinemanager.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

/**
 * Полноэкранное overlay для режима концентрации (секундомер).
 * Счетчик стартует с 0 и обновляется каждую минуту (без отображения секунд).
 * Системная кнопка «Назад» блокируется, а выйти можно только нажатием кнопки «Выключить режим».
 *
 * @param onDisable Вызывается при нажатии кнопки «Выключить режим».
 */
@Composable
fun ConcentrationModeScreen(
    onDisable: () -> Unit
) {
    // Счетчик времени в миллисекундах (старт с 0)
    var elapsedMs by remember { mutableStateOf(0L) }

    // Каждую минуту увеличиваем счетчик на 60,000 мс (1 минута)
    LaunchedEffect(Unit) {
        while (true) {
            delay(60_000)
            elapsedMs += 60_000
        }
    }

    // Блокируем нажатие кнопки "Назад"
    BackHandler(enabled = true) { /* Ничего не делаем */ }

    // Переводим миллисекунды в часы и минуты
    val hours = TimeUnit.MILLISECONDS.toHours(elapsedMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedMs) % 60

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xAA000000)),  // полупрозрачный затемнённый фон
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Заголовок
            Text(
                text = "Режим концентрации",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White
            )
            // Секундомер (только часы и минуты)
            Text(
                text = "Прошло: %02d:%02d".format(hours, minutes),
                style = MaterialTheme.typography.displaySmall,
                color = Color.White
            )
            // Кнопка "Выключить режим" с цветом из secondary (не фиолетовым)
            ElevatedButton(
                onClick = onDisable,
                colors = ButtonDefaults.elevatedButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Выключить режим",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White
                )
            }
        }
    }
}
