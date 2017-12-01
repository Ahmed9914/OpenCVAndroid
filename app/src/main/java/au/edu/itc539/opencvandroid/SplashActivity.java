package au.edu.itc539.opencvandroid;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by duane on 29/11/2017.
 */

public class SplashActivity extends AppCompatActivity {

    private static final String TAG = "CameraActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "Called onCreate of SplashActivity...");

        super.onCreate(savedInstanceState);

        Intent intent = new Intent(this, ProgramActivity.class);
        startActivity(intent);
        finish();
    }


}
