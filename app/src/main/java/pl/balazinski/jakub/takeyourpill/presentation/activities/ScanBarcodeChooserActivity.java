package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.OuterPillDatabase;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

/**
 * Created by Kuba on 23.02.2016.
 */
public class ScanBarcodeChooserActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    //Setting up components for activity
    @Bind(R.id.toolbarPill)
    Toolbar toolbar;
    @Bind(R.id.barcode_number_et)
    EditText barcodeNumberEditText;
    @Bind(R.id.search_by_barcode_button)
    Button searchByBarcodeButton;
    @Bind(R.id.add_pill_manually_chooser)
    Button addPillManually;

    private OutputProvider outputProvider;
    private Camera mCamera;
    private boolean isScanManually = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_chooser);
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

        Intent intent = getIntent();
        if(intent.getExtras()!=null)
            isScanManually = intent.getBooleanExtra("SCAN_MANUALLY", false);
        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int i = getCameraInformation();
        if (i != 0 || isScanManually) {
            outputProvider.displayShortToast("No rear camera available. \nAdd barcode number manually.");
        }else{
            isScanManually = false;
            startActivityForResult(new Intent(this, ScanBarcodeActivity.class), 1);
        }

        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            searchByBarcodeButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
            addPillManually.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));

        } else {
            searchByBarcodeButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
            addPillManually.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }

    }

    public void read(String code) {
        OuterPillDatabase outerPillDatabase = new OuterPillDatabase(getApplicationContext());
        Cursor cursor = outerPillDatabase.getReadableDatabase()
                .rawQuery("SELECT * FROM " + Constants.TABLE_NAME + " where kod = " + code + ";", null);
        outputProvider.displayLog(TAG, "read = " + code);
        if (cursor.getCount() != 0) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
            }
            String activeSubstance = cursor.getString(1);
            String nameAndDesc = cursor.getString(2);
            String[] nameSplit = nameAndDesc.split(",");
            String name = nameSplit[0];
            StringBuilder builder = new StringBuilder();
            for (int i = 1; i < nameSplit.length; i++)
                builder.append(nameSplit[i]);

            String description = builder.toString();

            String countString = cursor.getString(3);
            String[] countSplit = countString.split(" ");
            int count = Integer.parseInt(countSplit[0]);
            long barcode = Long.parseLong(code);
            String price = cursor.getString(5);
            int dosage = -1;

            Uri uri = Uri.parse("android.resource://pl.balazinski.jakub.takeyourpill/" + R.drawable.pill_white_background);
            String path = uri.toString();

            DatabaseRepository.addPill(getApplicationContext(), new Pill(name, description, count, dosage, path, activeSubstance, price, barcode));

            Intent i = new Intent(ScanBarcodeChooserActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        } else
            outputProvider.displayShortToast("Pill not found, try again or add manually.");
    }

    /**
     * @param requestCode Requests camera or select file
     * @param resultCode  1 if result is valid
     * @param data        Photo or Uri
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == Activity.RESULT_OK) {
                String result = data.getStringExtra("result");
                read(result);
                finish();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //TODO
            }
        }
    }


    @OnClick(R.id.search_by_barcode_button)
    public void searchBarcode(View v) {
        if (!barcodeNumberEditText.getText().toString().equals("")) {
            String number = barcodeNumberEditText.getText().toString();
            read(number);
        }
        outputProvider.displayShortToast("barcode number: " + barcodeNumberEditText.getText().toString());
    }

    private int getCameraInformation() {
        PackageManager packageManager = getApplicationContext().getPackageManager();
        if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA))
            return 0;
        else if (packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT))
            return 1;
        else
            return -1;
    }

}
