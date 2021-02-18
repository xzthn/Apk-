package com.lzui.apkupgrade.biz;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.lunzn.tool.log.LogUtil;
import com.lunzn.tool.util.CommonUtil;
import com.lzui.apkcheck.R;
import com.lzui.apkupgrade.net.ExecutorInstance;
import com.smart.net.SendCmd;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Desc: TODO
 * <p> 弹出重启提示框
 * Author: linje
 * PackageName: com.lzui.apkupgrade.biz
 * ProjectName: APKCheck
 * Date: 2019/11/21 0021 16:16
 */
public class ShowRebootAlertBiz {

    private final static String TAG = "showRebootDialog";

    private static ShowRebootAlertBiz instance;

    private Disposable rebootDisposable;

    private Dialog rebootDialog;

    private Context mContext;

    private TextView countView;

    public ShowRebootAlertBiz(Context mContext) {
        this.mContext = mContext;
    }

    public static ShowRebootAlertBiz getInstance(Context mContext) {
        synchronized (ShowRebootAlertBiz.class) {
            if (instance == null) {
                instance = new ShowRebootAlertBiz(mContext);
            }
        }
        return instance;
    }

    public void showRebootDialog() {
        LogUtil.i(TAG, "showRebootDialog rebootDialog:  " + rebootDialog);
        if (rebootDialog == null) {
            rebootDialog = new Dialog(mContext, R.style.dialog);
            View rootView = LayoutInflater.from(mContext).inflate(R.layout.dialog_reboot_alert, null);
            countView = (TextView) rootView.findViewById(R.id.tv_reboot_count);
            rebootDialog.setContentView(rootView);
            rebootDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ERROR);
            rebootDialog.setCancelable(false);
            rebootDialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
                @Override
                public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                    if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_ESCAPE) {
                        return true;
                    }
                    return false;
                }
            });
        }
        LogUtil.i(TAG, "showRebootDialog isShowing:  " + (rebootDialog == null ? false : rebootDialog.isShowing()));
        if (rebootDialog != null && !rebootDialog.isShowing()) {
            rebootDialog.show();
            Observable.interval(1, 1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
                @Override
                public void onSubscribe(Disposable d) {
                    rebootDisposable = d;
                }

                @Override
                public void onNext(Long aLong) {
                    int count = CommonUtil.toInt(countView.getText().toString());
                    if (count > 1) {
                        countView.setText(--count + "");
                    } else {
                        if (rebootDisposable != null && !rebootDisposable.isDisposed()) {
                            rebootDisposable.dispose();
                        }
                        ExecutorInstance.getExecutor().execute(new Runnable() {
                            @Override
                            public void run() {
                                LogUtil.i(TAG, "excute reboot");
                                SendCmd.get().exec("reboot");
                            }
                        });
                    }
                }

                @Override
                public void onError(Throwable e) {

                }

                @Override
                public void onComplete() {

                }
            });
        }
    }

}
