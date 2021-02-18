package com.lzui.apkupgrade.activity;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.lunzn.tool.autofit.AutoTextView;
import com.lunzn.tool.autofit.GetScreenSize;
import com.lunzn.tool.log.LogUtil;
import com.lzui.apkcheck.R;
import com.lzui.apkupgrade.biz.NetCheckBiz;
import com.lzui.apkupgrade.data.ExternalActions;
import com.lzui.apkupgrade.service.ExternalActionService;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;

public class MainActivity extends Activity {

    /**
     * 升级操作回调
     */
    public interface CmdCallback {

        // 升级检测结果回调
        void onCheckResult(boolean result);

        // 升级过程回调
        void onUpgradeProgress(int index, int total);

        // 升级结束回调
        void onUpgradeEnd();
    }

    // 日志tag
    private static final String TAG = "BootUpgradeBiz";

    // 显示界面退出消息提示框
    private final static int SHOW_QUIT_CONFIRM_DIALOG = 10001;

    // 隐藏界面退出消息提示框
    private final static int HIDE_QUIT_CONFIRM_DIALOG = 10002;

    // 根布局
    private View rootView;

    // 升级操作回调
    private CmdCallback mCallback;

    // 升级服务绑定Binder
    private ExternalActionService.UpgradeBinder mBinder;

    // 下载动画
    private AnimationDrawable mDrawable;

    // 提示消息
    private TextView tvAlert;

    // 进度提示条背景
    private FrameLayout flCountLayout;

    // 进度条布局
    private View progressView;

    // 进度条每个元素的左边距
    private int left;

    // 退出确认框
    private PopupWindow confirmDialog;

    private Disposable mDisposable;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_QUIT_CONFIRM_DIALOG:
                    showQuitConfirmDialog();
                    break;

                case HIDE_QUIT_CONFIRM_DIALOG:
                    if (!isDestroyed()) {
                        hideQuitConfirmDialog();
                        finish();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    };

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = ((ExternalActionService.UpgradeBinder) service);
            mBinder.setCmdCallback(mCallback);
            Log.i(TAG, "path:  " + getExternalCacheDir());
            Intent serviceIntent = new Intent(MainActivity.this, ExternalActionService.class);
            serviceIntent.putExtra("event", ExternalActions.EVENT_APP_UPGRADE_IMMEDIATE);
            startService(serviceIntent);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBinder = null;
        }
    };

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                if (mBinder != null && mBinder.isUpdateExcute()) {
                    mHandler.sendEmptyMessage(SHOW_QUIT_CONFIRM_DIALOG);
                    mHandler.sendEmptyMessageDelayed(HIDE_QUIT_CONFIRM_DIALOG, 4000);
                    return true;
                }
            }
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
        LogUtil.i(TAG, "width:  " + GetScreenSize.widthPixels + "; height:  " + GetScreenSize.heightPixels);
    }

    private void initData() {
        mCallback = new CmdCallback() {
            @Override
            public void onCheckResult(boolean result) {
                LogUtil.i(TAG, "onCheckResult result:  " + result);
                // 没有升级延迟三秒隐藏该界面
                if (!result) {
                    if (NetCheckBiz.isNetworkConnected(MainActivity.this)) {
                        finishActivityDelay("没有检测到应用更新，即将退出");
                    } else {
                        finishActivityDelay("网络未连接，应用检测更新失败，即将退出");
                    }
                }
            }

            @Override
            public void onUpgradeProgress(int index, int total) {
                LogUtil.i(TAG, "onCheckResult index:  " + index + ";  total:  " + total);
                updateProgress(index, total);
            }

            @Override
            public void onUpgradeEnd() {
                // 应用更新完成，结束界面
                finishActivityDelay("应用更新完成，即将退出");
            }
        };
        bindService();
    }

    private void initView() {
        rootView = LayoutInflater.from(this).inflate(R.layout.activity_main_layout, null);
        setContentView(rootView);
        mDrawable = (AnimationDrawable) ((ImageView) findViewById(R.id.iv_load_ani)).getDrawable();
        tvAlert = (TextView) findViewById(R.id.tv_message_alert);
        flCountLayout = (FrameLayout) findViewById(R.id.fl_count_layout);
        progressView = findViewById(R.id.fl_progress_layout);
        initStatus();
    }

    private void initStatus() {
        mDrawable.stop();
        tvAlert.setText("正在检测升级，请稍候...");
        progressView.setVisibility(View.GONE);
    }

    private void bindService() {
        Intent serviceIntent = new Intent("service.handle.push.event");
        serviceIntent.setPackage(getPackageName());
        bindService(serviceIntent, mConnection, Context.BIND_AUTO_CREATE);
    }

    /**
     * 显示再按一次退出确认框
     */
    private void showQuitConfirmDialog() {
        if (confirmDialog == null) {
            View confirmView = LayoutInflater.from(this).inflate(R.layout.quit_alert_layout, null);
            confirmDialog = new PopupWindow(confirmView, GetScreenSize.autofitX(254), GetScreenSize.autofitY(52));
        }
        if (!confirmDialog.isShowing()) {
            confirmDialog.showAtLocation(rootView, Gravity.BOTTOM, 0, GetScreenSize.autofitY(130));
        }
    }

    /**
     * 隐藏再按一次退出确认框
     */
    private void hideQuitConfirmDialog() {
        if (confirmDialog != null && confirmDialog.isShowing()) {
            confirmDialog.dismiss();
        }
    }

    private void finishActivityDelay(String msg) {
        Observable
                .just(1)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        tvAlert.setText(msg);
                        if (msg.contains("应用更新完成")) {
                            if (mDrawable != null && mDrawable.isRunning()) {
                                mDrawable.stop();
                            }
                        }
                    }
                });

        Observable
                .timer(3, TimeUnit.SECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Long>() {
                    @Override
                    public void accept(Long aLong) throws Exception {
                        finish();
                    }
                });


    }

    /**
     * 更新进度
     *
     * @param progress 当前下载的个数
     * @param total    总的下载个数
     */
    private void updateProgress(int progress, int total) {
        Observable
                .just(1).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        initLoadMsgView(progress, total);
                        int start = 0;
                        Object tag = flCountLayout.getTag();
                        if (tag != null && tag instanceof Integer) {
                            start = (int) tag;
                        }
                        int num = progress - start;
                        LogUtil.i(TAG, "start:  " + start + ";  num:   " + num);
                        for (int i = 0; i < num; i++) {
                            int currentIndex = start + i + 1;
                            flCountLayout.setTag(currentIndex);
                            int unit = 34 / total;
                            int last = 34 % total;
                            LogUtil.i(TAG, "unit:  " + unit + ";  last:   " + last);
                            // 下载第一个，先添加头部
                            if (currentIndex == 1) {
                                addHeader();
                            }
                            // 最后一个，如果不能整除，则把最后的余数一起添加进去，填满整个进度条
                            if (currentIndex == total) {
                                int count = last > 0 ? (unit + last) : unit;
                                for (int m = 0; m < count; m++) {
                                    addUnit(progress, m);
                                }
                                // 添加末尾圆角
                                addEnd();
                            } else {
                                if (currentIndex > 1) {
                                    // 添加中间斜条前，移除尾部圆角
                                    flCountLayout.removeViewAt(flCountLayout.getChildCount() - 1);
                                }
                                for (int n = 0; n < unit; n++) {
                                    addUnit(currentIndex, n);
                                }
                                // 添加末尾圆角
                                addEnd();
                            }
                        }
                    }
                });

    }

    /**
     * 初始化下载信息提示界面
     */
    private void initLoadMsgView(int progress, int total) {
        if (progressView.getVisibility() != View.VISIBLE) {
            progressView.setVisibility(View.VISIBLE);
        }
        if (mDrawable != null && !mDrawable.isRunning()) {
            mDrawable.start();
        }
        tvAlert.setText("检测到应用升级（" + progress + "/" + total + "）");
    }

    /**
     * 添加头部（头部半圆以及第一个中间方格）
     */
    private void addHeader() {
        // 缩进2dx
        left += 2;
        AutoTextView headerView = new AutoTextView(MainActivity.this);
        headerView.setBackgroundResource(R.drawable.bg_load_start);
        FrameLayout.LayoutParams headerLp = new FrameLayout.LayoutParams(14, FrameLayout.LayoutParams.MATCH_PARENT);
        headerLp.leftMargin = left;
        flCountLayout.addView(headerView, headerLp);
        left += 14;

        AutoTextView midView = new AutoTextView(MainActivity.this);
        midView.setBackgroundResource(R.drawable.bg_load_mid);
        FrameLayout.LayoutParams midLp = new FrameLayout.LayoutParams(20, FrameLayout.LayoutParams.MATCH_PARENT);
        midLp.leftMargin = left;
        flCountLayout.addView(midView, midLp);
        left += 20;
    }

    /**
     * 添加中间单元格
     *
     * @param progress 当前升级个数
     * @param index    单元格中的当前个数
     */
    private void addUnit(int progress, int index) {
        AutoTextView mTextView = new AutoTextView(MainActivity.this);
        mTextView.setBackgroundResource(R.drawable.bg_load_mid);
        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(20, FrameLayout.LayoutParams.MATCH_PARENT);
        if (progress == 1 && index == 0) {
            // 为了对齐斜条，第二个中间方格起向左移动18dx
            lp.leftMargin = left - 9;
            left += 11 - 9;
        } else {
            // 为了对齐斜条，第三个中间方格起依次向左移动9dx
            lp.leftMargin = left;
            left += 11;
        }
        flCountLayout.addView(mTextView, lp);
    }

    /**
     * 添加末尾
     */
    private void addEnd() {
        AutoTextView rightView = new AutoTextView(MainActivity.this);
        rightView.setBackgroundResource(R.drawable.bg_load_end);
        FrameLayout.LayoutParams rightLp = new FrameLayout.LayoutParams(16, FrameLayout.LayoutParams.MATCH_PARENT);
        // 为了对齐斜条，尾部向右移动7dx
        rightLp.leftMargin = left + 7;
        flCountLayout.addView(rightView, rightLp);
    }

    private void sendUpgradeFinishedBroadcast() {
        Intent intent = new Intent("com.lz.upgrade.finish");
        intent.setPackage("com.lzui.launcher");
        sendBroadcast(intent);
    }

    private void test() {
        Log.i(TAG, "path:  " + getExternalCacheDir());
        Intent serviceIntent = new Intent(this, ExternalActionService.class);
        serviceIntent.putExtra("event", "CHECK_FIRMWARE_UPGRADE");
        startService(serviceIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sendUpgradeFinishedBroadcast();
        if (mBinder != null) {
            unbindService(mConnection);
        }
        if (mDrawable != null && mDrawable.isRunning()) {
            mDrawable.stop();
        }
        if (mDisposable != null && !mDisposable.isDisposed()) {
            mDisposable.dispose();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
