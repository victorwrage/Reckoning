package com.zdv.reckoning.model;


import io.reactivex.Flowable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;

/**
 * Info:接口实现类
 * Created by xiaoyl
 * 创建时间:2017/4/7 9:42
 */
public class RequestModelImpl implements IRequestMode {
    IRequestMode iRequestMode;


    @Override
    public Flowable<ResponseBody> QueryOrder(@Field("th") String th) {
        return iRequestMode.QueryOrder(th);
    }

    @Override
    public Flowable<ResponseBody> QueryDiscountRate(@Field("th") String th, @Field("zkl") String zkl) {
        return iRequestMode.QueryDiscountRate(th, zkl);
    }

    @Override
    public Flowable<ResponseBody> QueryDiscountMoney(String th, String lxh, String dzj) {
        return iRequestMode.QueryDiscountMoney(th, lxh, dzj);
    }


    @Override
    public Flowable<ResponseBody> QuerySynchronizePay(@Field("th") String th) {
        return iRequestMode.QuerySynchronizePay(th);
    }


}
