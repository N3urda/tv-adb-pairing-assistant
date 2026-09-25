# ADB 配对助手 · Android TV

独立的 Kotlin Android TV 应用，用遥控器打开系统开发者选项，并按电视上的步骤完成无线 ADB 配对。最低 Android 8.0（API 26）；无线调试配对码功能需要电视系统本身提供，通常见于 Android 11 及更新版本。

应用包名 `com.hongguotv.adbremote`。它不含播放器代码，不请求网络、无障碍或系统设置写入权限，也不收集数据。源码按 [GPL-3.0](LICENSE) 开放。

## 作用与权限边界

- 主按钮调用 Android 公开的 `Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS`。在 AOSP TV 的开发者选项里，用方向键找到「无线调试」，然后选择「使用配对码配对设备」。
- 找不到开发者选项时，可打开「设备信息」查找版本号；各品牌启用方式和菜单名称可能不同。若专用入口未提供，应用尝试打开系统设置。
- 普通第三方应用不能通过公开 API 自行开启 ADB、替电视生成配对码或批准电脑。配对码与配对端口由电视系统设置提供，首次授权必须在电视上完成。本应用没有无障碍服务；电视厂商设置页本身的遥控器兼容性需要在该机型上确认。

依据：[Android 设置入口 API](https://developer.android.com/reference/android/provider/Settings#ACTION_APPLICATION_DEVELOPMENT_SETTINGS)、[AOSP TV 设置实现](https://android.googlesource.com/platform/packages/apps/TvSettings/+/refs/heads/android14-release/Settings/AndroidManifest.xml)、[AOSP 无线 ADB 配对架构](https://android.googlesource.com/platform/packages/modules/adb/+/HEAD/docs/dev/adb_wifi.md)。

## 安装与配对

从 [Releases](https://github.com/N3urda/tv-adb-pairing-assistant/releases) 下载标为 **release-signed** 的 APK。ADB 尚未配对时，用 U 盘或电视已有的文件传输/安装渠道安装。若此前安装过本仓库的 debug 签名 APK，需先卸载旧 APK，才能安装 release 签名版本；本应用不保存用户数据。

打开「ADB 配对助手」，按遥控器确定键进入系统开发者选项。进入「无线调试」，打开它，再选择「使用配对码配对设备」。Mac 与电视应在同一局域网，在 Mac 上运行以下命令，并将占位符替换成电视当前显示的值：

```sh
adb pair TV_IP:PAIR_PORT
# 输入电视显示的六位配对码
adb connect TV_IP:CONNECT_PORT
```

`PAIR_PORT` 是配对页显示的端口；`CONNECT_PORT` 是无线调试主页面显示的连接端口。两者不同，且可能变化。`_adb-tls-connect._tcp` 广播的是连接端口。更多说明见 [Android 官方无线调试指南](https://developer.android.com/studio/run/device#wireless)。

## 从源码构建

需要 JDK 17、Android SDK Platform 36 / Build Tools 36.0.0。仓库包含 Gradle 8.14.3 wrapper。设置 `ANDROID_HOME` 或在仓库根目录创建不入库的 `local.properties` 后运行：

```sh
./gradlew :app:assembleDebug :app:lintDebug
```

开发 APK 位于 `app/build/outputs/apk/debug/app-debug.apk`，使用本机 Android debug 签名。公开 Release 使用独立保管的 release 私钥签名；私钥与密码均不在仓库。自己构建 release 时，将含 `storeFile`、`storePassword`、`keyAlias`、`keyPassword` 的 properties 文件放在仓库之外，设置 `TV_ADB_SIGNING_PROPERTIES` 为其绝对路径，再运行 `./gradlew :app:assembleRelease`。不要发布未签名的 release 产物。

## 验证范围

Android TV 模拟器上验证过安装、遥控器焦点与滚动、进入 AOSP 系统开发者选项和无线调试页，以及返回助手后的焦点恢复。尚未在具体品牌电视上验证安装、配对码弹窗或 Mac 与电视的实际配对连接。
