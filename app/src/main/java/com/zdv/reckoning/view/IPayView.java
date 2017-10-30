package com.zdv.reckoning.view;


import okhttp3.ResponseBody;

/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/4/7 9:49
 */

public interface IPayView extends IView{

    void ResolveOrderInfo(ResponseBody info);
    void ResolveDiscountRateInfo(ResponseBody info);
    void ResolveDiscountMoneyInfo(ResponseBody info);
    void ResolveSynchronizePayInfo(ResponseBody info);


}
