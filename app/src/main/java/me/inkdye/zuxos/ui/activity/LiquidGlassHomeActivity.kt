package me.inkdye.zuxos.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.highcapable.yukihookapi.YukiHookAPI
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.R

class LiquidGlassHomeActivity : ComponentActivity() {

    @androidx.annotation.Keep
    fun isModuleActivated(): Boolean = false

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

private val BgGradientTop = Color(0xFFE8F5E9)
private val BgGradientMid = Color(0xFFE3F2FD)
private val BgGradientBottom = Color(0xFFF3E5F5)

private val GlassNavTint = Color.White.copy(alpha = 0.45f)

/* ── Feature Groups ─────────────────────────────── */
private data class FeatureGroup(
    val icon: String,
    val titleRes: Int,
    val items: List<Int>,
    val targetPackage: String? = null,
    val accentColor: Color = Color(0xFF3B82F6)
)

private val featureGroups = listOf(
    FeatureGroup("⚙", R.string.feature_group_system, listOf(
        R.string.feature_group_system_item1
    ), "com.android.settings", Color(0xFF94A3B8))
)

/* ── Navigation ─────────────────────────────────── */
private enum class NavPage(val labelRes: Int, val icon: String) {
    Home(R.string.nav_home, "⌂"),
    Features(R.string.nav_features, "≡"),
    Settings(R.string.nav_settings, "⚙"),
}

private const val TABLET_WIDTH_DP = 600

/* ── Main Composable ────────────────────────────── */
@Composable
private fun LiquidGlassHome() {
    var selectedTab by remember { mutableIntStateOf(0) }

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
                            stringResource(page.labelRes),
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
                            stringResource(page.labelRes),
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

/* ── Glass Card ─────────────────────────────────── */
@Composable
private fun GlassCard(
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
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
    val isXposedActive = YukiHookAPI.Status.isXposedModuleActive || (context as? LiquidGlassHomeActivity)?.isModuleActivated() == true

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(stringResource(R.string.page_home_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.page_home_subtitle), fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(8.dp))
        }

        item { ActivationStatusCard(isXposedActive) }
        item { ModuleInfoCard(isXposedActive) }
        item { DeviceInfoCard(context) }
    }
}

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
                    stringResource(if (isActive) R.string.home_status_active else R.string.home_status_inactive),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor
                )
            }

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(if (isActive) R.string.home_module_active else R.string.home_module_inactive),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(if (isActive) R.string.home_desc_active else R.string.home_desc_inactive),
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ModuleInfoCard(isActive: Boolean) {
    GlassCard {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.home_module_info), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(14.dp))

            InfoRow(stringResource(R.string.info_version), BuildConfig.VERSION_NAME)
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_package), BuildConfig.APPLICATION_ID)
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_sdk), "${android.os.Build.VERSION.SDK_INT}")
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_status), stringResource(if (isActive) R.string.info_active else R.string.info_inactive))
        }
    }
}

@Composable
private fun DeviceInfoCard(context: Context) {
    GlassCard {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.home_device_info), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
            Spacer(Modifier.height(14.dp))

            InfoRow(stringResource(R.string.info_brand), android.os.Build.BRAND)
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_model), android.os.Build.MODEL)
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_android), android.os.Build.VERSION.RELEASE)
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_sdk), "${android.os.Build.VERSION.SDK_INT}")
        }
    }
}

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
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 100.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                Text(stringResource(R.string.page_features_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.page_features_subtitle), fontSize = 14.sp, color = TextSecondary)
                Spacer(Modifier.height(16.dp))
            }
        }

        if (featureGroups.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_features_available), color = TextDim, fontSize = 14.sp)
                }
            }
        } else {
            items(featureGroups.size) { index ->
                val group = featureGroups[index]
                FeatureGroupCard(context, group) {
                    if (group.targetPackage == "com.android.settings") {
                        context.startActivity(Intent(context, SystemSettingsActivity::class.java))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureGroupCard(context: Context, group: FeatureGroup, onClick: () -> Unit = {}) {
    val appIcon = remember(group.targetPackage) {
        group.targetPackage?.let { pkg ->
            try {
                if (pkg == "android") null // 系统框架通常不显示图标
                else context.packageManager.getApplicationIcon(pkg)
            } catch (_: Exception) {
                null
            }
        }
    }

    GlassCard(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    ) {
        Column(
            Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (appIcon != null) {
                    Image(
                        bitmap = appIcon.toBitmap(96, 96).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(group.accentColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(group.icon, fontSize = 20.sp, color = Color.White)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    stringResource(group.titleRes),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = AccentBlue
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                group.items.forEach { itemRes ->
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", fontSize = 14.sp, color = TextSecondary, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            stringResource(itemRes),
                            fontSize = 13.sp,
                            color = TextSecondary,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }
    }
}

/* ── Page: Settings ─────────────────────────────── */
@Composable
private fun SettingsPage() {
    val context = LocalContext.current
    val settingsItems = listOf(
        Triple("⚙", R.string.settings_general, R.string.settings_general_desc),
        Triple("🎨", R.string.settings_appearance, R.string.settings_appearance_desc),
        Triple("📦", R.string.settings_export, R.string.settings_export_desc),
        Triple("ℹ", R.string.settings_about, R.string.settings_about_desc),
    )
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(stringResource(R.string.page_settings_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.page_settings_subtitle), fontSize = 14.sp, color = TextSecondary)
            Spacer(Modifier.height(16.dp))
        }
        items(settingsItems) { (icon, titleRes, descRes) ->
            SettingsCard(icon, titleRes, descRes)
        }
    }
}

@Composable
private fun SettingsCard(icon: String, titleRes: Int, descRes: Int) {
    GlassCard {
        Row(
            Modifier
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
                Text(stringResource(titleRes), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = TextPrimary)
                Spacer(Modifier.height(2.dp))
                Text(stringResource(descRes), fontSize = 12.sp, color = TextDim)
            }
        }
    }
}
