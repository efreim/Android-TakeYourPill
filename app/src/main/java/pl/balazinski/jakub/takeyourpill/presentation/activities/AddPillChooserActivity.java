package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;


public class AddPillChooserActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    //Setting up components for activity
    @Bind(R.id.toolbarPill)
    Toolbar toolbar;


    private OutputProvider outputProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_pill_chooser);
        ButterKnife.bind(this);
        outputProvider = new OutputProvider(this);
        /*
         * Setting up notification bar color:
         * 1. Clear FLAG_TRANSLUCENT_STATUS flag
         * 2. Add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
         * 3. Change the color
         */
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.notification_bar));


        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }


    @OnClick(R.id.scan_button)
    public void scanBarcode(View v){
        startActivity(new Intent(getApplicationContext(), ScanBarcodeChooserActivity.class));
    }

    @OnClick(R.id.add_manually_button)
    public void addManually(View v) {
        startActivity(new Intent(getApplicationContext(), PillActivity.class));
    }



}
