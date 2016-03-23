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
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.OuterPillDatabase;
import pl.balazinski.jakub.takeyourpill.data.database.Pill;
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

    private OutputProvider mOutputProvider;
    private Camera mCamera;
    private boolean isScanManually = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_chooser);
        ButterKnife.bind(this);
        mOutputProvider = new OutputProvider(this);
        Bundle extras = getIntent().getExtras();

        setupView();
        setupContent(extras);
    }

    private void setupView() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.notification_bar));

        if (Constants.VERSION >= Build.VERSION_CODES.M) {
            searchByBarcodeButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
            addPillManually.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));

        } else {
            searchByBarcodeButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
            addPillManually.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }
    }

    private void setupContent(Bundle extras) {
        if (extras != null)
            isScanManually = extras.getBoolean(Constants.ADD_BARCODE_MANUALLY);

        int i = getCameraInformation();
        if (i != 0 || isScanManually) {
            mOutputProvider.displayShortToast(getString(R.string.toast_no_rear_available_add_later));
        } else {
            isScanManually = false;
            startActivityForResult(new Intent(this, ScanBarcodeActivity.class), 1);
        }
    }

    public void read(String code) {
        OuterPillDatabase outerPillDatabase = new OuterPillDatabase(getApplicationContext());
        Cursor cursor = outerPillDatabase.getReadableDatabase()
                .rawQuery("SELECT * FROM " + Constants.OUTER_TABLE_NAME + " where kod = " + code + ";", null);
        mOutputProvider.displayLog(TAG, "read = " + code);
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

            Uri uri = Uri.parse(Constants.DRAWABLE_PATH + R.drawable.pill_white_background);
            String path = uri.toString();

            DatabaseRepository.addPill(getApplicationContext(), new Pill(name, description, count, dosage, path, activeSubstance, price, barcode));

            Intent i = new Intent(ScanBarcodeChooserActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        } else
            mOutputProvider.displayShortToast(getString(R.string.toast_pill_not_found));
    }

    @OnClick(R.id.search_by_barcode_button)
    public void searchBarcode(View v) {
        if (!barcodeNumberEditText.getText().toString().equals("")) {
            String number = barcodeNumberEditText.getText().toString();
            read(number);
        }
    }

    @OnClick(R.id.add_pill_manually_chooser)
    public void onAddPillClick(View v) {
        startActivity(new Intent(getApplicationContext(), PillActivity.class));
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
                String result = data.getStringExtra(Constants.SCAN_BARCODE_RESULT);
                read(result);
                finish();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //TODO co tu zrobic?
            }
        }
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
