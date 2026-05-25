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
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.colorControls
import com.kyant.backdrop.effects.lens
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
private val AccentPurple = Color(0xFF8B5CF6)
private val AccentRed = Color(0xFFEF4444)
private val SurfaceLight = Color(0xFFF5F5F5)
private val SurfaceCard = Color.White
private val TextPrimary = Color(0xFF111827)
private val TextSecondary = Color(0xFF6B7280)
private val TextDim = Color(0xFF9CA3AF)
private val BorderLight = Color(0xFFE5E7EB)
private val NavBg = Color.White

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

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val isTablet = maxWidth >= TABLET_WIDTH_DP.dp

        if (isTablet) {
            Row(Modifier.fillMaxSize().background(SurfaceLight).safeContentPadding()) {
                GlassNavigationRail(selectedTab) { selectedTab = it }
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) { page -> PageContent(page) }
            }
        } else {
            Column(Modifier.fillMaxSize().background(SurfaceLight).safeContentPadding()) {
                AnimatedContent(
                    targetState = selectedTab,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    modifier = Modifier.weight(1f).fillMaxWidth()
                ) { page -> PageContent(page) }
                GlassNavigationBar(selectedTab) { selectedTab = it }
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
private fun GlassNavigationBar(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val backdrop = rememberLayerBackdrop {
        drawRect(NavBg)
        drawContent()
    }
    Box(
        Modifier.fillMaxWidth()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp) },
                effects = { blur(12f.dp.toPx()); lens(8f.dp.toPx(), 16f.dp.toPx()) },
                onDrawSurface = { drawRect(NavBg); drawRect(Color.White.copy(alpha = 0.5f)) }
            )
    ) {
        NavigationBar(containerColor = Color.Transparent, contentColor = TextPrimary) {
            NavPage.entries.forEachIndexed { index, page ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    icon = { Text(page.icon, fontSize = 20.sp) },
                    label = { Text(page.label, fontSize = 11.sp, fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = AccentGreen, selectedTextColor = AccentGreen,
                        unselectedIconColor = TextDim, unselectedTextColor = TextDim,
                        indicatorColor = AccentGreen.copy(alpha = 0.1f),
                    ),
                )
            }
        }
    }
}

/* ── Glass NavigationRail (Tablet) ──────────────── */
@Composable
private fun GlassNavigationRail(selectedTab: Int, onTabSelected: (Int) -> Unit) {
    val backdrop = rememberLayerBackdrop {
        drawRect(NavBg)
        drawContent()
    }
    Box(
        Modifier.fillMaxHeight()
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(topEnd = 20.dp, bottomEnd = 20.dp) },
                effects = { blur(12f.dp.toPx()); lens(8f.dp.toPx(), 16f.dp.toPx()) },
                onDrawSurface = { drawRect(NavBg); drawRect(Color.White.copy(alpha = 0.5f)) }
            )
    ) {
        NavigationRail(
            containerColor = Color.Transparent,
            contentColor = TextPrimary,
            header = {
                Box(
                    Modifier.size(44.dp).background(
                        Brush.linearGradient(listOf(AccentGreen, AccentBlue)), RoundedCornerShape(12.dp)
                    ),
                    contentAlignment = Alignment.Center
                ) { Text("Z", fontWeight = FontWeight.ExtraBold, fontSize = 18.sp, color = Color.White) }
            },
        ) {
            NavPage.entries.forEachIndexed { index, page ->
                NavigationRailItem(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    icon = { Text(page.icon, fontSize = 22.sp) },
                    label = { Text(page.label, fontSize = 11.sp, fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal) },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = AccentGreen, selectedTextColor = AccentGreen,
                        unselectedIconColor = TextDim, unselectedTextColor = TextDim,
                        indicatorColor = AccentGreen.copy(alpha = 0.1f),
                    ),
                )
            }
        }
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
        // Title
        item {
            Text("ZUXOS+", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text("Xposed Module for OriginOS", fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
        }

        // Activation status card
        item { ActivationStatusCard(isXposedActive) }

        // Module info card
        item { ModuleInfoCard(isXposedActive) }

        // Device info card
        item { DeviceInfoCard(context) }
    }
}

/* ── LSPosed Activation Status Card ─────────────── */
@Composable
private fun ActivationStatusCard(isActive: Boolean) {
    val backdrop = rememberLayerBackdrop {
        drawRect(SurfaceCard)
        drawContent()
    }

    val statusColor = if (isActive) AccentGreen else AccentRed
    val statusBg = if (isActive) AccentGreen.copy(alpha = 0.08f) else AccentRed.copy(alpha = 0.06f)

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(20.dp) },
                effects = { blur(8f.dp.toPx()); lens(16f.dp.toPx(), 32f.dp.toPx()) },
                onDrawSurface = {
                    drawRect(statusBg)
                    drawRect(statusColor.copy(alpha = 0.15f))
                }
            )
            .padding(24.dp)
    ) {
        Column {
            // Status indicator
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

            // Status text
            Text(
                if (isActive) "Module Activated" else "Module Not Activated",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            // Description
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
    val backdrop = rememberLayerBackdrop {
        drawRect(SurfaceCard)
        drawContent()
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(16.dp) },
                effects = { blur(4f.dp.toPx()); lens(10f.dp.toPx(), 20f.dp.toPx()) },
                onDrawSurface = { drawRect(SurfaceCard) }
            )
            .padding(20.dp)
    ) {
        Column {
            Text("Module Info", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(14.dp))

            InfoRow("Version", BuildConfig.VERSION_NAME)
            Spacer(Modifier.height(10.dp))
            InfoRow("Package", BuildConfig.APPLICATION_ID)
            Spacer(Modifier.height(10.dp))
            InfoRow("Min SDK", "${android.os.Build.VERSION.SDK_INT}")
            Spacer(Modifier.height(10.dp))
            InfoRow("Status", if (isActive) "Active" else "Inactive")
        }
    }
}

/* ── Device Info Card ───────────────────────────── */
@Composable
private fun DeviceInfoCard(context: Context) {
    val backdrop = rememberLayerBackdrop {
        drawRect(SurfaceCard)
        drawContent()
    }

    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .drawBackdrop(
                backdrop = backdrop,
                shape = { RoundedCornerShape(16.dp) },
                effects = { blur(4f.dp.toPx()); lens(10f.dp.toPx(), 20f.dp.toPx()) },
                onDrawSurface = { drawRect(SurfaceCard) }
            )
            .padding(20.dp)
    ) {
        Column {
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
    val backdrop = rememberLayerBackdrop {
        drawRect(SurfaceCard)
        drawContent()
    }
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .drawBackdrop(backdrop = backdrop, shape = { RoundedCornerShape(16.dp) },
                effects = { blur(4f.dp.toPx()); lens(10f.dp.toPx(), 20f.dp.toPx()) },
                onDrawSurface = { drawRect(SurfaceCard) })
            .padding(16.dp)
    ) {
        Column {
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
    val backdrop = rememberLayerBackdrop {
        drawRect(SurfaceCard)
        drawContent()
    }
    Box(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
            .drawBackdrop(backdrop = backdrop, shape = { RoundedCornerShape(16.dp) },
                effects = { blur(4f.dp.toPx()); lens(10f.dp.toPx(), 20f.dp.toPx()) },
                onDrawSurface = { drawRect(SurfaceCard) })
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(44.dp).background(SurfaceLight, RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                Text(icon, fontSize = 20.sp)
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
