package com.example.letscontinue.ui.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.letscontinue.R

@Composable
fun AuthBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color.Black else Color.White)
    ) {
        Image(
            painter = painterResource(
                id = if (isDark) R.drawable.bg_dark_png
                else R.drawable.gemini_generated_image_en7l66en7l66en7l
            ),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark)
                        Color.Black.copy(alpha = 0.6f)  // dark fade
                    else
                        Color.White.copy(alpha = 0.4f)  // light fade
                )
        )
        Box(
            modifier = Modifier.fillMaxSize(),
            content = content,
            contentAlignment = Alignment.Center
        )
    }
}