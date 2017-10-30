package com.zdv.reckoning.model;


import io.reactivex.Flowable;
import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

/**
 * Created by xyl on 2017/4/6.
 */

public interface IRequestMode {




    @FormUrlEncoded
    @POST("zdvcy/cxmx")
    Flowable<ResponseBody> QueryOrder(@Field("th") String th);

    @FormUrlEncoded
    @POST("zdvcy/zkl ")
    Flowable<ResponseBody> QueryDiscountRate(@Field("th") String th, @Field("zkl") String zkl);

    @FormUrlEncoded
    @POST("zdvcy/zk ")
    Flowable<ResponseBody> QueryDiscountMoney(@Field("th") String th, @Field("zk") String zk);

    @FormUrlEncoded
    @POST("zdvcy/sgdy ")
    Flowable<ResponseBody> QuerySynchronizePay(@Field("th") String th);

   }
