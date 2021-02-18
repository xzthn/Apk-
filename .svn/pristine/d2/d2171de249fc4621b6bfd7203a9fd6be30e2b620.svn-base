package com.lzui.apkupgrade.net;

import android.content.Context;

import com.alibaba.fastjson.JSON;
import com.lunzn.tool.log.LogUtil;
import com.lunzn.tool.util.CommonUtil;
import com.lunzn.tool.util.DeviceInfoMgr;
import com.lunzn.tool.util.DeviceInfoMgrDs;
import com.lzui.apkcheck.BuildConfig;
import com.platform.http.AndroidHttpConnction;
import com.platform.sdk.m2.request.RequestInit;
import com.platform.sdk.m2.request.RequestRoot;
import com.platform.sdk.m2.response.ResponseRoot;
import com.smart.app.AppInfoTools;

import java.util.Arrays;
import java.util.Random;

/**
 * 影片HTTP请求
 *
 * @author LiZhengGuang
 * @version 1.0
 * @date 2014-3-10
 * @project COM.SMART.XIRI.MV.PROJECT.A20
 * @package com.smart.mv.voice.net
 * @package VoiceHttpRequest.java
 */
public class VoiceHttpRequest {
    private static final String TAG = "VoiceHttpRequest";

    /**
     * 发送post请求
     *
     * @param mContext 上下文
     * @param mRequest 请求参数对象
     * @return 请求结果
     */
    public static ResponseRoot sendHttpPostRequest(Context mContext, RequestRoot mRequest) {

        ResponseRoot mResponse = null;
        try {
            mResponse = sendHttpRequest(mContext, mRequest);
        } catch (Exception je) {
            je.printStackTrace();
            LogUtil.e(TAG, "Parse Response Failed." + mRequest.getRequestUrl() + "  ERROR  "
                    + je.toString());
        }
        return mResponse;
    }


    /**
     * 将具体的请求发送至服务器，在访问地址不通的情况下，会进行切源操作
     *
     * @param mContext 上下文
     * @param mRequest 具体请求对象
     * @return 返回结果
     */
    private static ResponseRoot sendHttpRequest(Context mContext, RequestRoot mRequest) {
        ResponseRoot res = null;
        initRequestPublicParams(mContext);
        String url = RequestInit.getServerIp();
        String[] urlList = MVDeviceConfig.getUrls();
        int currentPos = -1;
        // 是否是备份地址请求列表
        boolean isBackUrlList = false;
        // 如果第一次请求地址为空，则在正式请求地址列表中随机一个地址，这样可以平衡服务器的负担
        if (CommonUtil.isEmpty(url) && !CommonUtil.isEmpty(urlList)) {
            currentPos = new Random().nextInt(urlList.length);
        } else {
            // 判断当前地址是否是正式域名
            currentPos = getPositionInArray(url, urlList);
            // 如果当前域名不是正式域名，则判断是否是备份域名

            if (currentPos == -1 && !CommonUtil.isEmpty(MVDeviceConfig.getBackURLs())) {
                LogUtil.w("backList: " + Arrays.toString(MVDeviceConfig.getBackURLs()));
                urlList = MVDeviceConfig.getBackURLs();
                currentPos = getPositionInArray(url, urlList);
                // 标识当前是备份域名请求里诶包
                isBackUrlList = true;
            }
        }
        try {
            res = getResponseFromNet(mRequest, urlList, currentPos);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.e(TAG, "Parse Response Failed." + mRequest.getRequestUrl() + "  ERROR  " + e.toString());

            if (isBackUrlList || (e.toString() != null && (e.toString().toLowerCase().contains("host"))
                    )) {
                res = new ResponseRoot();
                res.setRetCode(999999);
            }
            LogUtil.e(TAG, "get netdata error ,final res :" + res);

        }


        // 如果请求失败，则切换其他的请求地址列表
        if (res != null && (res.getRetCode() == 999999)) {
            LogUtil.e(TAG, "-------start to change host------isBackUrlList " + isBackUrlList);
            // 如果当前是备份域名，则切换至正式域名组, 如果当前是正式域名，则切换至备份域名组
            if (isBackUrlList) {
                urlList = MVDeviceConfig.getUrls();
            } else {
                if (CommonUtil.isEmpty(MVDeviceConfig.getBackURLs())) {
                    LogUtil.e(TAG, "getBackURLs ------isEmpty------------");
                    return res;
                }
                urlList = MVDeviceConfig.getBackURLs();
            }
            // 从第一个url开始从新请求一遍新的接口列表，直至请求成功
            currentPos = 0;
            res = getResponseFromNet(mRequest, urlList, currentPos);
        }
        return res;
    }

    /**
     * 从服务器获取请求结果
     *
     * @param mRequest   接口请求对象
     * @param urlList    请求地址列表
     * @param currentPos 当前请求地址在请求列表中的位置
     * @return 请求结果
     */
    private static ResponseRoot getResponseFromNet(RequestRoot mRequest, String[] urlList, int currentPos) {
        LogUtil.d("getResponseFromNet", "urlList " + Arrays.toString(urlList) + ", currentPos " + currentPos);

        int urlLength = urlList.length;
        String url = urlList[currentPos];
        ResponseRoot res = null;
        for (int i = 0; i < urlLength; i++) {
            RequestInit.setServerIp(url);
            printRequestLog(mRequest);
            res = mRequest.getResponse();
            printNetLog(res, mRequest);

            // 如果请求失败则切换同组地址其他源
            if (res == null || (res.getRetCode() > 0 && res.getRetCode() < 1000)) {
                currentPos = currentPos + 1 >= urlLength ? 0 : currentPos + 1;
                url = urlList[currentPos];
            } else {
                // 请求成功，保存请求地址，并且返回
                //                SharedPreferenceUtil.set("request_url", url);
                break;
            }
        }
        return res;
    }

    private static void printRequestLog(RequestRoot mRequest) {
        long startTime = System.currentTimeMillis();
        StringBuilder sBuilder = new StringBuilder("[REQUEST][" + startTime + "][");
        sBuilder.append(RequestInit.getServerIp() + mRequest.getRequestUrl() + "?" + RequestInit.getString());
        sBuilder.append("&D=" + mRequest.getData());
        sBuilder.append("\r\n");
        LogUtil.e("getResponseFromNet", sBuilder.toString());
    }

    /**
     * 打印请求的全部信息
     *
     * @param res
     * @param mRequest
     */
    private static void printNetLog(ResponseRoot res, RequestRoot mRequest) {

        long endTime = System.currentTimeMillis();
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("[RESPONSE][" + endTime + "][TIME][" + (endTime) + "]");
        String str = mRequest.getRequestUrl();
        stringBuilder.append("[").append(str).append("]").append(JSON.toJSONString(res));
        LogUtil.e("getResponseFromNet", stringBuilder.toString());
    }


    /**
     * 初始化公共请求参数
     *
     * @param mContext 上下文
     */
    private static void initRequestPublicParams(Context mContext) {
        if (BuildConfig.APPLICATION_ID.equalsIgnoreCase("hs.apk.update")) {

            try {
                RequestInit.setLauncherSign(AppInfoTools.getAppChannel(mContext, "hs.launcher", "COVERSION"));
                RequestInit.setLauncherVsn(AppInfoTools.getApkVersion(mContext, "hs.launcher"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            DeviceInfoMgrDs.DeviceInfo deviceInfo = DeviceInfoMgrDs.getInstance().getDeviceInfo(mContext);
            if (null != deviceInfo) {
                RequestInit.setRomSign(deviceInfo.getSoftwareVersion());
                RequestInit.setGdid(deviceInfo.getGdid());
                RequestInit.setSn(deviceInfo.getSn());
                RequestInit.setMac(deviceInfo.getMac());
                RequestInit.setWifiMac(deviceInfo.getWifiMac());
            }

        } else {

            try {
                RequestInit.setLauncherSign(AppInfoTools.getAppChannel(mContext, "com.lzui.launcher", "COVERSION"));
                RequestInit.setLauncherVsn(AppInfoTools.getApkVersion(mContext, "com.lzui.launcher"));
            } catch (Exception e) {
                e.printStackTrace();
            }

            DeviceInfoMgr.DeviceInfo deviceInfo = DeviceInfoMgr.getInstance().getDeviceInfo(mContext);
            if (deviceInfo != null) {
                RequestInit.setRomSign(deviceInfo.getSoftwareVersion());
                RequestInit.setGdid(deviceInfo.getGdid());
                RequestInit.setSn(deviceInfo.getSn());
                RequestInit.setMac(deviceInfo.getMac());
                RequestInit.setWifiMac(deviceInfo.getWifiMac());
            }

        }

        RequestInit.setHttpConn(new AndroidHttpConnction());
        RequestInit.setRealmName(MVDeviceConfig.getRealmName());
    }

    /**
     * 获取字符串在字符数组中的地址
     *
     * @param str 要定位的字符串
     * @param arr 字符串数组
     * @return 位置索引
     */
    private static int getPositionInArray(String str, String[] arr) {
        int result = -1;
        if (CommonUtil.isNotEmpty(str) && CommonUtil.isNotEmpty(arr)) {
            for (int i = 0; i < arr.length; i++) {
                if (str.equals(arr[i])) {
                    result = i;
                    break;
                }
            }
        }
        return result;
    }
} // End class VoiceHttpRequest