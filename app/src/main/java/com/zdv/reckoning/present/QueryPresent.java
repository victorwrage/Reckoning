package com.zdv.reckoning.present;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;

import com.jakewharton.retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import com.zdv.reckoning.model.IRequestMode;
import com.zdv.reckoning.model.converter.CustomGsonConverter;
import com.zdv.reckoning.utils.Constant;
import com.zdv.reckoning.view.IPayView;
import com.zdv.reckoning.view.IView;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.concurrent.TimeUnit;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Administrator on 2017/4/6.
 */
public class QueryPresent implements IRequestPresent {
    private IView iView;
    private Context context;
    private IRequestMode iRequestMode;
    OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
            .build();
    private static QueryPresent instance = null;

    public void setView(Activity activity) {
        iView = (IView) activity;
    }

    public void setView(Fragment fragment) {
        iView = (IView) fragment;
    }

    private QueryPresent(Context context_) {
        context = context_;
    }

    public static QueryPresent getInstance(Context context) {
        if (instance == null) {
            synchronized (QueryPresent.class) {
                if (instance == null) {
                    return new QueryPresent(context);
                }
            }
        }
        return instance;
    }

    public void initRetrofit(String url, boolean isXml) {

        try {
            if (isXml) {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(url)
                        .client(client)
                        // .addConverterFactory(Xm.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build();
                iRequestMode = retrofit.create(IRequestMode.class);
            } else {
                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(url)
                        .addConverterFactory(GsonConverterFactory.create())
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .build();
                iRequestMode = retrofit.create(IRequestMode.class);
            }

        } catch (IllegalArgumentException e) {
            e.fillInStackTrace();
        }
    }

    public void initRetrofitSendMessage(String url) {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .client(genericClient())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
            iRequestMode = retrofit.create(IRequestMode.class);


        } catch (IllegalArgumentException e) {
            e.fillInStackTrace();
        }
    }

    /**
     * 添加统一header,超时时间,http日志打印
     *
     * @return
     */
    public static OkHttpClient genericClient() {
//        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
//        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        Request.Builder requestBuilder = request.newBuilder();
                        request = requestBuilder.post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded;charset=GBK"),
                                URLDecoder.decode(bodyToString(request.body()), "UTF-8")))
                                .build();
                        return chain.proceed(request);
                    }
                })
                //  .addInterceptor(logging)
                .connectTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .writeTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .readTimeout(Constant.DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .build();
        return httpClient;
    }

    private static String bodyToString(final RequestBody request) {
        try {
            final RequestBody copy = request;
            final Buffer buffer = new Buffer();
            if (copy != null)
                copy.writeTo(buffer);
            else
                return "";
            return buffer.readUtf8();
        } catch (final IOException e) {
            return "did not work";
        }
    }

    public void initRetrofit2(String url, boolean isXml) {
        try {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(url)
                    .addConverterFactory(CustomGsonConverter.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .client(new OkHttpClient.Builder()
                            .addNetworkInterceptor(
                                    new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                            .addNetworkInterceptor(
                                    new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BASIC))
                            .addNetworkInterceptor(
                                    new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)).build())
                    .build();
            iRequestMode = retrofit.create(IRequestMode.class);

        } catch (IllegalArgumentException e) {
            e.fillInStackTrace();
        }
    }

    @Override
    public void QueryOrder(String th) {
        iRequestMode.QueryOrder(th)
                .onErrorReturn(s -> new ResponseBody() {
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public long contentLength() {
                        return 0;
                    }

                    @Override
                    public BufferedSource source() {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> ((IPayView) iView).ResolveOrderInfo(s));
    }

    @Override
    public void QueryDiscountRate(String th, String zkl) {
        iRequestMode.QueryDiscountRate(th,zkl)
                .onErrorReturn(s -> new ResponseBody() {
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public long contentLength() {
                        return 0;
                    }

                    @Override
                    public BufferedSource source() {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> ((IPayView) iView).ResolveDiscountRateInfo(s));
    }

    @Override
    public void QueryDiscountMoney(String th, String zk) {
        iRequestMode.QueryDiscountMoney(th,zk)
                .onErrorReturn(s -> new ResponseBody() {
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public long contentLength() {
                        return 0;
                    }

                    @Override
                    public BufferedSource source() {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> ((IPayView) iView).ResolveDiscountMoneyInfo(s));
    }

    @Override
    public void QuerySynchronizePay(String th) {
        iRequestMode.QuerySynchronizePay(th)
                .onErrorReturn(s -> new ResponseBody() {
                    @Override
                    public MediaType contentType() {
                        return null;
                    }

                    @Override
                    public long contentLength() {
                        return 0;
                    }

                    @Override
                    public BufferedSource source() {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(s -> ((IPayView) iView).ResolveSynchronizePayInfo(s));
    }



}
