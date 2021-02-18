package com.realsil.ota;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Desc: TODO
 * <p>
 * Author: meijie
 * PackageName: com.realsil.ota
 * ProjectName: APKCheck
 * Date: 2018/11/26 10:03
 */
public class StepDialog extends Dialog {

    private static final String TAG = "StepDialog";

    private ProgressBar mProgressPb;
    private View mStepUpgradeV;
    private View mStepRl;
    private View mBatteryV;
    private View mFailureV;
    private View mSuccessV;
    private View mStepV;
    private TextView mProgressTv;
    private View mBatteryOkBtn;

    public StepDialog(Context context) {
        super(context, R.style.dialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View inflate = View.inflate(getContext(), R.layout.comm_dialog_main, null);
        mProgressPb = (ProgressBar) inflate.findViewById(R.id.pb_progress);
        mProgressTv = (TextView) inflate.findViewById(R.id.tv_progress);
        mStepV = inflate.findViewById(R.id.iv_step);
        mStepUpgradeV = inflate.findViewById(R.id.ll_step_upgrade);
        mBatteryV = inflate.findViewById(R.id.rl_battery);
        mFailureV = inflate.findViewById(R.id.rl_failure);
        mSuccessV = inflate.findViewById(R.id.rl_success);
        mStepRl = inflate.findViewById(R.id.rl_step);
        inflate.findViewById(R.id.btn_confirm).setOnClickListener((v) -> {
            if (mListener != null) {
                mListener.onConfirm();
            }
        });
        inflate.findViewById(R.id.btn_cancel).setOnClickListener((v) -> {
            if (mListener != null) {
                mListener.onCancel();
            }
        });

        mBatteryOkBtn = inflate.findViewById(R.id.btn_ok);
        mBatteryOkBtn.setOnClickListener((v) -> dismiss());

        setContentView(inflate);

        setCancelable(true);
        setCanceledOnTouchOutside(false);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
    }

    public void showSuccess() {
        setCancelable(true);
        mStepV.setVisibility(View.GONE);
        mProgressTv.setVisibility(View.GONE);
        mSuccessV.setVisibility(View.VISIBLE);
    }

    public void showConfirm() {
        setCancelable(true);
        mStepV.setVisibility(View.VISIBLE);
        mStepUpgradeV.setVisibility(View.VISIBLE);
        mFailureV.setVisibility(View.GONE);
        mProgressPb.setVisibility(View.GONE);
        mProgressTv.setVisibility(View.GONE);
        mSuccessV.setVisibility(View.GONE);
        mBatteryV.setVisibility(View.GONE);
    }

    public void showProgress() {
        setCancelable(false);
        mStepUpgradeV.setVisibility(View.GONE);
        mProgressPb.setVisibility(View.VISIBLE);
        mProgressTv.setVisibility(View.VISIBLE);
    }

    public void showFailure() {
        setCancelable(true);
        mStepV.setVisibility(View.GONE);
        mProgressPb.setVisibility(View.GONE);
        mProgressTv.setVisibility(View.GONE);
        mFailureV.setVisibility(View.VISIBLE);
    }

    public void showLowBattery() {
        setCancelable(true);
        mProgressPb.setVisibility(View.GONE);
        mProgressTv.setVisibility(View.GONE);
        mBatteryV.setVisibility(View.VISIBLE);
        mBatteryOkBtn.requestFocus();
    }

    private OnClickListener mListener;

    public void setOnClickListener(OnClickListener listener) {
        mListener = listener;
    }

    public void setProgress(int percent) {
        if (mProgressPb == null) {
            return;
        }
        mProgressPb.setProgress(percent);
    }

    public void setStep(String text) {
        mProgressTv.setText(text);

    }

    @Override
    public void dismiss() {
        super.dismiss();
        if (mListener != null) {
            mListener.dismiss();
        }
    }

    public interface OnClickListener {

        void onConfirm();

        void onCancel();

        // 当弹框消失时
        void dismiss();
    }



/*
1. 开机启动/mqtt

回连

调用固件升级方法
2. 获取本地固件版本、patch 版本
3. 请求网络，获取升级信息
5. 下载版本，安装成功删除版本
4. 需要升级，判断电池电量，提示电池电量，？
6. 下载固件版本，安装固件版本，
安装成功删除固件版本


 */

}
