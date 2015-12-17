package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.NumberPicker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.data.Constans;
import pl.balazinski.jakub.takeyourpill.data.Pill;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.manager.PillManager;

/**
 * Created by Kuba on 08.12.2015.
 */
public class PillActivity extends AppCompatActivity {

    public static final String ID = "id";
    public static final int INVALID_ID = -1;

    private enum State {NEW, EDIT}


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
    private int mCount, mTaken;
    private int mPosition = -1;
    private State state;
    private Pill mPill;
    private boolean mNewPhoto;

    private Uri ivImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        pillCountNumberPicker = (NumberPicker) findViewById(R.id.pill_number_picker);
        pillTakenNumberPicker = (NumberPicker) findViewById(R.id.pill_taken_picker);

        Intent intent = getIntent();
        if(extras==null){
            state = State.NEW;
            setView(state);
        }else{
            state  = State.EDIT;
            mPosition = extras.getInt("pos");
            if(mPosition!=-1) {
                mPill = PillManager.getInstance().getPill(mPosition);
                setView(state);
                ivImage = mPill.getPhoto();
            }
        }

        Window window = getWindow();
// clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
// add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
// finally change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.notification_bar));

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbarPill);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

    }

    private void setView(State state) {
        pillCountNumberPicker.setMinValue(1);
        pillCountNumberPicker.setMaxValue(100);
        pillCountNumberPicker.setWrapSelectorWheel(true);

        pillTakenNumberPicker.setMinValue(1);
        pillTakenNumberPicker.setMaxValue(10);
        pillTakenNumberPicker.setWrapSelectorWheel(true);

        if(state == State.NEW){
            pillCountNumberPicker.setValue(1);
            pillTakenNumberPicker.setValue(1);
            addPill.setText("SAVE");
            addPhoto.setText("ADD PHOTO");
        }else if(state == State.EDIT){
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
        if(pillNameEditText.getText()!=null)
            mName = pillNameEditText.getText().toString();
        if(pillDescEditText.getText()!=null)
            mDesc = pillDescEditText.getText().toString();
        mCount = pillCountNumberPicker.getValue();
        mTaken = pillTakenNumberPicker.getValue();

        String strNameEditText = pillNameEditText.getText().toString();
        String strDescEditText = pillDescEditText.getText().toString();

        if (TextUtils.isEmpty(strNameEditText))
            pillNameEditText.setError("Set name to your pill");
        else if (TextUtils.isEmpty(strDescEditText)) {
            pillDescEditText.setError("Set description to your pill");
        } else if (state == State.EDIT) {
            mPill.setName(mName);
            mPill.setDescription(mDesc);
            mPill.setPillsCount(mCount);
            mPill.setPillsTaken(mTaken);
            mPill.setPhoto(getIvImage());
            finish();
        }
        else if (state == State.NEW) {
            Pill pill = new Pill(mName, mDesc, mCount, mTaken, getIvImage());
            PillManager.getInstance().addPill(pill);
            finish();
        }

    }

    @OnClick(R.id.add_photo)
    public void addPhoto(View view) {
        selectImage();
    }

    private void selectImage() {
        final CharSequence[] items = {"Take Photo", "Choose from Library", "Cancel"};

        AlertDialog.Builder builder = new AlertDialog.Builder(PillActivity.this);
        builder.setTitle("Add Photo!");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Take Photo")) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, Constans.REQUEST_CAMERA);
                } else if (items[item].equals("Choose from Library")) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, "Select File"), Constans.SELECT_FILE);
                } else if (items[item].equals("Cancel")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //ivImage = new B();
        if (resultCode == RESULT_OK) {
            if (requestCode == Constans.REQUEST_CAMERA) {
                Bitmap thumbnail = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, bytes);
                File destination = new File(Environment.getExternalStorageDirectory(),
                        System.currentTimeMillis() + ".jpg");
                FileOutputStream fo;
                try {
                    destination.createNewFile();
                    fo = new FileOutputStream(destination);
                    fo.write(bytes.toByteArray());
                    fo.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ivImage = Uri.fromFile(destination);
                Log.i("PATH", String.valueOf(destination.getAbsolutePath()));

            } else if (requestCode == Constans.SELECT_FILE) {
                Uri selectedImageUri = data.getData();
 /*               String[] projection = {MediaStore.MediaColumns.DATA};
                CursorLoader cursorLoader = new CursorLoader(this, selectedImageUri, projection, null, null,
                        null);
                Cursor cursor = cursorLoader.loadInBackground();
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                String selectedImagePath = cursor.getString(column_index);
                Bitmap bm;
                BitmapFactory.Options options = new BitmapFactory.Options();

                options.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(selectedImagePath, options);
                final int REQUIRED_SIZE = 200;
                int scale = 1;
                while (options.outWidth / scale / 2 >= REQUIRED_SIZE
                        && options.outHeight / scale / 2 >= REQUIRED_SIZE)
                    scale *= 2;
                options.inSampleSize = scale;
                options.inJustDecodeBounds = false;
                bm = BitmapFactory.decodeFile(selectedImagePath, options);*/
                setIvImage(selectedImageUri);
            }
        }
    }

    public Uri getIvImage() {
        return ivImage;
    }

    public void setIvImage(Uri ivImage) {
        this.ivImage = ivImage;
    }
}