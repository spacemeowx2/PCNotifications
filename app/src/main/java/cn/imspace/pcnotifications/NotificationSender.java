package cn.imspace.pcnotifications;

/**
 * Created by space on 15/2/25.
 */

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.SharedPreferences;
import android.net.http.AndroidHttpClient;
import android.service.notification.StatusBarNotification;

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
    private String mServer, mCode, mKey, mMethod;
    private URI mURI;
    private int mRetry=0;
    class Sender extends Thread {
        private String getString() {
            JSONObject tObj = new JSONObject();
            JSONObject msg = new JSONObject();
            try {
                tObj.put("role", "phone");
                tObj.put("code", mCode);
                tObj.put("key", mKey);
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
                System.out.println("Send Error.");
                System.out.println();
                e.printStackTrace();
            } finally {
                httpClient.close();
            }
            if (reSend && (mRetry++ < 5)) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Sender sender = new Sender();
                sender.start();
            }
        }
    }
    public NotificationSender (Notification notification, SharedPreferences spReader) throws URISyntaxException{
        mNotification = new NotificationFetcher(notification);
        mServer = spReader.getString("server", "http://203.195.196.109:8765/");
        mCode = spReader.getString("code", "debug");
        mKey = spReader.getString("key", "debug");
        mMethod = "Posted";
        mURI = new URI(mServer);
    }
    @TargetApi(18)
    public NotificationSender (String method, StatusBarNotification sbn, SharedPreferences spReader) throws URISyntaxException{
        Notification notification = sbn.getNotification();
        mNotification = new NotificationFetcher(notification, sbn.getId());
        mServer = spReader.getString("server", "http://203.195.196.109:8765/");
        mCode = spReader.getString("code", "debug");
        mKey = spReader.getString("key", "debug");
        mMethod = method;
        mNotification.setPackage(sbn.getPackageName());
        mNotification.setPostTime(sbn.getPostTime());
        mURI = new URI(mServer);
    }
    public void send() throws Exception {
        if (mServer.isEmpty() || mCode.isEmpty() || mKey.isEmpty()) {
            throw new Exception("a");
        }
        Sender sender = new Sender();
        sender.start();
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