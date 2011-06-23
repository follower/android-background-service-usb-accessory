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
		
        // See:
        //
        //    <http://permalink.gmane.org/gmane.comp.handhelds.android.devel/154481> &
        //    <http://stackoverflow.com/questions/5567312/android-how-to-execute-main-fucntionality-of-project-only-by-clicking-on-the-ic/5567514#5567514>
        //
        // for combination of `Theme.NoDisplay` and `finish()` in `onCreate()`/`onResume()`.
        //
        finish();
		
        Log.d(TAG, "onCreate exited");
    }
}