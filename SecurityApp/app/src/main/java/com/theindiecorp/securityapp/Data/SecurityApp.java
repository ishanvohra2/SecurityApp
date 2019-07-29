package com.theindiecorp.securityapp.Data;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

public class SecurityApp extends Application {

    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";

    @Override
    public void onCreate() {
        super.onCreate();

        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            NotificationChannel channel1 = new NotificationChannel(
                    CHANNEL_1_ID,
                    "HELP ALERT",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.enableLights(true);
            channel1.setDescription("This channel is for sending help alerts by other users.");


            NotificationChannel channel2 = new NotificationChannel(
                    CHANNEL_2_ID,
                    "HELP ASSIGNED",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel2.enableLights(true);
            channel2.setDescription("This channel is for sending help assigned to the user.");

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel1);
            manager.createNotificationChannel(channel2);
        }
    }
}
