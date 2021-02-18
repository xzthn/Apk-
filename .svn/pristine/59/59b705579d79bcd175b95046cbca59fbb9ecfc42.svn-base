package com.lzui.apkupgrade.biz;

import android.content.Context;

import com.lunzn.systool.pkg.ApkManageBiz;
import com.lunzn.tool.log.LogUtil;
import com.platform.sdk.m2.bean.launcher.LauncherAppUpgrade;

public class PkgDeleteObserver implements ApkManageBiz.PackageDeleteListener {

    private static final String TAG = "PkgDeleteObserver";

    private Context mContext;
    private String mApkFilepath;
    private LauncherAppUpgrade mAppUpgrade;
    private BootUpgradeBiz.InstallCompCallback mCompCallback;

    public PkgDeleteObserver(Context context, String apkFilepath, LauncherAppUpgrade appUpgrade, BootUpgradeBiz.InstallCompCallback compCallback) {
        mContext = context;
        mApkFilepath = apkFilepath;
        mAppUpgrade = appUpgrade;
        mCompCallback = compCallback;
    }

    @Override
    public void packageDeleted(String packageName, int returnCode) {
        LogUtil.i(TAG, "packageDeleted mPkgName " + mAppUpgrade.getPkname() + ";  returnCode:  " + returnCode);
        if (returnCode == 1) {
            PkgInstallObserver observer = new PkgInstallObserver(mContext, mApkFilepath, mAppUpgrade, mCompCallback);
            try {
                LogUtil.i(TAG, "packageDeleted success, then install again");
                ApkManageBiz.autoInstallApk(mContext, mApkFilepath, mAppUpgrade.getPkname(), observer);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }else {
            mCompCallback.onIntallComplete(false);
        }
    }
}
