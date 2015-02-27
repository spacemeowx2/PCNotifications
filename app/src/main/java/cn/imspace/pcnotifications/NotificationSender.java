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
    private String mCode, mKey, mMethod, mDID;
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
                tObj.put("cmd", "broadcast");
                tObj.put("dest", "[all]");
                msg.put("notification", mNotification.getJSONObject());
                if (mMethod!=null && !mMethod.isEmpty()) {
                    msg.put("method", mMethod);
                }
                tObj.put("msg", msg);
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
        mMethod = method;
        mNotification.setPackage(sbn.getPackageName());
        mNotification.setPostTime(sbn.getPostTime());
    }
    public void send() throws Exception {
        if (mCode.isEmpty() || mKey.isEmpty()) {
            throw new Exception("a");
        }
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

//import java.security.Key;
//import javax.crypto.Cipher;
//import javax.crypto.spec.SecretKeySpec;
//    private static final String AESTYPE ="AES/ECB/PKCS5Padding";
//    private static String getBase64(byte[] s) {
//        return (new sun.misc.BASE64Encoder()).encode(s);
//    }
//    private static byte[] getFromBase64(String s) {
//        if (s == null) return null;
//        try {
//            return (new sun.misc.BASE64Decoder()).decodeBuffer(s);
//        } catch (Exception e) {
//            return null;
//        }
//    }
//    public static String AES_Encrypt(String keyStr, String plainText) {
//        byte[] encrypt = null;
//        try{
//            Key key = generateKey(keyStr);
//            Cipher cipher = Cipher.getInstance(AESTYPE);
//            cipher.init(Cipher.ENCRYPT_MODE, key);
//            encrypt = cipher.doFinal(plainText.getBytes());
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return getBase64(encrypt);
//    }
//    public static String AES_Decrypt(String keyStr, String encryptData) {
//        byte[] decrypt = null;
//        try{
//            Key key = generateKey(keyStr);
//            Cipher cipher = Cipher.getInstance(AESTYPE);
//            cipher.init(Cipher.DECRYPT_MODE, key);
//            decrypt = cipher.doFinal(getFromBase64(encryptData));
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return new String(decrypt).trim();
//    }
//    private static Key generateKey(String key)throws Exception{
//        try{
//            SecretKeySpec keySpec = new SecretKeySpec(key.getBytes(), "AES");
//            return keySpec;
//        }catch(Exception e){
//            e.printStackTrace();
//            throw e;
//        }
//    }