package com.hongguotv.adbremote

import java.util.Locale

/** Limits the picker to exported, system-owned Settings activities. */
internal object SettingsCandidatePolicy {
    fun accepts(packageName: String, label: String, isSystem: Boolean, isExported: Boolean, ownPackage: String): Boolean {
        if (!isSystem || !isExported || packageName == ownPackage) return false
        val text = "$packageName $label".lowercase(Locale.ROOT)
        return text.contains("settings") || text.contains("设置") || text.contains("設定")
    }

    fun priority(packageName: String): Int = when (packageName) {
        "com.android.tv.settings" -> 0
        "com.android.settings" -> 1
        else -> 2
    }
}
