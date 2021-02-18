package com.lzui.apkupgrade.net;

import com.lunzn.tool.util.CommonUtil;
import com.lunzn.tool.util.NetUtil;
import com.lzui.apkcheck.BuildConfig;

/**
 * 全局环境变量
 *
 * @author admin
 * @version 1.0
 * @date 2017-12-11
 * @project com.lzui.apkupgrade
 * @package com.lzui.apkupgrade.net
 * @package MVDeviceConfig.java
 */
public class MVDeviceConfig {

    private static String[] URL = new String[]{"http://" + BuildConfig.BASE_URL + "/m2/"};

    /**
     * 是否升级完需要重启
     */
    public static boolean needReboot;

    /**
     * 获取备份的域名地址
     */
    public static String[] backURLs = null;

    /**
     * 获取URL地址
     *
     * @return String[] URL地址
     */
    public static String[] getUrls() {
        return URL;
    }

    /**
     * 获取域名
     *
     * @return
     */
    public static String getRealmName() {

        return BuildConfig.BASE_URL;
    }

    /**
     * 获取备份的域名地址
     *
     * @return
     */
    public static String[] getBackURLs() {
        if (!CommonUtil.isEmpty(backURLs)) {
            return backURLs;
        }
        backURLs = NetUtil.GetDomainMapping(URL);
        return backURLs;
    }

}