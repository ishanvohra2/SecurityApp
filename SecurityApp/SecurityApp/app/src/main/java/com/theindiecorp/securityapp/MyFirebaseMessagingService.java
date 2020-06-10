package com.bits.securityapp;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MyFirebaseMsgService";
    private static final int BROADCAST_NOTIFICATION_ID = 1;

    @Override
    public void onDeletedMessages() {
        super.onDeletedMessages();
    }

    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {

        Log.e("msg", "received");

        Log.e(TAG, "onMessageReceived: new incoming message.");
        String type = remoteMessage.getData().get("data_type");
        String title = remoteMessage.getData().get("data_title");
        String shortMessage = remoteMessage.getData().get("data_message_short");
        String messageId = remoteMessage.getData().get("data_message_id");

        //The user id of the user who pressed the SOS button. Use this to query user location or any other thing
        String userId = remoteMessage.getData().get("data_user_id");

        sendMessageNotification(type, title, shortMessage, messageId);

        Log.e("title", title);
        Log.e("userId", userId);

    }

    /**
     * Build a push notification for a chat message
     */
    private void sendMessageNotification(String type, String title, String shortMessage, String messageId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.e(TAG, "sendChatmessageNotification: building a chatmessage notification");

            CharSequence name = "default";
            String description = "Default Channel";
            String channelId = "default_id";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            if(type.equals("sos")){
                name = "SOS";
                description = "Notification sent when a user near you is unsafe";
                channelId = "sos";
            }
            else if(type.equals("safe")){
                name = "Safe";
                description = "Notification sent when an unsafe user has marks himself as safe";
                channelId = "safe";
            }
            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            // Instantiate a Builder object.
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId);
            // Creates an Intent for the Activity
            Intent pendingIntent = new Intent(this, HomeActivity.class);
            // Sets the Activity to start in a new, empty task
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            // Creates the PendingIntent
            PendingIntent notifyPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            pendingIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

            //add properties to the builder
            builder.setSmallIcon(R.mipmap.ic_launcher_round)
                    .setContentTitle(title)
                    .setColor(getColor(R.color.colorPrimaryDark))
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT);

            if(shortMessage != null && !TextUtils.isEmpty(shortMessage)){
                builder.setStyle(new NotificationCompat.BigTextStyle().bigText(shortMessage));
            }

            builder.setContentIntent(notifyPendingIntent);
            NotificationManager mNotificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.createNotificationChannel(channel);

            mNotificationManager.notify(Integer.valueOf(messageId), builder.build());

        }
    }
}
