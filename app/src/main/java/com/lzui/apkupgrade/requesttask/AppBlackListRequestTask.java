package com.lzui.apkupgrade.requesttask;

import android.content.Context;
import android.os.Handler;

import com.lunzn.tool.log.LogUtil;
import com.lzui.apkupgrade.net.SyncRequestTask;
import com.lzui.apkupgrade.net.VoiceHttpRequest;
import com.lzui.apkupgrade.util.FileSavePathUtil;
import com.platform.sdk.m2.request.launcher.LauncherFilterRequest;
import com.platform.sdk.m2.response.ResponseRoot;
import com.platform.sdk.m2.response.launcher.LauncherFilterResponse;
import com.platform.sdk.m2.util.Code;

import java.util.List;

/**
 * Desc: TODO
 * <p>
 * Author: panwensheng
 * PackageName: com.lzui.apkupgrade.requesttask
 * ProjectName: APKCheck
 * Date: 2020/6/17 14:54
 */
public class AppBlackListRequestTask extends SyncRequestTask {

    /**
     * 请求成功之后，回调handler
     */
    private Handler mHandler;

    /**
     * 上下文
     */
    private Context mContext;

    public AppBlackListRequestTask(Handler mHandler, Context mContext) {
        this.mHandler = mHandler;
        this.mContext = mContext;
    }

    @Override
    public ResponseRoot onRequest() throws Exception {
        LauncherFilterRequest blackListReq = new LauncherFilterRequest();
        return VoiceHttpRequest.sendHttpPostRequest(mContext, blackListReq);
    }

    @Override
    public void onExcute(ResponseRoot mResponse) throws Exception {
        LogUtil.i("AppBlackListRequestTask", "onExcute  " + (mResponse == null ? "mResponse is null" : mResponse.getRetCode()));
        if (mResponse != null && mResponse.getRetCode() == Code.SUCCESS) {
            List<String> blacklist = ((LauncherFilterResponse) mResponse).getLauncherFilter().getBlacklist();
            LogUtil.i("AppBlackListRequestTask", "blacklist " + blacklist);
            mHandler.obtainMessage(FileSavePathUtil.MSG_GET_APP_BLACKLIST_SUCCESS, blacklist).sendToTarget();
        }
    }
}
