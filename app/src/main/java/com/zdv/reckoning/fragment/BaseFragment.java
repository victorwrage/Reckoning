package com.zdv.reckoning.fragment;


import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zdv.reckoning.R;
import com.zdv.reckoning.customView.ProgressBarItem;
import com.zdv.reckoning.view.IFragmentActivity;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public abstract class BaseFragment extends Fragment {
    protected final String COOKIE_KEY = "cookie";
    protected static final String SUCCESS = "200";
    protected static final int DIALOG_TYPE_1 = 0;
    protected static final int DIALOG_TYPE_2 = 1;
    protected static final int DIALOG_TYPE_3 = 2;
    protected Executor executor;
    protected IFragmentActivity listener;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        executor  = Executors.newSingleThreadScheduledExecutor();
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    protected void showWaitDialog(String tip){
        ProgressBarItem.show(getContext(),tip,false,null);
    }

    protected void hideWaitDialog() {
        ProgressBarItem.hideProgress();
    }

    protected void startLoading(ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        Animation rotate = AnimationUtils.loadAnimation(getContext(), R.anim.rotate);
        LinearInterpolator lin = new LinearInterpolator();
        rotate.setInterpolator(lin);
        imageView.startAnimation(rotate);
    }

    protected void stopLoading(ImageView imageView) {
        imageView.setVisibility(View.GONE);
        imageView.clearAnimation();
    }


    public void Back(){
         hidSoftInput();
    }

    protected void hidSoftInput(){
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            View v = new View(getContext());
            ViewGroup g1 = (ViewGroup)getActivity().getWindow().getDecorView();
            ViewGroup g2 = (ViewGroup)g1.getChildAt(0);
            g2.addView(v);
            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        }
    }

    AlertDialog dialog;
    protected void showDialog(int type, String title, String tip) {
        new MaterialDialog.Builder(getContext())
                .title(title)
                .content(tip)
                .positiveText(R.string.bga_pp_confirm)
                .negativeText(R.string.cancle)
                .onNegative((materialDialog, dialogAction) -> cancel(type))
                .autoDismiss(true)
                .onNeutral((materialDialog, dialogAction) -> confirm(type))
                .show();
    }

    public void refreshState(){

    }
    protected void cancel(int type) {
    }

    protected void confirm(int type) {
        dialog.dismiss();
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            listener = (IFragmentActivity) context;
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

}
