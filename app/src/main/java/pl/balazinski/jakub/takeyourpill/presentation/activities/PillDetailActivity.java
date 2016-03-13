/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.utilities.ShakeDetector;

/**
 * Activity that shows up after clicking on list item (PillListFragment item)
 */
public class PillDetailActivity extends AppCompatActivity {

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;
    private final String TAG = getClass().getSimpleName();
    private Pill pill;
    private OutputProvider outputProvider;
    //Setting up components for view

    @Bind(R.id.description)
    TextView descriptionTextView;
    @Bind(R.id.dosage)
    TextView dosageTextView;
    @Bind(R.id.pill_count)
    TextView pillCountTextView;
    @Bind(R.id.pill_count_left)
    TextView pillCountLeftTextView;
    @Bind(R.id.active_substance)
    TextView activeSubstanceTextView;
    @Bind(R.id.price)
    TextView priceTextView;
    @Bind(R.id.barcode_number)
    TextView barcodeNumberTextView;
    @Bind(R.id.alarms_attached)
    TextView alarmsAttachedTextView;

    @Bind(R.id.pill_description_cv)
    CardView pillDescriptionCardView;
    @Bind(R.id.pill_dosage_cv)
    CardView pillDosageCardView;
    @Bind(R.id.pill_count_cv)
    CardView pillCountCardView;
    @Bind(R.id.pill_count_left_cv)
    CardView pillCountLeftCardView;
    @Bind(R.id.active_substance_cv)
    CardView pillActiveSubCardView;
    @Bind(R.id.pill_price_cv)
    CardView pillPriceCardView;
    @Bind(R.id.barcode_number_cv)
    CardView pillBarcodeCardView;
    @Bind(R.id.alarms_attached_cv)
    CardView alarmsAttachedCardView;

    @Bind(R.id.toolbar_detail)
    Toolbar toolbar;



    @Bind(R.id.refill_button)
    Button refillButton;
    @Bind(R.id.search_web_button)
    Button searchWebButton;

    private List<CardView> cardViews;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        outputProvider = new OutputProvider(this);
        //Loading clicked item position from intent
        Long mPosition;
        Intent intent = getIntent();
        mPosition = intent.getLongExtra(Constants.EXTRA_LONG_ID, -1);
        outputProvider.displayLog(TAG, "Position = " + String.valueOf(mPosition));


        //Getting chosen pill from database
        List<Pill> list = DatabaseRepository.getAllPills(this);

        if (list != null) {
            for (Pill p : list) {
                if (p.getId().equals(mPosition))
                    pill = p;
            }
        } else
            outputProvider.displayShortToast("Error loading pills");


        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        setupView(pill);

        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                if(pill.getPillsCount() != -1 && pill.getPillsRemaining() != -1) {
                    refillButton.callOnClick();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Add the following line to register the Session Manager Listener onResume
        mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
    }

    @Override
    public void onPause() {
        // Add the following line to unregister the Sensor Manager onPause
        mSensorManager.unregisterListener(mShakeDetector);
        super.onPause();
    }

    /**
     * Loads image from photo into views background
     */
    private void loadBackdrop() {
        final ImageView imageView = (ImageView) findViewById(R.id.backdrop);
        Glide.with(this).load(Uri.parse(pill.getPhoto())).centerCrop().into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    private void setupView(final Pill pill) {

        cardViews = new ArrayList<>();
        final String pillName = pill.getName();
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(pillName);
        if (pill.getDescription().equals(""))
            pillDescriptionCardView.setVisibility(View.GONE);
        else {
            descriptionTextView.setText(pill.getDescription());
            cardViews.add(pillDescriptionCardView);
        }

        if (pill.getDosage() == -1)
            pillDosageCardView.setVisibility(View.GONE);
        else{
            dosageTextView.setText(String.valueOf(pill.getDosage()));
            cardViews.add(pillDosageCardView);
        }

        if (pill.getActiveSubstance().equals(""))
            pillActiveSubCardView.setVisibility(View.GONE);
        else{
            activeSubstanceTextView.setText(pill.getActiveSubstance());
            cardViews.add(pillActiveSubCardView);
        }

        if (pill.getPillsCount() == -1)
            pillCountCardView.setVisibility(View.GONE);
        else{
            pillCountTextView.setText(String.valueOf(pill.getPillsCount()));
            cardViews.add(pillCountCardView);
        }

        if (pill.getPillsRemaining() == -1)
            pillCountLeftCardView.setVisibility(View.GONE);
        else{
            pillCountLeftTextView.setText(String.valueOf(pill.getPillsRemaining()));
            cardViews.add(pillCountLeftCardView);
        }

        if (pill.getPrice().equals(""))
            pillPriceCardView.setVisibility(View.GONE);
        else{
            priceTextView.setText(pill.getPrice());
            cardViews.add(pillPriceCardView);
        }

        if (pill.getBarcodeNumber() == -1)
            pillBarcodeCardView.setVisibility(View.GONE);
        else{
            barcodeNumberTextView.setText(String.valueOf(pill.getBarcodeNumber()));
            cardViews.add(pillBarcodeCardView);
        }

        List<Long> alarmIds = DatabaseRepository.getAlarmsByPill(this,pill.getId());
        if (alarmIds.isEmpty())
            alarmsAttachedCardView.setVisibility(View.GONE);
        else {
            StringBuilder stringBuilder = new StringBuilder();
            for(Long l : alarmIds){
                Alarm alarm = DatabaseRepository.getAlarmById(this, l);
                if(alarm!=null) {
                    String s = buildString(alarm.getMinute(), alarm.getHour());
                    stringBuilder.append("id = ");
                    stringBuilder.append(alarm.getId().toString());
                    stringBuilder.append(", time  ");
                    stringBuilder.append(s);
                    stringBuilder.append("\n");
                }
            }
            alarmsAttachedTextView.setText(stringBuilder.toString());
            cardViews.add(alarmsAttachedCardView);
        }


        loadBackdrop();



        refillButton.setEnabled(false);
        if(pill.getPillsCount() != -1 && pill.getPillsRemaining() != -1) {
            refillButton.setEnabled(true);
        }


        final int version = Build.VERSION.SDK_INT;

        for(CardView cd : cardViews) {
            if (version >= 23)
                cd.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_cardview));
            else
                cd.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.rounded_cardview));
        }

        if (version >= 23) {
            refillButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
            searchWebButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
        } else {
            refillButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
            searchWebButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }
    }


    @OnClick(R.id.refill_button)
    public void onRefillClick(View v){
        //TODO Clear notification if pills are refilled
        int count = pill.getPillsCount();
        pill.setPillsRemaining(count);
        DatabaseHelper.getInstance(getApplicationContext()).getPillDao().update(pill);
        pillCountLeftTextView.setText(String.valueOf(pill.getPillsRemaining()));
        outputProvider.displayShortToast("Your medicine is refilled");
    }

    @OnClick(R.id.search_web_button)
    public void onWebSearchClick(View v){
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.bazalekow.mp.pl/leki/szukaj.html?item_name=" + pill.getName()));
        startActivity(browserIntent);
    }

    private String buildString(int minute, int hour) {
        String s = " : ";
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.valueOf(hour));
        stringBuilder.append(s);
        if (minute < 10)
            stringBuilder.append(String.valueOf(0));
        stringBuilder.append(String.valueOf(minute));
        return stringBuilder.toString();
    }

}
