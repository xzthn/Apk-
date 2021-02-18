package com.lzui.apkupgrade.biz;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;

import com.alibaba.fastjson.JSON;
import com.lunzn.systool.pkg.ApkManageBiz;
import com.lunzn.tool.log.LogUtil;
import com.lunzn.tool.util.CommonUtil;
import com.lzui.apkupgrade.bean.ExtraBean;
import com.lzui.apkupgrade.data.AppProperty;
import com.lzui.apkupgrade.net.MVDeviceConfig;
import com.platform.sdk.m2.bean.launcher.LauncherAppUpgrade;
import com.smart.net.SendCmd;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.Consumer;

public class PkgInstallObserver implements ApkManageBiz.PackageInstallListener {

    private static final String TAG = "app_upgrade";

    private Context mContext;
    private String mApkFilepath;
    private LauncherAppUpgrade mAppUpgrade;
    private ActivityManager mManager;
    private BootUpgradeBiz.InstallCompCallback mCompCallback;

    public PkgInstallObserver(Context context, String apkFilepath, LauncherAppUpgrade appUpgrade, BootUpgradeBiz.InstallCompCallback compCallback) {
        mContext = context;
        mApkFilepath = apkFilepath;
        mAppUpgrade = appUpgrade;
        mManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        mCompCallback = compCallback;
    }

    @Override
    public void packageInstalled(String packageName, int returnCode) {
        LogUtil.i(TAG, "packageInstalled return packageName: " + mAppUpgrade + ", returnCode: " + returnCode);
        if (returnCode == INSTALL_SUCCEEDED) {
            LogUtil.i(TAG, "packageInstalled success, then delete local file mApkFilepath: " + mApkFilepath);
            deleteLoadFile();
            LogUtil.i(TAG, "packageInstalled success, check open app, action:  " + mAppUpgrade.getAction());
            // 如果配置了安装完成之后打开的命令，则直接打开
            List<String> actions = sortNumber(mAppUpgrade.getAction());
            checkReboot(mAppUpgrade.getExtra());
            if ("com.lzpd.infraredemit".equals(packageName)) {
                killZhikongAndKukong();
            }
            if (mAppUpgrade.getType() == AppProperty.APP_FORCE_RECOMM && actions.contains("8")) {
                openApp();
            } else if (actions.contains("16")) {
                startService(packageName);
            }
            mCompCallback.onIntallComplete(true);
        } else {
            if (returnCode == INSTALL_FAILED_ALREADY_EXISTS || returnCode == INSTALL_FAILED_VERSION_DOWNGRADE || returnCode == INSTALL_FAILED_UPDATE_INCOMPATIBLE) {
                LogUtil.i(TAG, "packageInstalled failed delete local apk file, then install again");
                PkgDeleteObserver deleteObserver = new PkgDeleteObserver(mContext, mApkFilepath, mAppUpgrade, mCompCallback);
                // 如果本地apk存在说明签名冲突，服务器版本比本地版本低，则直接卸载掉本地的版本，重新安装服务器的版本
                try {
                    ApkManageBiz.autoUninstallApk(mContext, mAppUpgrade.getPkname(), deleteObserver);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mCompCallback.onIntallComplete(false);
            }
        }
    }

    /**
     * 启动服务
     */
    private void startService(String packageName) {
        LogUtil.i(TAG, "startService packageName " + packageName);
        String extra = mAppUpgrade.getExtra();
        if (CommonUtil.isNotEmpty(extra)) {
            ExtraBean bean = null;
            try {
                bean = JSON.parseObject(extra, ExtraBean.class);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.i(TAG, "startService JSON.parseObject err, extra " + extra);
            }
            LogUtil.i(TAG, "startService bean " + bean);
            if (bean != null) {
                Intent intent = new Intent();
                intent.setPackage(packageName);
                if (bean.getClassName() != null) {
                    intent.setClassName(packageName, bean.getClassName());
                }
                if (bean.getAction() != null) {
                    intent.setAction(bean.getAction());
                }
                LogUtil.i(TAG, "startService intent " + CommonUtil.toUri(intent));
                mContext.startService(intent);
            }
        }
    }

    /**
     * 判断升级完成之后是否需要重启机器
     */
    public static void checkReboot(String extra) {
        if (CommonUtil.isNotEmpty(extra)) {
            ExtraBean bean = null;
            try {
                bean = JSON.parseObject(extra, ExtraBean.class);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.i(TAG, "checkReboot JSON.parseObject err, extra " + extra);
            }
            LogUtil.i(TAG, "checkReboot bean " + bean);
            if (bean != null && bean.getReboot() == 1) {
                MVDeviceConfig.needReboot = true;
            }
        }
    }

    /**
     * 升级红外转发器之后 kill 掉酷控喝魔方智控
     */
    private void killZhikongAndKukong() {
        Observable.timer(3, TimeUnit.SECONDS).subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                // 杀掉魔方智控
                int controlPid = getProcessId("com.lz.smartcontrol");
                LogUtil.i(TAG, "kill mofang zhikoing pid: " + controlPid);
                SendCmd.get().exec("kill " + controlPid);

                // 杀掉酷控
                int kukongPid = getProcessId("com.kookong.tvplus");
                LogUtil.i(TAG, "kill kukong pid: " + kukongPid);
                SendCmd.get().exec("kill " + kukongPid);
            }
        });
    }

    private int getProcessId(String processName) {
        int result = -1;
        List<ActivityManager.RunningAppProcessInfo> appProcessList = mManager
                .getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessList) {
            if (processName.equals(appProcessInfo.processName)) {
                result = appProcessInfo.pid;
                break;
            }
        }
        LogUtil.i(TAG, "getProcessId id:  " + result);
        return result;
    }

    /**
     * 删除本地文件
     */
    private void deleteLoadFile() {
        // 安装完 删除本地文件
        if (mApkFilepath != null) {
            File file = new File(mApkFilepath);
            try {
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 如果应用推荐，配置了安装打开，则直接打开
     */
    private void openApp() {
        LogUtil.i(TAG, "openApp");
        try {
            Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mAppUpgrade.getPkname());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据给出的复合数值进行拆分
     *
     * @param num 要拆分的数值
     * @return List<int> 排序结果
     */
    private List<String> sortNumber(int num) {
        int[] sort = new int[]{1, 2, 4, 8, 16};
        List<String> result = new ArrayList<String>();
        // 排序
        for (int aSort : sort) {
            if ((aSort & num) == aSort) {
                result.add(String.valueOf(aSort));
            }
        }
        return result;
    }
}
