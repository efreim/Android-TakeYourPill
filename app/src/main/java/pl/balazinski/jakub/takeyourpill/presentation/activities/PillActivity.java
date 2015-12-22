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
import android.widget.NumberPicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.R;
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
    @Bind(R.id.add_pill)
    public Button addPill;
    @Bind(R.id.add_photo)
    public Button addPhoto;
    private NumberPicker pillCountNumberPicker, pillTakenNumberPicker;


    private String mName, mDesc;
    private State state;
    private Pill mPill;
    private Uri imageUri = null;

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
            int mPosition = extras.getInt(Constants.EXTRA_INT);
            mPosition++;
            mPill = DatabaseHelper.getInstance(this).getDao().queryForId(mPosition);
            setView(state);
            imageUri = Uri.parse(mPill.getPhoto());
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
        pillCountNumberPicker = (NumberPicker) findViewById(R.id.pill_number_picker);
        pillCountNumberPicker.setMinValue(1);
        pillCountNumberPicker.setMaxValue(100);
        pillCountNumberPicker.setWrapSelectorWheel(true);

        pillTakenNumberPicker = (NumberPicker) findViewById(R.id.pill_taken_picker);
        pillTakenNumberPicker.setMinValue(1);
        pillTakenNumberPicker.setMaxValue(10);
        pillTakenNumberPicker.setWrapSelectorWheel(true);

        if (state == State.NEW) {
            pillCountNumberPicker.setValue(1);
            pillTakenNumberPicker.setValue(1);
            addPill.setText("SAVE");
            addPhoto.setText("ADD PHOTO");
        } else if (state == State.EDIT) {
            addPill.setText("UPDATE");
            addPhoto.setText("EDIT PHOTO");
            pillNameEditText.setText(mPill.getName());
            pillDescEditText.setText(mPill.getName());
            pillCountNumberPicker.setValue(mPill.getPillsCount());
            pillTakenNumberPicker.setValue(mPill.getPillsTaken());
        }
    }

    @OnClick(R.id.add_pill)
    public void addPill(View view) {
        if (pillNameEditText.getText() != null)
            mName = pillNameEditText.getText().toString();
        if (pillDescEditText.getText() != null)
            mDesc = pillDescEditText.getText().toString();

        int mCount = pillCountNumberPicker.getValue();
        int mTaken = pillTakenNumberPicker.getValue();


        //  String strNameEditText = pillNameEditText.getText().toString();
        //  String strDescEditText = pillDescEditText.getText().toString();

        if (TextUtils.isEmpty(mName))
            pillNameEditText.setError("Set name to your pill");
        else if (TextUtils.isEmpty(mDesc)) {
            pillDescEditText.setError("Set description to your pill");
        } else if (state == State.EDIT) {
            mPill.setName(mName);
            mPill.setDescription(mDesc);
            mPill.setPillsCount(mCount);
            mPill.setPillsTaken(mTaken);
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
            Pill pill = new Pill(mName, mDesc, mCount, mTaken, path);
            PillRepository.addPill(this, pill);
            finish();
        }

    }

    @OnClick(R.id.add_photo)
    public void addPhoto(View view) {
        selectImage();
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