package com.lzui.apkupgrade.data;

public class AppProperty {

    /**
     * app 兼容升级 未安装不安装，已安装当前版本低升级
     */
    public final static int APP_COMPATIBLE_UPGRADE = 0;

    /**
     * app 强制升级 未安装不安装，版本和线上不一致升级
     */
    public final static int APP_FORCE_UPGRADE = 1;

    /**
     * app 推荐升级 未安装安装，已安装当前版本低升级
     */
    public final static int APP_FORCE_RECOMM = 2;

}
