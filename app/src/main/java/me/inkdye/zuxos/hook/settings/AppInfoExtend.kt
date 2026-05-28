package me.inkdye.zuxos.hook.settings

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.highcapable.yukihookapi.hook.factory.*
import com.highcapable.yukihookapi.hook.log.YLog
import com.highcapable.yukihookapi.hook.param.PackageParam
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Hook: 在 Settings 应用信息页面的头部注入扩展信息。
 * 完全参照 OriginTool 的实现方式。
 */
object AppInfoExtend : Hook {
    
    override val prefKey = "app_info_extend"
    override val alwaysRegister get() = true
    override val targetPackage = "com.android.settings"
    
    private const val DEDUP_TAG = "app_info_extend_divider"
    
    override fun PackageParam.onHook() {
        loadApp("com.android.settings") {
            
            // Hook: EntityHeaderController.done(boolean)
            // 当头部视图渲染完毕后，注入扩展信息
            "com.android.settings.widget.EntityHeaderController".toClass()
                .method { name = "done"; param(Boolean::class.java) }
                .hook {
                    after {
                        val controller = instance ?: run {
                            YLog.warn("AppInfoExtend: instance is null")
                            return@after
                        }
                        
                        // 1. 反射读取 packageName 和 headerView
                        val pkgName: String
                        val headerView: View
                        try {
                            val pkgField =
                                controller.javaClass.getDeclaredField("mPackageName")
                            pkgField.isAccessible = true
                            pkgName = pkgField.get(controller) as? String
                                ?: return@after
                            
                            val headerField =
                                controller.javaClass.getDeclaredField("mHeader")
                            headerField.isAccessible = true
                            headerView = headerField.get(controller) as? View
                                ?: return@after
                        } catch (e: Exception) {
                            YLog.error("AppInfoExtend: reflection failed - ${e.message}")
                            return@after
                        }
                        
                        val context = headerView.context
                        
                        // 2. 防重复注入
                        val dedupKey = headerView.id
                        if (dedupKey != View.NO_ID &&
                            headerView.getTag(dedupKey) != null
                        ) {
                            YLog.debug("AppInfoExtend: already injected, skip")
                            return@after
                        }
                        if (dedupKey != View.NO_ID) {
                            headerView.setTag(dedupKey, true)
                        }
                        
                        // 3. 获取 PackageInfo
                        val pm = context.packageManager
                        val pkgInfo = getPackageInfoCompat(pm, pkgName) ?: return@after
                        val appInfo = pkgInfo.applicationInfo
                        if (appInfo == null) return@after
                        
                        // 4. 找到文本容器
                        val textContainer = findTextContainer(headerView, context)
                        if (textContainer == null) {
                            YLog.warn("AppInfoExtend: textContainer not found")
                            return@after
                        }
                        
                        // 5. 读取用户偏好（通过 ModuleConfig 跨进程读取，参照 ZUX-ZTool）
                        val showPkgName =
                            ModuleConfig.isEnabled("app_info_show_package_name", false)
                        val showSdk =
                            ModuleConfig.isEnabled("app_info_show_sdk_version", false)
                        val showInstallTime =
                            ModuleConfig.isEnabled("app_info_show_install_time", false)
                        val showUpdateTime =
                            ModuleConfig.isEnabled("app_info_show_update_time", false)
                        val showInstallSource =
                            ModuleConfig.isEnabled("app_info_show_install_source", false)
                        val enableCopy =
                            ModuleConfig.isEnabled("app_info_enable_long_copy", false)
                        
                        YLog.info("AppInfoExtend: pkg=$showPkgName sdk=$showSdk install=$showInstallTime update=$showUpdateTime source=$showInstallSource copy=$enableCopy")
                        
                        // 6. 构建条目
                        val entries = mutableListOf<Pair<String, String>>()
                        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        
                        if (showPkgName) {
                            entries.add("包名" to pkgName)
                        }
                        if (showSdk) {
                            val minSdk = try {
                                appInfo.minSdkVersion
                            } catch (ignored: Exception) {
                                0
                            }
                            entries.add("最低SDK" to "API $minSdk")
                            entries.add("目标SDK" to "API ${appInfo.targetSdkVersion}")
                        }
                        if (showInstallTime) {
                            entries.add("首次安装" to sdf.format(Date(pkgInfo.firstInstallTime)))
                        }
                        if (showUpdateTime) {
                            entries.add("最近更新" to sdf.format(Date(pkgInfo.lastUpdateTime)))
                        }
                        if (showInstallSource) {
                            val installer = try {
                                pm.getInstallerPackageName(pkgName) ?: "未知来源"
                            } catch (ignored: Exception) {
                                "未知来源"
                            }
                            entries.add("安装来源" to installer)
                        }
                        
                        if (entries.isEmpty()) {
                            YLog.debug("AppInfoExtend: no entries enabled")
                            return@after
                        }
                        
                        // 7. 获取主题次级文本颜色
                        val ta = context.theme.obtainStyledAttributes(
                            intArrayOf(android.R.attr.textColorSecondary)
                        )
                        val secondaryColor = ta.getColor(0, Color.parseColor("#888888"))
                        ta.recycle()
                        
                        // 8. 延迟到下一帧注入
                        textContainer.post {
                            // 二次防重复
                            for (i in 0 until textContainer.childCount) {
                                if (textContainer.getChildAt(i).getTag() == DEDUP_TAG) {
                                    YLog.debug("AppInfoExtend: already injected")
                                    return@post
                                }
                            }

                            for ((label, value) in entries) {
                                val tv = TextView(context).apply {
                                    text = "$label: $value"
                                    textSize = 13f
                                    setTextColor(secondaryColor)
                                    layoutParams = ViewGroup.MarginLayoutParams(
                                        ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT
                                    ).apply { setMargins(0, 2, 0, 0) }
                                    gravity = Gravity.CENTER
                                    includeFontPadding = false
                                    
                                    if (enableCopy) {
                                        setOnLongClickListener {
                                            try {
                                                val clipboard =
                                                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                                clipboard.setPrimaryClip(
                                                    ClipData.newPlainText(label, value)
                                                )
                                                Toast
                                                    .makeText(
                                                        context,
                                                        "已复制: $value",
                                                        Toast.LENGTH_SHORT
                                                    )
                                                    .show()
                                            } catch (ignored: Exception) {
                                            }
                                            true
                                        }
                                    }
                                }
                                textContainer.addView(tv)
                            }
                            
                            textContainer.requestLayout()
                            textContainer.invalidate()
                        }
                        
                        YLog.debug("AppInfoExtend: scheduled ${entries.size} entries for $pkgName")
                    }
                }
        }
    }
    
    private fun getPackageInfoCompat(
        pm: PackageManager,
        pkgName: String
    ): PackageInfo? {
        return try {
            pm.getPackageInfo(pkgName, PackageManager.GET_META_DATA)
        } catch (e1: Exception) {
            try {
                val flagsClass =
                    Class.forName("android.content.pm.PackageManager\$PackageInfoFlags")
                val of = flagsClass.getMethod("of", Long::class.javaPrimitiveType)
                val getPm = pm.javaClass.getMethod(
                    "getPackageInfo",
                    String::class.java,
                    flagsClass
                )
                getPm.invoke(pm, pkgName, of.invoke(null, 0L)) as PackageInfo
            } catch (e2: Exception) {
                YLog.error("AppInfoExtend: getPackageInfo failed - ${e2.message}")
                null
            }
        }
    }
    
    private fun findTextContainer(
        headerView: View,
        context: Context
    ): ViewGroup? {
        val pkg = context.packageName
        
        // 策略1：通过 entity_header_summary 定位其直接父容器
        val summaryId = context.resources.getIdentifier(
            "entity_header_summary", "id", pkg
        )
        if (summaryId != 0) {
            val summary = headerView.findViewById<View>(summaryId)
            if (summary != null) {
                val parent = summary.parent
                if (parent is ViewGroup) {
                    YLog.debug("AppInfoExtend: [strategy1] via summary parent=$parent")
                    return parent
                }
            }
        }
        
        // 策略2：通过 entity_header_content 定位
        val contentId = context.resources.getIdentifier(
            "entity_header_content", "id", pkg
        )
        if (contentId != 0) {
            val content = headerView.findViewById<View>(contentId)
            if (content is ViewGroup) {
                YLog.debug("AppInfoExtend: [strategy2] via entity_header_content")
                return content
            }
        }
        
        // 策略3：通过 entity_header_title 定位其父容器
        val titleId = context.resources.getIdentifier(
            "entity_header_title", "id", pkg
        )
        if (titleId != 0) {
            val title = headerView.findViewById<View>(titleId)
            if (title != null) {
                val parent = title.parent
                if (parent is ViewGroup) {
                    YLog.debug("AppInfoExtend: [strategy3] via title parent=$parent")
                    return parent
                }
            }
        }
        
        // 策略4：直接使用 headerView 自身
        if (headerView is ViewGroup && headerView.id != View.NO_ID) {
            YLog.warn("AppInfoExtend: [strategy4] fallback to headerView itself")
            return headerView
        }
        
        return null
    }
}
