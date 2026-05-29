package me.inkdye.zuxos.ui.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import androidx.core.graphics.drawable.toBitmap
import com.highcapable.yukihookapi.YukiHookAPI
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.effects.vibrancy
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.highlight.HighlightStyle
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.R
import me.inkdye.zuxos.root.RootState
import me.inkdye.zuxos.root.RootStatus
import me.inkdye.zuxos.ui.components.pageHorizontalPadding
import me.inkdye.zuxos.ui.root.rememberRootUiState
import me.inkdye.zuxos.ui.settings.HookLoggingSetting
import me.inkdye.zuxos.ui.settings.rememberAppSettings
import me.inkdye.zuxos.ui.settings.rememberBooleanSettingState
import me.inkdye.zuxos.ui.settings.rememberSettingsRepository
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
private val FeatureGreen = Color(0xFF79B69B)
private val FeatureBlue = Color(0xFF8AA8CF)
private val FeatureNeutral = Color(0xFF96A3AE)

private fun glassNavTint(cs: AppColorScheme) =
    if (cs.isDark) cs.glassSurface.copy(alpha = 0.46f) else Color.White.copy(alpha = 0.42f)

private fun Modifier.liquidGlassNavSurface(
    backdrop: Backdrop,
    shape: Shape,
    liquidGlass: Boolean,
    cs: AppColorScheme,
    navTint: Color,
    pressedProgress: Float = 0f,
    blurRadius: Dp = 10.dp,
    lensRadius: Dp = 24.dp,
    lensRefraction: Dp = 44.dp,
): Modifier {
    val scale = 1f + pressedProgress * 0.025f
    return if (liquidGlass) {
        this.graphicsLayer {
            scaleX = scale
            scaleY = scale
        }.drawBackdrop(
            backdrop = backdrop,
            shape = { shape },
            effects = {
                vibrancy()
                blur(blurRadius.toPx())
                lens(lensRadius.toPx(), lensRefraction.toPx(), depthEffect = true)
            },
            highlight = {
                Highlight(
                    width = 1.2.dp,
                    blurRadius = 18.dp,
                    alpha = if (cs.isDark) 0.30f else 0.46f,
                    style = HighlightStyle.Default(
                        color = cs.glassHighlight.copy(alpha = if (cs.isDark) 0.22f else 0.40f),
                        angle = 38f,
                        falloff = 1.65f
                    )
                )
            },
            shadow = {
                Shadow(
                    radius = 24.dp,
                    offset = DpOffset(0.dp, 7.dp),
                    color = Color.Black.copy(alpha = if (cs.isDark) 0.22f else 0.08f),
                    alpha = 1f
                )
            },
            innerShadow = {
                InnerShadow(
                    radius = 10.dp,
                    offset = DpOffset(0.dp, 1.dp),
                    color = cs.glassHighlight.copy(alpha = if (cs.isDark) 0.08f else 0.18f),
                    alpha = 1f
                )
            },
            onDrawSurface = {
                drawRect(navTint)
                drawRect(cs.glassHighlight.copy(alpha = if (cs.isDark) 0.03f else 0.06f))
            }
        )
    } else {
        this.shadow(
            elevation = if (cs.isDark) 10.dp else 14.dp,
            shape = shape,
            clip = false,
            ambientColor = cs.cardShadow,
            spotColor = cs.cardShadow
        )
            .clip(shape)
            .background(cs.glassSurface, shape)
            .border(1.dp, cs.glassBorder, shape)
    }
}

/* ── Feature Groups ─────────────────────────────── */
private data class FeatureGroup(
    val icon: String,
    val titleRes: Int,
    val items: List<Int>,
    val targetPackage: String? = null,
    val accentColor: Color = FeatureBlue
)

private val featureGroups = listOf(
    FeatureGroup("▦", R.string.feature_group_framework, listOf(
        R.string.feature_group_framework_item1,
        R.string.feature_group_framework_item2
    ), "android", FeatureGreen),
    FeatureGroup("⚙", R.string.feature_group_system, listOf(
        R.string.feature_group_system_item1
    ), "com.android.settings", FeatureNeutral),
    FeatureGroup("↻", R.string.feature_group_lenovo_system_update, listOf(
        R.string.feature_group_lenovo_system_update_item1,
        R.string.feature_group_lenovo_system_update_item2
    ), "com.lenovo.ota", FeatureGreen),
    FeatureGroup("✦", R.string.feature_group_lenovo_game_assistant, listOf(
        R.string.feature_group_lenovo_game_assistant_item1,
        R.string.feature_group_lenovo_game_assistant_item2
    ), "com.zui.game.service", FeatureBlue),
    FeatureGroup("▣", R.string.feature_group_package_installer, listOf(
        R.string.feature_group_package_installer_item1,
        R.string.feature_group_package_installer_item2
    ), "com.android.packageinstaller", FeatureBlue)
)

/* ── Navigation ─────────────────────────────────── */
private enum class NavPage(val labelRes: Int, val icon: String, val iconRes: Int? = null) {
    Home(R.string.nav_home, "⌂"),
    Features(R.string.nav_features, "≡"),
    Logs(R.string.nav_logs, "", R.drawable.ic_nav_logs),
    Settings(R.string.nav_settings, "⚙"),
}

private const val TABLET_WIDTH_DP = 600
private const val MEDIUM_WIDTH_DP = 840
private val FEATURE_CARD_MIN_WIDTH = 200.dp

/* ── Main Composable ────────────────────────────── */
@Composable
private fun LiquidGlassHome() {
    var selectedTab by remember { mutableIntStateOf(0) }
    val settings = rememberAppSettings()
    val repository = rememberSettingsRepository()
    val loggingState = rememberBooleanSettingState(repository, HookLoggingSetting)
    val logsEnabled = loggingState.checked.value
    val navPages = remember(logsEnabled) {
        if (logsEnabled) NavPage.entries else NavPage.entries.filterNot { it == NavPage.Logs }
    }
    LaunchedEffect(navPages.size) {
        if (selectedTab >= navPages.size) selectedTab = 0
    }
    val floatingNav = settings.floatingNavEnabled.value
    val liquidGlass = settings.liquidGlassEnabled.value
    val cs = colorSchemeFor(resolveIsDark(settings.themeMode.value))
    val navTint = glassNavTint(cs)

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
                    ) { page -> PageContent(navPages.getOrElse(page) { NavPage.Home }, cs) }

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
                            navPages = navPages,
                            onTabSelected = { selectedTab = it }
                        )
                    }
                }
            }
            isCompactTablet -> {
                Row(
                    Modifier
                        .fillMaxSize()
                        .safeContentPadding(),
                    verticalAlignment = if (floatingNav) Alignment.CenterVertically else Alignment.Top
                ) {
                    GlassNavigationRail(
                        selectedTab = selectedTab,
                        backdrop = backdrop,
                        floatingNav = floatingNav,
                        liquidGlass = liquidGlass,
                        isCompactTablet = true,
                        cs = cs,
                        navTint = navTint,
                        navPages = navPages,
                        onTabSelected = { selectedTab = it }
                    )
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .layerBackdrop(backdrop)
                    ) { page -> PageContent(navPages.getOrElse(page) { NavPage.Home }, cs) }
                }
            }
            else -> {
                Row(
                    Modifier
                        .fillMaxSize()
                        .safeContentPadding(),
                    verticalAlignment = if (floatingNav) Alignment.CenterVertically else Alignment.Top
                ) {
                    GlassNavigationRail(
                        selectedTab = selectedTab,
                        backdrop = backdrop,
                        floatingNav = floatingNav,
                        liquidGlass = liquidGlass,
                        isCompactTablet = false,
                        cs = cs,
                        navTint = navTint,
                        navPages = navPages,
                        onTabSelected = { selectedTab = it }
                    )
                    AnimatedContent(
                        targetState = selectedTab,
                        transitionSpec = { fadeIn() togetherWith fadeOut() },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .layerBackdrop(backdrop)
                    ) { page -> PageContent(navPages.getOrElse(page) { NavPage.Home }, cs) }
                }
            }
        }
    }
}

@Composable
private fun PageContent(page: NavPage, cs: AppColorScheme) {
    when (page) {
        NavPage.Home -> HomePage(cs)
        NavPage.Features -> FeaturesPage(cs)
        NavPage.Logs -> HookLogsPage(cs)
        NavPage.Settings -> SettingsPage(cs)
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
    navPages: List<NavPage>,
    onTabSelected: (Int) -> Unit
) {
    val cornerRadius = if (compact) 20.dp else 24.dp
    val floatPadH = if (compact) 8.dp else 12.dp
    val floatPadV = if (compact) 4.dp else 8.dp
    val iconSize = if (compact) 18.sp else 21.sp
    val labelSize = if (compact) 10.sp else 11.sp

    val navShape = RoundedCornerShape(
        topStart = cornerRadius, topEnd = cornerRadius,
        bottomStart = if (floatingNav) cornerRadius else 0.dp,
        bottomEnd = if (floatingNav) cornerRadius else 0.dp,
    )
    val baseModifier = Modifier
        .fillMaxWidth()
        .height(if (compact) 62.dp else 72.dp)
        .then(if (floatingNav) Modifier.padding(horizontal = floatPadH, vertical = floatPadV) else Modifier)

    Box(
        baseModifier.liquidGlassNavSurface(
            backdrop = backdrop,
            shape = navShape,
            liquidGlass = liquidGlass,
            cs = cs,
            navTint = navTint
        )
    ) {
        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navPages.forEachIndexed { index, page ->
                LiquidNavItem(
                    page = page,
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    cs = cs,
                    iconSize = iconSize,
                    labelSize = labelSize,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                )
            }
        }
    }
}

@Composable
private fun LiquidNavItem(
    page: NavPage,
    selected: Boolean,
    onClick: () -> Unit,
    cs: AppColorScheme,
    iconSize: TextUnit,
    modifier: Modifier = Modifier,
    labelSize: TextUnit? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = when {
            pressed -> 0.92f
            selected -> 1.05f
            else -> 1f
        },
        animationSpec = spring(stiffness = 420f, dampingRatio = 0.72f),
        label = "navItemScale"
    )
    val itemShape = RoundedCornerShape(if (labelSize == null) 22.dp else 18.dp)
    val contentColor = if (selected) cs.accentGreen else cs.textDim
    val selectedBrush = Brush.verticalGradient(
        listOf(
            cs.glassHighlight.copy(alpha = if (cs.isDark) 0.08f else 0.32f),
            cs.accentGreen.copy(alpha = if (cs.isDark) 0.13f else 0.10f),
        )
    )

    Box(
        modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clip(itemShape)
            .then(if (selected) Modifier.background(selectedBrush, itemShape) else Modifier)
            .then(if (selected) Modifier.border(1.dp, cs.glassBorder, itemShape) else Modifier)
            .clickable(
                indication = null,
                interactionSource = interactionSource,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        if (labelSize == null) {
            NavPageIcon(page = page, color = contentColor, textSize = iconSize)
        } else {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                NavPageIcon(page = page, color = contentColor, textSize = iconSize)
                Spacer(Modifier.height(2.dp))
                Text(
                    text = stringResource(page.labelRes),
                    fontSize = labelSize,
                    color = contentColor,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun NavPageIcon(page: NavPage, color: Color, textSize: TextUnit) {
    val iconRes = page.iconRes
    if (iconRes != null) {
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(22.dp)
        )
    } else {
        Text(
            text = page.icon,
            fontSize = textSize,
            color = color,
            fontWeight = FontWeight.Normal
        )
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
    navPages: List<NavPage>,
    onTabSelected: (Int) -> Unit
) {
    val pillShape = if (floatingNav) {
        RoundedCornerShape(30.dp)
    } else {
        RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    }
    val pillHeight = if (floatingNav) 58.dp else 72.dp
    val pillWidth = if (floatingNav) maxOf(188.dp, 64.dp * navPages.size) else 232.dp
    val iconSize = 22.sp
    val surfaceModifier = if (floatingNav) {
        Modifier.width(pillWidth)
    } else {
        Modifier.fillMaxWidth()
    }

    BoxWithConstraints(
        surfaceModifier
            .height(pillHeight)
            .liquidGlassNavSurface(
                backdrop = backdrop,
                shape = pillShape,
                liquidGlass = liquidGlass,
                cs = cs,
                navTint = navTint,
            )
            .padding(5.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        val contentPadding = 2.dp
        val tabCount = navPages.size
        val tabWidth = (maxWidth - contentPadding * 2f) / tabCount.toFloat()
        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr
        val indicatorIndex = if (isLtr) selectedTab else tabCount - selectedTab - 1
        val indicatorOffset by animateDpAsState(
            targetValue = contentPadding + tabWidth * indicatorIndex.toFloat(),
            animationSpec = spring(stiffness = 430f, dampingRatio = 0.78f),
            label = "pillIndicatorOffset"
        )
        val indicatorShape = RoundedCornerShape(24.dp)

        Box(
            Modifier
                .offset(x = indicatorOffset)
                .width(tabWidth)
                .fillMaxHeight()
                .padding(3.dp)
                .shadow(
                    elevation = 8.dp,
                    shape = indicatorShape,
                    clip = false,
                    ambientColor = cs.accentGreen.copy(alpha = if (cs.isDark) 0.14f else 0.08f),
                    spotColor = cs.accentGreen.copy(alpha = if (cs.isDark) 0.14f else 0.08f)
                )
                .clip(indicatorShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            cs.glassHighlight.copy(alpha = if (cs.isDark) 0.10f else 0.36f),
                            cs.accentGreen.copy(alpha = if (cs.isDark) 0.18f else 0.12f),
                        )
                    ),
                    indicatorShape
                )
                .border(1.dp, cs.glassBorder.copy(alpha = if (cs.isDark) 0.10f else 0.34f), indicatorShape)
        )

        Row(
            Modifier
                .fillMaxSize()
                .padding(horizontal = contentPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            navPages.forEachIndexed { index, page ->
                LiquidNavItem(
                    page = page,
                    selected = selectedTab == index,
                    onClick = { onTabSelected(index) },
                    cs = cs,
                    iconSize = iconSize,
                    modifier = Modifier
                        .width(tabWidth)
                        .fillMaxHeight()
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
    floatingNav: Boolean,
    liquidGlass: Boolean,
    isCompactTablet: Boolean,
    cs: AppColorScheme,
    navTint: Color,
    navPages: List<NavPage>,
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

    val railModifier = baseModifier.liquidGlassNavSurface(
        backdrop = backdrop,
        shape = railShape,
        liquidGlass = liquidGlass,
        cs = cs,
        navTint = navTint,
        blurRadius = if (floatingNav) 10.dp else 8.dp,
        lensRadius = if (floatingNav) 24.dp else 18.dp,
        lensRefraction = if (floatingNav) 46.dp else 36.dp,
    )

    val iconSize = if (floatingNav) 22.sp else if (isCompactTablet) 18.sp else 22.sp
    val labelSize = if (isCompactTablet) 10.sp else 11.sp
    val headerSize = if (isCompactTablet) 36.dp else 44.dp
    val headerFontSize = if (isCompactTablet) 15.sp else 18.sp
    val brandBrush = Brush.linearGradient(
        listOf(
            cs.accentGreen.copy(alpha = 0.95f),
            cs.accentBlue.copy(alpha = 0.92f),
        )
    )

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
                            brandBrush,
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

                navPages.forEachIndexed { index, page ->
                    LiquidNavItem(
                        page = page,
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        cs = cs,
                        iconSize = iconSize,
                        modifier = Modifier.size(48.dp)
                    )
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
                                brandBrush,
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
                navPages.forEachIndexed { index, page ->
                    NavigationRailItem(
                        selected = selectedTab == index,
                        onClick = { onTabSelected(index) },
                        icon = {
                            NavPageIcon(
                                page = page,
                                color = if (selectedTab == index) cs.accentGreen else cs.textDim,
                                textSize = iconSize,
                            )
                        },
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
    val rootState = rememberRootUiState()
    val isXposedActive = YukiHookAPI.Status.isXposedModuleActive || (context as? LiquidGlassHomeActivity)?.isModuleActivated() == true
    val horizontalPadding = pageHorizontalPadding()

    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = horizontalPadding),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 120.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(stringResource(R.string.page_home_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cs.textPrimary)
            Spacer(Modifier.height(4.dp))
            Text(stringResource(R.string.page_home_subtitle), fontSize = 14.sp, color = cs.textSecondary)
            Spacer(Modifier.height(8.dp))
        }

        item {
            ActivationStatusCard(
                isActive = isXposedActive,
                rootState = rootState.state.value,
                rootBusy = rootState.busy.value,
                cs = cs,
            )
        }
        item { ModuleInfoCard(isXposedActive, cs) }
        item { DeviceInfoCard(context, cs) }
    }
}

@Composable
private fun ActivationStatusCard(
    isActive: Boolean,
    rootState: RootState,
    rootBusy: Boolean,
    cs: AppColorScheme,
) {
    val rootGranted = rootState.status == RootStatus.GRANTED
    val displayState = activationDisplayState(isActive, rootGranted)
    val statusColor = displayState.color(cs)
    val xposedColor = if (isActive) cs.accentGreen else cs.accentRed
    val rootColor = if (rootGranted) cs.accentGreen else cs.accentRed
    val shape = RoundedCornerShape(20.dp)
    val cardBg = Brush.verticalGradient(
        listOf(
            statusColor.copy(alpha = if (cs.isDark) 0.14f else 0.09f),
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
            StatusLine(
                label = stringResource(if (isActive) R.string.home_status_active else R.string.home_status_inactive),
                color = xposedColor,
            )
            Spacer(Modifier.height(8.dp))
            StatusLine(
                label = "${stringResource(R.string.root_status_title)} · ${rootStatusText(rootState.status, rootBusy)}",
                color = rootColor,
            )

            Spacer(Modifier.height(16.dp))

            Text(
                stringResource(displayState.titleRes),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = cs.textPrimary
            )

            Spacer(Modifier.height(8.dp))

            Text(
                stringResource(displayState.descRes),
                fontSize = 14.sp,
                color = cs.textSecondary,
                lineHeight = 22.sp
            )

            rootState.message?.takeIf { rootState.status != RootStatus.GRANTED }?.let {
                Spacer(Modifier.height(8.dp))
                Text(it, fontSize = 12.sp, color = cs.textDim)
            }
        }
    }
}

private enum class ActivationDisplayState(val titleRes: Int, val descRes: Int) {
    Active(R.string.home_module_active, R.string.home_desc_active),
    Partial(R.string.home_module_partial, R.string.home_desc_partial),
    Unavailable(R.string.home_module_unavailable, R.string.home_desc_unavailable),
    Inactive(R.string.home_module_inactive, R.string.home_desc_inactive);

    fun color(cs: AppColorScheme): Color = when (this) {
        Active -> cs.accentGreen
        Partial -> cs.accentBlue
        Unavailable, Inactive -> cs.accentRed
    }
}

private fun activationDisplayState(lspActive: Boolean, rootActive: Boolean): ActivationDisplayState =
    when {
        lspActive && rootActive -> ActivationDisplayState.Active
        lspActive && !rootActive -> ActivationDisplayState.Partial
        !lspActive && rootActive -> ActivationDisplayState.Unavailable
        else -> ActivationDisplayState.Inactive
    }

@Composable
private fun StatusLine(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = color,
        )
    }
}

@Composable
private fun rootStatusText(status: RootStatus, busy: Boolean): String {
    if (busy) return stringResource(R.string.root_status_checking)
    return when (status) {
        RootStatus.UNKNOWN -> stringResource(R.string.root_status_unknown)
        RootStatus.UNAVAILABLE -> stringResource(R.string.root_status_unavailable)
        RootStatus.DENIED -> stringResource(R.string.root_status_denied)
        RootStatus.GRANTED -> stringResource(R.string.root_status_granted)
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
    val horizontalPadding = pageHorizontalPadding()

    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = FEATURE_CARD_MIN_WIDTH),
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = horizontalPadding),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(top = 24.dp, bottom = 120.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            Column {
                Text(stringResource(R.string.page_features_title), fontSize = 28.sp, fontWeight = FontWeight.Bold, color = cs.textPrimary)
                Spacer(Modifier.height(4.dp))
                Text(stringResource(R.string.page_features_subtitle), fontSize = 14.sp, color = cs.textSecondary)
                Spacer(Modifier.height(16.dp))
            }
        }

        if (featureGroups.isEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                Box(Modifier.fillMaxWidth().padding(top = 40.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.no_features_available), color = cs.textDim, fontSize = 14.sp)
                }
            }
        } else {
            items(featureGroups.size) { index ->
                val group = featureGroups[index]
                FeatureGroupCard(context, group, cs) {
                    if (group.targetPackage == "android") {
                        context.startActivity(Intent(context, SystemFrameworkSettingsActivity::class.java))
                    } else if (group.targetPackage == "com.android.settings") {
                        context.startActivity(Intent(context, SystemSettingsActivity::class.java))
                    } else if (group.targetPackage == "com.lenovo.ota") {
                        context.startActivity(Intent(context, LenovoSystemUpdateSettingsActivity::class.java))
                    } else if (group.targetPackage == "com.zui.game.service") {
                        context.startActivity(Intent(context, LenovoGameAssistantSettingsActivity::class.java))
                    } else if (group.targetPackage == "com.android.packageinstaller") {
                        context.startActivity(Intent(context, PackageInstallerSettingsActivity::class.java))
                    }
                }
            }
        }
    }
}

@Composable
private fun FeatureGroupCard(context: Context, group: FeatureGroup, cs: AppColorScheme, onClick: () -> Unit = {}) {
    val density = LocalDensity.current
    val iconSize = 42.dp
    val iconBitmapSize = with(density) { iconSize.toPx().roundToInt().coerceAtLeast(1) }
    val appIcon = remember(group.targetPackage, iconBitmapSize) {
        group.targetPackage?.let { pkg ->
            try {
                context.packageManager.getApplicationIcon(pkg)
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
                        bitmap = appIcon.toBitmap(iconBitmapSize, iconBitmapSize).asImageBitmap(),
                        contentDescription = null,
                        modifier = Modifier
                            .size(iconSize)
                            .clip(RoundedCornerShape(12.dp))
                    )
                } else {
                    Box(
                        Modifier
                            .size(iconSize)
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
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = cs.accentBlue,
                    lineHeight = 20.sp,
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
                            modifier = Modifier.weight(1f),
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
    val horizontalPadding = pageHorizontalPadding()
    val settingsItems = listOf(
        Triple("⚙", R.string.settings_general, R.string.settings_general_desc),
        Triple("🎨", R.string.settings_appearance, R.string.settings_appearance_desc),
        Triple("📦", R.string.settings_export, R.string.settings_export_desc),
        Triple("ℹ", R.string.settings_about, R.string.settings_about_desc),
    )
    LazyColumn(
        Modifier.fillMaxSize().padding(horizontal = horizontalPadding),
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
                    R.string.settings_general -> {
                        context.startActivity(Intent(context, GeneralSettingsActivity::class.java))
                    }
                    R.string.settings_appearance -> {
                        context.startActivity(Intent(context, AppearanceSettingsActivity::class.java))
                    }
                    R.string.settings_export -> {
                        context.startActivity(Intent(context, ExportImportSettingsActivity::class.java))
                    }
                    R.string.settings_about -> {
                        context.startActivity(Intent(context, AboutSettingsActivity::class.java))
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
                    .background(cs.accentBlue.copy(alpha = if (cs.isDark) 0.14f else 0.08f), CircleShape),
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
