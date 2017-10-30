package com.zdv.reckoning.view;


import okhttp3.ResponseBody;

/**
 * Info:
 * Created by xiaoyl
 * 创建时间:2017/4/7 9:49
 */

public interface IUserView extends IView{

    void ResolveLoginInfo(ResponseBody info);

    void ResolveRegisterInfo(ResponseBody info);
    void ResolveForgetInfo(ResponseBody info);
    void ResolveCodeInfo(ResponseBody info);


}
