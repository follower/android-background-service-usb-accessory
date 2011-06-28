package com.rancidbacon.BackgroundUsbAccessory;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.util.Log;

public class BackgroundUsbService extends IntentService {

    private String TAG = "BackgroundUsbService";

	private static final int NOTIFICATION_ID = 1;    
    
	private boolean accessoryDetached = false;

	private ParcelFileDescriptor mFileDescriptor;
	private FileInputStream mInputStream;
	private FileOutputStream mOutputStream;	
	
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

	Notification createNotification(String accessoryDescription) {

		Notification notification = new Notification(android.R.drawable.ic_menu_info_details,
				"Accessory connected", System.currentTimeMillis());
		
		Context context = getApplicationContext();
		CharSequence contentTitle = "USB Accessory connected";
		CharSequence contentText = accessoryDescription + " connected";

		// This can be changed if we want to launch an activity when notification clicked
		Intent notificationIntent = new Intent();
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(context, contentTitle, contentText, contentIntent);		
		
		return notification;
	}
	
	@Override
	protected void onHandleIntent(Intent theIntent) {
		
		Log.d(TAG, "onHandleIntent entered");		
		
		// The necessary extras should've been added by `fillIn()` call in Activity.
        UsbAccessory accessory = UsbManager.getAccessory(theIntent);
        
        if (accessory != null) {
			Log.d(TAG, "Got accessory: " + accessory.getModel());
			
			// TODO: Check this order is okay or do we risk getting killed?
			startForeground(NOTIFICATION_ID, createNotification(accessory.getDescription()));

			// Register to receive detached messages
			IntentFilter filter = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
			registerReceiver(mUsbReceiver, filter);

		    mFileDescriptor = UsbManager.getInstance(this).openAccessory(accessory);
		    if (mFileDescriptor != null) {
		        FileDescriptor fd = mFileDescriptor.getFileDescriptor();
		        mInputStream = new FileInputStream(fd);
		        mOutputStream = new FileOutputStream(fd);
		    }
			
			while(true) {
				// Wait until the accessory detachment is flagged
				if (accessoryDetached) {
					break;
				}

				// In reality we'd do stuff here.
				
				SystemClock.sleep(10);
			}		

			// Without this clean-up code the app will work once but then
			// won't start again until it's force-quit.
			try {
				if (mFileDescriptor != null) {
					mFileDescriptor.close();
				}
			} catch (IOException e) {
			} finally {
				mFileDescriptor = null;
				accessory = null;
			}			
			
			stopForeground(true);
			
		} else {
			Log.d(TAG, "No accessory found.");
		}
		
		stopSelf();
		
		Log.d(TAG, "onHandleIntent exited");		
	}

}
