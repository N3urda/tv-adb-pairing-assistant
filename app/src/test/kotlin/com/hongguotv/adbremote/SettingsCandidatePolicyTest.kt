package com.hongguotv.adbremote

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsCandidatePolicyTest {
    @Test fun acceptsExportedSystemSettings() {
        assertTrue(SettingsCandidatePolicy.accepts("com.android.tv.settings", "设置", true, true, "com.hongguotv.adbremote"))
        assertTrue(SettingsCandidatePolicy.accepts("com.tcl.tvsettings", "电视设置", true, true, "com.hongguotv.adbremote"))
    }

    @Test fun excludesUntrustedOrUnlaunchableEntries() {
        assertFalse(SettingsCandidatePolicy.accepts("com.example.settings", "设置", false, true, "com.hongguotv.adbremote"))
        assertFalse(SettingsCandidatePolicy.accepts("com.android.tv.settings", "设置", true, false, "com.hongguotv.adbremote"))
        assertFalse(SettingsCandidatePolicy.accepts("com.hongguotv.adbremote", "设置", true, true, "com.hongguotv.adbremote"))
        assertFalse(SettingsCandidatePolicy.accepts("com.android.youtube.tv", "YouTube", true, true, "com.hongguotv.adbremote"))
    }
}
