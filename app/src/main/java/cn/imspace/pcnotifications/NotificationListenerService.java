package cn.imspace.pcnotifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.SharedPreferences;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;

/**
 * Created by space on 15/2/25.
 */
@TargetApi(18)
public class NotificationListenerService extends android.service.notification.NotificationListenerService {
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if ((notification.flags & NotificationCompat.FLAG_ONGOING_EVENT) == 0) {
            SharedPreferences sp = getSharedPreferences("connection", MODE_PRIVATE);
            try {
                NotificationSender ns = new NotificationSender("Posted", sbn, sp);
                ns.send();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Posted:");
        }
    }
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if ((notification.flags & NotificationCompat.FLAG_ONGOING_EVENT) == 0) {
            SharedPreferences sp = getSharedPreferences("connection", MODE_PRIVATE);
            try {
                NotificationSender ns = new NotificationSender("Removed", sbn, sp);
                ns.send();
            } catch (Exception e) {
                e.printStackTrace();
            }
            System.out.println("Removed:");
        }
    }
}
