package com.zdv.reckoning.fragment;

import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.jakewharton.rxbinding2.view.RxView;
import com.zdv.reckoning.R;
import com.zdv.reckoning.utils.Constant;
import com.zdv.reckoning.utils.Utils;
import com.zdv.reckoning.utils.VToast;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/8/8 18:42
 */
public class FragmentSetting extends DialogFragment {
    protected final String COOKIE_KEY = "cookie";
    private  final String SERVER_IP = "SERVER_IP";
    protected final String SERVER_PORT = "SERVER_PORT";
    protected final String SHOP_NAME = "SHOP_NAME";
    protected final String SHOP_TEL = "SHOP_TEL";
    Utils utils;
    SharedPreferences sp;
    @Bind(R.id.setting_server_ip)
    EditText setting_server_ip;
    @Bind(R.id.setting_server_port)
    EditText setting_server_port;
    @Bind(R.id.setting_shop_name)
    EditText setting_shop_name;
    @Bind(R.id.setting_shop_tel)
    EditText setting_shop_tel;

    @Bind(R.id.verify_cancel_btn)
    Button verify_cancel_btn;
    @Bind(R.id.verify_do_btn)
    Button verify_do_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.set_layout, container, false);
        ButterKnife.bind(FragmentSetting.this, view);

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDate();
        initView();
    }


    private void initDate() {
        utils = Utils.getInstance();
        sp = getActivity().getSharedPreferences(COOKIE_KEY, Context.MODE_PRIVATE);

    }

    protected void initView() {

        RxView.clicks(verify_do_btn).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(s -> Submit());
        RxView.clicks(verify_cancel_btn).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(s -> dismiss());

        setting_server_ip.setText(sp.getString(SERVER_IP,"192.168.1.66"));
        setting_server_port.setText(sp.getString(SERVER_PORT,"8080"));
        setting_shop_name.setText(sp.getString(SHOP_NAME,""));
        setting_shop_tel.setText(sp.getString(SHOP_TEL,""));
    }

    private void Submit() {
        if (!utils.isIP(setting_server_ip.getText().toString().trim())) {
            VToast.toast(getActivity(), "请输入正确的IP");
        }
        if (setting_server_port.getText().toString().trim().equals("")) {
            VToast.toast(getActivity(), "请输入端口");
        }
        if (setting_shop_name.getText().toString().trim().equals("")) {
            VToast.toast(getActivity(), "请输入门店名称");
        }
        if (setting_shop_tel.getText().toString().trim().equals("")) {
            VToast.toast(getActivity(), "请输入门店电话");
        }

        SharedPreferences.Editor editor = sp.edit();
        editor.putString(SERVER_IP,setting_server_ip.getText().toString().trim());
        editor.putString(SERVER_PORT,setting_server_port.getText().toString().trim());
        editor.putString(SHOP_NAME,setting_shop_name.getText().toString().trim());
        editor.putString(SHOP_TEL,setting_shop_tel.getText().toString().trim());
        editor.commit();

        Constant.URL_DIANCANG = "http://"+setting_server_ip.getText().toString().trim()+":"+
                setting_server_port.getText().toString().trim()+"/ZDV_CYXT/";

        VToast.toast(getActivity(),"设置成功");
        dismiss();
    }

}
