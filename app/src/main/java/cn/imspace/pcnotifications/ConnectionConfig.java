package cn.imspace.pcnotifications;

/**
 * Created by space on 15/2/26.
 */

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;

import java.net.URI;


public class ConnectionConfig {
    MyHandler mHandler;
    SharedPreferences.Editor mConnectionSPE, mServerSPE;
    SharedPreferences mConnection, mServerList;
    private static final String CONNECTION_NAME = "connection";
    private static final String SERVER_NAME = "serverlist";
    private static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    }
    public String getServerString() {
        return mConnection.getString("server", "http://203.195.196.109:8765/");
    }
    public URI getServer() {
        URI tURI, dURI=null;
        try {
            dURI = new URI("http://203.195.196.109:8765/");
        } catch (Exception e) {
            dURI = null; //impossible unless you changed the code above. fuck java.
        }
        try {
            tURI = new URI(getServerString());
        } catch(Exception e) {
            tURI = dURI;
        }
        return tURI;
    }
    public String getDID() {
        //TODO
        return mServerList.getString(getServerString(), "debug");
    }
    public String getCode() {
        //TODO
        return "debug";
    }
    public String getKey() {
        //TODO
        return "debug";
    }
    public void setDID(String did) {
        mServerSPE.putString(getServerString(), did).commit();
    }
    ConnectionConfig(Context context) {
        mHandler = new MyHandler();
        mConnectionSPE = context.getSharedPreferences(CONNECTION_NAME, Context.MODE_MULTI_PROCESS).edit();
        mServerSPE =  context.getSharedPreferences(SERVER_NAME, Context.MODE_MULTI_PROCESS).edit();
        mConnection = context.getSharedPreferences(CONNECTION_NAME, Context.MODE_MULTI_PROCESS);
        mServerList = context.getSharedPreferences(SERVER_NAME, Context.MODE_MULTI_PROCESS);
    }
}
