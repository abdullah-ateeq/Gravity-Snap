package com.example.gravitysnap.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gravitysnap.ui.theme.GravitySnapTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    isDanger: Boolean = true,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val palette = GravitySnapTheme.colors

    BasicAlertDialog(onDismissRequest = onDismiss) {
        GlassCard(
            cornerRadius = 24.dp,
            borderColor = if (isDanger) palette.hazardRed else palette.primaryCyan,
            borderWidth = 1.5.dp,
            padding = 24.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = title,
                    color = if (isDanger) palette.hazardRed else palette.textPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = message,
                    color = palette.textSecondary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    PremiumButton(
                        text = cancelText,
                        onClick = onDismiss,
                        isPrimary = false,
                        height = 46.dp,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    PremiumButton(
                        text = confirmText,
                        onClick = onConfirm,
                        isPrimary = true,
                        danger = isDanger,
                        height = 46.dp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
