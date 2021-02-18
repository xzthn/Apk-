package com.lzui.apkupgrade.util;

import android.content.Context;

import com.lunzn.download.command.StorageUtils;


public class FileSavePathUtil {

    /**
     * 安装apk完成
     */
    public static final int MSG_INSTALL_COMPELETE = 0X1000001;
    /**
     * 获取app升级数据成功
     */
    public final static int MSG_GET_APP_UPGRADE_SUCCESS = 0X1000002;

    /**
     * 获取app黑名单数据成功
     */
    public final static int MSG_GET_APP_BLACKLIST_SUCCESS = 0X1000003;
    /**
     * APK下载的保存路径
     */
    public static String mApkLoadSavePath = null;
    /**
     * 下载的临时路径
     */
    public static String mApkTempCachePath = null;

    public static void initCachePath(Context context) {
        String apkSaveDir = StorageUtils.getCacheDirectory(context).getAbsolutePath();
        mApkLoadSavePath = apkSaveDir + "/download/";
        mApkTempCachePath = apkSaveDir + "/temp/";
    }
}
    
    
