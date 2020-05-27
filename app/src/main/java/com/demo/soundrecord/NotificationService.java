package com.demo.soundrecord;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;

public class NotificationService extends Service {
    private static final String TAG = "NotificationService";
    private NotificationManager notificationManager;
    //通知的唯一标识号。
    private int NOTIFICATION = R.string.notification_live_start;
    private String CHANNEL_ONE_ID ="soundRecord";


    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        showNotification();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void showNotification(){
        // PendingIntent如果用户选择此通知，则启动我们的活动
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,new Intent(this,NotificationService.class),0);

        //设置通知面板中显示的视图的信息。
        Notification.Builder notificationBuild =new Notification.Builder(this).setSmallIcon(R.mipmap.ic_launcher).setTicker("正在通话")
                .setContentTitle(getText(R.string.notification_live_start))
                .setContentTitle("正在运行")
                .setContentIntent(pendingIntent);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            //修改安卓8.1以上系统报错
            NotificationChannel notificationChannel = new NotificationChannel(CHANNEL_ONE_ID, "测试", NotificationManager.IMPORTANCE_MIN);
            notificationChannel.enableLights(false);//如果使用中的设备支持通知灯，则说明此通知通道是否应显示灯
            notificationChannel.setShowBadge(false);//是否显示角标
            notificationChannel.setLockscreenVisibility(Notification.VISIBILITY_SECRET);
            NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            manager.createNotificationChannel(notificationChannel);
            notificationBuild.setChannelId(CHANNEL_ONE_ID);
        }
        Log.i(TAG,"显示通知");
        Notification notification = notificationBuild.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
//        startForeground(1, notification);
        //发送通知
        notificationManager.notify(NOTIFICATION,notification);
        startForeground(R.string.notification_live_start,notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancel(NOTIFICATION);
    }
}
