package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;

/**
 * Activity with information about app, terms&conditions and author
 */
public class AboutActivity extends Activity {

    private final String TAG = getClass().getSimpleName();

    @Bind(R.id.info_textview)
    TextView infoTextView;
    @Bind(R.id.continue_button)
    Button continueButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        ButterKnife.bind(this);
        setupView();
    }

    private void setupView(){
        if (Constants.VERSION >= Build.VERSION_CODES.M) {
            continueButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));

        } else {
            continueButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }

        infoTextView.setText(Html.fromHtml(getString(R.string.about_text)));

        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width*.78),(int)(height*.65));
        getWindow().setBackgroundDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.rounded_about));
    }

    @OnClick(R.id.continue_button)
    public void onClick(View v){
        finish();
    }
}
