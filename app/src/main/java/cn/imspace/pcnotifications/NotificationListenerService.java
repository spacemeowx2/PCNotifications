package cn.imspace.pcnotifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.service.notification.StatusBarNotification;

/**
 * Created by space on 15/2/25.
 */
@TargetApi(18)
public class NotificationListenerService extends android.service.notification.NotificationListenerService {
    public void onNotificationPosted(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();

        ActionReader actionReader = new ActionReader(notification.contentView);
        System.out.println("Posted:");
        System.out.println(actionReader.getTitleText());
        System.out.println(actionReader.getContentText());
    }
    public void onNotificationRemoved(StatusBarNotification sbn) {

        Notification notification = sbn.getNotification();
        System.out.println("Removed:");
    }
}
