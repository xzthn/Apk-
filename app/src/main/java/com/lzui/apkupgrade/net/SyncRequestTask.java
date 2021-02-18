package com.lzui.apkupgrade.net;

import android.os.AsyncTask;

import com.lunzn.tool.log.LogUtil;
import com.platform.sdk.m2.response.ResponseRoot;

/**
 * 向服务器请求基类，
 * 保存请求结果，当服务器没有返回时，网络恢复之后可以再次发送请求
 *
 * @author 曹林杰
 * @version [V1.7]
 * @date 2017年7月21日
 * @project com.lzui.apkupgrade
 * @package com.lzui.apkupgrade.net
 * @filename SyncRequestTask.java
 */
public abstract class SyncRequestTask {

    private final static String TAG = "BootUpgradeBiz";

    // 请求服务器返回结果
    private ResponseRoot mResult = null;

    // 当前正在请求的任务
    private RequestTask mCurrentTask = null;

    /**
     * 向服务器发送升级请求,如果出现异常，则抛出异常，避免应用崩溃
     *
     * @return 请求结果，允许为null,如果为null，则说明服务器没有返回，网络恢复之后需要再次请求
     */
    public abstract ResponseRoot onRequest() throws Exception;

    /**
     * 根据服务器返回的结果进行解析等操作，如果出现异常，则抛出异常，避免应用崩溃
     */
    public abstract void onExcute(ResponseRoot mResponse) throws Exception;

    /**
     * 检测升级，如果之前请求过，则不再请求
     */
    public void excuteRequestChecked(int delay) {
        LogUtil.i(TAG, "excuteRequestDelayed request result:  " + mResult + "; mCurrentTask:  "
                + mCurrentTask + "; " + (mCurrentTask == null || !mCurrentTask.isTaskExcuting()));
        if (mResult == null && (mCurrentTask == null || !mCurrentTask.isTaskExcuting())) {
            RequestTask mTask = new RequestTask();
            mCurrentTask = mTask;
            LogUtil.i(TAG, "excuteRequestDelayed current task:  " + mTask);
            mTask.executeOnExecutor(ExecutorInstance.getExecutor(), delay); // 延时 10s
        }
    }

    /**
     * 外部推送指令，延迟10s强制检测升级
     */
    public void excuteRequestForced() {
        LogUtil.i(TAG, "start excuteRequestForced, reset mResult");
        mResult = null;
        excuteRequestChecked(10 * 1000);
    }

    /**
     * 外部推送指令，立即强制检测升级
     */
    public void excuteRequestImmediate() {
        LogUtil.i(TAG, "mCurrentTask:  " + mCurrentTask + "; excuting:  " + mCurrentTask.isTaskExcuting());
        if (mCurrentTask != null && mCurrentTask.isTaskExcuting()) {
            LogUtil.i(TAG, "isSleep:  " + mCurrentTask.isSleep());
            if (mCurrentTask.isSleep()) {
                mCurrentTask.stopSleep();
            }
        } else {
            mResult = null;
            excuteRequestChecked(0);
        }
    }


    /**
     * 向服务器请求任务类
     */
    private class RequestTask extends AsyncTask<Integer, Integer, ResponseRoot> {

        private boolean isSleep;

        private boolean needSleep;

        private int currentCount;

        public void stopSleep() {
            LogUtil.i(TAG, "excute stop sleep");
            needSleep = false;
        }

        public boolean isSleep() {
            return isSleep;
        }

        @Override
        protected ResponseRoot doInBackground(Integer... param) {
            // 延迟请求
            int delay = param[0];
            LogUtil.i(TAG, "延迟 " + delay);
            if (delay > 0) {
                isSleep = true;
                LogUtil.i(TAG, "sleep start isSleep:  " + isSleep + "; currentTask:  " + mCurrentTask);
                int loop = delay / 1000;
                needSleep = true;
                if (loop > 0) {
                    while (currentCount < loop && needSleep) {
                        currentCount++;
                        try {
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    try {
                        Thread.sleep(delay);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            isSleep = false;
            LogUtil.i(TAG, "sleep end isSleep:  " + isSleep + "; currentTask:  " + mCurrentTask);
            try {
                mResult = onRequest();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return mResult;
        }

        @Override
        protected void onPostExecute(ResponseRoot result) {
            try {
                onExcute(result);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * 判断当前任务是否正在执行
         */
        public boolean isTaskExcuting() {
            return getStatus() != Status.FINISHED;
        }
    }
}
