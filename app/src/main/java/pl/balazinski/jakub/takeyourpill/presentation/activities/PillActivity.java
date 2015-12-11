package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.domain.PillManager;

/**
 * Created by Kuba on 08.12.2015.
 */
public class PillActivity extends Activity {

    public static final String ID = "id";
    public static final int INVALID_ID = -1;

    private enum State {NEW, EDIT}


    @Bind(R.id.pill_name)
    public EditText pillNameEditText;
    @Bind(R.id.pill_desc)
    public EditText pillDescEditText;

    private NumberPicker pillCountNumberPicker, pillTakenNumberPicker;

    private String mName, mDesc;
    private int mCount, mTaken;
    private State state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill);
        ButterKnife.bind(this);
        setView();

        state = State.NEW;

    }

    private void setView() {
        pillCountNumberPicker = (NumberPicker) findViewById(R.id.pill_number_picker);
        pillCountNumberPicker.setValue(1);
        pillCountNumberPicker.setMinValue(1);
        pillCountNumberPicker.setMaxValue(100);
        pillCountNumberPicker.setWrapSelectorWheel(true);

        pillTakenNumberPicker = (NumberPicker) findViewById(R.id.pill_taken_picker);
        pillTakenNumberPicker.setValue(1);
        pillTakenNumberPicker.setMinValue(1);
        pillTakenNumberPicker.setMaxValue(10);
        pillTakenNumberPicker.setWrapSelectorWheel(true);
    }

    @OnClick(R.id.add_pill)
    public void addPill(View view) {
        mName = pillNameEditText.getText().toString();
        mDesc = pillDescEditText.getText().toString();
        mCount = pillCountNumberPicker.getValue();
        mTaken = pillTakenNumberPicker.getValue();

        String strNameEditText = pillNameEditText.getText().toString();
        String strDescEditText = pillDescEditText.getText().toString();

        if (TextUtils.isEmpty(strNameEditText))
            pillNameEditText.setError("Set name to your pill");
        else if (TextUtils.isEmpty(strDescEditText)) {
            pillDescEditText.setError("Set description to your pill");
        }/* else if (state == State.EDIT) {
            pill = pillListActive.get(mID);
            pill.setName(mName);
            pill.setDescription(mDesc);
            pill.setPillsCount(mCount);
            pill.setPillsTaken(mTaken);
            finish();}*/ else if (state == State.NEW) {
            Pill pill = new Pill(mName, mDesc, mCount, mTaken);
            PillManager.getInstance().addPill(pill);
            finish();
        }

    }
}