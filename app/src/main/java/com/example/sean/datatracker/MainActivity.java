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

    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private Button networkButton;
    private Button mResetButton;

    private Handler mHandler = new Handler();
    private TextView mobileReceivedTextView;
    private TextView mobileTransmittedTextView;
    private TextView wifiReceivedTextView;
    private TextView wifiTransmittedTextView;

    //These are the values since the device was booted last
    private long mMobileBytesReceived;
    private long mMobileBytesTransmitted;
    private long mWifiBytesReceived;
    private long mWifiBytesTransmitted;

    //These are the values since the app was first launched, or the reset button was pressed
    private long mobileRXBytesSinceReset;
    private long mobileTXBytesSinceReset;
    private long wifiRXBytesSinceReset;
    private long wifiTXBytesSinceReset;

    //values to be stored on phone and eventually transmitted to database
    private long localMobileRCount;
    private long localMobileTCount;
    private long localWifiRCount;
    private long localWifiTCount;

    ArrayList<String> networkSwitchedTimeStamp = new ArrayList<String>();

    //interval in which data is submitted to server in hours
    private static final int DATA_SUBMISSION_INTERVAL = 3;
    private AlarmManager alarmManager;
    private BroadcastReceiver broadcastReceiver;
    private PendingIntent pendingIntent;

    private final Runnable mRunnable = new Runnable() {
        public void run() {

            long mobileRX = TrafficStats.getMobileRxBytes();
            long mobileTX = TrafficStats.getMobileTxBytes();

            mobileRXBytesSinceReset = mobileRX - mMobileBytesReceived;
            mobileTXBytesSinceReset = mobileTX - mMobileBytesTransmitted;
            wifiRXBytesSinceReset = TrafficStats.getTotalRxBytes() - mobileRX - mWifiBytesReceived;
            wifiTXBytesSinceReset = TrafficStats.getTotalTxBytes() - mobileTX - mWifiBytesTransmitted;

            mobileReceivedTextView.setText("Received Bytes: " + Long.toString(mobileRXBytesSinceReset));
            mobileTransmittedTextView.setText("Transmitted Bytes: " + Long.toString(mobileTXBytesSinceReset));
            wifiReceivedTextView.setText("Received Bytes: " + Long.toString(wifiRXBytesSinceReset));
            wifiTransmittedTextView.setText("Transmitted Bytes: " + Long.toString(wifiTXBytesSinceReset));

            mHandler.postDelayed(mRunnable, 15000);
        }
    };

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
            setWidgets();

            setTimer();
            //Since initially launching the app, this measures bytes since boot
            meausreBytesSinceDeviceBoot();

            displayBytes();

            networkButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    detectNetwork();
                }
            });
            mResetButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    meausreBytesSinceDeviceBoot();
                }
            });
        }
    }


    private void setTimer() {

       broadcastReceiver = new BroadcastReceiver() {
           @Override
           public void onReceive(Context context, Intent intent) {
               sendData();
           }
       };

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());

        //starts at midnight
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);


        //repeats every 3 hours
       int interval = 1000 * 60 * 60 * 3;

       registerReceiver(broadcastReceiver, new IntentFilter("com.example.sean.datatracker.MainActivity"));
       pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent("com.example.sean.datatracker.MainActivity"), 0);

       alarmManager = (AlarmManager)(this.getSystemService(Context.ALARM_SERVICE));
       alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), interval, pendingIntent);

    }

    private void sendData() {
        ParseUser user = ParseUser.getCurrentUser();

        SharedPreferences sp = getSharedPreferences(PREFS_NAME, Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        localMobileRCount = sp.getLong("local_mobile_received", TrafficStats.getMobileRxBytes());
        localMobileTCount = sp.getLong("local_mobile_transmitted", TrafficStats.getMobileTxBytes());
        localWifiRCount = sp.getLong("local_wifi_received", TrafficStats.getTotalRxBytes() - TrafficStats.getMobileRxBytes());
        localWifiTCount = sp.getLong("local_wifi_transmitted", TrafficStats.getTotalTxBytes() - TrafficStats.getMobileTxBytes());

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

    private void setWidgets() {
        networkButton = (Button) findViewById(R.id.displayNetworkButton);
        mResetButton = (Button) findViewById(R.id.resetButton);
        mobileReceivedTextView = (TextView)findViewById(R.id.mobileReceived);
        mobileTransmittedTextView = (TextView)findViewById(R.id.mobileTransmitted);
        wifiReceivedTextView = (TextView) findViewById(R.id.wifiReceived);
        wifiTransmittedTextView = (TextView) findViewById(R.id.wifiTransmitted);
    }

    private void meausreBytesSinceDeviceBoot() {
        mMobileBytesReceived = TrafficStats.getMobileRxBytes();
        mMobileBytesTransmitted = TrafficStats.getMobileTxBytes();
        mWifiBytesReceived = TrafficStats.getTotalRxBytes() - mMobileBytesReceived;
        mWifiBytesTransmitted = TrafficStats.getTotalTxBytes() - mMobileBytesTransmitted;
    }

    private void detectNetwork() {
        if (isWifiNetwork()){
            Toast.makeText(this, "Connected to Wi-Fi.", Toast.LENGTH_LONG).show();
        }
        else if (isMobileNetwork()){
            Toast.makeText(this, "Connected to 3G/4G.", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "Not connected.", Toast.LENGTH_LONG).show();
        }
    }

    private boolean isMobileNetwork(){
        networkInfo = null;
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null){
            networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        }
        if (networkInfo.isConnected()){
            return true;
        }
        else{
            return false;
        }
    }

    private boolean isWifiNetwork(){
        networkInfo = null;
        connectivityManager = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null){
            networkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        }
        if (networkInfo.isConnected()){
            return true;
        }
        else{
            return false;
        }
    }

    private void displayBytes(){
        if (mMobileBytesReceived == TrafficStats.UNSUPPORTED || mMobileBytesReceived == TrafficStats.UNSUPPORTED) {
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Uh Oh!");
            alert.setMessage("Your device does not support traffic stat monitoring.");
            alert.show();
        } else {
            mHandler.postDelayed(mRunnable, 1000);
        }
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
