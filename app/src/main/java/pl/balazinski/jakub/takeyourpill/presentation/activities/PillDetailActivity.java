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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Alarm;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.utilities.ShakeDetector;

/**
 * Activity that shows up after clicking on list item (PillListFragment item)
 */
public class PillDetailActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

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

    // The following are used for the shake detection
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private ShakeDetector mShakeDetector;

    private OutputProvider mOutputProvider;
    private Pill mPill;
    private List<CardView> mCardViewList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);
        mOutputProvider = new OutputProvider(this);
        Bundle extras = getIntent().getExtras();

        setupContent(extras);
        setupComponents();
        setupView();

    }

    private void setupContent(Bundle extras) {
        if (extras != null) {
            Long id = extras.getLong(Constants.EXTRA_LONG_ID);
            mOutputProvider.displayLog(TAG, "Position = " + String.valueOf(id));

            //Getting chosen mPill from database
            List<Pill> list = DatabaseRepository.getAllPills(this);
            if (list != null) {
                for (Pill p : list) {
                    if (p.getId().equals(id))
                        mPill = p;
                }
            } else
                mOutputProvider.displayShortToast(getString(R.string.error_loading_pills));
        }
        // ShakeDetector initialization
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager
                .getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new ShakeDetector.OnShakeListener() {

            @Override
            public void onShake(int count) {
                if (mPill.getPillsCount() != -1 && mPill.getPillsRemaining() != -1) {
                    refillButton.callOnClick();
                }
            }
        });
    }

    private void setupComponents() {

        mCardViewList = new ArrayList<>();
        final String pillName = mPill.getName();
        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(pillName);
        if (mPill.getDescription().equals(""))
            pillDescriptionCardView.setVisibility(View.GONE);
        else {
            descriptionTextView.setText(mPill.getDescription());
            mCardViewList.add(pillDescriptionCardView);
        }

        if (mPill.getDosage() == -1)
            pillDosageCardView.setVisibility(View.GONE);
        else {
            dosageTextView.setText(String.valueOf(mPill.getDosage()));
            mCardViewList.add(pillDosageCardView);
        }

        if (mPill.getActiveSubstance().equals(""))
            pillActiveSubCardView.setVisibility(View.GONE);
        else {
            activeSubstanceTextView.setText(mPill.getActiveSubstance());
            mCardViewList.add(pillActiveSubCardView);
        }

        if (mPill.getPillsCount() == -1)
            pillCountCardView.setVisibility(View.GONE);
        else {
            pillCountTextView.setText(String.valueOf(mPill.getPillsCount()));
            mCardViewList.add(pillCountCardView);
        }

        if (mPill.getPillsRemaining() == -1)
            pillCountLeftCardView.setVisibility(View.GONE);
        else {
            pillCountLeftTextView.setText(String.valueOf(mPill.getPillsRemaining()));
            mCardViewList.add(pillCountLeftCardView);
        }

        if (mPill.getPrice().equals(""))
            pillPriceCardView.setVisibility(View.GONE);
        else {
            priceTextView.setText(mPill.getPrice());
            mCardViewList.add(pillPriceCardView);
        }

        if (mPill.getBarcodeNumber() == -1)
            pillBarcodeCardView.setVisibility(View.GONE);
        else {
            barcodeNumberTextView.setText(String.valueOf(mPill.getBarcodeNumber()));
            mCardViewList.add(pillBarcodeCardView);
        }

        List<Long> alarmIds = DatabaseRepository.getAlarmsByPill(this, mPill.getId());
        if (alarmIds.isEmpty())
            alarmsAttachedCardView.setVisibility(View.GONE);
        else {
            StringBuilder stringBuilder = new StringBuilder();
            for (Long l : alarmIds) {
                Alarm alarm = DatabaseRepository.getAlarmById(this, l);
                if (alarm != null) {
                    String s = buildString(alarm.getMinute(), alarm.getHour());
                    stringBuilder.append("id = ");
                    stringBuilder.append(alarm.getId().toString());
                    stringBuilder.append(", time  ");
                    stringBuilder.append(s);
                    stringBuilder.append("\n");
                }
            }
            alarmsAttachedTextView.setText(stringBuilder.toString());
            mCardViewList.add(alarmsAttachedCardView);
        }


        loadBackdrop();

        refillButton.setEnabled(false);
        if (mPill.getPillsCount() != -1 && mPill.getPillsRemaining() != -1) {
            refillButton.setEnabled(true);
        }
    }


    private void setupView() {
        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        for (CardView cd : mCardViewList) {
            if (Constants.VERSION >= Build.VERSION_CODES.M)
                cd.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_cardview));
            else
                cd.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.rounded_cardview));
        }

        if (Constants.VERSION >= Build.VERSION_CODES.M) {
            refillButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
            searchWebButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
        } else {
            refillButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
            searchWebButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }
    }


    @OnClick(R.id.refill_button)
    public void onRefillClick(View v) {
        //TODO Clear notification if pills are refilled
        int count = mPill.getPillsCount();
        mPill.setPillsRemaining(count);
        DatabaseHelper.getInstance(getApplicationContext()).getPillDao().update(mPill);
        pillCountLeftTextView.setText(String.valueOf(mPill.getPillsRemaining()));
        if (isPillNotification(mPill.getId()))
            clearNotification(mPill.getId());
        mOutputProvider.displayShortToast(getString(R.string.toast_pill_refilled));
    }

    @OnClick(R.id.search_web_button)
    public void onWebSearchClick(View v) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.SEARCH_PILL_WEBSITE + mPill.getName()));
        startActivity(browserIntent);
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
        Glide.with(this).load(Uri.parse(mPill.getPhoto())).centerCrop().into(imageView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sample_actions, menu);
        return true;
    }

    private boolean isPillNotification(Long id) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        PendingIntent test = PendingIntent.getBroadcast(this, longToInt(id), intent, PendingIntent.FLAG_NO_CREATE);
        return test != null;
    }

    private void clearNotification(Long id) {
        Intent intent = new Intent(this, MapsActivity.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, longToInt(id), intent, 0);
        pendingIntent.cancel();
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


    /**
     * Converts Long value to int value.
     *
     * @param l Long value we want to transform.
     * @return Transformed int value.
     */
    private int longToInt(Long l) {
        return (int) (long) l;
    }

}
