package com.hongguotv.adbremote

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView

/** A remote-friendly guide. A regular APK cannot change ADB trust or start pairing itself. */
class MainActivity : Activity() {
    private lateinit var status: TextView
    private lateinit var diagnostics: TextView
    private val failures = mutableListOf<String>()
    private var candidateCount = "—"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        status = findViewById(R.id.status)
        diagnostics = findViewById(R.id.diagnostics)
        updateDiagnostics()

        val pairingButton = findViewById<Button>(R.id.open_pairing)
        pairingButton.setOnClickListener {
            openSystemPage(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS, R.string.opened_developer, true)
        }
        findViewById<Button>(R.id.open_about).setOnClickListener {
            openSystemPage(Settings.ACTION_DEVICE_INFO_SETTINGS, R.string.opened_about, true)
        }
        findViewById<Button>(R.id.open_settings).setOnClickListener {
            openSystemPage(Settings.ACTION_SETTINGS, R.string.opened_settings, false)
        }
        findViewById<Button>(R.id.find_settings).setOnClickListener {
            failures.clear()
            showSystemSettingsPicker()
        }
        pairingButton.requestFocus()
    }

    private fun openSystemPage(action: String, successMessage: Int, fallbackToSettings: Boolean) {
        failures.clear()
        candidateCount = "—"
        if (launch(Intent(action), action.substringAfterLast('.'))) {
            status.setText(successMessage)
            return
        }
        if (fallbackToSettings && launch(Intent(Settings.ACTION_SETTINGS), "SETTINGS")) {
            status.setText(R.string.fallback_settings)
            return
        }
        status.setText(R.string.try_settings_candidates)
        showSystemSettingsPicker()
    }

    private fun showSystemSettingsPicker() {
        val entries = try {
            SettingsEntryFinder.find(this)
        } catch (error: RuntimeException) {
            failures.add("查找入口: ${error.javaClass.simpleName}")
            candidateCount = "?"
            status.setText(R.string.no_settings_candidates)
            updateDiagnostics()
            return
        }
        candidateCount = entries.size.toString()
        updateDiagnostics()
        if (entries.isEmpty()) {
            status.setText(R.string.no_settings_candidates)
            return
        }
        val labels = entries.map { "${it.label}  (${it.packageName})" }.toTypedArray()
        AlertDialog.Builder(this)
            .setTitle(R.string.settings_candidates_title)
            .setItems(labels) { _, index ->
                val entry = entries[index]
                val intent = Intent(Intent.ACTION_MAIN).addCategory(entry.category).setComponent(entry.component)
                if (launch(intent, entry.component.flattenToShortString())) {
                    status.setText(R.string.opened_candidate)
                } else {
                    status.setText(R.string.candidate_failed)
                }
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun launch(intent: Intent, name: String): Boolean = try {
        startActivity(intent)
        updateDiagnostics()
        true
    } catch (error: RuntimeException) {
        // Record the type; OEM builds can omit an action or deny it by policy.
        failures.add("$name: ${error.javaClass.simpleName}")
        updateDiagnostics()
        false
    }

    private fun updateDiagnostics() {
        val device = "${Build.BRAND} / ${Build.MANUFACTURER} / ${Build.MODEL} · Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT}) · ${Build.DISPLAY}"
        diagnostics.text = getString(R.string.diagnostics, device, candidateCount, failures.joinToString("；").ifEmpty { "无" })
    }
}
