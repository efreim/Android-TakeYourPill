package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.OuterPillDatabase;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;


public class ChooseAddPillActivity extends AppCompatActivity {

    private final String TAG = getClass().getSimpleName();

    //Setting up components for activity
    @Bind(R.id.toolbarPill)
    Toolbar toolbar;
    @Bind(R.id.barcode_number_et)
    EditText barcodeNumberEditText;

    private OutputProvider outputProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
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
    public void scanBarcode(View v) {
        Intent i = new Intent(this, ScanBarcodeActivity.class);
        startActivityForResult(i, 1);
    }

    @OnClick(R.id.search_by_barcode_button)
    public void searchBarcode(View v) {
        if (!barcodeNumberEditText.getText().toString().equals("")) {
            String number = barcodeNumberEditText.getText().toString();
            read(number);
        }
        outputProvider.displayShortToast("barcode number: " + barcodeNumberEditText.getText().toString());
    }

    @OnClick(R.id.add_manually_button)
    public void addManually(View v) {
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
                String result = data.getStringExtra("result");
                read(result);
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //TODO
            }
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
            int dosage = 1;

            DatabaseRepository.addPill(getApplicationContext(), new Pill(name, description, count, dosage, "", activeSubstance, price, barcode));

            finish();
        } else
            outputProvider.displayShortToast("Pill not found, try again or add manually.");
    }


}
