package com.example.wallet.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.wallet3.R
import com.example.wallet.data.SampleData
import com.example.wallet.ui.AppColors
import com.example.wallet.ui.components.BackButton


@Composable
fun MapScreen(onBack: () -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val screenHeight = maxHeight

        Image(
            painter = painterResource(id = R.drawable.map_krasnoyarsk),
            contentDescription = "Карта Красноярска",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Метки пунктов обмена
        SampleData.exchangePoints.forEach { point ->
            Box(
                modifier = Modifier
                    .offset(
                        x = screenWidth * point.fx - 11.dp,
                        y = screenHeight * point.fy - 11.dp
                    )
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(AppColors.MarkerGreen)
                    .border(1.dp, Color.Black, CircleShape)
            )
        }

        // Строка «Пункты обмена» (в ПР 4 станет полем поиска)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = screenHeight * 0.19f)
                .fillMaxWidth(0.55f)
                .height(36.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(AppColors.MapLabelBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Пункты обмена", fontSize = 13.sp)
        }

        BackButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
        )
    }
}
