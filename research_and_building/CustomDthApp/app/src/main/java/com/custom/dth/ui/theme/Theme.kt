package com.custom.dth.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// ==========================================
// Colors
// ==========================================
object AppColors {
    val PrimaryBackground = Color(0xFF15181D)
    val SecondarySurface = Color(0xFF1C2026)
    val ElevatedSurface = Color(0xFF252A31)
    
    val Divider = Color(0xFF313842)
    
    val PrimaryText = Color(0xFFFFFFFF)
    val SecondaryText = Color(0xFFAEB5BF)
    val DisabledText = Color(0xFF6D737D)
    
    val AccentBlue = Color(0xFF2E8BFF)
    
    val FocusedSurface = Color(0xFFE5E7EB)
    val FocusedText = Color(0xFF111111)
}

// ==========================================
// Spacing
// ==========================================
object AppSpacing {
    val dp4 = 4.dp
    val dp8 = 8.dp
    val dp12 = 12.dp
    val dp16 = 16.dp
    val dp20 = 20.dp
    val dp24 = 24.dp
    val dp32 = 32.dp
}

// ==========================================
// Radius
// ==========================================
object AppRadius {
    val dp6 = 6.dp
    val dp8 = 8.dp
    val dp10 = 10.dp
}

// ==========================================
// Animation
// ==========================================
object AppAnimation {
    const val fast = 150
    const val normal = 180
}

// ==========================================
// Typography
// ==========================================
object AppTypography {
    val Display = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp,
        color = AppColors.PrimaryText
    )
    
    val Title = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        color = AppColors.PrimaryText
    )
    
    val Body = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        color = AppColors.PrimaryText
    )
    
    val Caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        color = AppColors.SecondaryText
    )
}
