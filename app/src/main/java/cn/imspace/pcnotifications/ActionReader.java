package cn.imspace.pcnotifications;

import java.util.ArrayList;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import android.widget.RemoteViews;

/**
 * Created by space on 15/2/25.
 */
public class ActionReader {
    public static final int ID_CONTENT_TEXT = 0x1020046;
    public static final int ID_TITLE_TEXT = 0x1020016;
    private Object getActions (RemoteViews remoteViews) throws Exception{
        Class<?> remoteViewsType = remoteViews.getClass();
        Field field = remoteViewsType.getDeclaredField("mActions");
        field.setAccessible(true);
        return field.get(remoteViews);
    }
    private Object getActionDetail(Object action, Class<?> actionType, String fieldStr, Object defaultValue) {
        //Class<?> actionType = action.getClass();
        try {
            Field field = actionType.getDeclaredField(fieldStr);
            field.setAccessible(true);
            Object tRet = field.get(action);
            if (tRet == null)
                return defaultValue;
            else
                return tRet;
        } catch (Exception e) {
            return defaultValue;
        }
    }
    class BriefAction {
        public String methodName;
        public Object value;
        public int viewId;
        public BriefAction(Object inName, Object inValue, Object inViewId) {
            methodName = inName.toString();
            value = inValue;
            viewId = (int) inViewId;
        }
    }
    private ArrayList<BriefAction> mActions = new ArrayList<>();
    String mTitleText="", mContentText="";
    public String getTitleText() {
        return mTitleText;
    }
    public String getContentText() {
        return mContentText;
    }
    public ActionReader(RemoteViews remoteViews) {
        //@RemoteViews.java:1714
        ArrayList<Object> actions;
        try {
            actions = (ArrayList<Object>) getActions(remoteViews);
        } catch (Exception e) {
            actions = new ArrayList<Object>();
            e.printStackTrace();
        }
        for (Object action: actions) {
            Object tMethodName, tValue, tViewId;
            tMethodName = getActionDetail(action, action.getClass(), "methodName", "NoName");
            tValue = getActionDetail(action, action.getClass(), "value", "Nothing");
            tViewId = getActionDetail(action, action.getClass().getSuperclass(), "viewId", 0);
            mActions.add(new BriefAction(tMethodName, tValue, tViewId));
        }
        ArrayList<BriefAction> toDelete = new ArrayList<>();
        for (BriefAction action: mActions) {
            if (!action.methodName.equals("setText")) {
                toDelete.add(action);
            }
        }
        for (BriefAction action: toDelete) {
            mActions.remove(action);
        }
        for (BriefAction action: mActions) {
            if (action.viewId == ID_TITLE_TEXT) {
                mTitleText = action.value.toString();
            } else if (action.viewId == ID_CONTENT_TEXT) {
                mContentText = action.value.toString();
            }
        }
    }
}