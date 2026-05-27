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
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
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
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.theme.AppColorScheme
import me.inkdye.zuxos.ui.theme.colorSchemeFor
import me.inkdye.zuxos.ui.theme.resolveIsDark

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

private fun glassNavTint(isDark: Boolean) =
    if (isDark) Color.Black.copy(alpha = 0.3f) else Color.White.copy(alpha = 0.5f)

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
private const val MEDIUM_WIDTH_DP = 840

/* ── Main Composable ────────────────────────────── */
@Composable
private fun LiquidGlassHome() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val settings = rememberAppSettings()
    val floatingNav = settings.floatingNavEnabled.value
    val liquidGlass = settings.liquidGlassEnabled.value
    val cs = colorSchemeFor(resolveIsDark(settings.themeMode.value))
    val navTint = glassNavTint(cs.isDark)

    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(cs.bgGradientTop, cs.bgGradientMid, cs.bgGradientBottom)))
    ) {
        val isPhone = maxWidth < TABLET_WIDTH_DP.dp
        val isCompactTablet = maxWidth >= TABLET_WIDTH_DP.dp && maxWidth < MEDIUM_WIDTH_DP.dp

        val backdrop = rememberLayerBackdrop {
            drawRect(Brush.verticalGradient(listOf(cs.bgGradientTop, cs.bgGradientMid, cs.bgGradientBottom)))
            drawContent()
        }

        when {
            isPhone -> {
                Box(Modifier.fillMaxSize().safeContentPadding()) {
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(backdrop)
                    ) { page -> PageContent(page, cs) }

                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = if (floatingNav) 28.dp else 0.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        FloatingNavPill(
                            selectedTab = selectedTab,
                            backdrop = backdrop,
                            floatingNav = floatingNav,
                            liquidGlass = liquidGlass,
                            cs = cs,
                            navTint = navTint,
                            onTabSelected = { selectedTab = it }
                        )
                    }
                }
            }
            isCompactTablet -> {
                Row(
                    Modifier
                        .fillMaxSize()
                        .safeContentPadding()
                ) {
                    GlassNavigationRail(
                        selectedTab = selectedTab,
                        backdrop = backdrop,
                        floatingNav = floatingNav,
                        liquidGlass = liquidGlass,
                        isCompactTablet = true,
                        cs = cs,
                        navTint = navTint,
                        onTabSelected = { selectedTab = it }
                    )
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .layerBackdrop(backdrop)
                    ) { page -> PageContent(page, cs) }
                }
            }
            else -> {
                Row(
                    Modifier
                        .fillMaxSize()
                        .safeContentPadding()
                ) {
                    GlassNavigationRail(
                        selectedTab = selectedTab,
                        backdrop = backdrop,
                        floatingNav = floatingNav,
                        liquidGlass = liquidGlass,
                        isCompactTablet = false,
                        cs = cs,
                        navTint = navTint,
                        onTabSelected = { selectedTab = it }
                    )
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .layerBackdrop(backdrop)
                    ) { page -> PageContent(page, cs) }
                }
            }
        }
    }
}

@Composable
private fun PageContent(page: Int, cs: AppColorScheme) {
    when (page) {
        0 -> HomePage(cs)
        1 -> FeaturesPage(cs)
        2 -> SettingsPage(cs)
    }
}

/* ── Glass Bottom NavigationBar (Phone) ─────────── */
@Composable
private fun GlassNavigationBar(
    selectedTab: Int,
    backdrop: Backdrop,
    floatingNav: Boolean,
    liquidGlass: Boolean,
    compact: Boolean,
    cs: AppColorScheme,
    navTint: Color,
    onTabSelected: (Int) -> Unit
) {
    val cornerRadius = if (compact) 20.dp else 24.dp
    val floatPadH = if (compact) 8.dp else 12.dp
    val floatPadV = if (compact) 4.dp else 8.dp
    val iconSize = if (compact) 16.sp else 20.sp
    val labelSize = if (compact) 10.sp else 11.sp

    val navShape = RoundedCornerShape(
        topStart = cornerRadius, topEnd = cornerRadius,
        bottomStart = if (floatingNav) cornerRadius else 0.dp,
        bottomEnd = if (floatingNav) cornerRadius else 0.dp,
    )
    val baseModifier = Modifier
        .fillMaxWidth()
        .then(if (compact) Modifier.height(64.dp) else Modifier)
        .then(if (floatingNav) Modifier.padding(horizontal = floatPadH, vertical = floatPadV) else Modifier)

    val navModifier = if (liquidGlass) {
        baseModifier.drawBackdrop(
            backdrop = backdrop,
            shape = { navShape },
            effects = {
                vibrancy()
                blur(4f.dp.toPx())
                lens(16f.dp.toPx(), 32f.dp.toPx())
            },
            onDrawSurface = { drawRect(navTint) }
        )
    } else {
        baseModifier
            .clip(navShape)
            .background(navTint, navShape)
    }

    Box(navModifier) {
        NavigationBar(containerColor = Color.Transparent, contentColor = cs.textPrimary) {
            NavPage.entries.forEachIndexed { index, page ->
                NavigationBarItem(
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    icon = { Text(page.icon, fontSize = iconSize) },
                    label = {
                        Text(
                            stringResource(page.labelRes),
                            fontSize = labelSize,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = cs.accentGreen,
                        selectedTextColor = cs.accentGreen,
                        unselectedIconColor = cs.textDim,
                        unselectedTextColor = cs.textDim,
                        indicatorColor = cs.accentGreen.copy(alpha = 0.12f),
                    ),
                )
            }
        }
    }
}

/* ── Floating Pill Navigation (Phone) ───────────── */
@Composable
private fun FloatingNavPill(
    selectedTab: Int,
    backdrop: Backdrop,
    floatingNav: Boolean,
    liquidGlass: Boolean,
    cs: AppColorScheme,
    navTint: Color,
    onTabSelected: (Int) -> Unit
) {
    val pillShape = RoundedCornerShape(28.dp)
    val pillHeight = 52.dp
    val iconSize = 22.sp

    val pillModifier = if (floatingNav) {
        val base = Modifier
        if (liquidGlass) {
            base.drawBackdrop(
                backdrop = backdrop,
                shape = { pillShape },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(16f.dp.toPx(), 32f.dp.toPx())
                },
                onDrawSurface = { drawRect(navTint) }
            )
        } else {
            base.clip(pillShape).background(navTint, pillShape)
        }
    } else {
        val base = Modifier
        if (liquidGlass) {
            base.drawBackdrop(
                backdrop = backdrop,
                shape = { pillShape },
                effects = {
                    vibrancy()
                    blur(4f.dp.toPx())
                    lens(16f.dp.toPx(), 32f.dp.toPx())
                },
                onDrawSurface = { drawRect(navTint) }
            )
        } else {
            base.clip(pillShape).background(navTint, pillShape)
        }
    }

    Box(pillModifier.height(pillHeight).padding(horizontal = 8.dp)) {
        Row(
            Modifier
                .fillMaxHeight()
                .padding(horizontal = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavPage.entries.forEachIndexed { index, page ->
                val selected = selectedTab == index
                val itemBg = if (selected) cs.accentGreen.copy(alpha = 0.12f) else Color.Transparent
                val itemShape = RoundedCornerShape(20.dp)

                Box(
                    Modifier
                        .size(44.dp)
                        .clip(itemShape)
                        .background(itemBg, itemShape)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ) { onTabSelected(index) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = page.icon,
                        fontSize = iconSize,
                        color = if (selected) cs.accentGreen else cs.textDim
                    )
                }
            }
        }
    }
}

/* ── Glass NavigationRail (Tablet) ──────────────── */
@Composable
private fun GlassNavigationRail(
    selectedTab: Int,
    backdrop: Backdrop,
    floatingNav: Boolean,
    liquidGlass: Boolean,
    isCompactTablet: Boolean,
    cs: AppColorScheme,
    navTint: Color,
    onTabSelected: (Int) -> Unit
) {
    val railShape = RoundedCornerShape(
        topStart = if (floatingNav) 24.dp else 0.dp,
        topEnd = 24.dp,
        bottomStart = if (floatingNav) 24.dp else 0.dp,
        bottomEnd = 24.dp,
    )
    val baseModifier = if (floatingNav) {
        Modifier
            .wrapContentWidth()
            .wrapContentHeight(Alignment.CenterVertically)
    } else {
        Modifier.fillMaxHeight()
    }

    val railModifier = if (liquidGlass) {
        baseModifier.drawBackdrop(
            backdrop = backdrop,
            shape = { railShape },
            effects = {
                vibrancy()
                blur(4f.dp.toPx())
                lens(16f.dp.toPx(), 32f.dp.toPx())
            },
            onDrawSurface = { drawRect(navTint) }
        )
    } else {
        baseModifier
            .clip(railShape)
            .background(navTint, railShape)
    }

    val iconSize = if (floatingNav) 22.sp else if (isCompactTablet) 18.sp else 22.sp
    val labelSize = if (isCompactTablet) 10.sp else 11.sp
    val headerSize = if (isCompactTablet) 36.dp else 44.dp
    val headerFontSize = if (isCompactTablet) 15.sp else 18.sp

    Box(railModifier) {
        if (floatingNav) {
            Column(
                Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    Modifier
                        .size(headerSize)
                        .background(
                            Brush.linearGradient(listOf(AccentGreen, AccentBlue)),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Z",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = headerFontSize,
                        color = Color.White
                    )
                }

                NavPage.entries.forEachIndexed { index, page ->
                    val selected = selectedTab == index
                    val itemBg = if (selected) cs.accentGreen.copy(alpha = 0.15f) else Color.Transparent
                    val itemShape = RoundedCornerShape(18.dp)

                    Box(
                        Modifier
                            .size(46.dp)
                            .clip(itemShape)
                            .background(itemBg, itemShape)
                            .clickable(
                                indication = null,
                                interactionSource = remember { MutableInteractionSource() }
                            ) { onTabSelected(index) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = page.icon,
                            fontSize = iconSize,
                            color = if (selected) cs.accentGreen else cs.textDim
                        )
                    }
                }
            }
        } else {
            NavigationRail(
                containerColor = Color.Transparent,
                contentColor = cs.textPrimary,
                header = {
                    Box(
                        Modifier
                            .size(headerSize)
                            .background(
                                Brush.linearGradient(listOf(AccentGreen, AccentBlue)),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "Z",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = headerFontSize,
                            color = Color.White
                        )
                    }
                },
            ) {
                NavPage.entries.forEachIndexed { index, page ->
                    NavigationRailItem(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        icon = { Text(page.icon, fontSize = iconSize) },
                        label = {
                            Text(
                                stringResource(page.labelRes),
                                fontSize = labelSize,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationRailItemDefaults.colors(
                            selectedIconColor = cs.accentGreen,
                            selectedTextColor = cs.accentGreen,
                            unselectedIconColor = cs.textDim,
                            unselectedTextColor = cs.textDim,
                            indicatorColor = cs.accentGreen.copy(alpha = 0.12f),
                        ),
                    )
                }
            }
        }
    }
}

/* ── Glass Card ─────────────────────────────────── */
@Composable
private fun GlassCard(
    cs: AppColorScheme,
    shape: RoundedCornerShape = RoundedCornerShape(16.dp),
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(
        modifier
            .fillMaxWidth()
            .clip(shape)
            .background(cs.cardBackground, shape)
    ) {
        content()
    }
}

/* ── Page: Home ─────────────────────────────────── */
@Composable
private fun HomePage(cs: AppColorScheme) {
    val context = LocalContext.current
    val isXposedActive = YukiHookAPI.Status.isXposedModuleActive || (context as? LiquidGlassHomeActivity)?.isModuleActivated() == true

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(stringResource(R.string.page_home_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cs.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.page_home_subtitle), fontSize = 14.sp, color = cs.textSecondary)
            Spacer(Modifier.height(8.dp))
        }

        item { ActivationStatusCard(isXposedActive, cs) }
        item { ModuleInfoCard(isXposedActive, cs) }
        item { DeviceInfoCard(context, cs) }
    }
}

@Composable
private fun ActivationStatusCard(isActive: Boolean, cs: AppColorScheme) {
    val statusColor = if (isActive) cs.accentGreen else cs.accentRed
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
                color = cs.textPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(if (isActive) R.string.home_desc_active else R.string.home_desc_inactive),
                fontSize = 14.sp,
                color = cs.textSecondary,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
private fun ModuleInfoCard(isActive: Boolean, cs: AppColorScheme) {
    GlassCard(cs) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.home_module_info), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = cs.textPrimary)
            Spacer(Modifier.height(14.dp))

            InfoRow(stringResource(R.string.info_version), BuildConfig.VERSION_NAME, cs)
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_package), BuildConfig.APPLICATION_ID, cs)
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_sdk), "${android.os.Build.VERSION.SDK_INT}", cs)
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_status), stringResource(if (isActive) R.string.info_active else R.string.info_inactive), cs)
        }
    }
}

@Composable
private fun DeviceInfoCard(context: Context, cs: AppColorScheme) {
    GlassCard(cs) {
        Column(Modifier.padding(20.dp)) {
            Text(stringResource(R.string.home_device_info), fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = cs.textPrimary)
            Spacer(Modifier.height(14.dp))

            InfoRow(stringResource(R.string.info_brand), android.os.Build.BRAND, cs)
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_model), android.os.Build.MODEL, cs)
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_android), android.os.Build.VERSION.RELEASE, cs)
            Spacer(Modifier.height(10.dp))
            InfoRow(stringResource(R.string.info_sdk), "${android.os.Build.VERSION.SDK_INT}", cs)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, cs: AppColorScheme) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(label, fontSize = 14.sp, color = cs.textSecondary)
        Text(value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = cs.textPrimary)
    }
}

/* ── Page: Features ─────────────────────────────── */
@Composable
private fun FeaturesPage(cs: AppColorScheme) {
    val context = LocalContext.current

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(2) }) {
            Column {
                Text(stringResource(R.string.page_features_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cs.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.page_features_subtitle), fontSize = 14.sp, color = cs.textSecondary)
                Spacer(Modifier.height(16.dp))
            }
        }

        if (featureGroups.isEmpty()) {
            item(span = { GridItemSpan(2) }) {
                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_features_available), color = cs.textDim, fontSize = 14.sp)
                }
            }
        } else {
            items(featureGroups.size) { index ->
                val group = featureGroups[index]
                FeatureGroupCard(context, group, cs) {
                    if (group.targetPackage == "com.android.settings") {
                        context.startActivity(Intent(context, SystemSettingsActivity::class.java))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureGroupCard(context: Context, group: FeatureGroup, cs: AppColorScheme, onClick: () -> Unit = {}) {
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
        cs = cs,
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
                    color = cs.accentBlue
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                group.items.forEach { itemRes ->
                    Row(
                        verticalAlignment = Alignment.Top
                    ) {
                        Text("•", fontSize = 14.sp, color = cs.textSecondary, modifier = Modifier.padding(end = 8.dp))
                        Text(
                            stringResource(itemRes),
                            fontSize = 13.sp,
                            color = cs.textSecondary,
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
private fun SettingsPage(cs: AppColorScheme) {
    val context = LocalContext.current
    val settingsItems = listOf(
        Triple("⚙", R.string.settings_general, R.string.settings_general_desc),
        Triple("🎨", R.string.settings_appearance, R.string.settings_appearance_desc),
        Triple("📦", R.string.settings_export, R.string.settings_export_desc),
        Triple("ℹ", R.string.settings_about, R.string.settings_about_desc),
    )
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = 20.dp),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text(stringResource(R.string.page_settings_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cs.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.page_settings_subtitle), fontSize = 14.sp, color = cs.textSecondary)
            Spacer(Modifier.height(16.dp))
        }
        items(settingsItems) { (icon, titleRes, descRes) ->
            SettingsCard(icon, titleRes, descRes, cs) {
                when (titleRes) {
                    R.string.settings_appearance -> {
                        context.startActivity(Intent(context, AppearanceSettingsActivity::class.java))
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsCard(icon: String, titleRes: Int, descRes: Int, cs: AppColorScheme, onClick: () -> Unit = {}) {
    GlassCard(
        cs = cs,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() }
        ) { onClick() }
    ) {
        Row(
            Modifier
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier
                    .size(36.dp)
                    .background(cs.accentBlue.copy(alpha = if (cs.isDark) 0.18f else 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(icon, fontSize = 16.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(stringResource(titleRes), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = cs.textPrimary)
                Spacer(Modifier.height(2.dp))
                Text(stringResource(descRes), fontSize = 12.sp, color = cs.textDim)
            }
        }
    }
}
