package com.example.sean.datatracker;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseAnalytics;

/**
 * Created by Sean on 8/4/2015.
 */
public class DataTrackerApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //initializes Parse info
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "d6YVgjwzKHAgiNRjhRhGxrsgHLuxl8RMk9UQHqsd", "2YLaPvkYszww0lz6YSRW91CLwFziRr2y3b4MQomy");
    }
}
