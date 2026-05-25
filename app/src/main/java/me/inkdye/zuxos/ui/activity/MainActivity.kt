package me.inkdye.zuxos.ui.activity

import android.content.Intent
import android.os.Bundle
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.highcapable.betterandroid.ui.component.activity.AppViewsActivity

class MainActivity : AppViewsActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, LiquidGlassHomeActivity::class.java))
        finish()
    }
}
