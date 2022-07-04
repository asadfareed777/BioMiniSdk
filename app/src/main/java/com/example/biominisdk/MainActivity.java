package com.example.biominisdk;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.fareed.bioMini.BioMetricUtility;

public class MainActivity extends AppCompatActivity {

    private BioMetricUtility bioMetricUtility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bioMetricUtility = BioMetricUtility.getInstance(this);
    }


    @Override
    protected void onDestroy() {
        bioMetricUtility.destructEverything();
        super.onDestroy();
    }
}