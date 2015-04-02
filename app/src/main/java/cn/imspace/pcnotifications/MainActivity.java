package cn.imspace.pcnotifications;
//TODO  new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    private int mCount = 0;
    private ConnectionConfig mConfig;
    private class BindConfig {
        private String mFieldName;
        BindConfig(EditText et, String fieldName) {
            mFieldName = fieldName;
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(Editable s) {
                    mConfig.setConfig(mFieldName, s.toString());
                }
            });
        }
    }
    private void setEditText(EditText et, CharSequence s) {
        et.setText(s);
    }
    public void test(View view) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat
                .Builder(MainActivity.this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("This is title.")
                .setContentText("This is content. ["+(++mCount)+"]")
                .setContentIntent(PendingIntent.getActivity(this, 0,
                        new Intent(), PendingIntent.FLAG_UPDATE_CURRENT));
        mNotificationManager.notify(1, mBuilder.build());
    }
    public void saveLog(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Process logcatProcess;
                BufferedReader bufferedReader;
                try {
                    String[] running=new String[]{ "logcat","-d","System.err:W" };
                    logcatProcess = Runtime.getRuntime().exec(running);
                    bufferedReader = new BufferedReader(new InputStreamReader(
                            logcatProcess.getInputStream()));
                    String line;
                    FileOutputStream fout = new FileOutputStream(new File(Environment.getExternalStorageDirectory().getPath()+"/space.log"));
                    while ((line = bufferedReader.readLine()) != null) {
                        fout.write(line.getBytes());
                        fout.write("\n".getBytes());
                    }
                    fout.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public void openSystemSettings(View view) {
        Intent intent;
        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.JELLY_BEAN_MR2){
            intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        } else {
            intent = new Intent("android.settings.ACCESSIBILITY_SETTINGS"); //AccessibilitySettings
        }
        startActivity(intent);
    }
    public class MyViewPagerAdapter extends PagerAdapter{
        private ArrayList<View> mListViews;
        private ArrayList<String> mTitleList;
        public MyViewPagerAdapter(ArrayList<View> listViews, ArrayList<String> titleList) {
            this.mListViews = listViews;
            mTitleList = titleList;
        }
        @Override
        public CharSequence getPageTitle(int position) {
            return mTitleList.get(position);
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object)   {
            container.removeView(mListViews.get(position));
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }
        @Override
        public int getCount() {
            return  mListViews.size();
        }
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0==arg1;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mConfig = new ConnectionConfig(this);

        PagerTabStrip pagerTabStrip;
        pagerTabStrip=(PagerTabStrip) findViewById(R.id.pagertab);
        pagerTabStrip.setTabIndicatorColor(0x4286f5);
        pagerTabStrip.setDrawFullUnderline(false);
        pagerTabStrip.setBackgroundColor(0x000000);

        ArrayList<View> mViewList = new ArrayList<>();
        ArrayList<String> mTitleList = new ArrayList<>();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        LayoutInflater lf = getLayoutInflater().from(this);
        View viewSetup = lf.inflate(R.layout.view_setup, null);
        mViewList.add(viewSetup);
        mTitleList.add(getString(R.string.tab_setup));
        mViewList.add(lf.inflate(R.layout.view_apps, null));
        mTitleList.add(getString(R.string.tab_apps));
        MyViewPagerAdapter pagerAdapter = new MyViewPagerAdapter(mViewList, mTitleList);
        viewPager.setAdapter(pagerAdapter);

        setEditText((EditText)viewSetup.findViewById(R.id.editText_key), mConfig.getKey());
        setEditText((EditText)viewSetup.findViewById(R.id.editText_code), mConfig.getCode());
        new BindConfig((EditText)viewSetup.findViewById(R.id.editText_key), "key");
        new BindConfig((EditText)viewSetup.findViewById(R.id.editText_code), "code");
//        if (savedInstanceState == null) {
//            getSupportFragmentManager().beginTransaction()
//                    .add(R.id.container, new PlaceholderFragment())
//                    .commit();
//        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_main, menu);
//        return true;
//    }

//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
////            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
////            startActivity(intent);
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    /**
     * A placeholder fragment containing a simple view.
     */
//    public static class PlaceholderFragment extends Fragment {
//
//        public PlaceholderFragment() {
//        }
//
//        @Override
//        public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                                 Bundle savedInstanceState) {
//            View rootView = inflater.inflate(R.layout.view_setup, container, false);
//
//            return rootView;
//        }
//    }
}
