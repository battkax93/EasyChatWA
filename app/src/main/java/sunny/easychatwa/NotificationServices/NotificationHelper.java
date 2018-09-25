package sunny.easychatwa.NotificationServices;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.RequiresApi;

import sunny.easychatwa.MainActivity;
import sunny.easychatwa.R;
import sunny.easychatwa.util.util;

import static sunny.easychatwa.BuildConfig.*;

/**
 * Created by Wayan-MECS on 7/2/2018.
 */

public class NotificationHelper extends ContextWrapper {
    private NotificationManager mManager;
    public static final String ANDROID_CHANNEL_ID = "id.co.adira.ad1mobileakses.ANDROID";
    public static final String ANDROID_CHANNEL_NAME = "ANDROID CHANNEL";

    @RequiresApi(api = Build.VERSION_CODES.O)
    public NotificationHelper(Context base) {
        super(base);
        createChannels();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void createChannels() {
        //create notification channel
        NotificationChannel androidChannel = new NotificationChannel(getPackageName(), ANDROID_CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        // Sets whether notifications posted to this channel should display notification lights
        androidChannel.enableLights(true);
        // Sets whether notification posted to this channel should vibrate.
        androidChannel.enableVibration(true);
        // Sets the notification light color for notifications posted to this channel
        androidChannel.setLightColor(Color.YELLOW);
        // Sets whether notifications posted to this channel appear on the lockscreen or not
        androidChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        getManager().createNotificationChannel(androidChannel);
    }

    public NotificationManager getManager() {
        if (mManager == null) {
            mManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return mManager;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getAndroidChannelNotificationON (String title, String body) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

        util.printMe("cek pkg " + getPackageName());

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,notificationIntent, 0);
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ew_icon2);

        return new Notification.Builder(getApplicationContext(), getPackageName())
                .setSmallIcon(R.drawable.ic_enable)
                .setLargeIcon(logo)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(contentIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public Notification.Builder getAndroidChannelNotificationOFF (String title, String body) {
        Intent intent = new Intent();
        intent.setAction(android.content.Intent.ACTION_VIEW);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                notificationIntent, 0);
        Bitmap logo = BitmapFactory.decodeResource(getResources(), R.drawable.ew_icon2);

        return new Notification.Builder(getApplicationContext(), getPackageName())
                .setSmallIcon(R.drawable.ic_disable)
                .setLargeIcon(logo)
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(contentIntent);
    }
}

