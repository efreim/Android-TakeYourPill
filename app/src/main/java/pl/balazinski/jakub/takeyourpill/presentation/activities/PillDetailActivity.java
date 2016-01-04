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
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.w3c.dom.Text;

import butterknife.Bind;
import butterknife.ButterKnife;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;

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
    @Bind(R.id.alarms)
    TextView alarmsTextView;
    @Bind(R.id.toolbar_detail)
    Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ButterKnife.bind(this);

        //Loading clicked item position from intent
        int mPosition = -1;
        Intent intent = getIntent();
        mPosition = intent.getIntExtra(Constants.EXTRA_INT, -1);
        Log.i("POZYCJA", String.valueOf(mPosition));
        mPosition++;


        //Getting chosen pill from database
        pill = DatabaseHelper.getInstance(this).getDao().queryForId(mPosition);

        final String pillName = pill.getName();


        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(pillName);
        descriptionTextView.setText(pill.getDescription());
        dosageTextView.setText(getString(R.string.lorem_ipsum));
        alarmsTextView.setText(getString(R.string.lorem_ipsum));
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
