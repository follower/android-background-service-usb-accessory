package com.rancidbacon.BackgroundUsbAccessory;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.android.future.usb.UsbAccessory;
import com.android.future.usb.UsbManager;

import android.app.IntentService;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.SystemClock;
import android.provider.ContactsContract.PhoneLookup;
import android.telephony.SmsMessage;
import android.text.format.Time;
import android.util.Log;

public class BackgroundUsbService extends IntentService {

    private String TAG = "BackgroundUsbService";

	private static final int NOTIFICATION_ID = 1;    
    
	private boolean accessoryDetached = false;

	private ParcelFileDescriptor mFileDescriptor;
	private FileInputStream mInputStream;
	private FileOutputStream mOutputStream;	
	
	public class DisplayText {
		private String text;
		private int x;
		private int y;
		
		public DisplayText(String text, int x, int y) {
			this.text = text;
			
			this.x = x;
			this.y = y;
		}

		public String getText() {
			return text;
		}

		public int getX() {
			return x;
		}

		public int getY() {
			return y;
		}
	}
	
	private LinkedBlockingQueue<DisplayText> actionQueue = new LinkedBlockingQueue<DisplayText>();
	
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

	void writeBytes(byte[]theStringBytes) {
		/*
		  
		   Writes the supplied byte array to the output stream as one byte per USB packet.
		   
		   In addition to that functionality this acts as a convenience function
		   that catches write errors.
		    
		 */
		if (mOutputStream == null) {
			return;
		}
		
		try {
			// We send one byte per packet because the Arduino sketch doesn't
			// currently handle more than one--mainly because I don't know how
			// to get the packet length and/or to read packet data in multiple
			// passes.
			for (int i = 0; i < theStringBytes.length; i++) {
				mOutputStream.write(theStringBytes[i]);
			}
		}  catch (IOException e) {
			// We can/should ignore the "no such device" error here if it means we've disconnected.
			Log.e(TAG, "write failed", e);
		}		
	}
	
	private Handler mHandler = new Handler();
	
	private Runnable mUpdateTimeTask = new Runnable() {
		   public void run() {
			   actionQueue.add(new DisplayText(new SimpleDateFormat("HH:mm:ss").format(new Date()), 0, 0));
			   
			   // TODO: Switch to taking account of start time with System.currentTimeMillis()
			   //       a la <http://developer.android.com/resources/articles/timed-ui-updates.html>.
		       mHandler.postAtTime(this, (int) ((SystemClock.uptimeMillis() / 1000) + 1) * 1000);
		   }
		};
	
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
			
		    mHandler.removeCallbacks(mUpdateTimeTask);
		    mHandler.postDelayed(mUpdateTimeTask, 100);
		    
	    	registerReceiver(receiver, new IntentFilter("android.provider.Telephony.SMS_RECEIVED"));
		    
		    DisplayText newAction = null;
		    
			while(true) {
				
				try {
					newAction = actionQueue.poll(1000, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) { 
		             // Restore the interrupted status
					 // See: <http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html>
		             Thread.currentThread().interrupt();
		        }
				
				// TODO: Allow us to be interrupted with this immediately?
				// Check if the accessory detachment was flagged
				if (accessoryDetached) {
					break;
				}

				// In reality we'd do stuff here.
				
				if (newAction != null) {
					// TODO: Make use of x,y args here.
					writeBytes(new byte[]{(byte) (newAction.getY()+1)});
					writeBytes(newAction.getText().getBytes());
				}
			}		

		    mHandler.removeCallbacks(mUpdateTimeTask);
		    
	    	unregisterReceiver(receiver);		    
		    
		    actionQueue.clear();

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

	
	private BroadcastReceiver receiver = new BroadcastReceiver () {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			
			Log.d("BackgroundSmsReceiver", "onReceive entered");

			for (Object thePdu : (Object []) arg1.getExtras().get("pdus") ) {
				SmsMessage theMessage = SmsMessage.createFromPdu((byte []) thePdu);
				
				Cursor managedCursor = getContentResolver().query(Uri.withAppendedPath(PhoneLookup.CONTENT_FILTER_URI, Uri.encode(theMessage.getOriginatingAddress())),
						new String[]{PhoneLookup.DISPLAY_NAME},
						null, null, null);
				
				String sender;
				
				if (managedCursor.moveToFirst()) {
					sender = managedCursor.getString(managedCursor.getColumnIndex(PhoneLookup.DISPLAY_NAME));
				} else {
					sender = theMessage.getOriginatingAddress(); 
				}
				
				Log.d("BackgroundSmsReceiver", "From: " + sender);
				
				actionQueue.add(new DisplayText((sender + ": " + theMessage.getMessageBody()).substring(0, 16), 0, 1));

				Log.d("BackgroundSmsReceiver", theMessage.getMessageBody());				
				
				break; // We only care about the first one.
			}
			
		}
	};
	
}
