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

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.PillRepository;

/**
 * Activity that shows up after clicking on list item (PillListFragment item)
 */
public class PillDetailActivity extends AppCompatActivity {

    private Pill pill;

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

    @Bind(R.id.toolbar_detail)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        //Loading clicked item position from intent
        Long mPosition;
        Intent intent = getIntent();
        mPosition = intent.getLongExtra(Constants.EXTRA_LONG_ID, -1);
        Log.i("POZYCJA", String.valueOf(mPosition));


        //Getting chosen pill from database
        List<Pill> list = PillRepository.getAllPills(this);
        for (Pill p : list) {
            if (p.getId().equals(mPosition))
                pill = p;
        }
        //  pill = list.get(mPosition);

        final String pillName = pill.getName();


        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(pillName);
        descriptionTextView.setText(pill.getDescription());
        dosageTextView.setText(String.valueOf(pill.getDosage()));

        if (pill.getActiveSubstance().equals(""))
            pillActiveSubCardView.setVisibility(View.GONE);
        else
            activeSubstanceTextView.setText(pill.getActiveSubstance());

        if (pill.getPillsCount() == -1)
            pillCountCardView.setVisibility(View.GONE);
        else
            pillCountTextView.setText(String.valueOf(pill.getPillsCount()));

        if(pill.getPillsRemaining() == -1)
            pillCountLeftCardView.setVisibility(View.GONE);
        else
            pillCountLeftTextView.setText(String.valueOf(pill.getPillsRemaining()));

        if(pill.getPrice().equals(""))
            pillPriceCardView.setVisibility(View.GONE);
        else
            priceTextView.setText(pill.getPrice());

        if(pill.getBarcodeNumber() == -1)
            pillBarcodeCardView.setVisibility(View.GONE);
        else
            barcodeNumberTextView.setText(String.valueOf(pill.getBarcodeNumber()));

        loadBackdrop();
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
}
