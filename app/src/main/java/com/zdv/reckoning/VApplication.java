
package com.zdv.reckoning;

import android.app.Activity;
import android.support.multidex.MultiDexApplication;

import java.util.LinkedList;
import java.util.List;

/** 
 * @ClassName VApplication 
 * @Description TODO  Application基类
 * @Version 1.0
 * @Creation 2013-8-10 上午10:09:20 
 * @Mender xiaoyl
 * @Modification 2013-8-10 上午10:09:20 
 **/
public class VApplication extends MultiDexApplication {
	protected static VApplication instance;
	private List<Activity> activityList = new LinkedList<Activity>();
	private String myState;
	public static boolean isExit = false;
	
	public VApplication() {

	}



	@Override
	public void onCreate() {
		super.onCreate();

	}
	
	public String getState() {
		return myState;
	}

	public void setState(String s) {
		myState = s;
	}
	
	/**
	 * 
	 * @param activity
	 */
	public void addActivitys(Activity activity) {
		activityList.add(activity);
	}

	/**
	 * 退出应用程序
	 */
	public void exitApplication() {
		isExit = true;
		for (Activity a : activityList) {
			a.finish();
		}
		System.exit(0);
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
	}
}
