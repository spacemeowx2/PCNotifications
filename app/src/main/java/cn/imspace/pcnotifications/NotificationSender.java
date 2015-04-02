package cn.imspace.pcnotifications;

/**
 * Created by space on 15/2/25.
 */

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.client.methods.HttpPost;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.util.EntityUtils;
import org.json.*;

public class NotificationSender {
    private NotificationFetcher mNotification;
    private String mCode, mKey, mMethod, mDID, mName;
    private URI mURI;
    private int mRetry=0;
    private static final String TAG = "Sender";
    class Sender extends Thread {
        private String getString() {
            JSONObject tObj = new JSONObject();
            JSONObject msg = new JSONObject();
            try {
                //TODO: 可变ROLE
                tObj.put("role", "phone");
                tObj.put("code", mCode);
                tObj.put("key", mKey);
                tObj.put("did", mDID);
                tObj.put("name", mName);
                tObj.put("cmd", "broadcast");
                tObj.put("dest", "[all]");
                msg.put("notification", mNotification.getJSONObject());
                if (mMethod!=null && !mMethod.equals("")) {
                    msg.put("method", mMethod);
                }
                //TODO ENCODE
                tObj.put("msg", msg.toString());
            } catch(Exception e) {
                e.printStackTrace();
            }
            return tObj.toString();
        }
        public void run () {
            String responseText;
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("");
            HttpPost requestPost = new HttpPost(mURI);
            HttpEntity requestEntity = new ByteArrayEntity(getString().getBytes());
            requestPost.setEntity(requestEntity);
            boolean reSend = false;
            try {
                HttpResponse response = httpClient.execute(requestPost);
                responseText = EntityUtils.toString(response.getEntity(), "utf-8");
                JSONObject ret = new JSONObject(responseText);
                if (ret.get("ok") != true) {
                    throw new Exception("Server say it's not ok. ");
                }
            } catch (Exception e) {
                reSend = true;
                Log.i(TAG, "Send Error.");
                e.printStackTrace();
            } finally {
                httpClient.close();
            }
            if (reSend && (mRetry++ < 6)) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                run();
            }
        }
    }
    public NotificationSender (Notification notification, ConnectionConfig cc) throws URISyntaxException{
        mNotification = new NotificationFetcher(notification);
        mURI = cc.getServer();
        mCode = cc.getCode();
        mKey = cc.getKey();
        mDID = cc.getDID();
        mName = cc.getName();
        mMethod = "Posted";
    }
    @TargetApi(18)
    public NotificationSender (String method, StatusBarNotification sbn, ConnectionConfig cc) {
        Notification notification = sbn.getNotification();
        mNotification = new NotificationFetcher(notification, getId(sbn));
        mURI = cc.getServer();
        mCode = cc.getCode();
        mKey = cc.getKey();
        mDID = cc.getDID();
        mName = cc.getName();
        mMethod = method;
        mNotification.setPackage(sbn.getPackageName());
        mNotification.setPostTime(sbn.getPostTime());
    }
    public void send() throws Exception {
        Sender sender = new Sender();
        sender.start();
    }
    @TargetApi(18)
    private String getId(StatusBarNotification sbn) {
        String tag = sbn.getTag();
        if (tag==null) {
            tag = "{[null]}";
        }
        return sbn.getPackageName()+";;"+tag+";;"+sbn.getId();
    }
}