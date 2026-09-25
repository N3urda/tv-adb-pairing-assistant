package com.hongguotv.adbremote

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView

/** A remote-friendly guide. A regular APK cannot change ADB trust or start pairing itself. */
class MainActivity : Activity() {
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)

        val pairingButton = findViewById<Button>(R.id.open_pairing)
        pairingButton.setOnClickListener {
            openSystemPage(
                Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS,
                R.string.opened_developer,
                fallbackToSettings = true,
            )
        }
        findViewById<Button>(R.id.open_about).setOnClickListener {
            openSystemPage(
                Settings.ACTION_DEVICE_INFO_SETTINGS,
                R.string.opened_about,
                fallbackToSettings = true,
            )
        }
        findViewById<Button>(R.id.open_settings).setOnClickListener {
            openSystemPage(Settings.ACTION_SETTINGS, R.string.opened_settings)
        }

        // TV remotes have no touch gesture. Start on the first actionable control.
        pairingButton.requestFocus()
    }

    private fun openSystemPage(action: String, successMessage: Int, fallbackToSettings: Boolean = false) {
        if (launch(action)) {
            status.setText(successMessage)
            return
        }
        if (fallbackToSettings && launch(Settings.ACTION_SETTINGS)) {
            status.setText(R.string.fallback_settings)
            return
        }
        status.setText(R.string.no_settings)
    }

    private fun launch(action: String): Boolean = try {
        startActivity(Intent(action))
        true
    } catch (_: RuntimeException) {
        // OEM Settings can omit or protect an action even when AOSP implements it.
        false
    }
}
