package com.hongguotv.adbremote

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.os.Build

internal data class SettingsEntry(val label: String, val packageName: String, val component: ComponentName, val category: String)

internal object SettingsEntryFinder {
    const val LEANBACK_SETTINGS = "android.intent.category.LEANBACK_SETTINGS"
    private val categories = listOf(LEANBACK_SETTINGS, Intent.CATEGORY_LEANBACK_LAUNCHER, Intent.CATEGORY_LAUNCHER)

    fun find(context: Context): List<SettingsEntry> {
        val manager = context.packageManager
        val found = LinkedHashMap<ComponentName, SettingsEntry>()
        for (category in categories) {
            val intent = Intent(Intent.ACTION_MAIN).addCategory(category)
            @Suppress("DEPRECATION")
            val matches = if (Build.VERSION.SDK_INT >= 33) {
                manager.queryIntentActivities(intent, android.content.pm.PackageManager.ResolveInfoFlags.of(0))
            } else {
                manager.queryIntentActivities(intent, 0)
            }
            for (match in matches) {
                val activity = match.activityInfo ?: continue
                val app = activity.applicationInfo ?: continue
                val isSystem = (app.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0
                val label = match.loadLabel(manager)?.toString()?.trim().orEmpty()
                if (!SettingsCandidatePolicy.accepts(activity.packageName, label, isSystem, activity.exported, context.packageName)) continue
                val component = ComponentName(activity.packageName, activity.name)
                found.putIfAbsent(component, SettingsEntry(label.ifEmpty { activity.packageName }, activity.packageName, component, category))
            }
        }
        return found.values.sortedWith(compareBy(
            { if (it.category == LEANBACK_SETTINGS) 0 else 1 },
            { SettingsCandidatePolicy.priority(it.packageName) },
            { if (it.label.equals("Settings", true) || it.label == "设置" || it.label == "設定") 0 else 1 },
            { it.label },
        ))
    }
}
