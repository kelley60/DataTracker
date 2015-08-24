package com.example.sean.datatracker;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private final static String PREFS_NAME = "MyPrefsFile";
    private final static String TAG = MainActivity.class.getSimpleName();

    //interval in which data is submitted to server in hours
    private static final int DATA_SUBMISSION_INTERVAL = 3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checks to see if it is first time user is opening app

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (settings.getBoolean("my_first_time", true)) {
            //the app is being launched for first time, do something
            //start first launch activity
            Intent userSignUpIntent = new Intent(this, SignUpActivity.class);
            userSignUpIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            userSignUpIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(userSignUpIntent);
        }
        else {
            //initializes buttons and textfields
           // mSendButton = (Button) findViewById(R.id.sendDataButton);
            setTimer();
            /*
            mSendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendData();
                }
            });
            */
        }
    }

    public void onResume(){
        super.onResume();
        setTimer();
    }

    public void onDestroy() {
        super.onDestroy();
    }


    private void setTimer() {

      AlarmManager alarmManager;
      BroadcastReceiver broadcastReceiver;
      PendingIntent pendingIntent;

       broadcastReceiver = new BroadcastReceiver() {
           @Override
           public void onReceive(Context context, Intent intent) {
               WakeLocker.acquire(context);
               sendData();
               WakeLocker.release();
           }
       };

        Calendar cal = Calendar.getInstance();

        //starts at midnight
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);

        //repeats every 3 hours
       int interval = 1000 * 60 * 60 * 3;

       registerReceiver(broadcastReceiver, new IntentFilter("com.example.sean.datatracker.MainActivity"));
       pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.example.sean.datatracker.MainActivity"), 0);
       alarmManager = (AlarmManager) (this.getSystemService(Context.ALARM_SERVICE));
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, pendingIntent);
    }

    public void sendData() {
        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        ParseUser user = ParseUser.getCurrentUser();

        SharedPreferences.Editor editor = sp.edit();
        long localMobileRCount = sp.getLong("local_mobile_received", TrafficStats.getMobileRxBytes());
        long localMobileTCount = sp.getLong("local_mobile_transmitted", TrafficStats.getMobileTxBytes());
        long  localWifiRCount = sp.getLong("local_wifi_received", TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes());
        long localWifiTCount = sp.getLong("local_wifi_transmitted", TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes());

        //mobile
        int tempMR = (int)(TrafficStats.getMobileRxBytes() - localMobileRCount);
        int tempMT = (int)(TrafficStats.getMobileTxBytes() - localMobileTCount);
        //wifi
        int tempWR = ((int)(TrafficStats.getTotalRxBytes() - localWifiRCount)) - tempMR;
        int tempWT = ((int)(TrafficStats.getTotalTxBytes() - localWifiTCount)) - tempMT;

        user.add("Mobile_Received", tempMR);
        user.add("Mobile_Transmitted", tempMT);
        user.add("WiFi_Received", tempWR);
        user.add("WiFi_Transmited", tempWT);

        editor.putLong("local_mobile_received", TrafficStats.getMobileRxBytes());
        editor.putLong("local_mobile_transmitted", TrafficStats.getMobileTxBytes());
        editor.putLong("local_wifi_received", TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes());
        editor.putLong("local_wifi_transmitted", TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes());
        editor.commit();


        user.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null){
                    Toast.makeText(MainActivity.this, "Data Submitted to backend.", Toast.LENGTH_LONG).show();
                }
                else{
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage(e.getMessage());
                    builder.setTitle("Oops!");
                    builder.setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        Intent intent;

        if (id == R.id.personal_info) {
            intent = new Intent(this, SignUpActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

}
