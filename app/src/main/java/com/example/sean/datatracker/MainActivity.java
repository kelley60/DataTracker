package com.example.sean.datatracker;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private final static String TAG = MainActivity.class.getSimpleName();
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    private Button networkButton;

    private int wifiByteCount;
    private int mobileByteCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        networkButton = (Button) findViewById(R.id.displayNetworkButton);

        networkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detectNetwork();
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
