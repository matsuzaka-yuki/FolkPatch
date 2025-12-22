package me.yuki.folk.ui.theme

import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun getCardElevation() = CardDefaults.cardElevation(
    defaultElevation = if (BackgroundConfig.isCustomBackgroundEnabled) 0.dp else 6.dp
)
