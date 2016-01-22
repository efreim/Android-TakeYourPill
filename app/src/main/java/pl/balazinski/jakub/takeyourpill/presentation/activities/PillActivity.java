package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.PillRepository;

/**
 * Activity lets you add or edit pills
 */
public class PillActivity extends AppCompatActivity {


    //States for setting up components for adding or edition

    private enum State {
        NEW, EDIT
    }

    //Setting up components for activity
    @Bind(R.id.toolbarPill)
    Toolbar toolbar;

    @Bind(R.id.pill_name)
    public EditText pillNameEditText;
    @Bind(R.id.pill_desc)
    public EditText pillDescEditText;
    @Bind(R.id.pill_dose)
    public EditText pillDosageEditText;

    //Optional
    @Bind(R.id.optional_layout)
    public LinearLayout optionalLayout;
    @Bind(R.id.pill_count)
    public EditText pillCountEditText;
    @Bind(R.id.active_substance_et)
    public EditText pillActiveSubEditText;
    @Bind(R.id.pill_price)
    public EditText pillPriceEditText;
    @Bind(R.id.pill_barcode)
    public EditText pillBarcodeEditText;
    @Bind(R.id.add_photo)
    public Button addPhoto;

    @Bind(R.id.add_pill)
    public Button addPill;

    private String mName, mDesc;
    private int mDosage = -1;
    private State state;
    private Pill mPill = null;
    private Uri imageUri = null;
    public static int id = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        /*
         * If extras are empty state is new otherwise
         * state is edit and edited pill must be loaded
         * from database.
         */

        if (extras == null) {
            state = State.NEW;
            setView(state);
        } else {
            state = State.EDIT;
            Long mId = extras.getLong(Constants.EXTRA_LONG_ID);

            List<Pill> list = PillRepository.getAllPills(this);

            for (Pill p : list) {
                if (p.getId().equals(mId)) {
                    mPill = p;
                }
            }
            if (mPill != null) {
                setView(state);
                imageUri = Uri.parse(mPill.getPhoto());
            }
        }

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

    /**
     * Sets components to view.
     *
     * @param state lets method know to set up add or edit view
     */
    private void setView(State state) {
        if (state == State.NEW) {
            addPill.setText("SAVE");
            addPhoto.setText("ADD PHOTO");
        } else if (state == State.EDIT) {
            addPill.setText("UPDATE");
            addPhoto.setText("EDIT PHOTO");

            pillNameEditText.setText(mPill.getName());
            pillDescEditText.setText(mPill.getDescription());
            pillDosageEditText.setText(String.valueOf(mPill.getDosage()));

            if (mPill.getPillsCount() != -1)
                pillCountEditText.setText(String.valueOf(mPill.getPillsCount()));
            if (!mPill.getActiveSubstance().equals(""))
                pillActiveSubEditText.setText(mPill.getActiveSubstance());
            if (!mPill.getPrice().equals(""))
                pillPriceEditText.setText(mPill.getPrice());
            if (mPill.getBarcodeNumber() != -1)
                pillBarcodeEditText.setText(String.valueOf(mPill.getBarcodeNumber()));
        }
    }

    @OnClick(R.id.add_pill)
    public void addPill(View view) {
        int mCount = -1;
        String activeSubstance = "";
        String price = "";
        long barcode = -1;


        if (pillNameEditText.getText() != null)
            mName = pillNameEditText.getText().toString();
        if (pillDescEditText.getText() != null)
            mDesc = pillDescEditText.getText().toString();
        if (!pillDosageEditText.getText().toString().equals(""))
            mDosage = Integer.parseInt(pillDosageEditText.getText().toString());

        //if(optionalLayout.getVisibility() == View.VISIBLE)
        if (!pillCountEditText.getText().toString().equals(""))
            mCount = Integer.parseInt(pillCountEditText.getText().toString());
        if (pillActiveSubEditText.getText() != null)
            activeSubstance = pillActiveSubEditText.getText().toString();
        if (pillPriceEditText.getText() != null)
            price = pillPriceEditText.getText().toString();
        if (!pillBarcodeEditText.getText().toString().equals(""))
            barcode = Long.parseLong(pillBarcodeEditText.getText().toString());


        if (TextUtils.isEmpty(mName))
            pillNameEditText.setError("Set name to your pill");
        else if (TextUtils.isEmpty(mDesc)) {
            pillDescEditText.setError("Set description to your pill");
        } else if (mDosage == -1) {
            pillDosageEditText.setError("Set dosage");
        } else if (state == State.EDIT) {
            mPill.setName(mName);
            mPill.setDescription(mDesc);
            mPill.setDosage(mDosage);
            mPill.setPillsCount(mCount);
            mPill.setActiveSubstance(activeSubstance);
            mPill.setPrice(price);
            mPill.setBarcodeNumber(barcode);
            mPill.setPhoto(getImageUri());
            DatabaseHelper.getInstance(this).getDao().update(mPill);
            finish();
        } else if (state == State.NEW) {
            String path;
            //If path equals "" pill image is empty.
            if (getImageUri() != null)
                path = getImageUri().toString();
            else
                path = "";

            Pill pill = new Pill(mName, mDesc, mCount, mDosage, path, activeSubstance, price, barcode);
            PillRepository.addPill(this, pill);
            finish();
        }

    }

    @OnClick(R.id.add_photo)
    public void addPhoto(View view) {
        selectImage();
    }

    @OnCheckedChanged(R.id.optional_checkbox)
    public void onChecked(boolean checked) {
        if (checked)
            optionalLayout.setVisibility(View.VISIBLE);
        else
            optionalLayout.setVisibility(View.GONE);
    }

    /**
     * Function build dialog that let you choose between capturing
     * photo or choosing from gallery.
     */
    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(PillActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, Constants.REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"), Constants.SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    /**
     * @param requestCode Requests camera or select file
     * @param resultCode  1 if result is valid
     * @param data        Photo or Uri
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Constants.REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                if (thumbnail != null) {
                    thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                }
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri = Uri.fromFile(destination);

            } else if (requestCode == Constants.SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                setImageUri(selectedImageUri);
            }
        }
    }

    public Uri getImageUri() {
        return imageUri;
    }

    public void setImageUri(Uri imageUri) {
        this.imageUri = imageUri;
    }
}