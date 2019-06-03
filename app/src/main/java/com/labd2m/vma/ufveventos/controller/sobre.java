package com.labd2m.vma.ufveventos.controller;

import android.content.pm.PackageInfo;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import com.labd2m.vma.ufveventos.R;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

public class sobre extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sobre);

        //Google Analytics
        MyApplication application = (MyApplication) getApplication();
        Tracker mTracker = application.getDefaultTracker();
        mTracker.setScreenName("sobre");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(),0);
            String versao = pInfo.versionName;
            ((TextView) findViewById(R.id.versao)).setText("Versão "+versao);
        }catch(Exception e){}
    }
}