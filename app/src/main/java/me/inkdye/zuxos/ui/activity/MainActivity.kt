package me.inkdye.zuxos.ui.activity

import android.os.Bundle
import android.widget.TextView
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.highcapable.betterandroid.ui.component.activity.AppViewsActivity
import com.highcapable.yukihookapi.YukiHookAPI
import me.inkdye.zuxos.BuildConfig
import me.inkdye.zuxos.R

class MainActivity : AppViewsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val isActive = YukiHookAPI.Status.isXposedModuleActive || isModuleActivated()
        findViewById<TextView>(R.id.tv_status).text = if (isActive)
            getString(R.string.module_is_activated)
        else
            getString(R.string.module_not_activated)

        findViewById<TextView>(R.id.tv_version).text =
            getString(R.string.module_version, BuildConfig.VERSION_NAME)
    }

    @androidx.annotation.Keep
    private fun isModuleActivated(): Boolean = false
}
