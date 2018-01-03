package com.dieuninh.soundrecorder.soundrecorder.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.astuetz.PagerSlidingTabStrip;
import com.dieuninh.soundrecorder.soundrecorder.R;
import com.dieuninh.soundrecorder.soundrecorder.fragments.RecordFragment;
import com.dieuninh.soundrecorder.soundrecorder.fragments.SavedRecordFragment;

public class MainActivity extends AppCompatActivity {

  //  private static final String LOG_TAG=MainActivity.class.getSimpleName();
    private PagerSlidingTabStrip tabs;
    private ViewPager pager;
    private Resources resources;

    //xin câp quyền với android M
    public static String[] M_VERSION_REQUEST_PERMISSIONS=new String[]
            {
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        resources=getResources();
        initPermissions();
        initControls();
        addEvents();

    }

    private void addEvents() {
        pager.setAdapter(new MyAdapter(getSupportFragmentManager()));
        tabs.setViewPager(pager);
    }

    private void initControls() {
        pager= (ViewPager) findViewById(R.id.pager);
        tabs= (PagerSlidingTabStrip) findViewById(R.id.tabs);
    }

    /*Do từ android M quyền truy cập bị giới hạn
    nên phải xin quyền sd mic, storage
    * */
    public void initPermissions() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if(!hasPermissionGranted())
            {
                requestAudioPermission();
            }
        }
    }

    private boolean hasPermissionGranted() {
        for(String i:M_VERSION_REQUEST_PERMISSIONS)
        {
            if(ActivityCompat.checkSelfPermission(this, i) != PackageManager.PERMISSION_GRANTED)
            {
                return false;
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestAudioPermission() {
        if(shouldShowRationale())
        {
            Toast.makeText(this, resources.getString(R.string.need_allow),Toast.LENGTH_SHORT).show();
        }
        requestPermissions(M_VERSION_REQUEST_PERMISSIONS,4);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean shouldShowRationale() {
        for(String i: M_VERSION_REQUEST_PERMISSIONS)
        {
            if(shouldShowRequestPermissionRationale(i))
            {
                return true;
            }
        }
        return false;
    }

    //my adapter
    private class MyAdapter extends FragmentPagerAdapter{

        private String[] titles={"Record","Files"};

        public MyAdapter(FragmentManager supportFragmentManager) {
            super(supportFragmentManager);
        }


        @Override
        public Fragment getItem(int position) {
            switch (position)
            {
                case 0:
                    return RecordFragment.newInstance(position);
                case 1:
                    return SavedRecordFragment.newInstance(position);
            }
            return null;
        }

        @Override
        public int getCount() {
            return titles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles[position];
        }
    }
    public MainActivity() {
    }
}
