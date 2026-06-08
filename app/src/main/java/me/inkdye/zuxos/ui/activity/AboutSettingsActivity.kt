package me.inkdye.zuxos.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import kotlin.math.roundToInt
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.R
import me.inkdye.zuxos.ui.components.AppBackground
import me.inkdye.zuxos.ui.components.GlassCard
import me.inkdye.zuxos.ui.components.PageHeader
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

class AboutSettingsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AboutSettingsPage(onBack = { finish() })
        }
    }
}

@Composable
private fun AboutSettingsPage(onBack: () -> Unit) {
    val settings = rememberAppSettings()
    val cs = colorSchemeFor(
        resolveIsDark(settings.themeMode.value),
        titleTextContrast = settings.titleTextContrast.value,
        descriptionTextContrast = settings.descriptionTextContrast.value,
    )
    val context = androidx.compose.ui.platform.LocalContext.current

    AppBackground(colorScheme = cs) {
        val horizontalPadding = pageHorizontalPadding()
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                PageHeader(
                    title = stringResource(R.string.about_settings_title),
                    subtitle = "",
                    colorScheme = cs,
                    onBack = onBack,
                )
            }

            item {
                AboutHeroCard(
                    onGithubClick = { context.openUrl("https://github.com/MORANNLX") },
                    onCoolapkClick = { context.openUrl("https://www.coolapk.com/u/3473766") },
                    onTelegramClick = { context.openUrl("https://t.me/ZUXOS233") },
                )
            }

            item {
                SectionTitle(text = stringResource(R.string.about_acknowledgements))
            }

            acknowledgements.forEach { project ->
                item {
                    CreditCard(
                        project = project,
                        onClick = { context.openUrl(project.url) },
                    )
                }
            }
        }
    }
}

@Composable
private fun AboutHeroCard(
    onGithubClick: () -> Unit,
    onCoolapkClick: () -> Unit,
    onTelegramClick: () -> Unit,
) {
    val settings = rememberAppSettings()
    val context = LocalContext.current
    val density = LocalDensity.current
    val cs = colorSchemeFor(
        resolveIsDark(settings.themeMode.value),
        titleTextContrast = settings.titleTextContrast.value,
        descriptionTextContrast = settings.descriptionTextContrast.value,
    )
    val iconSize = 58.dp
    val iconBitmapSize = with(density) { iconSize.toPx().roundToInt().coerceAtLeast(1) }
    val appIcon = remember(iconBitmapSize) {
        runCatching { context.packageManager.getApplicationIcon(BuildConfig.APPLICATION_ID) }.getOrNull()
    }
    GlassCard(colorScheme = cs) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 26.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(82.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(cs.accentBlue.copy(alpha = 0.24f), cs.accentGreen.copy(alpha = 0.22f)),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon.toBitmap(iconBitmapSize, iconBitmapSize).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier.size(iconSize),
                    )
                } else {
                    Text(
                        text = "Z+",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = cs.textPrimary,
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.app_name),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = cs.textPrimary,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = stringResource(R.string.about_author_line, stringResource(R.string.about_author_name)),
                fontSize = 14.sp,
                color = cs.textSecondary,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.about_version_line, BuildConfig.VERSION_NAME),
                fontSize = 14.sp,
                color = cs.textSecondary,
            )
            Spacer(Modifier.height(14.dp))
            Text(
                text = stringResource(R.string.about_description),
                fontSize = 13.sp,
                lineHeight = 19.sp,
                color = cs.textDim,
            )
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SocialButton(iconRes = R.drawable.ic_about_github, onClick = onGithubClick)
                SocialButton(iconRes = R.drawable.ic_about_coolapk, onClick = onCoolapkClick)
                SocialButton(iconRes = R.drawable.ic_about_telegram, onClick = onTelegramClick)
            }
        }
    }
}

@Composable
private fun SocialButton(iconRes: Int, onClick: () -> Unit) {
    val settings = rememberAppSettings()
    val cs = colorSchemeFor(
        resolveIsDark(settings.themeMode.value),
        titleTextContrast = settings.titleTextContrast.value,
        descriptionTextContrast = settings.descriptionTextContrast.value,
    )
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(cs.accentBlue.copy(alpha = if (cs.isDark) 0.2f else 0.1f))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick,
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = androidx.compose.ui.graphics.Color.Unspecified,
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    val settings = rememberAppSettings()
    val cs = colorSchemeFor(
        resolveIsDark(settings.themeMode.value),
        titleTextContrast = settings.titleTextContrast.value,
        descriptionTextContrast = settings.descriptionTextContrast.value,
    )
    Text(
        text = text,
        modifier = Modifier.padding(top = 14.dp, bottom = 2.dp),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = cs.textPrimary,
    )
}

@Composable
private fun CreditCard(project: AcknowledgementProject, onClick: () -> Unit) {
    val settings = rememberAppSettings()
    val cs = colorSchemeFor(
        resolveIsDark(settings.themeMode.value),
        titleTextContrast = settings.titleTextContrast.value,
        descriptionTextContrast = settings.descriptionTextContrast.value,
    )
    GlassCard(
        colorScheme = cs,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(cs.accentGreen.copy(alpha = if (cs.isDark) 0.22f else 0.12f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = project.shortName,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = cs.textPrimary,
                )
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(project.name, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = cs.textPrimary)
                Text(stringResource(project.descriptionRes), fontSize = 12.sp, lineHeight = 17.sp, color = cs.textDim)
            }
        }
    }
}

private data class AcknowledgementProject(
    val name: String,
    val shortName: String,
    @param:StringRes val descriptionRes: Int,
    val url: String,
)

private val acknowledgements = listOf(
    AcknowledgementProject(
        name = "ZUX-ZTool",
        shortName = "Z",
        descriptionRes = R.string.about_credit_zux_ztool_desc,
        url = "https://github.com/qwqawa64/ZUX-ZTool.git",
    ),
    AcknowledgementProject(
        name = "CorePatch",
        shortName = "CP",
        descriptionRes = R.string.about_credit_corepatch_desc,
        url = "https://github.com/LSPosed/CorePatch.git",
    ),
    AcknowledgementProject(
        name = "YukiHookAPI",
        shortName = "YK",
        descriptionRes = R.string.about_credit_yukihookapi_desc,
        url = "https://github.com/HighCapable/YukiHookAPI.git",
    ),
)

private fun android.content.Context.openUrl(url: String) {
    runCatching {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }
}
