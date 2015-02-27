package cn.imspace.pcnotifications;

import android.accessibilityservice.AccessibilityService;
import android.annotation.TargetApi;
import android.content.Context;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.service.notification.*;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URI;

/**
 * Created by space on 15/2/26.
 */
public  class CommandReceiver  {
    private static final String TAG = "CommandReceiver";
    private static final int MSG_OK = 0;
    private static final int MSG_FAILED = 1;
    private MyHandler mHandler;
    private ConnectionConfig mcc;
    private Context mContext;
    private int mConnection;
    private final Object mLock = new Object();
    private boolean mStop = false;
    private Thread getThread, monitorThread;
    class monitor implements Runnable {
        public void run(){
            while (true) {
                if (mStop) {
                    Log.i(TAG, "Monitor stopped because service stopped.");
                    return ;
                }
                Log.v(TAG, String.valueOf(mConnection));
//                synchronized (mLock) {
//                    if (mConnection==0) {
//                        System.out.println("Wake up.");
//                        get();
//                    }
//                }
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    class httpGetter implements Runnable{
        private URI mURI;
        private String mRequestText;
        //private int mRetry = 0;
        public httpGetter(URI server, String request) {
            mURI = server;
            mRequestText = request;
        }
        public void run(){
            if (mStop) {
                Log.i(TAG, "Getter stopped because service stopped.");
                return ;
            }
            synchronized (mLock) {
                mConnection++;
            }

            String responseText;
            AndroidHttpClient httpClient = AndroidHttpClient.newInstance("");
            httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,10000000);//连接时间
            httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,10000000);         //数据传输时间
            HttpPost requestPost = new HttpPost(mURI);
            HttpEntity requestEntity = new ByteArrayEntity(mRequestText.getBytes());
            requestPost.setEntity(requestEntity);
            boolean nextGet=false;
            try {
                HttpResponse response = httpClient.execute(requestPost);
                responseText = EntityUtils.toString(response.getEntity(), "utf-8");
                JSONObject ret = new JSONObject(responseText);
                if (ret.get("ok") != true) {
                    throw new Exception("Server say it's not ok. ");
                }
                Message tMsg = Message.obtain();
                Bundle tBundle = new Bundle();
                tBundle.putString("json", responseText);
                tMsg.setData(tBundle);
                tMsg.what = MSG_OK;
                tMsg.obj = CommandReceiver.this;
                mHandler.sendMessage(tMsg);
                Thread.sleep(1000);
                nextGet = true;
            } catch (Exception e) {
                Log.i(TAG, "Send Error.");
                mHandler.sendEmptyMessage(MSG_FAILED);
                e.printStackTrace();
            } finally {
                synchronized (mLock) {
                    mConnection--;
                }
                httpClient.close();
            }
            Log.i(TAG, String.valueOf(nextGet));
            if (nextGet) {
                get();
            } else { // && (mRetry++ < 50)
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                get();
            }
        }
    }
    static class MyHandler extends Handler
    {
        CommandReceiver self;
        @TargetApi(18)
        private void remove(JSONObject tRet) throws Exception {
            if (self.mContext instanceof NotificationListenerService) {
                NotificationListenerService nls = (NotificationListenerService) self.mContext;
                JSONArray ids = tRet.getJSONObject("msg").getJSONArray("id");
                for (int i=0; i<ids.length(); i++) {
                    String pkg, tag;
                    int id;
                    String[] parts = ids.getString(i).split(";;");
                    pkg = parts[0];
                    tag = parts[1];
                    if (tag.equals("{[null]}")) {
                        tag = null;
                    }
                    id = Integer.parseInt(parts[2]);
                    nls.cancelNotification(pkg, tag, id);
                }
            } else if (self.mContext instanceof AccessibilityService) {
                AccessibilityService as = (AccessibilityService) self.mContext;
            }
        }
        public void handleMessage(Message msg) {
            if (msg.what == MSG_OK) {
                try {
                    self = (CommandReceiver)msg.obj;
                    JSONObject tRet = new JSONObject(msg.getData().getString("json"));
                    String cmd = tRet.getString("cmd");
                    if (cmd.equals("remove")) {
                        remove(tRet);
                    } else if(cmd.equals("did")) {
                        self.mcc.setDID(tRet.getString("did"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public CommandReceiver (Context context) {
        ConnectionConfig cc = new ConnectionConfig(context);
        mConnection = 0;
        mContext = context;
        mcc = cc;
        mHandler = new MyHandler();
        monitorThread = new Thread(new monitor());
        monitorThread.start();
    }
    public void start() {
        get();
    }
    public void stop() {
        mStop = true;
    }
    private String getGetString() {
        JSONObject tReq = new JSONObject();
        try {
            tReq.put("cmd", "get");
            //TODO: 可变ROLE
            tReq.put("role", "phone");
            tReq.put("did", mcc.getDID());
            tReq.put("code", mcc.getCode());
            tReq.put("key", mcc.getKey());
            tReq.put("name", mcc.getName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tReq.toString();
    }
    private void get() {
        if (mStop) {
            Log.i(TAG, "Getter stopped because service stopped.");
            return;
        }
        synchronized (mLock) {
            if (mConnection>0) {
                Log.e(TAG, "Double run?");
                Exception e = new Exception();
                e.printStackTrace();
//                    try {
//                        Thread.sleep(1000);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
                return;
            }
        }
        getThread = new Thread(new httpGetter(mcc.getServer(), getGetString()));
        getThread.start();
        }
    protected void finalize() throws java.lang.Throwable {
        super.finalize();
    }
//    public abstract JSONObject onMessage(JSONObject msg);
}
