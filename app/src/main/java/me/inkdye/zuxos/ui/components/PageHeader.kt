package me.inkdye.zuxos.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.LightColorScheme

@Composable
fun PageHeader(
    title: String,
    subtitle: String,
    colorScheme: AppColorScheme = LightColorScheme,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(title, fontSize = 28.sp, fontWeight = FontWeight.Bold, color = colorScheme.textPrimary)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, fontSize = 14.sp, color = colorScheme.textSecondary)
        Spacer(Modifier.height(16.dp))
    }
}
