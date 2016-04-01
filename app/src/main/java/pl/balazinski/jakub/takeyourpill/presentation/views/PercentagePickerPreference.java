package pl.balazinski.jakub.takeyourpill.presentation.views;


import android.content.Context;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

public class PercentagePickerPreference extends DialogPreference {

    private NumberPicker numberPicker;

    public PercentagePickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected View onCreateDialogView() {
        return generateNumberPicker();
    }

    public NumberPicker generateNumberPicker() {
        numberPicker = new NumberPicker(getContext());
        numberPicker.setMinValue(1);
        numberPicker.setMaxValue(99);
        numberPicker.setValue(10);

        /*
         * Anything else you want to add to this.
         */

        return numberPicker;
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            int port = numberPicker.getValue();
            Log.d("NumberPickerPreference", "NumberPickerValue : " + port);
        }
    }

}