package com.qrcode.qrcode;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {


    private TextView pageTitle;
    private ImageView pageIndicator1;
    private ImageView pageIndicator2;
    private ViewPager viewPager;
    private List<Fragment> fragments;
    private PagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        setContentView(R.layout.activity_main);
        initViews();
        initEvent();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void initEvent() {
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                CameraFragment cameraFragment = (CameraFragment) fragments.get(0);
                if (position == 0) {
                    pageIndicator1.setImageResource(R.drawable.viewpager_indicator_selected);
                    pageIndicator2.setImageResource(R.drawable.viewpager_indicator_normal);
                    cameraFragment.toggleRunning();

                } else {
                    pageIndicator2.setImageResource(R.drawable.viewpager_indicator_selected);
                    pageIndicator1.setImageResource(R.drawable.viewpager_indicator_normal);
                    cameraFragment.turnLightOff();
                    cameraFragment.toggleRunning();
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    private void initViews() {
        pageIndicator1 = (ImageView) findViewById(R.id.id_viewpager_indicator1);
        pageIndicator2 = (ImageView) findViewById(R.id.id_viewpager_indicator2);
        viewPager = (ViewPager) findViewById(R.id.id_viewpager);
        fragments = new ArrayList<Fragment>();
        fragments.add(new CameraFragment());
        fragments.add(new GenerateFragment());

        pagerAdapter = new FragmentPagerAdapter(getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                return fragments.get(position);
            }

            @Override
            public int getCount() {
                return fragments.size();
            }
        };

        viewPager.setAdapter(pagerAdapter);
    }
}
