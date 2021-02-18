package com.lzui.apkupgrade.data;

public class ExternalActions {

    /**
     * 立即检测升级
     */
    public final static String EVENT_APP_UPGRADE_IMMEDIATE = "app_upgrade_immediate";

    /**
     * 推送的apk升级指令
     */
    public final static String EVENT_APP_UPGRADE_FORCED = "app_upgrade";

    /**
     * 内部检测apk升级，如果升级过则不作请求
     */
    public final static String EVENT_APP_UPGRADE_CHECK = "app_upgrade_check";

    /**
     * 应用升级检测
     */
    public static final String CHECK_APPS_UPGRADE = "CHECK_APPS_UPGRADE";

    /**
     * 推送固件升级
     */
    public final static String EVENT_CHECK_FIRMWARE_UPGRADE = "CHECK_FIRMWARE_UPGRADE";

    /**
     *  推送安装单个应用
     */
    public final static String EVENT_SINGLE_APP_INSTALL = "SINGLE_APP_INSTALL";

    /**
     * 推送卸载单个应用
     */
    public final static String EVENT_SINGLE_APP_UNINSTALL = "SINGLE_APP_UNINSTALL";

    /**
     * Launcher广告播放完成，可以进行升级检测（本项目定义的 action）
     */
    public final static String EVENT_LAUNCHER_ADV_COMPLETED = "launcher_adv_complete";

    /**
     * Launcher广告播放完成广播action（在 launcher 项目中定义的）
     */
    public final static String ACTION_LAUNCHER_ADV_COMPLETED = "com.lzui.launcher.adv.completed";

}
