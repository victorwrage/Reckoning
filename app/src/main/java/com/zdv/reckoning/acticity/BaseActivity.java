package com.zdv.reckoning.acticity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

import com.afollestad.materialdialogs.MaterialDialog;
import com.zdv.reckoning.R;
import com.zdv.reckoning.ReckoningApplication;
import com.zdv.reckoning.customView.ProgressBarItem;
import com.zdv.reckoning.fragment.BaseFragment;
import com.zdv.reckoning.fragment.FragmentMain;
import com.zdv.reckoning.fragment.FragmentScan;
import com.zdv.reckoning.utils.DoubleConfirm;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executor;


/**
 * @ClassName: BaseActivity
 * @Description:TODO(界面的基类)
 * @author: xiaoyl
 * @date: 2013-7-10 下午2:30:06
 */
public class BaseActivity extends FragmentActivity {

    protected Executor executor;

    protected FragmentMain fragment0;
    protected FragmentScan fragment1;


    protected static final String PAGE_0 = "page_0";
    protected static final String PAGE_1 = "page_1";
    protected static final String PAGE_2 = "page_2";
    protected static final String PAGE_3 = "page_3";
    protected static final String PAGE_4 = "page_4";

    protected BaseFragment cur_fragment;

    private DoubleConfirm double_c;

    protected Context context;
    protected int cur_page = -1;

    /**
     * 双击事件
     */
    private DoubleConfirm.DoubleConfirmEvent doubleConfirmEvent = new DoubleConfirm.DoubleConfirmEvent() {
        public void doSecondConfirmEvent() {
            new ReckoningApplication().getInstance().exitApplication();
        }

        public int getFirstConfirmTipsId() {
            return R.string.msg_exit;
        }
    };

    public String currentDate(String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date());
    }

    protected void showWaitDialog(String tip){
        ProgressBarItem.show(BaseActivity.this,tip,false,null);
    }
    protected void hideWaitDialog() {
        ProgressBarItem.hideProgress();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new ReckoningApplication().getInstance().addActivitys(this);
        context = getApplicationContext();
       // this.double_c = new DoubleConfirm();
       // this.double_c.setEvent(this.doubleConfirmEvent);
    }


    public boolean onKeyDown(int paramInt, KeyEvent paramKeyEvent) {
        if (paramInt == KeyEvent.KEYCODE_BACK) {

            switch (cur_page) {
                case 0:
                    new MaterialDialog.Builder(this)
                            .title("提示")
                            .content("确定退出结算吗？")
                            .positiveText(R.string.bga_pp_confirm)
                            .negativeText(R.string.cancle)
                            .autoDismiss(true)
                            .onPositive((materialDialog, dialogAction) -> {
                                new ReckoningApplication().getInstance().exitApplication();
                            })
                            .show();
                    break;
                case 2:
                    this.double_c.onKeyPressed(paramKeyEvent, this);
                    return true;
                case 1:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                    cur_fragment.Back();
                    return true;
                default:
                    this.double_c.onKeyPressed(paramKeyEvent, this);
                    return true;
            }
        }
        return false;
    }


}
