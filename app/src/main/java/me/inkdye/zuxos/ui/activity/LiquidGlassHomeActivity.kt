package me.inkdye.zuxos.ui.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.highcapable.yukihookapi.YukiHookAPI
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import me.inkdye.zuxos.BuildConfig

class LiquidGlassHomeActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LiquidGlassHome()
        }
    }
}

/* ── Colors ─────────────────────────────────────── */
private val AccentGreen = Color(0xFF22C55E)
private val AccentBlue = Color(0xFF3B82F6)
private val AccentRed = Color(0xFFEF4444)
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val TextDim = Color(0xFF9CA3AF)

/* Background gradient — gives the backdrop something rich to refract */
private val BgGradientTop = Color(0xFFE8F5E9)
private val BgGradientMid = Color(0xFFE3F2FD)
private val BgGradientBottom = Color(0xFFF3E5F5)

/* Glass card styling — white with inner highlight gradient for depth */
private val CardBorder = Color.White
private val GlassNavTint = Color.White.copy(alpha = 0.45f)

/* ── Navigation ─────────────────────────────────── */
private enum class NavPage(val label: String, val icon: String) {
    Home("Home", "⌂"),
    Features("Features", "≡"),
    Settings("Settings", "⚙"),
}

private const val TABLET_WIDTH_DP = 600

/* ── Main Composable ────────────────────────────── */
@Composable
private fun LiquidGlassHome() {
    var selectedTab by remember { mutableIntStateOf(0) }

    // Backdrop captures the gradient + page content for glass nav to refract
    val backdrop = rememberLayerBackdrop {
        drawRect(Brush.verticalGradient(listOf(BgGradientTop, BgGradientMid, BgGradientBottom)))
        drawContent()
    }

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= TABLET_WIDTH_DP.dp

        if (isTablet) {
            Row(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(BgGradientTop, BgGradientMid, BgGradientBottom)))
                    .safeContentPadding()
            ) {
                GlassNavigationRail(selectedTab, backdrop) { selectedTab = it }
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .layerBackdrop(backdrop)
                ) { page -> PageContent(page) }
            }
        } else {
            Column(
                Modifier
                    .fillMaxSize()
                    .background(Brush.verticalGradient(listOf(BgGradientTop, BgGradientMid, BgGradientBottom)))
                    .safeContentPadding()
            ) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .layerBackdrop(backdrop)
                ) { page -> PageContent(page) }
                GlassNavigationBar(selectedTab, backdrop) { selectedTab = it }
            }
        }
    }
}

@Composable
private fun PageContent(page: Int) {
    when (page) {
        0 -> HomePage()
        1 -> FeaturesPage()
        2 -> SettingsPage()
    }
}

/* ── Glass Bottom NavigationBar (Phone) ─────────── */
@Composable
private fun GlassNavigationBar(
    selectedTab: Int,
    backdrop: Backdrop,
    onTabSelected: (Int) -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp) },
                effects = {
                    vibrancy()
                    blur(20f.dp.toPx())
                    lens(10f.dp.toPx(), 20f.dp.toPx())
                },
                onDrawSurface = { drawRect(GlassNavTint) }
            )
    ) {
        NavigationBar(containerColor = Color.Transparent, contentColor = TextPrimary) {
            NavPage.entries.forEachIndexed { index, page ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    icon = { Text(page.icon, fontSize = 20.sp) },
                    label = {
                        Text(
                            page.label,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentGreen,
                        selectedTextColor = AccentGreen,
                        unselectedIconColor = TextDim,
                        unselectedTextColor = TextDim,
                        indicatorColor = AccentGreen.copy(alpha = 0.12f),
                    ),
                )
            }
        }
    }
}

/* ── Glass NavigationRail (Tablet) ──────────────── */
@Composable
private fun GlassNavigationRail(
    selectedTab: Int,
    backdrop: Backdrop,
    onTabSelected: (Int) -> Unit
) {
    Box(
        Modifier
            .fillMaxHeight()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp) },
                effects = {
                    vibrancy()
                    blur(20f.dp.toPx())
                    lens(10f.dp.toPx(), 20f.dp.toPx())
                },
                onDrawSurface = { drawRect(GlassNavTint) }
            )
    ) {
        NavigationRail(
            containerColor = Color.Transparent,
            contentColor = TextPrimary,
            header = {
                Box(
                    Modifier
                        .size(44.dp)
                        .background(
                            Brush.linearGradient(listOf(AccentGreen, AccentBlue)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Z",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            },
        ) {
            NavPage.entries.forEachIndexed { index, page ->
                NavigationRailItem(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    icon = { Text(page.icon, fontSize = 22.sp) },
                    label = {
                        Text(
                            page.label,
                            fontSize = 11.sp,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = AccentGreen,
                        selectedTextColor = AccentGreen,
                        unselectedIconColor = TextDim,
                        unselectedTextColor = TextDim,
                        indicatorColor = AccentGreen.copy(alpha = 0.12f),
                    ),
                )
            }
        }
    }
}

/* ── Glass Card — layered for depth ─────────────── */
@Composable
private fun GlassCard(
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .clip(shape)
            .background(Color.White, shape)
    ) {
        content()
    }
}

/* ── Page: Home ─────────────────────────────────── */
@Composable
private fun HomePage() {
    val context = LocalContext.current
    val isXposedActive = YukiHookAPI.Status.isXposedModuleActive || isModuleActivated()

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("ZUXOS+", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Xposed Module for ZUXOS", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
        }

        item { ActivationStatusCard(isXposedActive) }
        item { ModuleInfoCard(isXposedActive) }
        item { DeviceInfoCard(context) }
    }
}

/* ── LSPosed Activation Status Card ─────────────── */
@Composable
private fun ActivationStatusCard(isActive: Boolean) {
    val statusColor = if (isActive) AccentGreen else AccentRed
    val shape = RoundedCornerShape(20.dp)
    val cardBg = Brush.verticalGradient(
        listOf(
            statusColor.copy(alpha = 0.12f),
            statusColor.copy(alpha = 0.04f),
        )
    )

    Box(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cardBg, shape)
            .padding(24.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(12.dp)
                        .background(statusColor, CircleShape)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    if (isActive) "LSPosed Active" else "LSPosed Not Active",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                if (isActive) "Module Activated" else "Module Not Activated",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                if (isActive)
                    "The module is running under LSPosed. All hook features are available."
                else
                    "Please activate this module in LSPosed Manager and reboot your device.",
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}

/* ── Module Info Card ───────────────────────────── */
@Composable
private fun ModuleInfoCard(isActive: Boolean) {
    GlassCard {
        Column(Modifier.padding(20.dp)) {
            Text("Module Info", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(14.dp))

            InfoRow("Version", BuildConfig.VERSION_NAME)
            Spacer(Modifier.height(10.dp))
            InfoRow("Package", BuildConfig.APPLICATION_ID)
            Spacer(Modifier.height(10.dp))
            InfoRow("SDK", "${android.os.Build.VERSION.SDK_INT}")
            Spacer(Modifier.height(10.dp))
            InfoRow("Status", if (isActive) "Active" else "Inactive")
        }
    }
}

/* ── Device Info Card ───────────────────────────── */
@Composable
private fun DeviceInfoCard(context: Context) {
    GlassCard {
        Column(Modifier.padding(20.dp)) {
            Text("Device Info", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(14.dp))

            InfoRow("Brand", android.os.Build.BRAND)
            Spacer(Modifier.height(10.dp))
            InfoRow("Model", android.os.Build.MODEL)
            Spacer(Modifier.height(10.dp))
            InfoRow("Android", android.os.Build.VERSION.RELEASE)
            Spacer(Modifier.height(10.dp))
            InfoRow("SDK", "${android.os.Build.VERSION.SDK_INT}")
        }
    }
}

/* ── Info Row ───────────────────────────────────── */
@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = TextSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = TextPrimary)
    }
}

/* ── Page: Features ─────────────────────────────── */
@Composable
private fun FeaturesPage() {
    val featureGroups = listOf(
        "Theme" to listOf("VIP theme unlock", "Wallpaper bypass"),
        "Installer" to listOf("Skip virus scan", "ADB auto-install", "Skip password", "Block low-version check"),
        "Display" to listOf("120Hz / 144Hz unlock", "Extended lock timeout"),
        "Desktop" to listOf("Dock bar customization", "5x6 / 5x7 grid layout"),
        "Audio" to listOf("VAFX framework", "Spatial audio", "Holographic audio", "Dolby Atmos"),
        "GameBox" to listOf("Graphics boost", "Touch enhancement", "4D haptic", "Game assistant"),
        "Framework" to listOf("Force screenshot allowed"),
    )

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Features", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("All available system enhancements", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
        }
        items(featureGroups) { (group, items) -> FeatureGroupCard(group, items) }
    }
}

@Composable
private fun FeatureGroupCard(group: String, items: List<String>) {
    GlassCard {
        Column(Modifier.padding(16.dp)) {
            Text(group, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AccentBlue)
            Spacer(Modifier.height(10.dp))
            items.forEach { item ->
                Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(4.dp).background(AccentBlue.copy(alpha = 0.5f), CircleShape))
                    Spacer(Modifier.width(10.dp))
                    Text(item, fontSize = 13.sp, color = TextSecondary)
                }
            }
        }
    }
}

/* ── Page: Settings ─────────────────────────────── */
@Composable
private fun SettingsPage() {
    val settingsItems = listOf(
        Triple("⚙", "General", "Module preferences and defaults"),
        Triple("🎨", "Appearance", "Theme and color settings"),
        Triple("📦", "Export / Import", "Backup and restore configurations"),
        Triple("ℹ", "About", "Version info and credits"),
    )
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("Settings", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Configure module behavior", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
        }
        items(settingsItems) { (icon, title, desc) -> SettingsCard(icon, title, desc) }
    }
}

@Composable
private fun SettingsCard(icon: String, title: String, description: String) {
    GlassCard {
        Row(
            Modifier
                .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(AccentBlue.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 16.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(description, fontSize = 12.sp, color = TextDim)
            }
        }
    }
}

/* ── Helper ─────────────────────────────────────── */
@androidx.annotation.Keep
private fun isModuleActivated(): Boolean = false
