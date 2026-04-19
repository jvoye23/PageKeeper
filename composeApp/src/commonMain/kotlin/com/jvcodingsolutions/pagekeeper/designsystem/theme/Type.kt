package com.jvcodingsolutions.pagekeeper.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import org.jetbrains.compose.resources.Font
import pagekeeper.composeapp.generated.resources.Res
import pagekeeper.composeapp.generated.resources.inter_bold
import pagekeeper.composeapp.generated.resources.inter_medium
import pagekeeper.composeapp.generated.resources.inter_regular
import pagekeeper.composeapp.generated.resources.lora_bold
import pagekeeper.composeapp.generated.resources.lora_medium
import pagekeeper.composeapp.generated.resources.lora_regular

@Composable
fun InterFontFamily(): FontFamily = FontFamily(
    Font(Res.font.inter_regular, FontWeight.Normal),
    Font(Res.font.inter_medium, FontWeight.Medium),
    Font(Res.font.inter_bold, FontWeight.Bold),
)

@Composable
fun LoraFontFamily(): FontFamily = FontFamily(
    Font(Res.font.lora_regular, FontWeight.Normal),
    Font(Res.font.lora_medium, FontWeight.Medium),
    Font(Res.font.lora_bold, FontWeight.Bold),
)

@Composable
fun PageKeeperTypography(): Typography {
    val inter = InterFontFamily()
    val lora = LoraFontFamily()

    return Typography(
        // Title-L-Bold: Lora Bold 25/30 letterSpacing -1
        headlineMedium = TextStyle(
            fontFamily = lora,
            fontWeight = FontWeight.Bold,
            fontSize = 25.sp,
            lineHeight = 30.sp,
            letterSpacing = (-1).sp,
        ),
        // Title-M-Medium: Inter Medium 22/28 letterSpacing -1
        titleLarge = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Medium,
            fontSize = 22.sp,
            lineHeight = 28.sp,
            letterSpacing = (-1).sp,
        ),
        // Title-S-Medium: Lora Medium 17/20 letterSpacing -1
        titleMedium = TextStyle(
            fontFamily = lora,
            fontWeight = FontWeight.Medium,
            fontSize = 17.sp,
            lineHeight = 20.sp,
            letterSpacing = (-1).sp,
        ),
        // Body-L-Regular: Inter Regular 16/24
        bodyLarge = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.sp,
        ),
        // Body-M-Medium: Inter Medium 15/18
        bodyMedium = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Medium,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp,
        ),
        // Body-M-Regular: Inter Regular 15/18
        bodySmall = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 15.sp,
            lineHeight = 18.sp,
            letterSpacing = 0.sp,
        ),
        // Body-S-Regular: Inter Regular 13/16
        labelSmall = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp,
        ),
        // Navigation label: Inter Medium 14/16
        labelMedium = TextStyle(
            fontFamily = inter,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            lineHeight = 16.sp,
            letterSpacing = 0.sp,
        ),
    )
}
