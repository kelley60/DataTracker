package com.example.sean.datatracker;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.os.Handler;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.Parse;
import com.parse.ParseAnalytics;

import java.util.HashMap;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

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

            mHandler.postDelayed(mRunnable, 3000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initializes Parse info
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "d6YVgjwzKHAgiNRjhRhGxrsgHLuxl8RMk9UQHqsd", "2YLaPvkYszww0lz6YSRW91CLwFziRr2y3b4MQomy");
        ParseAnalytics.trackAppOpenedInBackground(getIntent());

        //initializes buttons and textfields
        setWidgets();
        //Since initially launching the app, this measures bytes since boot
        meausreBytesSinceDeviceBoot();

        displayBytes();

        Map<String, String> dimensions = new HashMap<String, String>();
// What type of news is this?
        dimensions.put("category", "politics");
// Is it a weekday or the weekend?
        dimensions.put("dayType", "weekday");
// Send the dimensions to Parse along with the 'read' event
        ParseAnalytics.trackEventInBackground("read", dimensions);


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


}
