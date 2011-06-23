package com.rancidbacon.BackgroundUsbAccessory;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.SystemClock;
import android.util.Log;

public class BackgroundUsbService extends IntentService {

    private String TAG = "BackgroundUsbService";

	private static final int NOTIFICATION_ID = 1;    
    
	private boolean accessoryDetached = false;
	
	// We use this to catch the USB accessory detached message
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        final String TAG = "mUsbReceiver";

	        Log.d(TAG,"onReceive entered");
	        
	        String action = intent.getAction(); 

	        if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
	        	UsbAccessory accessory = UsbManager.getAccessory(intent);

		        Log.d(TAG,"Accessory detached");	        	
	        	
	        	// TODO: Check it's us here?
		        
		        accessoryDetached = true;
	        	
	        	unregisterReceiver(mUsbReceiver);
	        	
	            if (accessory != null) {
	                // TODO: call method to clean up and close communication with the accessory?
	            }
	        }
	        
	        Log.d(TAG,"onReceive exited");
	    }
	};
	
	
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

		startForeground(NOTIFICATION_ID, getNotification());

		// Register to receive detached messages
		IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
		registerReceiver(mUsbReceiver, filter);

		while(true) {
			// Wait until the accessory detachment is flagged
			if (accessoryDetached) {
				break;
			}

			// In reality we'd do stuff here.
			
			SystemClock.sleep(10);
		}
		
		stopForeground(true);
		stopSelf();
		
		Log.d(TAG, "onHandleIntent exited");		
	}

}
