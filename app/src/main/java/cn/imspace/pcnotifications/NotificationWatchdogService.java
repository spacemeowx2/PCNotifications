/**
 * Created by space on 15/2/24.
 */
package cn.imspace.pcnotifications;

import android.accessibilityservice.*;
import android.app.Notification;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.view.accessibility.AccessibilityEvent;
import android.util.Log;

public class NotificationWatchdogService extends AccessibilityService {
    private static final int EVENT_NOTIFICATION_TIMEOUT_MILLIS = 80;
    private static final String[] PACKAGE_NAMES = new String[] {};
    private static final String TAG = "NotificationWatchdog";
    private ConnectionConfig mcc;
    private CommandReceiver mcr;
    public void onCreate() {
        mcc = new ConnectionConfig(this);
        mcr = new CommandReceiver(this);
        mcr.start();
    }
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event.getEventType()==AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED) {
            Log.i(TAG, "onAccessibilityEvent");
            Log.i(TAG, (String) event.getPackageName());
            for (CharSequence subText: event.getText()) {
                Log.i(TAG, (String) subText);
            }
            Parcelable data = event.getParcelableData();
            if (data instanceof Notification) {
                Notification notification = (Notification) data;
                //SharedPreferences sp = getSharedPreferences("connection", MODE_PRIVATE);
                try {
                    NotificationSender ns = new NotificationSender(notification, mcc);
                    ns.send();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public void onInterrupt() {
        Log.i(TAG, "onInterrupt");
    }
    public void onServiceConnected() {
        Log.i(TAG, "onServiceConnected");
        setServiceInfo();
    }
    private void setServiceInfo() {
        Log.i(TAG, "setServiceInfo");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.notificationTimeout = EVENT_NOTIFICATION_TIMEOUT_MILLIS;
        info.packageNames = PACKAGE_NAMES;
        setServiceInfo(info);
    }
}
