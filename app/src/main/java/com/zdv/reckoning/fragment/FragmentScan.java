package com.zdv.reckoning.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.jakewharton.rxbinding2.view.RxView;
import com.socks.library.KLog;
import com.zdv.reckoning.R;
import com.zdv.reckoning.present.QueryPresent;
import com.zdv.reckoning.utils.Constant;
import com.zdv.reckoning.utils.Utils;
import com.zdv.reckoning.utils.VToast;
import com.zdv.reckoning.view.IView;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bingoogolapple.qrcode.core.QRCodeView;
import cn.bingoogolapple.qrcode.zbar.ZBarView;

import static android.content.Context.VIBRATOR_SERVICE;

/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/8/8 18:42
 */
public class FragmentScan extends BaseFragment implements QRCodeView.Delegate,IView {
    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    private static final int REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY = 666;

    private final int DECODE_BITMAP_SUCCESS = 101;

    QueryPresent present;
    Utils util;
    SharedPreferences sp;

    boolean lightOpen = false;
    @Bind(R.id.mQRCodeView)
    ZBarView mQRCodeView;
    @Bind(R.id.header_btn_lay)
    LinearLayout header_btn_lay;
    @Bind(R.id.header_edit_lay)
    LinearLayout header_edit_lay;
    String codeStr = "";


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_scan, container, false);
        ButterKnife.bind(FragmentScan.this, view);

        return view;
    }


    @Override
    public void Back() {
        super.Back();
        codeStr = "";
        mQRCodeView.stopSpot();
        mQRCodeView.hiddenScanRect();
        mQRCodeView.stopCamera();
        listener.gotoMain();
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDate();
        initView();
    }

    @Override
    public void refreshState() {
        super.refreshState();
        mQRCodeView.startCamera();
        mQRCodeView.showScanRect();
        mQRCodeView.startSpotDelay(500);
    }

    @Override
    public void onResume() {
        super.onResume();
        mQRCodeView.startCamera();
        mQRCodeView.showScanRect();
        mQRCodeView.startSpotDelay(500);
    }

    private void initDate() {
        mQRCodeView.setDelegate(this);
        util = Utils.getInstance();
        present = QueryPresent.getInstance(getContext());
        present.setView(FragmentScan.this);
        sp = getContext().getSharedPreferences(COOKIE_KEY, Context.MODE_PRIVATE);

    }

    protected void initView() {

        RxView.clicks(header_btn_lay).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(s -> Back());
        mQRCodeView.startCamera();
        mQRCodeView.showScanRect();
        mQRCodeView.startSpotDelay(500);
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // 识别图片中的二维码还有问题，占时不要用
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_CHOOSE_QRCODE_FROM_GALLERY) {


        } else {
            mQRCodeView.startCamera();
            mQRCodeView.showScanRect();
            mQRCodeView.startSpotDelay(500);
        }
    }


    @Override
    public void onStop() {
        lightOpen = false;
        mQRCodeView.destroyDrawingCache();
        mQRCodeView.hiddenScanRect();
        mQRCodeView.startSpot();
        mQRCodeView.closeFlashlight();
        mQRCodeView.stopCamera();

        super.onStop();
    }

    @Override
    public void onDestroy() {
        mQRCodeView.onDestroy();
        super.onDestroy();
    }

    private void vibrate() {
        Vibrator vibrator = (Vibrator) getContext().getSystemService(VIBRATOR_SERVICE);
        vibrator.vibrate(200);
    }

    @Override
    public void onScanQRCodeSuccess(String result) {
        KLog.v("result:" + result);
        codeStr = result;
        Constant.codeStr = result;
        listener.gotoMain();
    }

    @Override
    public void onScanQRCodeOpenCameraError() {
        KLog.e("打开相机出错");
        VToast.toast(getContext(),"没有相机使用权限");
    }

}
