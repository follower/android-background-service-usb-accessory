package com.rancidbacon.BackgroundUsbAccessory;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BackgroundUsbService extends IntentService {

    private String TAG = "BackgroundUsbService";

	private static final int NOTIFICATION_ID = 1;    
    
	public BackgroundUsbService() {
		super("BackgroundUsbService");
	}

	Notification getNotification() {

		Notification notification = new Notification(android.R.drawable.ic_menu_info_details,
				"Accessory connected", System.currentTimeMillis());
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "USB Accessory connected";
		CharSequence contentText = "Background USB Demo Accessory connected";

		// This can be changed if we want to launch an activity when notification clicked
		Intent notificationIntent = new Intent();
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);		
		
		return notification;
	}
	
	@Override
	protected void onHandleIntent(Intent arg0) {

		Log.d(TAG, "onHandleIntent entered");

		Log.d(TAG, "onHandleIntent exited");		
	}

}
