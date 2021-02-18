package com.realsil.ota;

import android.annotation.SuppressLint;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.android.settings.porting.PrivateSettingCommon;
import com.google.gson.Gson;
import com.lunzn.download.FinalFileLoad;
import com.lunzn.download.command.DownloadUrlInfo;
import com.lunzn.download.util.IDownloadInfoCallBack;
import com.lunzn.tool.log.LogUtil;
import com.lunzn.tool.util.CommonUtil;
import com.realsil.android.blehub.dfu.BinInputStream;
import com.realsil.android.blehub.dfu.GlobalGatt;
import com.realsil.android.blehub.dfu.RealsilDfu;
import com.realsil.android.blehub.dfu.RealsilDfuCallback;
import com.realsil.api.ApiRetrofit;
import com.realsil.api.ApiService;
import com.realsil.api.RespParam;
import com.realsil.api.UpgradeResp;
import com.realsil.sdk.dfu.DfuConstants;
import com.realsil.sdk.dfu.image.BinIndicator;
import com.realsil.sdk.dfu.model.DfuConfig;
import com.realsil.sdk.dfu.model.DfuProgressInfo;
import com.realsil.sdk.dfu.model.OtaDeviceInfo;
import com.realsil.sdk.dfu.model.OtaModeInfo;
import com.realsil.sdk.dfu.model.Throughput;
import com.realsil.sdk.dfu.utils.BaseDfuAdapter;
import com.realsil.sdk.dfu.utils.ConnectParams;
import com.realsil.sdk.dfu.utils.DfuAdapter;
import com.realsil.sdk.dfu.utils.DfuUtils;
import com.realsil.sdk.dfu.utils.GattDfuAdapter;
import com.smart.localfile.LocalFileCRUDUtils;
import com.smart.localfile.LocalFileZipUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import okhttp3.RequestBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Desc: 固件升级操作类
 * <p>
 * Author: meijie
 * PackageName: com.realsil.ota
 * ProjectName: APKCheck
 * Date: 2018/11/22 16:04
 */
public class DfuController {

    private static final String TAG = "DfuController";
    /*
     * DFU Service UUID
     */
    public static final UUID DFU_SERVICE_UUID = UUID.fromString("00006287-3c17-d293-8e48-14fe2e4da212");
    private static final int OTA_FIND_CHARAC_SUCCESS = 0;
    private static final int OTA_GATT_CONNECTIION_FAIL = 1;
    private static final int OTA_GATT_DISCOVERY_FAIL = 2;
    private static final int OTA_GET_SERVICE_FAIL = 3;
    private static final int OTA_GET_CHARA_FAIL = 4;
    private static final int OTA_GET_TARGET_APP_VERSION = 6;
    private static final int OTA_GET_TARGET_PATCH_VERSION = 7;
    private static final int OTA_CALLBACK_STATE_CHANGE = 10;
    private static final int OTA_CALLBACK_PROCESS_CHANGE = 11;
    private static final int OTA_CALLBACK_SUCCESS = 12;
    private static final int OTA_CALLBACK_ERROR = 13;
    private static final int OTA_GET_DFU_SERVICE = 14;
    private static final int OTA_GET_SILENT_MODE_SERVICE = 15;
    private static final int OTA_GET_TARGET_BANK_INFO = 16;
    private static final int OTA_GET_TARGET_PATCH_EXTENSION_INFO = 17;
    private static final int OTA_GET_EXTEND_MODE_SERVICE = 18;
    private static final int OTA_CONNECT = 19;
    private static final int OTA_WAIT_FOR_REFRESH = 20;
    private static final int NOTIFY_UNPAIR = 21;
    //正在传输中
    private static final int OTA_THIRD_PROGRESS = 23;
    //升级成功
    private static final int OTA_THIRD_SUCCESS = 24;
    //升级状态改变
    private static final int OTA_THIRD_STATE_STARTED = 25;
    private final static UUID OTA_SERVICE_UUID = UUID.fromString("0000ffd0-0000-1000-8000-00805f9b34fb");
    private static final UUID NEW_OTA_SERVICE_UUID = UUID.fromString("0000d0ff-3c17-d293-8e48-14fe2e4da212");
    private final static UUID OTA_READ_PATCH_CHARACTERISTIC_UUID = UUID.fromString("0000ffd3-0000-1000-8000-00805f9b34fb");
    private final static UUID OTA_READ_APP_CHARACTERISTIC_UUID = UUID.fromString("0000ffd4-0000-1000-8000-00805f9b34fb");
    private static final UUID GATT_DEVICE_SERVICE_UUID = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");// added by jmf20161024
    private static final UUID GATT_FIRMWARE_VERSION_CHARACTERISTIC_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");// added by jmf 20161024
    // Add for silent OTA upload.
    private final static UUID OTA_READ_BANK_CHARACTERISTIC_UUID = UUID.fromString("0000fff1-0000-1000-8000-00805f9b34fb");
    // Add for extend OTA upload.
    private final static UUID OTA_EXTEND_FLASH_CHARACTERISTIC_UUID = UUID.fromString("00006587-3c17-d293-8e48-14fe2e4da212");
    // Add for read patch extend.
    private final static UUID OTA_READ_PATCH_EXTENSION_CHARACTERISTIC_UUID = UUID.fromString("0000ffd5-0000-1000-8000-00805f9b34fb");
    @SuppressLint("StaticFieldLeak")
    private static StepDialog mStepDialog; // 升级对话框
    private String mDeviceAddress;
    private long mOtaStartTime = 0;
    private long mOtaImageSendStartTime = 0;
    private int oldPatchVersion;
    private BluetoothGattCharacteristic mReadPatchCharacteristic;
    private BluetoothGattCharacteristic mReadBankCharacteristic;
    private BluetoothGattCharacteristic mReadPatchExtensionCharacteristic;

    // dfu object
    private RealsilDfu dfu = null;
    private BluetoothGatt mBtGatt;
    private BluetoothDevice mSelectedDevice;
    private GlobalGatt mGlobalGatt;
    private int mFileCount; // 待安装文件个数
    private int mFileIndex; // 正在安装的文件
    private String mDeviceName;
    private boolean mIsZip; // 下载的是否是 zip 包
    private String[] mPaths; // 下载的 bin 路径列表
    private String mDir; // 文件下载父目录
    private String mFirmwareVersion;
    private boolean mIsUpdating = false; // 是否是正在升级
    private boolean mNeedRequest; // 是否需要发起升级检测，过滤多次网络连接发起的升级请求
    private Application mApplication;
    private boolean mIsNameMatch; // 升级前的遥控器名称是否与升级后遥控器名称相同
    private BluetoothDevice mBluetoothDevice;

    //是否获取到了历史版本
    private volatile boolean isGetTargePatchVersion = false;
    //是否获取到了fireversion请求升级
    private volatile boolean isGetFirmwareVersion = false;


    /**
     * 第三代遥控器升级所需
     */
    private GattDfuAdapter mDfuHelper;
    private OtaDeviceInfo mOtaDeviceInfo;
    public static final int STATE_INIT = 0x0000;
    public static final int STATE_OTA_PROCESSING = 0x0400;
    public static final int STATE_ABORTED = 0x0800;
    public static final int STATE_OTA_SUCCESS = STATE_ABORTED | 0x01;
    public static final int STATE_OTA_ERROR = STATE_ABORTED | 0x02;
    protected int mProcessState;
    protected int mState = STATE_INIT;
    private DfuConfig mDfuConfig;


    @SuppressWarnings("handlerLeak")
    private Handler mHandle = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            LogUtil.d(TAG, "MSG No " + msg.what);
            switch (msg.what) {
                case NOTIFY_UNPAIR:
                    notifyReconnect();
                    break;
                case OTA_FIND_CHARAC_SUCCESS:
                    showToast(R.string.toast_gatt_connect);
                    // set the dfu service work mode
                    if (!dfu.setWorkMode(RealsilDfu.OTA_MODE_FULL_FUNCTION)) {
                        showToast(R.string.toast_some_error_in_set_ota_mode);
                        mGlobalGatt.disconnectGatt(mDeviceAddress);
                    }
                    break;
                case OTA_GET_SILENT_MODE_SERVICE:
                    showToast(R.string.toast_gatt_connect);
                    break;
                case OTA_GET_EXTEND_MODE_SERVICE:
                    showToast(R.string.toast_gatt_connect);
                    int mode = RealsilDfu.OTA_MODE_EXTEND_FUNCTION;
                    // set the dfu service work mode
                    if (!dfu.setWorkMode(mode)) {
                        showToast(R.string.toast_some_error_in_set_ota_mode);
                        mGlobalGatt.disconnectGatt(mDeviceAddress);
                    }
                    break;
                case OTA_GET_TARGET_PATCH_VERSION:
                    obtainMessage(OTA_GET_TARGET_APP_VERSION).sendToTarget();
                    break;
                case OTA_GET_TARGET_APP_VERSION:
                    // 获取到版本号
                    // 请求升级信息
                    removeMessages(OTA_GET_TARGET_APP_VERSION);
                    LogUtil.i("OTA_GET_TARGET_APP_VERSION isGetTargePatchVersion =" + isGetTargePatchVersion + "_isGetFirmwareVersion =" + isGetFirmwareVersion);
                    //只有历史版本 以及TargeVersion 都获取到的场景下才开始升级
                    if (isGetTargePatchVersion && isGetFirmwareVersion) {
                        if (isNotInOtaProcessing()) {
                            requestUpgrade();
                        }
                    }
                    break;
                case OTA_GET_DFU_SERVICE:
                    // in dfu mode can not get version info
                    showToast(R.string.toast_dfu_gatt_connect);
                    // set the dfu service work mode
                    if (!dfu.setWorkMode(RealsilDfu.OTA_MODE_LIMIT_FUNCTION)) {
                        showToast(R.string.toast_some_error_in_set_ota_mode);
                        mGlobalGatt.disconnectGatt(mDeviceAddress);
                    }
                    break;
                case OTA_CALLBACK_PROCESS_CHANGE:
                    if (mOtaImageSendStartTime == 0) {
                        mOtaImageSendStartTime = System.currentTimeMillis();
                    }
                    mStepDialog.setProgress(msg.arg1);
                    break;
                case OTA_CALLBACK_STATE_CHANGE:
                    switch (msg.arg1) {
                        case RealsilDfu.STA_ORIGIN:
                            LogUtil.i("STA_ORIGIN");
                            break;
                        case RealsilDfu.STA_REMOTE_ENTER_OTA:
                            LogUtil.i("STA_REMOTE_ENTER_OTA");
                            break;
                        case RealsilDfu.STA_FIND_OTA_REMOTE:
                            LogUtil.i("STA_FIND_OTA_REMOTE");
                            break;
                        case RealsilDfu.STA_CONNECT_OTA_REMOTE:
                            LogUtil.i("STA_CONNECT_OTA_REMOTE");
                            break;
                        case RealsilDfu.STA_START_OTA_PROCESS:
                            LogUtil.i("STA_START_OTA_PROCESS");
                            break;
                        case RealsilDfu.STA_OTA_UPGRADE_SUCCESS:
                            LogUtil.i("STA_OTA_UPGRADE_SUCCESS");
                            break;
                        default:
                            break;
                    }
                    break;
                case OTA_CALLBACK_SUCCESS:
                    showToast(String.format(mApplication.getString(R.string.toast_ota_success), msg.arg1 + ""));
                    long currentTime = System.currentTimeMillis();
                    LogUtil.i(TAG, "Transfer of Ota progress has taken " + (currentTime -
                            mOtaStartTime) + " ms");
                    LogUtil.i(TAG, "Transfer of Ota image has taken " + (currentTime - mOtaImageSendStartTime) + " ms");

                    if (mFileIndex == mFileCount) {
                        mStepDialog.showSuccess();
                        mIsUpdating = false;
                        LogUtil.v("misupdating=false 1111111");
                        close();
                        if (!mIsNameMatch) {
                            LogUtil.w(TAG, "遥控器名称不匹配，需要断开遥控器，重新配对");
                            // 断开配对
                            m_Interface.unPair(mBluetoothDevice);
                            mHandle.sendEmptyMessageDelayed(NOTIFY_UNPAIR, 2 * 1000);
                        }
                    } else {
                        if (mGlobalGatt != null) {
                            mGlobalGatt.close(mDeviceAddress);
                        }
                        // 传输成功后需要等 profile 刷新，大约需要 10s
                        mHandle.sendEmptyMessageDelayed(OTA_WAIT_FOR_REFRESH, 10 * 1000);
                    }
                    break;

                case OTA_THIRD_SUCCESS: {
                    mStepDialog.showSuccess();
                    mIsUpdating = false;
                    LogUtil.v("misupdating=false 2222222");
                    close();
                    if (!mIsNameMatch) {
                        LogUtil.w(TAG, "遥控器名称不匹配，需要断开遥控器，重新配对");
                        // 断开配对
                        m_Interface.unPair(mBluetoothDevice);
                        mHandle.sendEmptyMessageDelayed(NOTIFY_UNPAIR, 2 * 1000);
                    }
                    break;
                }
                case OTA_THIRD_STATE_STARTED: {
                    mStepDialog.setProgress(0);
                    break;
                }

                case OTA_THIRD_PROGRESS: {
                    mStepDialog.setProgress(msg.arg1);
                    mStepDialog.setStep((String) msg.obj);
                    break;
                }
                case OTA_WAIT_FOR_REFRESH:
                    // 继续安装
                    LogUtil.i(TAG, "继续安装=================");
                    startOtaProcess(mPaths[mFileIndex]);
                    break;
                case OTA_CONNECT:
                    LogUtil.v("ota_connect:" + mOtaDeviceInfo);
                    if (null == mOtaDeviceInfo) {
                        connectDevice();
                    } else {
                        connect();
                    }
                    break;
                case OTA_CALLBACK_ERROR:
                    mIsUpdating = false;
                    LogUtil.v("misupdating=false 3333333");
                    showToast(String.format(mApplication.getString(R.string.toast_ota_failed), msg.arg1 + ""));
                    // 269 为电池电量低
                    if(null!=mStepDialog){
                        if (msg.arg1 == 269) {
                            mStepDialog.showLowBattery();
                        } else {
                            mStepDialog.showFailure();
                        }
                    }
                    close();
                    break;
                case OTA_GATT_CONNECTIION_FAIL:
                case OTA_GATT_DISCOVERY_FAIL:
                case OTA_GET_CHARA_FAIL:
                case OTA_GET_SERVICE_FAIL:
                    mIsUpdating = false;
                    close();
                    LogUtil.v("misupdating=false 44444");
                    showToast(String.format(mApplication.getString(R.string.toast_ota_initial_connect_failed), msg.what + ""));
                    break;
                default:
                    break;
            }
        }
    };

    private void notifyReconnect() {
        // 通知 remoteConnect 重新连接
        Intent pairIntent = new Intent("com.lunzn.remote.connect.handler");
        pairIntent.setPackage("com.lzui.remoteconnect");
        pairIntent.putExtra("type", "home_check_start_scan");
        mApplication.startService(pairIntent);
    }

    private DfuController() {
    }

    private static class HOLDER {
        private static DfuController INSTANCE = new DfuController();
    }

    // 使用单例
    public static DfuController getInstance() {
        return HOLDER.INSTANCE;
    }

    private void requestUpgrade() {
        LogUtil.i(TAG, "requestUpgrade");
        RespParam param = new RespParam();
        param.setPackageName(mApplication.getPackageName());
        param.setChannel(mDeviceName);
        List<RespParam.UpgradeTypesBean> types = new ArrayList<>();
        types.add(new RespParam.UpgradeTypesBean("firmware", mFirmwareVersion, mOtaDeviceInfo.icType));
        types.add(new RespParam.UpgradeTypesBean("patch", String.valueOf(oldPatchVersion), mOtaDeviceInfo.icType));
        param.setUpgradeTypes(types);
        String obj = new Gson().toJson(param);// json
        LogUtil.i(TAG, "requestUpgrade " + obj);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), obj);
        ApiRetrofit.getInstance().getService(ApiService.class).getUpgrade(body)
                .subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UpgradeResp>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        LogUtil.e(TAG, "requestUpgrade onError " + e);
                        mIsUpdating = false;
                        LogUtil.v("misupdating=false 5555555");
                        mNeedRequest = true;
                    }

                    @Override
                    public void onNext(UpgradeResp upgradeResp) {
                        LogUtil.i(TAG, "requestUpgrade " + upgradeResp);
                        if (upgradeResp.getRetCode() == 0) {
                            // 请求成功
                            List<UpgradeResp.UpgradeListBean> upgradeList = upgradeResp.getUpgradeList();
                            if (upgradeList != null && !upgradeList.isEmpty()) {
                                UpgradeResp.UpgradeListBean bean = upgradeList.get(0);
                                // 判断升级遥控器前后名称是否相同
                                mIsNameMatch = mDeviceName.equals(bean.getDstChannel());
                                downloadFile(bean);
                            } else {
                                LogUtil.i(TAG, "requestUpgrade 没有升级信息 ");
                                close();
                                mIsUpdating = false;
                                LogUtil.v("misupdating=false 66666666");
                            }
                        } else {
                            LogUtil.e(TAG, "requestUpgrade 业务处理失败 " + upgradeResp.getRetCode());
                            close();
                            mIsUpdating = false;
                            LogUtil.v("misupdating=false 777777");
                        }
                    }
                });
    }

    private void downloadFile(UpgradeResp.UpgradeListBean bean) {
        String url = bean.getDownloadUrl();
        long checkCode = 0L;
        try {
            checkCode = Long.parseLong(bean.getCheckCode());
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        FinalFileLoad mFileLoad = new FinalFileLoad(mApplication);
        DownloadUrlInfo[] downloadInfo = new DownloadUrlInfo[1];
        Map<String, Long> codeMap = new HashMap<>();
        codeMap.put(url, checkCode);
        downloadInfo[0] = new DownloadUrlInfo(url, 3, codeMap);

        String apkName = url.substring(url.lastIndexOf("/"));
        mIsZip = url.endsWith(".zip");

        String path = mDir + "/" + apkName;
        LogUtil.i(TAG, "文件路径 path " + path + ", checkCode " + checkCode);

        String cachePath = mDir + "/tmp/" + apkName;
        downloadInfo[0].setFileLocalPath(path);
        downloadInfo[0].setFileName(apkName);
        LogUtil.i(TAG, "downloadFile path: " + downloadInfo[0].getFileLocalPath());
        mFileLoad.downloadFile(downloadInfo, new IDownloadInfoCallBack() {
            @Override
            public void onDownloadProgressCallback(long downloadsize, long totlesize) {

            }

            @Override
            public void onDownloadCompleteCallback(String savePath) {
                LogUtil.w(TAG, "download success " + savePath);
                // 检查文件
                boolean result = true;
                if (mIsZip) {
                    // 解压文件
                    mPaths = unZip(savePath, mDir + "/zip");
                    if (mPaths == null) {
                        LogUtil.e(TAG, "解压文件失败");
                        return;
                    }
                    for (String s : mPaths) {
                        boolean b = LoadFileInfo(s);
                        if (!b) {
                            result = false;
                            break;
                        }
                    }
                } else {
                    mPaths = new String[]{savePath};
                    result = LoadFileInfo(savePath);
                }
                LogUtil.w(TAG, "download success mPaths " + Arrays.toString(mPaths));
                mFileCount = mPaths.length;
                mFileIndex = 0;
                // 删除临时文件
                LocalFileCRUDUtils.deleteFile(mDir + "/tmp");
                if (result) {
                    // 提示升级
                    showUpgrade();
                } else {
                    LogUtil.i(TAG, "加载文件失败 ");
                    mIsUpdating = false;
                    LogUtil.v("misupdating=false 888888");
                }
            }

            @Override
            public void onDownloadSpaceNotEnough(String savePath) {
                LogUtil.e(TAG, "onDownloadSpaceNotEnough ");
                mIsUpdating = false;
                LogUtil.v("misupdating=false 99999");
            }

            @Override
            public void onDownloadFail() {
                LogUtil.e(TAG, "onDownloadFail ");
                mIsUpdating = false;
                LogUtil.v("misupdating=false 1000000");
            }
        }, cachePath);
    }

    /**
     * 解压文件，删除压缩文件
     */
    private String[] unZip(String path, String dest) {
        LogUtil.i("解压文件 path " + path + ", dest " + dest);
        // 解压前删除存在的目的目录
        LocalFileCRUDUtils.deleteFile(dest);
        List<String> strings = LocalFileZipUtils.unZipFile(path, dest);
        // 删除压缩文件
        LocalFileCRUDUtils.deleteFile(path);

        String[] list = new File(dest).list();
        // 与后台协定，压缩包内为一个目录，目录下为升级的文件
        if (list != null && list.length == 1) {
            File file = new File(dest + "/", list[0]);
            LogUtil.i("解压文件 " + file.getAbsolutePath());
            File[] files = new File(dest + "/", list[0]).listFiles();
            String[] ret = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                ret[i] = files[i].getAbsolutePath();
            }
            return ret;
        }

        LogUtil.e(TAG, "解压文件出现失败 path " + path + ", strings " + strings);
        return null;
    }

    private RealsilDfuCallback cb = new RealsilDfuCallback() {
        public void onServiceConnectionStateChange(boolean status, RealsilDfu d) {
            LogUtil.e(TAG, "RealsilDfuCallback status: " + status);
            if (status) {
                showToast(R.string.toast_dfu_service_connect);
                dfu = d;
                dfu.setBatteryCheck(true);
                // 延时连接设备
                LogUtil.i("延时主动连接设备");
                mHandle.sendEmptyMessageDelayed(OTA_CONNECT, 5000);
            } else {
                showToast(R.string.toast_dfu_service_disconnect);
                dfu = null;
            }
        }

        public void onError(int e) {
            LogUtil.e(TAG, "RealsilDfuCallback onError: " + e);
            mState = STATE_ABORTED;
            Message msg = mHandle.obtainMessage(OTA_CALLBACK_ERROR);
            msg.arg1 = e;
            mHandle.sendMessage(msg);
        }

        public void onSucess(int s) {
            LogUtil.e(TAG, "onSucess: " + s);
            if (mFileIndex == mFileCount) {
                // 安装成功，删除原始文件
                LogUtil.i(TAG, "删除文件 ");
                LocalFileCRUDUtils.deleteFile(mDir + "/zip");
                mState = STATE_OTA_SUCCESS;
            }
            // send msg to update ui
            Message msg = mHandle.obtainMessage(OTA_CALLBACK_SUCCESS);
            msg.arg1 = s;
            mHandle.sendMessage(msg);

        }

        public void onProcessStateChanged(int state) {
            LogUtil.e(TAG, "onProcessStateChanged: " + state);
            Message msg = mHandle.obtainMessage(OTA_CALLBACK_STATE_CHANGE);
            msg.arg1 = state;
            mHandle.sendMessage(msg);
        }

        public void onProgressChanged(int progress) {
            Message msg = mHandle.obtainMessage(OTA_CALLBACK_PROCESS_CHANGE);
            msg.arg1 = progress;
            mHandle.sendMessage(msg);
        }
    };

    private BluetoothGattCallback mBluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            LogUtil.d(TAG, "onConnectionStateChange: status = " + status + ",newState = " + newState);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    LogUtil.d(TAG, "device connected. gatt = " + gatt);
                    mBtGatt = mGlobalGatt.getBluetoothGatt(mDeviceAddress);
                    if (gatt == null) {
                        LogUtil.e(TAG, "gatt is null");
                    } else {
                        LogUtil.d(TAG, "Start to Discovery");
                        gatt.discoverServices();
                    }
                } else if ((newState == BluetoothProfile.STATE_DISCONNECTED)) {
                    LogUtil.e(TAG, "disconnect, try to close gatt");
                    mGlobalGatt.close(mDeviceAddress);
                    mBtGatt = null;
                }

            } else {
                mHandle.sendMessage(mHandle.obtainMessage(OTA_GATT_CONNECTIION_FAIL));
                mGlobalGatt.close(mDeviceAddress);
                mBtGatt = null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            LogUtil.d(TAG, "onServicesDiscovered: status = " + status + " thread is " + Thread.currentThread());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                BluetoothGattService service = gatt.getService(OTA_SERVICE_UUID);
                if (service == null) {
                    LogUtil.w(TAG, "OTA service not found");
                    service = gatt.getService(NEW_OTA_SERVICE_UUID);
                    if (service == null) {
                        LogUtil.e(TAG, "new OTA service not found");
                        //mHandle.sendMessage(mHandle.obtainMessage(OTA_GET_SERVICE_FAIL));
                        // Try to find the DFU service, may devices is already in OTA mode
                        service = gatt.getService(DFU_SERVICE_UUID);
                        if (service == null) {
                            mHandle.sendMessage(mHandle.obtainMessage(OTA_GET_SERVICE_FAIL));
                        } else {
                            LogUtil.d(TAG, "DFU service = " + service.getUuid());
                            mHandle.sendMessage(mHandle.obtainMessage(OTA_GET_DFU_SERVICE));
                        }
                        return;
                    }
                } else {
                    LogUtil.d(TAG, "OTA service = " + service.getUuid());
                }

                BluetoothGattCharacteristic readAppCharacteristic = service.getCharacteristic(OTA_READ_APP_CHARACTERISTIC_UUID);
                if (readAppCharacteristic == null) {
                    LogUtil.e(TAG, "OTA read app characteristic not found");
                    mHandle.sendMessage(mHandle.obtainMessage(OTA_GET_CHARA_FAIL));
                    return;
                } else {
                    LogUtil.d(TAG, "mEnterOTACharacteristic = " + readAppCharacteristic.getUuid());
                    readDeviceInfo(readAppCharacteristic);
                }

                mReadPatchCharacteristic = service.getCharacteristic(OTA_READ_PATCH_CHARACTERISTIC_UUID);
                if (mReadPatchCharacteristic == null) {
                    LogUtil.e(TAG, "OTA read patch version characteristic not found");
                    mHandle.sendMessage(mHandle.obtainMessage(OTA_GET_CHARA_FAIL));
                    return;
                } else {
                    LogUtil.d(TAG, "mReadPatchCharacteristic = " + mReadPatchCharacteristic.getUuid());
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    readDeviceInfo(mReadPatchCharacteristic);
                }

                // 获取 patch 版本号
                mReadPatchExtensionCharacteristic = service.getCharacteristic(OTA_READ_PATCH_EXTENSION_CHARACTERISTIC_UUID);
                if (mReadPatchExtensionCharacteristic == null) {
                    LogUtil.w(TAG, "OTA read patch extension version characteristic not found, do not read patch extension info.");
                } else {
                    LogUtil.d(TAG, "mReadPatchExtensionCharacteristic = " + mReadPatchExtensionCharacteristic.getUuid());
                }
                // Try to find the DFU service, check the remote is support Silent work mode.
                BluetoothGattService serviceDfu = gatt.getService(DFU_SERVICE_UUID);
                if (serviceDfu == null) {
                    // If do not have dfu service, we think it is work on normal mode
                    mHandle.sendMessage(mHandle.obtainMessage(OTA_FIND_CHARAC_SUCCESS));
                } else {
                    // If do have dfu service, we think it is work on silent mode
                    LogUtil.d(TAG, "DFU service = " + serviceDfu.getUuid());
                    // Check support extend flash OTA
                    BluetoothGattCharacteristic extendFlashChara = serviceDfu.getCharacteristic(OTA_EXTEND_FLASH_CHARACTERISTIC_UUID);
                    if (extendFlashChara == null) {
                        LogUtil.e(TAG, "OTA extend flash characteristic not found");
                        mReadBankCharacteristic = service.getCharacteristic(OTA_READ_BANK_CHARACTERISTIC_UUID);
                        if (mReadBankCharacteristic == null) {
                            LogUtil.e(TAG, "OTA read bank characteristic not found");

                            mHandle.sendMessage(mHandle.obtainMessage(OTA_GET_CHARA_FAIL));
                            return;
                        } else {
                            LogUtil.d(TAG, "mReadBankCharacteristic = " + mReadBankCharacteristic.getUuid());
                        }
                        //cut by pandom on 2020.8.4
//                        BluetoothGattCharacteristic reset = service.getCharacteristic(OTA_CHARACTERISTIC_UUID);
//                        // Check support both silent and normal?
//                        if (reset != null) {
////                            runOnUiThread(new Runnable() {
////                                @Override
////                                public void run() {
////                                    showNormalOrSlientUpdateDialog();
////                                }
////                            });
//                        } else {
//                            mHandle.sendMessage(mHandle.obtainMessage(OTA_GET_SILENT_MODE_SERVICE));
//                        }
//                        return;
                    } else {

                        LogUtil.d(TAG, "extendFlashChara = " + extendFlashChara.getUuid());
                    }
                    mHandle.sendMessage(mHandle.obtainMessage(OTA_GET_EXTEND_MODE_SERVICE));
                }

                // 获取固件版本号
                final BluetoothGattService battService = gatt.getService(GATT_DEVICE_SERVICE_UUID);
                if (battService == null) {
                    LogUtil.d(TAG, "No version service");
                    return;
                }
                mReadBankCharacteristic = battService.getCharacteristic(GATT_FIRMWARE_VERSION_CHARACTERISTIC_UUID);
                if (mReadBankCharacteristic != null) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    gatt.readCharacteristic(mReadBankCharacteristic);
                } else {
                    LogUtil.e(TAG, "No version");
                }
            } else {
                LogUtil.e(TAG, "service discovery failed !!!");
                mHandle.sendMessage(mHandle.obtainMessage(OTA_GATT_DISCOVERY_FAIL));
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtil.d(TAG, "data = " + Arrays.toString(characteristic.getValue()));
                LogUtil.d(TAG, "uuid = " + characteristic.getUuid());
                if (GATT_FIRMWARE_VERSION_CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                    // 获取固件版本号
                    mFirmwareVersion = characteristic.getStringValue(0);
                    isGetFirmwareVersion = true;
                    if (isGetFirmwareVersion && isGetTargePatchVersion) {
                        Message msg = Message.obtain();
                        msg.what = OTA_GET_TARGET_APP_VERSION;
                        msg.obj = mFirmwareVersion;
                        mHandle.sendMessage(msg);
                    }
                    LogUtil.w(TAG, "old firmware version " + mFirmwareVersion);
                } else if (characteristic.getUuid().equals(OTA_READ_APP_CHARACTERISTIC_UUID)) {
                    byte[] appVersionValue = characteristic.getValue();
                    ByteBuffer wrapped = ByteBuffer.wrap(appVersionValue);
                    wrapped.order(ByteOrder.LITTLE_ENDIAN);
                    int oldFwVersion = wrapped.getShort(0);
                    LogUtil.i(TAG, "old firmware version: " + oldFwVersion + " .getValue=" + Arrays.toString(characteristic.getValue()));
                    if (mReadPatchCharacteristic != null) {
                        readDeviceInfo(mReadPatchCharacteristic);
                    }
                } else if (characteristic.getUuid().equals(OTA_READ_PATCH_CHARACTERISTIC_UUID)) {
                    byte[] patchVersionValue = characteristic.getValue();
                    ByteBuffer wrapped = ByteBuffer.wrap(patchVersionValue);
                    wrapped.order(ByteOrder.LITTLE_ENDIAN);
                    oldPatchVersion = wrapped.getShort(0);
                    isGetTargePatchVersion = true;
                    //int oldPatchVersion = (characteristic.getValue()[1] & 0xff) *256 + (characteristic.getValue()[0] & 0xff); //This method can also get oldPatchVersion.
//                    if (isGetFirmwareVersion && isGetTargePatchVersion) {
//                        Message msg = new Message();
//                        msg.what = OTA_GET_TARGET_PATCH_VERSION;
//                        msg.arg1 = oldPatchVersion;
//                        mHandle.sendMessage(msg);
//                    }
                    LogUtil.w(TAG, "old patch version: " + oldPatchVersion + " .getValue=" + Arrays.toString(characteristic.getValue()));
                    //here can add read other characteristic
                    // Check need to read patch extension info
                    if (mReadPatchExtensionCharacteristic != null) {
                        readDeviceInfo(mReadPatchExtensionCharacteristic);
                    } else if (mReadBankCharacteristic != null) {
                        readDeviceInfo(mReadBankCharacteristic);
                    }
                } else if (characteristic.getUuid().equals(OTA_READ_PATCH_EXTENSION_CHARACTERISTIC_UUID)) {
                    byte[] patchExtensionVersionValue = characteristic.getValue();
                    if (null != patchExtensionVersionValue && patchExtensionVersionValue.length > 0) {
                        ByteBuffer wrapped = ByteBuffer.wrap(patchExtensionVersionValue);
                        wrapped.order(ByteOrder.LITTLE_ENDIAN);
                        int targetPatchExtensionVersion = wrapped.getShort(0);
                        //int oldPatchVersion = (characteristic.getValue()[1] & 0xff) *256 + (characteristic.getValue()[0] & 0xff); //This method can also get oldPatchVersion.
                        Message msg = new Message();
                        msg.what = OTA_GET_TARGET_PATCH_EXTENSION_INFO;
                        msg.arg1 = targetPatchExtensionVersion;
                        mHandle.sendMessage(msg);
                        LogUtil.d(TAG, "Target patch extension version: " + targetPatchExtensionVersion + " .getValue=" + Arrays.toString(characteristic.getValue()));
                    }
                    //here can add read other characteristic
                    // Check need to read patch extension info
                    if (mReadBankCharacteristic != null) {
                        readDeviceInfo(mReadBankCharacteristic);
                    }
                } else if (characteristic.getUuid().equals(OTA_READ_BANK_CHARACTERISTIC_UUID)) {
                    byte[] bankValue = characteristic.getValue();
                    int targetBankInfo = bankValue[0];
                    //int oldPatchVersion = (characteristic.getValue()[1] & 0xff) *256 + (characteristic.getValue()[0] & 0xff); //This method can also get oldPatchVersion.
                    Message msg = new Message();
                    msg.what = OTA_GET_TARGET_BANK_INFO;
                    msg.arg1 = targetBankInfo;
                    mHandle.sendMessage(msg);
                    LogUtil.d(TAG, "Target bank: " + targetBankInfo + " .getValue=" + Arrays.toString(characteristic.getValue()));
                }
            } else {
                LogUtil.e(TAG, "Read version characteristic failure on " + gatt + " " + characteristic);
            }
        }
    };

    public void init(Application app) {
        mApplication = app;
        mDir = mApplication.getFilesDir().getAbsolutePath();
    }

    private void start() {
        LogUtil.w(TAG, "start 启动:" + mIsUpdating);
        if (mIsUpdating) {
            LogUtil.w(TAG, "downloadFile 正在检测升级中，忽略本次调用");
            return;
        }
        if(!isNotInOtaProcessing()){
            LogUtil.w(TAG, "正在传输中，忽略本次调用");
            return;
        }
        mIsUpdating = true;
        mNeedRequest = false;
        mOtaDeviceInfo = null;
        LogUtil.w(TAG, "start 启动 222222");
        GlobalGatt.initial(mApplication);
        BluetoothInputDeviceManager.initial(mApplication);
        m_Interface = PrivateSettingCommon.getBluetoothInstance(mApplication);
        if (m_Interface == null) {
            LogUtil.e(TAG, " 蓝牙连接失败");
            return;
        }
        //第三代遥控器升级要初始化的变量
        getDfuHelper().initialize(dfuCallBack);
        // get the Realsil Dfu proxy
        RealsilDfu.getDfuProxy(mApplication, cb);
        // Check whether the ble support or not
        isBLESupported();
        // request to enable BT
        BluetoothAdapter btAdapter;
        if (!isBLEEnabled()) {
            LogUtil.e(TAG, "isBLEEnabled is false");
            btAdapter = BluetoothAdapter.getDefaultAdapter();
            if (btAdapter != null) {
                boolean result = btAdapter.enable();
                LogUtil.e(TAG, "isBLEEnabled enable " + result);
            } else {
                LogUtil.e(TAG, "mBtAdapter is null");
            }
        }
        mGlobalGatt = GlobalGatt.getInstance();
        mGlobalGatt.initialize();
        LogUtil.i(TAG, "initialize mGlobalGatt " + mGlobalGatt);
    }

    private PrivateSettingCommon.BluetoothInterface m_Interface;

    private void connect() {
        mBluetoothDevice = getBluetoothDevice();
        if (mBluetoothDevice == null) {
            LogUtil.e(TAG, "蓝牙设备未连接，退出");
            close();
            mIsUpdating = false;
            LogUtil.v("misupdating=false 101101010");
            return;
        }
        mDeviceAddress = mBluetoothDevice.getAddress();
        mDeviceName = mBluetoothDevice.getName();
        LogUtil.i(TAG, "蓝牙已连接 mDeviceAddress " + mDeviceAddress + ", mDeviceName " + mDeviceName);
        connectWithGatt();
    }

    //------第三代遥控器升级所需变量 start --------
    private GattDfuAdapter getDfuHelper() {
        if (mDfuHelper == null) {
            mDfuHelper = GattDfuAdapter.getInstance(mApplication);
        }
        return mDfuHelper;
    }


    private void connectDevice() {
        //保持一个链接
        getDfuHelper().disconnect();
        mBluetoothDevice = getBluetoothDevice();
        if (mBluetoothDevice != null) {
            if (isNotInOtaProcessing()) {
                connectRemoteDevice(mBluetoothDevice, false);
            }
        } else {
            mIsUpdating = false;
        }
    }

    public boolean isNotInOtaProcessing() {
        return (mState & STATE_OTA_PROCESSING) != STATE_OTA_PROCESSING;
    }

    public void connectRemoteDevice(BluetoothDevice bluetoothDevice, boolean isHid) {
        mSelectedDevice = bluetoothDevice;
        ConnectParams.Builder connectParamsBuilder = new ConnectParams.Builder()
                .address(mSelectedDevice.getAddress())
                .hid(isHid)
                .reconnectTimes(3)
                .localName(getDfuConfig().getLocalName());

        connectParamsBuilder.otaServiceUuid(NEW_OTA_SERVICE_UUID);
        getDfuHelper().connectDevice(connectParamsBuilder.build());
    }


    protected DfuConfig getDfuConfig() {
        if (mDfuConfig == null) {
            mDfuConfig = new DfuConfig();
        }
        return mDfuConfig;
    }

    BaseDfuAdapter.DfuHelperCallback dfuCallBack = new BaseDfuAdapter.DfuHelperCallback() {
        @Override
        public void onStateChanged(int state) {
            super.onStateChanged(state);
            switch (state) {
                case DfuAdapter.STATE_INIT_OK:
                    break;
                case DfuAdapter.STATE_PREPARED:
                    LogUtil.v("STATE_PREPARED CONNECT:" + mOtaDeviceInfo);
                    mOtaDeviceInfo = getDfuHelper().getOtaDeviceInfo();
                    if (isNotInOtaProcessing()) {
                        connect();
                    }
                    break;
                case DfuAdapter.STATE_DISCONNECTED:
                case DfuAdapter.STATE_CONNECT_FAILED:
                    //第三代
                    if (null != mOtaDeviceInfo && mOtaDeviceInfo.i == 5) {
                        if (isNotInOtaProcessing()) {
                            LogUtil.v("STATE_DISCONNECTED");
                            mHandle.sendMessage(mHandle.obtainMessage(OTA_GATT_CONNECTIION_FAIL));
                            mGlobalGatt.close(mDeviceAddress);
                            mBtGatt = null;
                        }
                    }
                    break;
            }
        }

        @Override
        public void onError(int type, int code) {
            super.onError(type, code);
            mState = STATE_ABORTED;
            LogUtil.v("STATE_ABORTED");
            Message msg = mHandle.obtainMessage(OTA_CALLBACK_ERROR);
            msg.arg1 = code;
            mHandle.sendMessage(msg);
        }

        @Override
        public void onProcessStateChanged(int state, Throughput throughput) {
            super.onProcessStateChanged(state, throughput);
            mProcessState = state;
            if (state == DfuConstants.PROGRESS_IMAGE_ACTIVE_SUCCESS) {
                mState = STATE_OTA_SUCCESS;
                Message msg = mHandle.obtainMessage(OTA_THIRD_SUCCESS);
                mHandle.sendMessage(msg);
            } else if (state == DfuConstants.PROGRESS_STARTED) {
                Message msg = mHandle.obtainMessage(OTA_THIRD_STATE_STARTED);
                mHandle.sendMessage(msg);

            }
        }

        @Override
        public void onProgressChanged(DfuProgressInfo dfuProgressInfo) {
            super.onProgressChanged(dfuProgressInfo);
            if (mProcessState == DfuConstants.PROGRESS_START_DFU_PROCESS && dfuProgressInfo != null) {
                Message msg = mHandle.obtainMessage(OTA_THIRD_PROGRESS);
                msg.arg1 = dfuProgressInfo.getProgress();
                msg.obj = (Math.min(dfuProgressInfo.getCurrentFileIndex() + 1, dfuProgressInfo.getMaxFileCount())) + "/" + dfuProgressInfo.getMaxFileCount();
                mHandle.sendMessage(msg);
            }
        }
    };


    private void startOtaProcess() {
        mState = STATE_OTA_PROCESSING;
        getDfuConfig().setFileIndicator(BinIndicator.INDICATOR_FULL);
        //低电量检测
        getDfuConfig().setBatteryCheckEnabled(true);
        getDfuConfig().setLowBatteryThreshold(20);
        // Mandatory, bluetooth device address
        getDfuConfig().setAddress(mSelectedDevice.getAddress());
        // Mandatory, if you know the exactly workmode, you can set it directly.
        // or you can read the otaDeviceInfo first, and then call `mDfuAdapter.getPriorityWorkMode(DfuConstants.OTA_MODE_SILENT_FUNCTION)` to get the priority work mode
        OtaModeInfo otaModeInfo = getDfuHelper().getPriorityWorkMode(DfuConstants.OTA_MODE_SILENT_FUNCTION);
        getDfuConfig().setOtaWorkMode(otaModeInfo.getWorkmode());

        //设置路径
        getDfuConfig().setFilePath(mPaths[0]);
        // It's recommend to read device info first, and set the right protocol type
        if (mOtaDeviceInfo != null) {
            getDfuConfig().setProtocolType(mOtaDeviceInfo.getProtocolType());
        } else {
            getDfuConfig().setProtocolType(0);
        }
        getDfuConfig().setBufferCheckLevel(DfuUtils.getRecommendBuffercheckLevel(mOtaDeviceInfo.icType));
        getDfuConfig().setSpeedControlEnabled(false);
        getDfuConfig().setControlSpeed(0);
        boolean ret = getDfuHelper().startOtaProcedure(getDfuConfig());
        if (!ret) {
            showToast(R.string.toast_prepare_ota_process_failed);
            mState = STATE_OTA_ERROR;
            Message msg = mHandle.obtainMessage(OTA_CALLBACK_ERROR);
            mHandle.sendMessage(msg);
        } else {
            mStepDialog.showProgress();
            mStepDialog.setProgress(0);
            mStepDialog.setStep(1 + "/" + 2);
        }

    }

    //------第三代遥控器升级所需变量 end --------
    private BluetoothDevice getBluetoothDevice() {
        if (m_Interface == null) {
            return null;
        }
        Set<BluetoothDevice> l_setPairedList = m_Interface.getBondedDevices();
        Object[] l_arrPairedDevice = l_setPairedList.toArray();

        for (Object o : l_arrPairedDevice) {
            BluetoothDevice l_BluetoothDevice = (BluetoothDevice) o;
            String l_strDeviceName = l_BluetoothDevice.getName();
            LogUtil.v(TAG, "l_strDeviceName " + l_strDeviceName);
            // 海外第一批出货遥控器型号为XFRB，之后遥控器固件升级为Lunzn_Max。为支持遥控器固件升级，需要系统和单端遥控固件升级app，支持两种型号遥控器适配
            if (CommonUtil.isNotEmpty(l_strDeviceName) && (l_strDeviceName.startsWith("Lunzn") || l_strDeviceName.startsWith("XFRB"))) {
                if (m_Interface.isConnected(l_BluetoothDevice)) {
                    return l_BluetoothDevice;
                } else {
                    LogUtil.w(TAG, "getBluetoothDevice not isConnected ");
                }
            }
        }
        return null;
    }

    private boolean LoadFileInfo(String path) {
        // check the file path
        if (TextUtils.isEmpty(path)) {
            LogUtil.e("TAG", "the file path string is null");
            return false;
        }
        // check the file type
        if (!MimeTypeMap.getFileExtensionFromUrl(path).equalsIgnoreCase("BIN")) {
            LogUtil.e("TAG", "the file type is not right");
            return false;
        }
        BinInputStream binInputStream;
        try {
            binInputStream = openInputStream(path);
            LogUtil.d(TAG, "newFwVersion = " + binInputStream.binFileVersion());
        } catch (final IOException e) {
            LogUtil.e(TAG, "An exception occurred while opening file " + e);
            return false;
        }
        try {
            binInputStream.close();
        } catch (IOException e) {
            LogUtil.e(TAG, "error in close file " + e);
            return false;
        }
        return true;
    }

    /**
     * 显示升级框
     */
    private void showUpgrade() {
        if (mStepDialog == null) {
            mStepDialog = new StepDialog(mApplication);
        }
        mStepDialog.show();
        mStepDialog.showConfirm();
        mStepDialog.setOnClickListener(new StepDialog.OnClickListener() {
            @Override
            public void onConfirm() {
                if (null != mOtaDeviceInfo) {
                    if (null != mPaths && mPaths.length == mFileCount) {
                        //1代，2代遥控器
                        if (mOtaDeviceInfo.icType <= 3) {
                            startOtaProcess(mPaths[mFileIndex]);
                        } else {
                            startOtaProcess();
                        }
                    } else {
                        showToast(R.string.dfu_file_status_invalid);
                    }

                }

            }

            @Override
            public void onCancel() {
                mStepDialog.cancel();
                close();
                mIsUpdating = false;
                LogUtil.v("misupdating=102102102");
            }

            @Override
            public void dismiss() {
                mIsUpdating = false;
                LogUtil.d("dismiss: isupdating false");

            }
        });
    }

    private BinInputStream openInputStream(final String filePath) throws IOException {
        final InputStream is = new FileInputStream(filePath);
        return new BinInputStream(is);
    }

    private void startOtaProcess(String filePath) {
        mState = STATE_OTA_PROCESSING;
        LogUtil.i(TAG, "startOtaProcess filePath " + filePath + ", mFileIndex " + mFileIndex + ", mFileCount " + mFileCount);
        if (dfu == null) {
            showToast(R.string.toast_dfu_service_not_ready);
            LogUtil.e(TAG, "the realsil dfu didn't ready");
            return;
        }

        // If in silent work mode, we should make sure the OTA progress didn't affect the normal traffic.
        // Normally we set the speed is 100B/s, user can change the value for customer platform.
        if ((dfu.getWorkMode() & RealsilDfu.OTA_MODE_SILENT_UPLOAD_MASK) != 0) {
            dfu.setSpeedControl(false, 0);
            // set the need wait user check flag, in silent mode, we should wait user
            // check to active the image
            dfu.setNeedWaitUserCheckFlag(true);
        } else {
            dfu.setNeedWaitUserCheckFlag(false);
        }

        // set the total speed for android 4.4, to escape the internal error
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            dfu.setSpeedControl(true, 1000);
        }
        // Use GlobalGatt do not need to disconnect, just unregister the callback
        mGlobalGatt.unRegisterCallback(mDeviceAddress, mBluetoothGattCallback);
            /*
            // disconnect the gatt
            disconnect(mBtGatt);// be care here
            // wait a while for close gatt.
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }*/

        LogUtil.w(TAG, "Start OTA, address is: " + mDeviceAddress);

        mOtaStartTime = System.currentTimeMillis();
        mOtaImageSendStartTime = 0;
        if (dfu.start(mDeviceAddress, filePath)) {
            showToast(R.string.toast_start_ota_process);
            LogUtil.d(TAG, "true");
        } else {
            showToast(R.string.toast_prepare_ota_process_failed);
            LogUtil.e(TAG, "something error in device info or the file, false");
            mState = STATE_ABORTED;
            Message msg = mHandle.obtainMessage(OTA_CALLBACK_ERROR);
            mHandle.sendMessage(msg);
        }
        mFileIndex++;

        mStepDialog.showProgress();
        mStepDialog.setProgress(0);
        mStepDialog.setStep(mFileIndex + "/" + mFileCount);
    }


    private void connectWithGatt() {
        LogUtil.v(TAG, "invoke connectWithGatt");
        if (mGlobalGatt == null) {
            LogUtil.e(TAG, "connectWithGatt mGlobalGatt is null");
            mIsUpdating = false;
            LogUtil.v("misupdating=false 103103103");
            return;
        }
        isGetFirmwareVersion = false;
        isGetTargePatchVersion = false;
        mGlobalGatt.connect(mDeviceAddress, mBluetoothGattCallback);
    }

    private void readDeviceInfo(BluetoothGattCharacteristic characteristic) {
        if (characteristic != null) {
            LogUtil.d(TAG, "read readDeviceinfo:" + characteristic.getUuid());
            mBtGatt.readCharacteristic(characteristic);
        } else {
            LogUtil.e(TAG, "readDeviceinfo Characteristic is null");
        }
    }

    private void isBLESupported() {
        if (!mApplication.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            showToast(R.string.no_ble);
        }
    }

    private boolean isBLEEnabled() {
        final BluetoothManager manager = (BluetoothManager) mApplication.getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            final BluetoothAdapter adapter = manager.getAdapter();
            return adapter != null && adapter.isEnabled();
        }
        return false;
    }

    private void showToast(final int messageResId) {
        LogUtil.w("提示 " + mApplication.getString(messageResId));
    }

    private void showToast(final String message) {
        LogUtil.w("提示1 " + message);
    }

    private void close() {
        LogUtil.d(TAG, "onDestroy");
        if (dfu != null) {
            dfu.close();
        }
        if (mGlobalGatt != null) {
            mGlobalGatt.closeAll();
        }
        if (mHandle != null) {
            mHandle.removeMessages(OTA_GET_TARGET_APP_VERSION);
        }
    }

    // 收到回连广播, 开机广播后调用升级
    public void checkUpgradeForce() {
        boolean networkConnected = isNetworkConnected(mApplication);
        LogUtil.i(TAG, "networkConnected " + networkConnected);
        if (networkConnected) {
            start();
        } else {
            mNeedRequest = true;
        }
    }

    // 收到网络连接广播后
    public void checkUpgrade() {
        LogUtil.i(TAG, "checkUpgrade mNeedRequest " + mNeedRequest);
        if (mNeedRequest) {
            boolean networkConnected = isNetworkConnected(mApplication);
            LogUtil.i(TAG, "checkUpgrade networkConnected " + networkConnected);
            if (networkConnected) {
                try {
                    start();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // 判断网络连接状态
    public static boolean isNetworkConnected(Context context) {
        if (context != null) {
            ConnectivityManager mgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (mgr != null) {
                NetworkInfo mNetworkInfo = mgr.getActiveNetworkInfo();
                if (mNetworkInfo != null) {
                    return mNetworkInfo.isAvailable();
                }
            }
        }
        return false;
    }

}
