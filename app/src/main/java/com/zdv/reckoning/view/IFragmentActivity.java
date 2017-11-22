package com.zdv.reckoning.view;

import com.zdv.reckoning.bean.DishBean;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/8/22 17:09
 */

public interface IFragmentActivity {
    void gotoMain() ;

    void printOrder(ArrayList<DishBean> data, HashMap<String, String> print_info);

    void restartScan();

    void gotoScan();

    void closeScan();

    void showTableNum(String result);

    void hideTableNum();
}
