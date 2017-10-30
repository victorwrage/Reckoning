package com.zdv.reckoning.present;


/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/4/7 9:46
 */

public interface IRequestPresent {


    void QueryOrder(String th);
    void QueryDiscountRate(String th, String zkl);
    void QueryDiscountMoney(String th,String zk);
    void QuerySynchronizePay(String th);
}
