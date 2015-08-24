package com.example.sean.datatracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by Sean on 8/23/2015.
 */
public class MyAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        WakeLocker.acquire(context);
        MainActivity main = new MainActivity();
        main.sendData();
        WakeLocker.release();
    }
}