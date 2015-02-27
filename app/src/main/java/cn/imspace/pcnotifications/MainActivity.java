package cn.imspace.pcnotifications;
//TODO  new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS);
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;


public class MainActivity extends ActionBarActivity {
    public void test(View view) {
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder mBuilder = new NotificationCompat
                .Builder(MainActivity.this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("This is title.")
                .setContentText("This is content.");
        mNotificationManager.notify(1, mBuilder.build());
    }
    public void openSystemSettings(View view) {
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
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

        PagerTabStrip pagerTabStrip;
        pagerTabStrip=(PagerTabStrip) findViewById(R.id.pagertab);
        pagerTabStrip.setTabIndicatorColor(0xffff00);
        pagerTabStrip.setDrawFullUnderline(false);
        pagerTabStrip.setBackgroundColor(0xffffff);
        pagerTabStrip.setTextSpacing(1);

        ArrayList<View> mViewList = new ArrayList<>();
        ArrayList<String> mTitleList = new ArrayList<>();
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        LayoutInflater lf = getLayoutInflater().from(this);
        mViewList.add(lf.inflate(R.layout.view_setup, null));
        mTitleList.add(getString(R.string.tab_setup));
        mViewList.add(lf.inflate(R.layout.view_apps, null));
        mTitleList.add(getString(R.string.tab_apps));
        MyViewPagerAdapter pagerAdapter = new MyViewPagerAdapter(mViewList, mTitleList);
        viewPager.setAdapter(pagerAdapter);
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
