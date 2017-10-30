package com.zdv.reckoning;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.gyf.barlibrary.ImmersionBar;
import com.pos.api.Printer;
import com.socks.library.KLog;
import com.zdv.reckoning.acticity.BaseActivity;
import com.zdv.reckoning.bean.DishBean;
import com.zdv.reckoning.fragment.FragmentMain;
import com.zdv.reckoning.fragment.FragmentScan;
import com.zdv.reckoning.utils.Constant;
import com.zdv.reckoning.utils.D2000V1ScanInitUtils;
import com.zdv.reckoning.utils.Utils;
import com.zdv.reckoning.utils.VToast;
import com.zdv.reckoning.view.IFragmentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;

import butterknife.Bind;
import butterknife.ButterKnife;
import cn.bmob.v3.update.BmobUpdateAgent;
import cn.bmob.v3.update.UpdateStatus;


public class MainActivity extends BaseActivity implements IFragmentActivity {
    /**
     * =====================打印信息================
     */
    private final String pay_type = "pay_type";//支付类型
    private final String cashier = "cashier";//收银员
    private final String order_no = "order_no";//账单号
    private final String table_no = "table_no";//台号
    private final String discount_money = "discount_money";//优惠金额
    private final String discount_rate = "discount_rate";//优惠折扣

    private final String total_money = "total_money";//总金额
    private final String act_money = "act_money";//总金额
    private final String eat_time = "eat_time";//下单时间
    private final String merchant_name = "merchant_name";//商户名称
    private final String merchant_phone = "merchant_phone";//商户电话
    private final String meal_order = "meal_order";//餐时
    private final String shop_name = "shop_name";//餐时
    private final String eat_person_num = "eat_person_num";//吃饭人数

    protected static final String SUCCESS = "200";

    private final String COOKIE_KEY = "cookie";

    D2000V1ScanInitUtils d2000V1ScanInitUtils;
    private final static int SCAN_CLOSED = 20;
    Printer printer;


    @Bind(R.id.main_header_lay)
    RelativeLayout main_header_lay;
    @Bind(R.id.activity_table_tv)
    TextView activity_table_tv;

    SharedPreferences sp;

    Utils util;
    private String code;
    private boolean isInit = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fore_lay);
        ButterKnife.bind(MainActivity.this);
        initDate();
        initView();
    }

    private void initDate() {
        Constant.temp_info = new HashMap<>();

        executor = Executors.newSingleThreadScheduledExecutor();
        util = Utils.getInstance();
        sp = getSharedPreferences(COOKIE_KEY, 0);

        BmobUpdateAgent.setUpdateListener((updateStatus, updateInfo) -> {
            if (updateStatus == UpdateStatus.Yes) {//版本有更新

            } else if (updateStatus == UpdateStatus.No) {
                KLog.v("版本无更新");
                if (Constant.MESSAGE_UPDATE_TIP.equals("")) {
                    Constant.MESSAGE_UPDATE_TIP = "APP已是最新版本!";
                } else {
                    VToast.toast(context, Constant.MESSAGE_UPDATE_TIP);
                }
            } else if (updateStatus == UpdateStatus.EmptyField) {//此提示只是提醒开发者关注那些必填项，测试成功后，无需对用户提示
                KLog.v("请检查你AppVersion表的必填项，1、target_size（文件大小）是否填写；2、path或者android_url两者必填其中一项。");
            } else if (updateStatus == UpdateStatus.IGNORED) {
                KLog.v("该版本已被忽略更新");
            } else if (updateStatus == UpdateStatus.ErrorSizeFormat) {
                KLog.v("请检查target_size填写的格式，请使用file.length()方法获取apk大小。");
            } else if (updateStatus == UpdateStatus.TimeOut) {
                KLog.v("查询出错或查询超时");
            }
        });
        // BmobUpdateAgent.initAppVersion(context);
        BmobUpdateAgent.update(MainActivity.this);
        initScannerPrinter();
    }

    private void initView() {
        ImmersionBar.with(this).barColor(R.color.reckoning_txt).init();

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        fragment0 = new FragmentMain();
        cur_page = 0;
        cur_fragment = fragment0;
        ft.add(R.id.fragment_container, fragment0, PAGE_0);
        ft.show(fragment0);
        ft.commit();
    }

    private Handler promptHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 6:
                    KLog.v((String) msg.obj);
                    sendData((String) msg.obj);
                    break;
                case SCAN_CLOSED:
                    //        if (fragment1 != null) fragment1.print();
                    break;
                default:
                    break;
            }
        }


    };

    private void sendData(String obj) {
        isInit = true;
        code = obj;
        fragment0.fetchFromNetWork(obj);
    }

    public void closeScan() {
        showWaitDialog("请稍等");
        executor.execute(() -> d2000V1ScanInitUtils.setScanState());
        promptHandler.postDelayed(() -> {
            hideWaitDialog();
        }, 2000);
    }

    @Override
    public void showTableNum() {
        activity_table_tv.setText("结账（台号:" + code + ")");
    }

    @Override
    public void hideTableNum() {
        activity_table_tv.setText("结账");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Constant.ENABLE_SCAN_PRINT && cur_fragment instanceof FragmentMain) {
            initDevice();
            if (isInit) {
                showWaitDialog("请稍后");
                promptHandler.postDelayed(() -> {
                    hideWaitDialog();
                }, 5000);
            }
        }
    }

    @Override
    protected void onDestroy() {
        ImmersionBar.with(this).destroy();
        super.onDestroy();
        if (Constant.ENABLE_SCAN_PRINT) {
            if (d2000V1ScanInitUtils != null) {
                d2000V1ScanInitUtils.close();
                if (printer != null) {
                    printer.DLL_PrnRelease();
                }
                d2000V1ScanInitUtils = null;
            }
        }
    }

    private void startScan() {
        showWaitDialog("请稍等");
        promptHandler.postDelayed(() -> {
            hideWaitDialog();
            executor.execute(() -> {
                d2000V1ScanInitUtils.open();
                d2000V1ScanInitUtils.d2000V1ScanOpen();
            });
        }, 2000);
    }

    @Override
    public void printOrder(ArrayList<DishBean> data, HashMap<String, String> print_info) {
        KLog.v("print" + Constant.ENABLE_SCAN_PRINT);
        if (!Constant.ENABLE_SCAN_PRINT) {
            return;
        }
        showWaitDialog("请等待打印完成");
        KLog.v("print" + Constant.ENABLE_SCAN_PRINT);
        printer = new Printer(this, bRet -> executor.execute(() -> {
            int iRet = -1;
            iRet = printer.DLL_PrnInit();
            KLog.v("iRet" + iRet);
            if (iRet == 0) {
                printContent(data, print_info);
            } else {
                hideWaitDialog();
                VToast.toast(context, "打印错误");
            }
        }));

        promptHandler.postDelayed(() -> {
            hideWaitDialog();
            new MaterialDialog.Builder(MainActivity.this)
                    .title("提示")
                    .content("是否重新打印?")
                    .positiveText(R.string.bga_pp_confirm)
                    .negativeText(R.string.cancle)
                    .onNegative((materialDialog, dialogAction) -> {
                        restartScan();
                    })
                    .onPositive((materialDialog, dialogAction) -> {
                        printOrder(data, print_info);
                    })
                    .autoDismiss(true)
                    .cancelable(false)
                    .show();
        }, 3000);
    }

    @Override
    public void restartScan() {
        if (!Constant.ENABLE_SCAN_PRINT) {
            return;
        }
        startScan();
    }

    @Override
    public void gotoScan() {
        gotoPage(1);
    }

    private void printContent(ArrayList<DishBean> data, HashMap<String, String> print_info) {
        String pay_tp = "";

        switch (Integer.parseInt(print_info.get(pay_type) + "")) {
            case 0:
                pay_tp = "微信";
                break;
            case 1:
                pay_tp = "支付宝";
                break;
            case 2:
                pay_tp = "现金";
                break;
        }
        //       Bitmap bitmap = util.readBitMap(context, R.drawable.print_icon);
//        Bitmap allBitmap2 = util.createLogo2(context,bitmap);
        //    printer.DLL_PrnBmp(allBitmap2);

        printer.DLL_PrnSetFont((byte) 24, (byte) 24, (byte) 0x00);
        printer.DLL_PrnStr("---------------------------------------\n");
        if(print_info.get(merchant_name) != null) {
            printer.DLL_PrnStr(print_info.get(merchant_name) + "\n");
        }
        printer.DLL_PrnStr("---------------------------------------\n");
        printer.DLL_PrnStr("台    号:" + print_info.get(table_no) + "\n");
        printer.DLL_PrnStr("账 单 号:" + print_info.get(order_no) + "\n");
        printer.DLL_PrnStr("餐    时:" + print_info.get(meal_order) + "         人   数" + print_info.get(eat_person_num) + "（人）\n");
        printer.DLL_PrnStr("---------------------------------------\n");
        printer.DLL_PrnStr("消费项目      单价       数量    金额\n");
        printer.DLL_PrnStr("---------------------------------------\n");
        for (int s = 0; s < data.size(); s++) {
            int len = data.get(s).getSpfl().length();
            String blank_space =  "";
            switch(len){
                case 3:
                    blank_space =  "     ";
                    break;
                case 2:
                     blank_space =  "        ";
                    break;
                case 1:
                    blank_space =  "              ";
                    break;
                default:
                    break;
            }
            printer.DLL_PrnStr(data.get(s).getSpfl() + blank_space + "      " + data.get(s).getDj() + "        " + data.get(s).getSl() + "      " +
                    Double.parseDouble(data.get(s).getSl()) * Double.parseDouble(data.get(s).getDj()) + "\n");
        }
        printer.DLL_PrnStr("---------------------------------------\n");
        printer.DLL_PrnStr("总计:" + print_info.get(total_money) + "元\n");
        if(print_info.get(discount_money) == null){
            printer.DLL_PrnStr("优惠金额 ");
        }else {
            String dis_m = Double.parseDouble(print_info.get(discount_money)) == 0 ? "" : print_info.get(discount_money) + "元\n";
            printer.DLL_PrnStr("优惠金额 " + dis_m);
        }
        if(print_info.get(discount_rate) == null){
            printer.DLL_PrnStr("折扣率");
        }else {
            String rate = (Integer.parseInt(print_info.get(discount_rate)) == 1) ? "" : "" + Double.parseDouble(print_info.get(discount_rate)) * 100 + "折\n";
            printer.DLL_PrnStr("折扣率" + rate);
        }

        printer.DLL_PrnStr("---------------------------------------\n");
        printer.DLL_PrnStr("支付日期:" + currentDate("yyyyMMdd HH:mm:ss") + "\n");
        printer.DLL_PrnStr("支付类型:" + pay_tp + "\n");
        printer.DLL_PrnStr("  \n");

        printer.DLL_PrnStr("应收金额:" + print_info.get(act_money) + "元\n");
        printer.DLL_PrnStr("---------------------------------------\n");
        printer.DLL_PrnStr("  \n");
        printer.DLL_PrnSetFont((byte) 20, (byte) 20, (byte) 0x00);
        printer.DLL_PrnStr("备注:" + "  \n");
        printer.DLL_PrnStr("  \n");
        printer.DLL_PrnStr("          签名：" + "  \n");
        printer.DLL_PrnStr("  \n");
        //  Bitmap bitmap2 = syncEncodeQRCode("http://weixin.qq.com/r/VC6MlO3Ew7C8ran393tG");

        //     Bitmap allBitmap = util.createLogo(context,bitmap2);

        //   printer.DLL_PrnBmp(allBitmap);
        printer.DLL_PrnStr("  \n");
        printer.DLL_PrnSetFont((byte) 17, (byte) 17, (byte) 0x00);
        printer.DLL_PrnStr("             多谢惠顾，欢迎再次光临  \n");
        if(print_info.get(merchant_phone) != null) {
            printer.DLL_PrnStr("             订座电话：" + print_info.get(merchant_phone) + "\n");
        }
        printer.DLL_PrnStr("            \n");
        printer.DLL_PrnStr("---------------------------------------\n");
        printer.DLL_PrnStr("            \n");
        printer.DLL_PrnStr("            \n");
        printer.DLL_PrnStart();

    }

    private void initDevice() {
        executor.execute(() -> {
            KLog.v("initDevice");
            d2000V1ScanInitUtils = D2000V1ScanInitUtils.getInstance(MainActivity.this, promptHandler);
            if (!d2000V1ScanInitUtils.getStart()) {
                d2000V1ScanInitUtils.open();
            }
            d2000V1ScanInitUtils.d2000V1ScanOpen();
        });
    }

    private void initScannerPrinter() {
        String model = android.os.Build.MODEL;
        if (model.equals("D2000")) {
            Constant.ENABLE_SCAN_PRINT = true;
        }
    }


    private void gotoPage(int pageId) {
        if (pageId == cur_page) {
            return;
        }
        cur_page = pageId;
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.hide(cur_fragment);

        switch (pageId) {
            case 0:
                if (fragment0 == null) {
                    fragment0 = new FragmentMain();
                }
                if (!fragment0.isAdded()) {
                    ft.add(R.id.fragment_container, fragment0, PAGE_0);
                } else {
                    fragment0.refreshState();
                }
                cur_fragment = fragment0;
                ft.show(fragment0);
                main_header_lay.setVisibility(View.VISIBLE);
                break;
            case 1:
                if (fragment1 == null) {
                    fragment1 = new FragmentScan();
                }
                if (!fragment1.isAdded()) {
                    ft.add(R.id.fragment_container, fragment1, PAGE_1);
                } else {
                    fragment1.refreshState();
                }
                main_header_lay.setVisibility(View.GONE);
                cur_fragment = fragment1;
                ft.show(fragment1);
                break;

        }
        ft.commitNowAllowingStateLoss();
    }


    @Override
    public void gotoMain() {
        gotoPage(0);
    }
}
