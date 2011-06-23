package com.rancidbacon.BackgroundUsbAccessory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class StartServiceActivity extends Activity {
    /** Called when the activity is first created. */
    
    private String TAG = "StartServiceActivity";
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate entered");

        Intent intent = new Intent(this, BackgroundUsbService.class);
        startService(intent);
		
        finish();
		
        Log.d(TAG, "onCreate exited");
    }
}