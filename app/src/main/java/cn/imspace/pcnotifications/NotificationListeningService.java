package cn.imspace.pcnotifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.SharedPreferences;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import org.json.JSONObject;

/**
 * Created by space on 15/2/25.
 */
@TargetApi(18)
public class NotificationListeningService extends NotificationListenerService {
    private ConnectionConfig mcc;
    private CommandReceiver mcr;
    private static final String TAG = "Listener";
    public void onCreate() {
        mcc = new ConnectionConfig(this);
        mcr = new CommandReceiver(this);
        mcr.start();
    }
    public void onDestroy() {
        mcr.stop();
    }
    public void onNotificationPosted(StatusBarNotification sbn) {
        //android.os.Debug.waitForDebugger();
        Notification notification = sbn.getNotification();
        if ((notification.flags & NotificationCompat.FLAG_ONGOING_EVENT) == 0) {
            try {
                NotificationSender ns = new NotificationSender("Posted", sbn, mcc);
                ns.send();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Posted:");
        }
    }
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();
        if ((notification.flags & NotificationCompat.FLAG_ONGOING_EVENT) == 0) {
            SharedPreferences sp = getSharedPreferences("connection", MODE_PRIVATE);
            try {
                NotificationSender ns = new NotificationSender("Removed", sbn, mcc);
                ns.send();
            } catch (Exception e) {
                e.printStackTrace();
            }
            Log.i(TAG, "Removed:");
        }
    }
}
