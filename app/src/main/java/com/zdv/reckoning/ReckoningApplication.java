package com.zdv.reckoning;



import com.zdv.reckoning.utils.Constant;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import cn.bmob.v3.Bmob;


/**
 * @ClassName:	NFCApplication 
 * @Description:TODO(Application) 
 * @author:	xiaoyl
 * @date:	2013-7-10 下午4:01:27 
 *  
 */
public class ReckoningApplication extends VApplication {
	private ReckoningApplication instance;

	@Override
	public void onCreate() {
		super.onCreate();
		instance = this;

		initCrash();

		Bmob.initialize(getInstance(), Constant.PUBLIC_BMOB_KEY);

	}



	private void initCrash() {
		CaocConfig.Builder.create()
				.backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) //default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
				.enabled(true) //default: true
				.showErrorDetails(true) //default: true
				.showRestartButton(true) //default: true
				.trackActivities(true) //default: false
				.minTimeBetweenCrashesMs(2000) //default: 3000
				.restartActivity(MainActivity.class) //default: null (your app's launch activity)
				.errorActivity(null) //default: null (default error activity)
				.eventListener(null) //default: null
				.apply();
	}

	public ReckoningApplication getInstance() {
		if (instance == null) {
			instance = this;
		}
		return instance;
	}



}
