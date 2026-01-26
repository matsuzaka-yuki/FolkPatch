package me.bmax.apatch.util.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight

object AnsiUtils {
    private val ANSI_ESCAPE = Regex("\u001B\\[[;\\d?]*[A-Za-z]")

    fun parseAnsi(text: String): AnnotatedString {
        return buildAnnotatedString {
            var currentIndex = 0
            var currentColor = Color.Unspecified
            var isBold = false
            
            ANSI_ESCAPE.findAll(text).forEach { match ->
                if (match.range.first > currentIndex) {
                    append(text.substring(currentIndex, match.range.first))
                    addStyle(
                        SpanStyle(color = currentColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal),
                        length - (match.range.first - currentIndex),
                        length
                    )
                }
                
                // Parse codes
                // Only process color codes (ending in 'm')
                if (match.value.endsWith("m")) {
                    val content = match.value.drop(2).dropLast(1)
                    val codes = if (content.isEmpty()) emptyList() else content.split(";").mapNotNull { it.toIntOrNull() }
                    
                    if (codes.isEmpty()) {
                        // Reset
                        currentColor = Color.Unspecified
                        isBold = false
                    } else {
                        for (code in codes) {
                            when (code) {
                                0 -> { currentColor = Color.Unspecified; isBold = false }
                                1 -> isBold = true
                                in 30..37 -> currentColor = getAnsiColor(code - 30)
                                in 90..97 -> currentColor = getAnsiColor(code - 90, bright = true)
                                39 -> currentColor = Color.Unspecified
                                // Background colors (40-47, 100-107) are ignored for now to keep it simple
                            }
                        }
                    }
                }
                // Non-color codes (like [2J, [H) are effectively stripped by skipping them
                
                currentIndex = match.range.last + 1
            }
            if (currentIndex < text.length) {
                append(text.substring(currentIndex))
                val start = length - (text.length - currentIndex)
                if (start >= 0) {
                    addStyle(
                         SpanStyle(color = currentColor, fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal),
                         start,
                         length
                    )
                }
            }
        }
    }

    private fun getAnsiColor(index: Int, bright: Boolean = false): Color {
        return when (index) {
            0 -> if (bright) Color.Gray else Color.Black
            1 -> if (bright) Color(0xFFFF5555) else Color(0xFFAA0000) // Red
            2 -> if (bright) Color(0xFF55FF55) else Color(0xFF00AA00) // Green
            3 -> if (bright) Color(0xFFFFFF55) else Color(0xFFAA5500) // Yellow
            4 -> if (bright) Color(0xFF5555FF) else Color(0xFF0000AA) // Blue
            5 -> if (bright) Color(0xFFFF55FF) else Color(0xFFAA00AA) // Magenta
            6 -> if (bright) Color(0xFF55FFFF) else Color(0xFF00AAAA) // Cyan
            7 -> if (bright) Color.White else Color.LightGray
            else -> Color.Unspecified
        }
    }
}
