package com.rofyfirm.autoplay;

import android.support.annotation.Nullable;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

public class MainActivity extends AppCompatActivity {

    public String cookie;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            Fragment preferenceFragment = new SettingFragment();
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.add(R.id.fragment, preferenceFragment);
            ft.commit();
        } else {
            String cookieData = savedInstanceState.getString("CookieData");
            if(!TextUtils.isEmpty(cookieData)){
                cookie = cookieData;
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("CookieData",cookie);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        String cookieData = savedInstanceState.getString("CookieData");
        if (!TextUtils.isEmpty(cookieData)){
            cookie = cookieData;
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}