package com.zdv.reckoning.fragment;

import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.jakewharton.rxbinding2.view.RxView;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.ViewHolder;
import com.socks.library.KLog;
import com.zdv.reckoning.R;
import com.zdv.reckoning.adapter.DishAdapter;
import com.zdv.reckoning.bean.DishBean;
import com.zdv.reckoning.customView.RecyclerViewWithEmpty;
import com.zdv.reckoning.present.QueryPresent;
import com.zdv.reckoning.utils.Constant;
import com.zdv.reckoning.utils.Utils;
import com.zdv.reckoning.utils.VToast;
import com.zdv.reckoning.view.IPayView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import it.gmariotti.recyclerview.adapter.AlphaAnimatorAdapter;
import okhttp3.ResponseBody;

public class FragmentMain extends BaseFragment implements IPayView {
    private static final int INTENT_QHW_SWISHCARDAPP = 101;
    /**
     * =====================打印信息================
     */
    private final String pay_type = "pay_type";//支付类型
    private final String cashier = "cashier";//收银员
    private final String order_no = "order_no";//账单号
    private final String table_no = "table_no";//台号
    private final String discount_money = "discount_money";//优惠金额
    private final String discount_rate_flag = "discount_rate_flag";//优惠折扣
    private final String total_money = "total_money";//总金额
    private final String act_money = "act_money";//总金额
    private final String eat_time = "eat_time";//下单时间
    private final String merchant_name = "merchant_name";//商户名称
    private final String merchant_phone = "merchant_phone";//商户电话
    private final String meal_order = "meal_order";//餐时
    private final String shop_name = "shop_name";//餐时
    private final String eat_person_num = "eat_person_num";//吃饭人数

    QueryPresent present;
    Utils util;
    SharedPreferences sp;
    View view;

    ArrayList<DishBean> data;
    DishAdapter adapter;
    @Bind(R.id.main_dish_list)
    RecyclerViewWithEmpty main_dish_list;

    @Bind(R.id.main_tip_lay)
    LinearLayout main_tip_lay;
    @Bind(R.id.main_root_lay)
    RelativeLayout main_root_lay;

    @Bind(R.id.main_discount_lay)
    LinearLayout main_discount_lay;
    @Bind(R.id.main_pay_lay)
    LinearLayout main_pay_lay;
    @Bind(R.id.main_total_tv)
    TextView main_total_tv;
    @Bind(R.id.main_origin_tv)
    TextView main_origin_tv;
    @Bind(R.id.main_tip_tv)
    TextView main_tip_tv;

    @Bind(R.id.main_discount_tv)
    TextView main_discount_tv;
    @Bind(R.id.main_discount_iv)
    ImageView main_discount_iv;

    @Bind(R.id.main_tip_scan_iv)
    ImageView main_tip_scan_iv;

    @Bind(R.id.empty_iv)
    ImageView empty_iv;
    @Bind(R.id.empty_tv)
    TextView empty_tv;
    @Bind(R.id.empty_lay)
    RelativeLayout empty_lay;
    private String key;
    Double total;//实际付款
    Double discount_amount;//优惠额
    Double discount_rate;//折扣率
    Double act;//原本需付款
    HashMap<String, String> print_info = new HashMap<>();
    private PayResultBroadcastReceiver receiver = new PayResultBroadcastReceiver();
    /**
     * 计算器界面数字
     *****/
    StringBuilder temp_add_sb, temp_dec_sb;
    protected View popupWindowViewVerify;
    protected PopupWindow popupWindowVerify;
    private int calculate_type;
    Button cancel, confirm;
    protected TextView calculate_act_et, calculate_charge_tv, calculate_total_tv, calculate_label1_tv, calculate_label2_tv, calculate_label3_tv,
            btn_dot, verify_btn_txt0, verify_btn_txt1, verify_btn_txt2, verify_btn_txt3, verify_btn_txt4, verify_btn_txt5, verify_btn_txt6,
            verify_btn_txt7, verify_btn_txt8, verify_btn_txt9;
    protected ImageView btn_delete;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            KLog.v(msg.what + "");
            switch (msg.what) {
                case 0x11:
                    String[] payResult = (String[]) msg.obj;
                    KLog.v(payResult[1]);
                    if (payResult[1].equals("success")) {
                        if (discount_rate == null) {
                            if (discount_amount == null) {//没有优惠
                                SynchronizePay();
                                return;
                            }
                            SynchronizeMoney();//反馈折扣

                        } else {
                            SynchronizeRate();//反馈减免
                        }
                    } else {
                        VToast.toast(getContext(), "支付失败，请重试");
                    }
                    break;
            }
        }

    };

    private void CashPay() {
        print_info.put(pay_type, "2");
        print_info.put(act_money, total + "");
        print_info.put(discount_money, total - act + "");
        KLog.v("discount_amount"+discount_amount+"discount_rate"+discount_rate);
        if (discount_rate == null) {
            if (discount_amount == null) {//没有优惠
                SynchronizePay();
                return;
            }
            SynchronizeMoney();//反馈折扣
        } else {
            SynchronizeRate();//反馈减免
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.bind(FragmentMain.this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initDate();
        initView();
    }

    private void initView() {
        main_root_lay.setVisibility(View.GONE);
        main_tip_lay.setVisibility(View.VISIBLE);

        RxView.clicks(main_pay_lay).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(s -> ChoosePay());
        RxView.clicks(main_tip_scan_iv).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(s -> Scan());
        RxView.clicks(main_discount_lay).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(s -> ChooseDiscount());

        data = new ArrayList<>();
        adapter = new DishAdapter(data, getContext());
        main_dish_list.setLayoutManager(new LinearLayoutManager(getActivity()));
        AlphaAnimatorAdapter animatorAdapter = new AlphaAnimatorAdapter(adapter, main_dish_list);
        main_dish_list.setEmptyView(empty_lay);
        main_dish_list.setAdapter(animatorAdapter);

        setEmptyStatus(false);
        adapter.notifyDataSetChanged();

        if (!Constant.codeStr.equals("")) {
            fetchFromNetWork(Constant.codeStr);
            Constant.codeStr = "";
        }

    }

    private void test() {
        total = 0.00;
        data.clear();
        for (int s = 0; s < 6; s++) {
            DishBean dishBean = new DishBean();
            dishBean.setDj("60");
            dishBean.setSpfl("菜菜菜菜菜菜菜菜菜" + s);
            dishBean.setSl(s + "");
            dishBean.setZk("1");
            total = util.add(total, Double.parseDouble(dishBean.getSl()) * Double.parseDouble(dishBean.getDj()));
            data.add(dishBean);
        }
        act = total;
        main_total_tv.setText("(￥" + total + ")");
        main_origin_tv.setText("总计:" + total);

        main_root_lay.setVisibility(View.VISIBLE);
        main_tip_lay.setVisibility(View.GONE);
        listener.showTableNum();

        adapter.notifyDataSetChanged();
    }

    /**
     * 弹出计算器
     *
     * @param type 1  折扣  2 优惠金额  3 找零
     */
    private void initVerifyView(int type) {
        calculate_type = type;

        calculate_act_et = (TextView) popupWindowViewVerify.findViewById(R.id.calculate_act_et);
        calculate_charge_tv = (TextView) popupWindowViewVerify.findViewById(R.id.calculate_charge_tv);
        calculate_total_tv = (TextView) popupWindowViewVerify.findViewById(R.id.calculate_total_tv);

        calculate_label1_tv = (TextView) popupWindowViewVerify.findViewById(R.id.calculate_label1_tv);
        calculate_label2_tv = (TextView) popupWindowViewVerify.findViewById(R.id.calculate_label2_tv);
        calculate_label3_tv = (TextView) popupWindowViewVerify.findViewById(R.id.calculate_label3_tv);

        cancel = (Button) popupWindowViewVerify.findViewById(R.id.calculate_cancel_btn);
        confirm = (Button) popupWindowViewVerify.findViewById(R.id.calculate_confirm_btn);

        btn_dot = (TextView) popupWindowViewVerify.findViewById(R.id.btn_dot);
        verify_btn_txt0 = (TextView) popupWindowViewVerify.findViewById(R.id.btn_txt0);
        verify_btn_txt1 = (TextView) popupWindowViewVerify.findViewById(R.id.btn_txt1);
        verify_btn_txt2 = (TextView) popupWindowViewVerify.findViewById(R.id.btn_txt2);
        verify_btn_txt3 = (TextView) popupWindowViewVerify.findViewById(R.id.btn_txt3);
        verify_btn_txt4 = (TextView) popupWindowViewVerify.findViewById(R.id.btn_txt4);
        verify_btn_txt5 = (TextView) popupWindowViewVerify.findViewById(R.id.btn_txt5);
        verify_btn_txt6 = (TextView) popupWindowViewVerify.findViewById(R.id.btn_txt6);
        verify_btn_txt7 = (TextView) popupWindowViewVerify.findViewById(R.id.btn_txt7);
        verify_btn_txt8 = (TextView) popupWindowViewVerify.findViewById(R.id.btn_txt8);
        verify_btn_txt9 = (TextView) popupWindowViewVerify.findViewById(R.id.btn_txt9);

        btn_delete = (ImageView) popupWindowViewVerify.findViewById(R.id.btn_delete);

        RxView.clicks(verify_btn_txt1).subscribe(s -> textInput('1'));
        RxView.clicks(verify_btn_txt2).subscribe(s -> textInput('2'));
        RxView.clicks(verify_btn_txt3).subscribe(s -> textInput('3'));
        RxView.clicks(verify_btn_txt4).subscribe(s -> textInput('4'));
        RxView.clicks(verify_btn_txt5).subscribe(s -> textInput('5'));
        RxView.clicks(verify_btn_txt6).subscribe(s -> textInput('6'));
        RxView.clicks(verify_btn_txt7).subscribe(s -> textInput('7'));
        RxView.clicks(verify_btn_txt8).subscribe(s -> textInput('8'));
        RxView.clicks(verify_btn_txt9).subscribe(s -> textInput('9'));
        RxView.clicks(verify_btn_txt0).subscribe(s -> textInput('0'));
        RxView.clicks(btn_dot).subscribe(s -> textInput('.'));
        RxView.clicks(btn_delete).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(s -> backspace());
        RxView.clicks(confirm).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(s -> CalculateConfirm());
        RxView.clicks(cancel).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(s -> CalculateCancel());

        switch (calculate_type) {
            case 1:
                calculate_total_tv.setText(act + "");
                btn_dot.setVisibility(View.INVISIBLE);
                calculate_label1_tv.setText("折后");
                calculate_label2_tv.setText("折扣率");
                calculate_label3_tv.setText("总额");
                break;
            case 2:
                calculate_charge_tv.setText(act + "");
                calculate_total_tv.setText(act + "");
                calculate_label1_tv.setText("优惠后");
                calculate_label2_tv.setText("优惠");
                calculate_label3_tv.setText("总额");
                break;
            case 3:
                calculate_total_tv.setText(total + "");
                calculate_label1_tv.setText("找零");
                calculate_label2_tv.setText("实收");
                calculate_label3_tv.setText("应收");
                break;
        }
    }

    /**
     * 计算器按取消键
     */
    private void CalculateCancel() {
        temp_dec_sb = null;
        temp_add_sb = null;
        popupWindowVerify.dismiss();
    }

    /**
     * 计算器按确定键
     */
    private void CalculateConfirm() {
        switch (calculate_type) {
            case 1:
                discount_rate = Integer.parseInt(calculate_act_et.getText().toString()) * 0.01;
                total = Double.parseDouble(calculate_charge_tv.getText().toString());
                print_info.put(discount_rate_flag, discount_rate + "");
                main_total_tv.setText(" (￥" + total + ")");
                main_discount_iv.setImageResource(R.drawable.zhe_red);
                main_discount_tv.setText(" " + discount_rate);
                break;
            case 2:
                discount_amount = Double.parseDouble(calculate_act_et.getText().toString());

                total = Double.parseDouble(calculate_charge_tv.getText().toString());
                print_info.put(discount_money, discount_money + "");
                main_total_tv.setText(" (￥" + Math.round(total) + ")");
                main_discount_iv.setImageResource(R.drawable.jianmian);
                main_discount_tv.setText(" " + discount_amount);
                break;
            case 3:

                KLog.v(Double.parseDouble(calculate_act_et.getText().toString()) + "--------" + total);
                if (Double.parseDouble(calculate_act_et.getText().toString()) - total < 0) {
                    VToast.toast(getContext(), "不能小于应收金额");
                    return;
                }
                listener.closeScan();
                mHandler.postDelayed(()->  CashPay(),2000);
                break;
        }
        temp_dec_sb = null;
        temp_add_sb = null;
        popupWindowVerify.dismiss();
    }

    /**
     * 计算器输入
     *
     * @param paramChar
     */
    protected void textInput(char paramChar) {
        temp_add_sb = new StringBuilder();
        if (calculate_act_et.getText().toString().equals("0")) {
            if (paramChar == '.') {
                temp_add_sb.append('0').append('.');
            } else {
                temp_add_sb.append(paramChar);
            }
        } else {
            temp_add_sb.append(calculate_act_et.getText().toString().toCharArray());
            if (paramChar == '.' && calculate_act_et.getText().toString().indexOf('.') != -1) {

            } else {
                temp_add_sb.append(paramChar);
            }
        }
        if (temp_add_sb.length() > 3 && temp_add_sb.charAt(temp_add_sb.length() - 3) == '.') {//保留一位小数
            return;
        }
        if (temp_add_sb.length() > 7) {
            return;
        }
        if (paramChar == '.') {
            calculate_act_et.setText(temp_add_sb);
            return;
        }
        switch (calculate_type) {// 计算器类型
            case 1://折扣
                switch (temp_add_sb.length()) {
                    case 1:
                    case 2:
                        calculate_act_et.setText(temp_add_sb.toString());
                        break;
                    default:
                        calculate_act_et.setText("100");
                        break;
                }
                BigDecimal b1 = new BigDecimal(act *  Double.parseDouble(calculate_act_et.getText().toString())*0.01);
                double result2 = b1.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                calculate_charge_tv.setText(result2 + "");
                break;
            case 2://优惠
                if (Double.parseDouble(temp_add_sb.toString()) > act) {
                    discount_amount = act;
                    calculate_act_et.setText(act + "");
                    calculate_charge_tv.setText("0");
                    return;
                }
                calculate_act_et.setText(temp_add_sb.toString());
                double result = new BigDecimal(act - Double.parseDouble(calculate_act_et.getText().toString())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                calculate_charge_tv.setText(result + "");

                break;
            case 3://找零
                calculate_act_et.setText(temp_add_sb.toString());
                double result3 = new BigDecimal(Double.parseDouble(temp_add_sb.toString()) - total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                calculate_charge_tv.setText(result3 + "");
                break;
        }
    }

    /**
     * 计算器退格
     */
    protected void backspace() {
        temp_dec_sb = new StringBuilder();
        temp_dec_sb.append(calculate_act_et.getText().toString().toCharArray()).deleteCharAt(calculate_act_et.getText().toString().length() - 1);
        if (temp_dec_sb.length() == 0) {
            temp_dec_sb.append('0');
            calculate_act_et.setText("0");
        }
        switch (calculate_type) {
            case 1://折扣
                calculate_act_et.setText(temp_dec_sb.toString());
                BigDecimal b1 = new BigDecimal(act * Double.parseDouble(calculate_act_et.getText().toString())*0.01);
                double result2 = b1.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                calculate_charge_tv.setText(result2 + "");

                break;
            case 2://优惠
                calculate_act_et.setText(temp_dec_sb.toString());
                double result = new BigDecimal(act - Double.parseDouble(calculate_act_et.getText().toString())).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                calculate_charge_tv.setText(result + "");
                break;
            case 3://找零
                double result3 = new BigDecimal(Double.parseDouble(temp_dec_sb.toString()) - total).setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
                calculate_charge_tv.setText(result3 + "");
                calculate_act_et.setText(temp_dec_sb.toString());
                break;
        }

    }

    /**
     * 打开摄像头扫描
     */
    private void Scan() {
        listener.gotoScan();
    }

    /**
     * 选择优惠方式
     */
    private void ChooseDiscount() {
        DialogPlus dialog = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(R.layout.discount_lay))
                .setCancelable(true)
                .setOnClickListener((dialog1, view1) -> {
                    switch (view1.getId()) {
                        case R.id.item_hui_lay:
                            showCalculate(2);
                            break;
                        case R.id.item_zhe_lay:
                            showCalculate(1);
                            break;
                        case R.id.item_cancel_lay:
                            discount_rate = null;
                            discount_amount = null;
                            total = act;

                            main_total_tv.setText(act + "");
                            main_discount_iv.setImageResource(R.drawable.pay_icon);
                            main_discount_tv.setText("");
                            break;

                    }
                    dialog1.dismiss();
                })
                .create();
        dialog.show();
    }

    /**
     * 选择支付方式
     */
    private void ChoosePay() {
        DialogPlus dialog = DialogPlus.newDialog(getContext())
                .setContentHolder(new ViewHolder(R.layout.pay_lay))
                .setCancelable(true)
                .setOnClickListener((dialog1, view1) -> {
                    switch (view1.getId()) {
                        case R.id.item_wxpay_lay:
                            //          KLog.v("微信");
                            listener.closeScan();
                            mHandler.postDelayed(()-> Pay("1", "0"),2000);

                            break;
                        case R.id.item_alipay_lay:
                            //            KLog.v("支付宝");
                            listener.closeScan();
                            mHandler.postDelayed(()-> Pay("1", "1"),2000);
                            break;
                        case R.id.item_cash_lay:
                            //           KLog.v("现金");
                            print_info.put(pay_type, "3");
                            showCalculate(3);
                            break;
                    }
                    dialog1.dismiss();
                })
                .create();
        dialog.show();
    }

    /**
     * 显示计算器
     *
     * @param type
     */
    protected void showCalculate(int type) {
        if (popupWindowVerify == null) {
            popupWindowViewVerify = View.inflate(getContext(), R.layout.caculate_lay, null);
            popupWindowVerify = new PopupWindow(popupWindowViewVerify, ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindowVerify.setAnimationStyle(R.style.AnimationBottomFade);
            ColorDrawable dw = new ColorDrawable(0xffffffff);
            popupWindowVerify.setBackgroundDrawable(dw);
        }
        popupWindowVerify.setOnDismissListener(() -> {
            backgroundAlpha(1.0f);
            popupWindowViewVerify.destroyDrawingCache();
            popupWindowViewVerify = null;
            popupWindowVerify = null;
        });

        backgroundAlpha(0.5f);
        initVerifyView(type);
        popupWindowVerify.showAtLocation(View.inflate(getContext(), R.layout.fragment_main, null),
                Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);
    }

    /**
     * popview背景变暗
     *
     * @param v
     */
    protected void backgroundAlpha(float v) {
        WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
        lp.alpha = v;
        getActivity().getWindow().setAttributes(lp);
    }

    /**
     * 提交折扣信息
     */
    private void SynchronizeRate() {
        showWaitDialog("请稍等");
        present.initRetrofit(Constant.URL_DIANCANG, false);
        present.QueryDiscountRate(key, discount_rate + "");
    }

    /**
     * 提交优惠信息
     */
    private void SynchronizeMoney() {
        showWaitDialog("请稍等");
        present.initRetrofit(Constant.URL_DIANCANG, false);
        present.QueryDiscountMoney(key, print_info.get("order_no"),"-"+discount_amount );
    }

    /**
     * 同步支付信息
     */
    private void SynchronizePay() {
        showWaitDialog("请稍等");
        present.initRetrofit(Constant.URL_DIANCANG, false);
        present.QuerySynchronizePay(key);
    }


    @Override
    public void Back() {
        super.Back();
    }


    @Override
    public void refreshState() {
        super.refreshState();
        if (!Constant.codeStr.equals("")) {
            fetchFromNetWork(Constant.codeStr);
            Constant.codeStr = "";
        }
    }

    private void initDate() {
        util = Utils.getInstance();
        present = QueryPresent.getInstance(getContext());
        present.setView(FragmentMain.this);
        sp = getContext().getSharedPreferences(COOKIE_KEY, Context.MODE_PRIVATE);
        registerReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unRegisterReceiver();
    }

    /**
     * 列表为空时显示
     *
     * @param isOffLine
     */
    protected void setEmptyStatus(boolean isOffLine) {

        if (isOffLine) {
            empty_iv.setImageResource(R.drawable.network_error);
            empty_tv.setText("(=^_^=)，粗错了，点我刷新试试~");
            empty_lay.setEnabled(true);
            RxView.clicks(empty_iv).throttleFirst(500, TimeUnit.MILLISECONDS).subscribe(s -> emptyClick());
        } else {
            empty_lay.setEnabled(false);
            empty_iv.setImageResource(R.drawable.smile);
            empty_tv.setText("无内容");
        }
    }

    /**
     * 无网络点击
     */
    protected void emptyClick() {
        showWaitDialog("正在努力加载...");
        fetchFromNetWork(key);
    }

    /**
     * 查询台号详情
     *
     * @param key_
     */
    public void fetchFromNetWork(String key_) {
        showWaitDialog("请稍等");
        key = key_;
        present.initRetrofit(Constant.URL_DIANCANG, false);
        present.QueryOrder(key_);
    }

    /**
     * 移动支付
     *
     * @param payType
     */
    private void Pay(String type, String payType) {

        print_info.put(pay_type, payType);
        print_info.put(act_money, total + "");
        print_info.put(discount_money, act - total + "");

        //   SynchronizeRate();
        //    listener.printOrder(data, print_info);
        try {
            Intent swishIntent = this.getActivity().getPackageManager().getLaunchIntentForPackage("com.qhw.swishcardapp");
            swishIntent.setFlags(INTENT_QHW_SWISHCARDAPP);
            Bundle bundle = new Bundle();
            bundle.putString("type", type);
            bundle.putString("money", total + "");
            bundle.putString("payType", payType);
            swishIntent.putExtra("bundle", bundle);
            startActivity(swishIntent);

        } catch (ActivityNotFoundException e) {
            e.fillInStackTrace();
            VToast.toast(getContext(), "请先前往万店通应用商店下载万店通刷App");
        }
    }

    /**
     * 注册广播接收者
     */
    private void registerReceiver() {
        IntentFilter filter = new IntentFilter("com.qhw.shopmanagerapp.pay.action");
        filter.addAction("com.qhw.shopmanagerapp.pay.action");
        getActivity().registerReceiver(receiver, filter);
    }

    /**
     * 注销广播接收者
     */
    private void unRegisterReceiver() {
        if (receiver == null) return;

        getActivity().unregisterReceiver(receiver);
        receiver = null;
    }

    @Override
    public void ResolveOrderInfo(ResponseBody info) {
        hideWaitDialog();
        listener.restartScan();
        if (info.source() == null) {
            VToast.toast(getContext(), "网络错误，请重试!");
            main_root_lay.setVisibility(View.GONE);
            main_tip_lay.setVisibility(View.VISIBLE);
            main_tip_tv.setText("服务端未开启");

            return;
        }
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(info.string());
            print_info = new HashMap<>();
            /**======解析台号信息======*/
            JSONArray dishObj2 = jsonObject.optJSONArray("ctxx");
            if (dishObj2 == null) {
                main_root_lay.setVisibility(View.GONE);
                main_tip_lay.setVisibility(View.VISIBLE);
                main_tip_tv.setText("台号("+key+")已经结账");
                return;
            }

            JSONObject jsonObject2 = dishObj2.getJSONObject(0);
            KLog.v(jsonObject2.toString());
            if( Boolean.parseBoolean(jsonObject2.optString("sfprint"))){
                VToast.toast(getContext(),"台号("+key+")已经结账");
                main_root_lay.setVisibility(View.GONE);
                main_tip_lay.setVisibility(View.VISIBLE);
                main_tip_tv.setText("台号("+key+")已经结账");
                return;
            }
            //        print_info.put(merchant_name, jsonObject2.optString("dj"));
            print_info.put(total_money, jsonObject2.optString("xxf"));
            print_info.put(meal_order, jsonObject2.optString("bc"));
            print_info.put(eat_time, jsonObject2.optString("kaitsj"));
            print_info.put(eat_person_num, jsonObject2.optString("rs"));
            print_info.put(shop_name, jsonObject2.optString("flag"));
            print_info.put(table_no, jsonObject2.optString("th"));
            print_info.put(order_no, jsonObject2.optString("zdh"));
            print_info.put(cashier, jsonObject2.optString("printp"));
            //       print_info.put(merchant_phone, jsonObject2.optString("yyr"));
            /**======解析菜单======*/
            JSONArray dishObj = jsonObject.optJSONArray("dcmx");
            if (dishObj == null) {
                main_root_lay.setVisibility(View.GONE);
                main_tip_lay.setVisibility(View.VISIBLE);
                main_tip_tv.setText("台号错误或者已经结账");
                return;
            }
            // KLog.v(dishObj.toString());
            total = 0.00;
            data.clear();
            for (int s = 0; s < dishObj.length(); s++) {
                JSONObject jsonObject1 = dishObj.getJSONObject(s);
                DishBean dishBean = new DishBean();
                dishBean.setDj(jsonObject1.optString("dj"));
                dishBean.setSpfl(jsonObject1.optString("spfl"));
                dishBean.setSl(jsonObject1.optString("sl"));
                dishBean.setZk(jsonObject1.optString("zk"));
                total = util.add(total, Double.parseDouble(dishBean.getSl()) * Double.parseDouble(dishBean.getDj()));
                data.add(dishBean);
            }
            act = total;
            main_total_tv.setText("(￥" + total + ")");
            main_origin_tv.setText("总计:" + total);
            main_discount_iv.setImageResource(R.drawable.pay_icon);
            main_discount_tv.setText("");
            discount_rate = null;
            discount_amount = null;

            main_root_lay.setVisibility(View.VISIBLE);
            main_tip_lay.setVisibility(View.GONE);
            listener.showTableNum();
            //listener.printOrder(data,print_info);
        } catch (JSONException e) {
            main_root_lay.setVisibility(View.GONE);
            main_tip_lay.setVisibility(View.VISIBLE);
            main_tip_tv.setText("台号错误或者已经结账");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            main_root_lay.setVisibility(View.GONE);
            main_tip_lay.setVisibility(View.VISIBLE);
            main_tip_tv.setText("台号错误或者已经结账");
            VToast.toast(getContext(), "网络超时");
            return;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void ResolveDiscountRateInfo(ResponseBody info) {
        hideWaitDialog();
        if (info.source() == null) {
            VToast.toast(getContext(), "网络错误，请重试!");
            return;
        }
        try {
            JSONObject jsonObject  = new JSONObject(info.string());
            KLog.v(jsonObject.toString());
            String dishObj2 = jsonObject.optString("mas");
            if (dishObj2 == null) {//提交没响应
                KLog.v("折扣提交失败");
                new MaterialDialog.Builder(getActivity())
                        .title("提示")
                        .content("与服务端连接错误，是否重试?")
                        .positiveText(R.string.bga_pp_confirm)
                        .negativeText(R.string.cancle)
                        .onNegative((materialDialog, dialogAction) -> {

                        })
                        .onPositive((materialDialog, dialogAction) -> {
                            SynchronizeRate();
                        })
                        .autoDismiss(true)
                        .cancelable(false)
                        .show();
            }

            if (dishObj2.equals("00")) {//提交折扣成功
               // fetchFromNetWork(key);
                SynchronizePay();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            VToast.toast(getContext(), "网络超时");
            return;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void ResolveDiscountMoneyInfo(ResponseBody info) {
        hideWaitDialog();
        if (info.source() == null) {
            VToast.toast(getContext(), "网络错误，请重试!");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(info.string());
            KLog.v(jsonObject.toString());
            String dishObj2 = jsonObject.optString("mas");
            if (dishObj2 == null) {//提交没响应
                KLog.v("优惠提交失败");
                new MaterialDialog.Builder(getActivity())
                        .title("提示")
                        .content("与服务端连接错误，是否重试?")
                        .positiveText(R.string.bga_pp_confirm)
                        .negativeText(R.string.cancle)
                        .onNegative((materialDialog, dialogAction) -> {

                        })
                        .onPositive((materialDialog, dialogAction) -> {
                            SynchronizeRate();
                        })
                        .autoDismiss(true)
                        .cancelable(false)
                        .show();
            }

            if (dishObj2.equals("00")) {//提交折扣成功
               // fetchFromNetWork(key);
                SynchronizePay();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            VToast.toast(getContext(), "网络超时");
            return;
        }
        adapter.notifyDataSetChanged();
    }

    @Override
    public void ResolveSynchronizePayInfo(ResponseBody info) {
        hideWaitDialog();
        if (info.source() == null) {
            VToast.toast(getContext(), "网络错误，请重试!");
            return;
        }
        try {
            JSONObject jsonObject = new JSONObject(info.string());
            KLog.v(info.string());
            String dishObj2 = jsonObject.optString("mas");
            KLog.v("dishObj2--"+dishObj2);
            if (dishObj2 == null) {//提交没响应
                KLog.v("结账提交失败");
                new MaterialDialog.Builder(getActivity())
                        .title("提示")
                        .content("与服务端连接错误，是否重试?")
                        .positiveText(R.string.bga_pp_confirm)
                        .negativeText(R.string.cancle)
                        .onNegative((materialDialog, dialogAction) -> {

                        })
                        .onPositive((materialDialog, dialogAction) -> {
                            SynchronizeRate();
                        })
                        .autoDismiss(true)
                        .cancelable(false)
                        .show();
            }
            if (dishObj2.equals("00")) {//结账成功
                listener.printOrder(data, print_info);
                VToast.toast(getContext(), "结账成功");
                listener.hideTableNum();
                main_tip_lay.setVisibility(View.VISIBLE);
                main_tip_tv.setText("台号("+key+")已经结账");
                main_root_lay.setVisibility(View.GONE);
            }else if (dishObj2.equals("01")) {//已经结账
                VToast.toast(getContext(), "台号已经结账");
                main_tip_lay.setVisibility(View.VISIBLE);
                main_root_lay.setVisibility(View.GONE);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            VToast.toast(getContext(), "网络超时");
            return;
        }
        adapter.notifyDataSetChanged();
    }

    /**
     * 接收支付状态广播
     */
    public class PayResultBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals("com.qhw.shopmanagerapp.pay.action")) {
                String result = intent.getBundleExtra("bundle").getString("result");
                String type = intent.getBundleExtra("bundle").getString("pay_type");
                String str[] = {type, result};
                Message message = mHandler.obtainMessage();
                message.what = 0x11;
                message.obj = str;
                mHandler.sendMessage(message);
            }
        }
    }
}
