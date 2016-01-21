package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.OuterPillDatabase;
import pl.balazinski.jakub.takeyourpill.data.database.PillRepository;


public class AddPillActivity extends AppCompatActivity {
    //Setting up components for activity
    @Bind(R.id.toolbarPill)
    Toolbar toolbar;
    @Bind(R.id.barcode_number_et)
    EditText barcodeNumberEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        ButterKnife.bind(this);

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

        //displayDialog();
    }

    private void displayDialog() {
        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle("Information").setMessage("Scan barcode with your app to add pill from database,you can also skip" +
                "scanning by typing barcode on your own.\n You can also add your custom pill.");
        alertDialogBuilder.setPositiveButton("I got it!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    @OnClick(R.id.scan_button)
    public void scanBarcode(View v) {
        /*IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.initiateScan();*/

        Intent i = new Intent(this, ScanBarcodeActivity.class);
        startActivityForResult(i, 1);
        Toast.makeText(getApplicationContext(), "barcode number: " + barcodeNumberEditText.getText().toString(), Toast.LENGTH_SHORT).show();

    }

    @OnClick(R.id.search_by_barcode_button)
    public void searchBarcode(View v) {
        if (!barcodeNumberEditText.getText().toString().equals("")) {
            String number = barcodeNumberEditText.getText().toString();
            read(number);
        }
        Toast.makeText(getApplicationContext(), "barcode number: " + barcodeNumberEditText.getText().toString(), Toast.LENGTH_SHORT).show();
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
       /* IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            Snackbar snackbar = Snackbar
                    .make(this.findViewById(android.R.id.content), "Barcode: " + scanResult.toString(), Snackbar.LENGTH_LONG);
            snackbar.show();
            read(scanResult.getContents());
        }*/
        if (requestCode == 1) {
            if(resultCode == Activity.RESULT_OK){
                String result=data.getStringExtra("result");
                Snackbar snackbar = Snackbar
                        .make(this.findViewById(android.R.id.content), "Barcode: " + result, Snackbar.LENGTH_LONG);
                snackbar.show();
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }

    public void read(String code) {
        OuterPillDatabase outerPillDatabase = new OuterPillDatabase(getApplicationContext());
        Cursor cursor = outerPillDatabase.getReadableDatabase()
                .rawQuery("SELECT * FROM " + Constants.TABLE_NAME + " where kod = " + code + ";", null);

        if(cursor!=null) {
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
            }
            String activeSubstance = cursor.getString(1);
            String nameAndDesc = cursor.getString(2);
            String[] nameSplit = nameAndDesc.split(",");
            String name = nameSplit[0];
            StringBuilder builder = new StringBuilder();
            for(int i=1; i<nameSplit.length;i++)
                builder.append(nameSplit[i]);

            String description = builder.toString();

            String countString = cursor.getString(3);
            String[] countSplit = countString.split(" ");
            int count = Integer.parseInt(countSplit[0]);
            long barcode = Long.parseLong(code);
            String price = cursor.getString(5);
            int dosage = 1;

            PillRepository.addPill(getApplicationContext(), new Pill(PillActivity.id++, name, description, count, dosage, "", activeSubstance, price, barcode));

            finish();
        }else
            Toast.makeText(getApplicationContext(), "Pill not found, try again or add manually.", Toast.LENGTH_SHORT);
    }

  /*  private int showDosageDialog(){
        LayoutInflater inflater = (LayoutInflater)
                getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View npView = inflater.inflate(R.layout.number_picker_dialog_layout, null);
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle("Dosage")
                .setView(npView)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                npView.
                            }
                        }).create();
                .setNegativeButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                            }
                        })
                .create();
    }*/

}
