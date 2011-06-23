package com.rancidbacon.BackgroundUsbAccessory;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

public class BackgroundUsbService extends IntentService {

    private String TAG = "BackgroundUsbService";

	public BackgroundUsbService() {
		super("BackgroundUsbService");
	}

	@Override
	protected void onHandleIntent(Intent arg0) {

		Log.d(TAG, "onHandleIntent entered");

		Log.d(TAG, "onHandleIntent exited");		
	}

}
