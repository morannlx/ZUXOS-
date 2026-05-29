package me.inkdye.zuxos.hook.lenovo

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.graphics.BitmapFactory.Options
import android.net.Uri
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.preference.PreferenceViewHolder
import com.highcapable.yukihookapi.hook.param.PackageParam
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import me.inkdye.zuxos.config.ModuleConfig
import me.inkdye.zuxos.hook.Hook
import java.io.File

object LenovoAboutDeviceInfoHook : Hook {
    override val prefKey = ModuleConfig.LENOVO_ABOUT_DEVICE_INFO_ENABLED
    override val targetPackage = PACKAGE_SETTINGS
    override val moduleName = "lenovo_about_device"

    private const val TAG = "ZUXOS.AboutDevice"
    private const val PACKAGE_SETTINGS = "com.android.settings"
    private const val KEY_REBRAND_MODEL = "csdk_rebrand_model_name"
    private const val DEFAULT_DEVICE_IMAGE_FILE_NAME = "custom_lenovo_about_device.png"

    private data class SummaryHookTarget(
        val className: String,
        val configKey: String,
        val label: String,
    )

    private val summaryHookTargets = listOf(
        SummaryHookTarget(
            "com.android.settings.deviceinfo.hardwareinfo.DeviceModelPreferenceController",
            ModuleConfig.LENOVO_ABOUT_DEVICE_MODEL,
            "device model",
        ),
        SummaryHookTarget(
            "com.lenovo.settings.deviceinfo.controller.CpuInfoDisplayPreferenceController",
            ModuleConfig.LENOVO_ABOUT_DEVICE_PROCESSOR,
            "processor",
        ),
        SummaryHookTarget(
            "com.lenovo.settings.deviceinfo.controller.RomSizePreferenceController",
            ModuleConfig.LENOVO_ABOUT_DEVICE_ROM,
            "storage",
        ),
        SummaryHookTarget(
            "com.lenovo.settings.deviceinfo.controller.RamSizePreferenceController",
            ModuleConfig.LENOVO_ABOUT_DEVICE_RAM,
            "ram",
        ),
        SummaryHookTarget(
            "com.android.settings.deviceinfo.BuildNumberPreferenceController",
            ModuleConfig.LENOVO_ABOUT_DEVICE_SOFTWARE_VERSION,
            "software version",
        ),
        SummaryHookTarget(
            "com.lenovo.settings.deviceinfo.controller.FirmwareVersionPreferenceController",
            ModuleConfig.LENOVO_ABOUT_DEVICE_ANDROID_VERSION,
            "android version",
        ),
    )

    override fun PackageParam.onHook() {
        val classLoader = appClassLoader
        hookSettingsSecureNames(classLoader)
        hookTopImagePreference(classLoader)
        hookDeviceNameController(classLoader)
        hookSummaryControllers(classLoader)
    }

    private fun hookSettingsSecureNames(classLoader: ClassLoader?) {
        runCatching {
            XposedHelpers.findAndHookMethod(
                Settings.Secure::class.java,
                "getString",
                ContentResolver::class.java,
                String::class.java,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val key = param.args.getOrNull(1) as? String ?: return
                        val replacement = when (key) {
                            KEY_REBRAND_MODEL -> customText(ModuleConfig.LENOVO_ABOUT_DEVICE_MODEL)
                            else -> null
                        }
                        if (replacement != null && isCalledFromLenovoAboutPage()) {
                            param.result = replacement
                        }
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook Settings.Secure.getString failed", it) }
    }

    private fun hookTopImagePreference(classLoader: ClassLoader?) {
        val clazz = XposedHelpers.findClassIfExists(
            "com.lenovo.settings.deviceinfo.aboutphone.PadTopImgPreference",
            classLoader,
        ) ?: return

        runCatching {
            val holderClass = XposedHelpers.findClass(
                "androidx.preference.PreferenceViewHolder",
                classLoader,
            )
            XposedHelpers.findAndHookMethod(
                clazz,
                "onBindViewHolder",
                holderClass,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val holder = param.args.getOrNull(0) as? PreferenceViewHolder ?: return
                        Log.d(TAG, "PadTopImgPreference.onBindViewHolder hooked")
                        replaceTopTitle(param.thisObject)
                        findImageView(holder)
                            ?.let { replaceTopImage(it, holder.itemView.context.contentResolver) }
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook PadTopImgPreference.onBindViewHolder failed", it) }

        runCatching {
            val imageViewClass = XposedHelpers.findClass("android.widget.ImageView", classLoader)
            XposedHelpers.findAndHookMethod(
                clazz,
                "setImage",
                imageViewClass,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val image = param.args.getOrNull(0) as? ImageView ?: return
                        Log.d(TAG, "PadTopImgPreference.setImage hooked")
                        replaceTopImage(image, image.context.contentResolver)
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook PadTopImgPreference.setImage failed", it) }

        runCatching {
            XposedHelpers.findAndHookMethod(
                clazz,
                "updateText",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        Log.d(TAG, "PadTopImgPreference.updateText hooked")
                        replaceTopTitle(param.thisObject)
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook PadTopImgPreference.updateText failed", it) }

        runCatching {
            XposedHelpers.findAndHookMethod(
                clazz,
                "onResume",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        replaceTopTitle(param.thisObject)
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook PadTopImgPreference.onResume failed", it) }

        runCatching {
            val preferenceClass = XposedHelpers.findClass("androidx.preference.Preference", classLoader)
            val viewHolderClass = XposedHelpers.findClass("androidx.preference.PreferenceViewHolder", classLoader)
            XposedHelpers.findAndHookMethod(
                preferenceClass,
                "onBindViewHolder",
                viewHolderClass,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val preference = param.thisObject as? androidx.preference.Preference ?: return
                        if (!preference.javaClass.name.endsWith("PadTopImgPreference")) return
                        if (preference.key != "about_device_image") return
                        val holder = param.args.getOrNull(0) as? PreferenceViewHolder ?: return
                        Log.d(TAG, "Preference.onBindViewHolder fallback hit for about_device_image")
                        replaceTopTitle(preference)
                        findImageView(holder)
                            ?.let { replaceTopImage(it, holder.itemView.context.contentResolver) }
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook Preference.onBindViewHolder fallback failed", it) }
    }

    private fun hookDeviceNameController(classLoader: ClassLoader?) {
        val clazz = XposedHelpers.findClassIfExists(
            "com.lenovo.settings.deviceinfo.controller.ZuiDeviceNamePreferenceController",
            classLoader,
        ) ?: return

        runCatching {
            XposedHelpers.findAndHookMethod(
                clazz,
                "getAvailabilityStatus",
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        param.result = 0
                        Log.d(TAG, "ZuiDeviceNamePreferenceController.getAvailabilityStatus forced available")
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook ZuiDeviceNamePreferenceController.getAvailabilityStatus failed", it) }

        runCatching {
            val screenClass = XposedHelpers.findClass("androidx.preference.PreferenceScreen", classLoader)
            XposedHelpers.findAndHookMethod(
                clazz,
                "displayPreference",
                screenClass,
                object : XC_MethodHook() {
                    override fun afterHookedMethod(param: MethodHookParam) {
                        val preference = runCatching {
                            XposedHelpers.getObjectField(param.thisObject, "mPreference") as? androidx.preference.Preference
                        }.getOrNull() ?: return
                        preference.isEnabled = true
                        preference.isSelectable = true
                        preference.isVisible = true
                        preference.shouldDisableView = false
                        Log.d(TAG, "ZuiDeviceNamePreferenceController.displayPreference forced interactive")
                    }
                },
            )
        }.onFailure { Log.w(TAG, "Hook ZuiDeviceNamePreferenceController.displayPreference failed", it) }
    }

    private fun replaceTopImage(image: ImageView, contentResolver: ContentResolver) {
        if (!ModuleConfig.isEnabled(ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_ENABLED, false)) return
        val bitmap = decodeCustomDeviceImage(contentResolver) ?: return
        image.setImageBitmap(bitmap)
    }

    private fun replaceTopTitle(instance: Any?) {
        val name = customText(ModuleConfig.LENOVO_ABOUT_DEVICE_NAME) ?: return
        val title = runCatching { XposedHelpers.getObjectField(instance, "tvPad") as? TextView }.getOrNull() ?: return
        title.text = name
    }

    private fun hookSummaryControllers(classLoader: ClassLoader?) {
        summaryHookTargets.forEach { target ->
            val clazz = XposedHelpers.findClassIfExists(target.className, classLoader) ?: return@forEach
            runCatching {
                XposedHelpers.findAndHookMethod(
                    clazz,
                    "getSummary",
                    object : XC_MethodHook() {
                        override fun afterHookedMethod(param: MethodHookParam) {
                            val text = customText(target.configKey) ?: return
                            param.result = text
                        }
                    },
                )
                Log.i(TAG, "Hooked ${target.label}: ${target.className}")
            }.onFailure { Log.w(TAG, "Hook ${target.label} failed: ${target.className}", it) }
        }
    }

    private fun customText(key: String): String? =
        ModuleConfig.readString(key).trim().takeIf { it.isNotEmpty() }

    private fun resourceId(holder: PreferenceViewHolder, name: String): Int =
        holder.itemView.resources.getIdentifier(name, "id", PACKAGE_SETTINGS)

    private fun findImageView(holder: PreferenceViewHolder): ImageView? {
        val direct = holder.findViewById(resourceId(holder, "img_pad")) as? ImageView
        if (direct != null) return direct
        return findImageViewRecursive(holder.itemView)
    }

    private fun findImageViewRecursive(view: android.view.View): ImageView? {
        if (view is ImageView) return view
        if (view is ViewGroup) {
            for (index in 0 until view.childCount) {
                findImageViewRecursive(view.getChildAt(index))?.let { return it }
            }
        }
        return null
    }

    private fun decodeCustomDeviceImage(contentResolver: ContentResolver): android.graphics.Bitmap? {
        val configured = customText(ModuleConfig.LENOVO_ABOUT_DEVICE_IMAGE_PATH)
        if (configured?.startsWith("content://") == true) {
            runCatching {
                contentResolver.openFileDescriptor(Uri.parse(configured), "r")?.use { fd ->
                    BitmapFactory.decodeFileDescriptor(fd.fileDescriptor, null, Options())
                }
            }.onSuccess { bitmap ->
                if (bitmap != null) {
                    Log.d(TAG, "Loaded custom image from content uri")
                    return bitmap
                }
            }.onFailure { error ->
                Log.w(TAG, "Failed to load custom image from content uri: $configured", error)
            }
        }

        configured
            ?.let(::File)
            ?.takeIf { it.isFile && it.canRead() }
            ?.let {
                Log.d(TAG, "Loaded custom image from file path: $configured")
                BitmapFactory.decodeFile(it.absolutePath)
            }
            ?.let { return it }

        val moduleExternalFile = File(
            "/sdcard/Android/data/me.inkdye.zuxos/files",
            DEFAULT_DEVICE_IMAGE_FILE_NAME,
        )
        if (moduleExternalFile.isFile && moduleExternalFile.canRead()) {
            BitmapFactory.decodeFile(moduleExternalFile.absolutePath)?.let { return it }
        }

        val sharedPicturesFile = File(
            "/sdcard/Pictures/ZUXOS",
            DEFAULT_DEVICE_IMAGE_FILE_NAME,
        )
        if (sharedPicturesFile.isFile && sharedPicturesFile.canRead()) {
            BitmapFactory.decodeFile(sharedPicturesFile.absolutePath)?.let { return it }
        }

        return null
    }

    private fun isCalledFromLenovoAboutPage(): Boolean {
        val stack = Throwable().stackTrace
        return stack.any { frame ->
            frame.className.startsWith("com.lenovo.settings.deviceinfo.aboutphone.") ||
                frame.className.startsWith("com.lenovo.settings.deviceinfo.controller.") ||
                frame.className.startsWith("com.android.settings.deviceinfo.aboutphone.")
        }
    }

}
