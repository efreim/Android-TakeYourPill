package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.utilities.Constants;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseHelper;
import pl.balazinski.jakub.takeyourpill.data.database.DatabaseRepository;
import pl.balazinski.jakub.takeyourpill.data.database.OuterPillDatabase;
import pl.balazinski.jakub.takeyourpill.data.model.Pill;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

/**
 * Activity lets you add or edit pills
 */
public class PillActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {

    private final String TAG = getClass().getSimpleName();

    /**
     * State activity is entered in
     */
    private enum State {
        NEW, EDIT
    }

    //Setting up components for activity
    @Bind(R.id.toolbarPill)
    Toolbar toolbar;
    @Bind(R.id.pill_name)
    public AutoCompleteTextView pillNameEditText;
    @Bind(R.id.pill_desc)
    public EditText pillDescEditText;
    @Bind(R.id.pill_dose)
    public EditText pillDosageEditText;
    //Optional components
    @Bind(R.id.optional_layout)
    LinearLayout optionalLayout;
    @Bind(R.id.pill_count)
    EditText pillCountEditText;
    @Bind(R.id.active_substance_et)
    EditText pillActiveSubEditText;
    @Bind(R.id.pill_price)
    EditText pillPriceEditText;
    @Bind(R.id.pill_barcode)
    EditText pillBarcodeEditText;
    @Bind(R.id.add_photo)
    Button addPhoto;
    //@Bind(R.id.add_pill)
    //Button addPill;
    @Bind(R.id.toolbar_pill_add_button)
    ImageButton toolbarAddButton;

    private OuterPillDatabase mOuterPillDatabase;
    private OutputProvider mOutputProvider;
    private String mName;
    private State mState;
    private Pill mPill;
    private Uri mImageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pill);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        mOutputProvider = new OutputProvider(this);

        setupContent(extras);
        setupView();
        setupComponents(mState);

    }

    private void setupContent(Bundle extras) {

        /*
         * If extras are empty mState is new otherwise
         * mState is edit and edited pill must be loaded
         * from database.
         */
        if (extras == null) {
            mState = State.NEW;
        } else {
            mState = State.EDIT;
            Long mId = extras.getLong(Constants.EXTRA_LONG_ID);

            mPill = DatabaseRepository.getPillByID(this, mId);
            if (mPill == null)
                mOutputProvider.displayShortToast(getString(R.string.error_loading_pills));
            else {
                mImageUri = Uri.parse(mPill.getPhoto());
            }
        }
        /*
        Setting autocomplete text view
         */
        mOuterPillDatabase = new OuterPillDatabase(getApplicationContext());
        pillNameEditText = (AutoCompleteTextView) findViewById(R.id.pill_name);
        pillNameEditText.setOnItemClickListener(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, getPillNameList());
        pillNameEditText.setAdapter(adapter);
        pillNameEditText.setThreshold(2);
    }

    private void setupView() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.notification_bar));

        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (Constants.VERSION >= Build.VERSION_CODES.M) {
            //addPill.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
            addPhoto.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
        } else {
            //addPill.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
            addPhoto.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }
    }


    /**
     * Sets components to view.
     *
     * @param state lets method know to set up add or edit view
     */
    private void setupComponents(State state) {
        if (state == State.NEW) {
            if (Constants.VERSION >= Build.VERSION_CODES.M)
                toolbarAddButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_add_white_36dp));
            else
                toolbarAddButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.ic_add_white_36dp));
            //addPill.setText(getString(R.string.save));
            addPhoto.setText(getString(R.string.add_photo));
        } else if (state == State.EDIT) {
            //addPill.setText(getString(R.string.update));
            if (Constants.VERSION >= Build.VERSION_CODES.M)
                toolbarAddButton.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_done_white_36dp));
            else
                toolbarAddButton.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.ic_done_white_36dp));
            addPhoto.setText(getString(R.string.edit_photo));

            pillNameEditText.setText(mPill.getName());

            if (!mPill.getDescription().equals(""))
                pillDescEditText.setText(mPill.getDescription());
            if (mPill.getDosage() != -1)
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String pillDbName = (String) parent.getItemAtPosition(position);
        Cursor cursor = mOuterPillDatabase.getReadableDatabase()
                .rawQuery("SELECT * FROM " + Constants.OUTER_TABLE_NAME + " where nazwa = " + "\"" + pillDbName + "\"" + ";", null);

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
            long barcode = cursor.getLong(4);
            String price = cursor.getString(5);
            int dosage = -1;

            Uri uri = Uri.parse(Constants.DRAWABLE_PATH + R.drawable.pill_white_background);

            mPill = new Pill();
            mPill.setName(name);
            mPill.setDescription(description);
            mPill.setDosage(dosage);
            mPill.setPillsCount(count);
            mPill.setActiveSubstance(activeSubstance);
            mPill.setPrice(price);
            mPill.setBarcodeNumber(barcode);
            mPill.setPhoto(uri.toString());
            onChecked(true);

            setupComponents(State.EDIT);
            setupComponents(State.NEW);
        }

    }

    @OnClick(R.id.toolbar_pill_add_button)
    public void addPill(View view) {
        int mCount = -1;
        int mDosage = -1;
        long barcode = -1;
        String mDesc = "";
        String activeSubstance = "";
        String price = "";


        if (pillNameEditText.getText() != null)
            mName = pillNameEditText.getText().toString();
        if (pillDescEditText.getText() != null)
            mDesc = pillDescEditText.getText().toString();
        if (!pillDosageEditText.getText().toString().equals(""))
            mDosage = Integer.parseInt(pillDosageEditText.getText().toString());
        if (!pillCountEditText.getText().toString().equals(""))
            mCount = Integer.parseInt(pillCountEditText.getText().toString());
        if (pillActiveSubEditText.getText() != null)
            activeSubstance = pillActiveSubEditText.getText().toString();
        if (pillPriceEditText.getText() != null)
            price = pillPriceEditText.getText().toString();
        if (!pillBarcodeEditText.getText().toString().equals(""))
            barcode = Long.parseLong(pillBarcodeEditText.getText().toString());
        if (TextUtils.isEmpty(mName))
            pillNameEditText.setError(getString(R.string.pill_error_set_name));

        if (mState == State.NEW) {
            String path;
            if (getImageUri() != null)
                path = getImageUri().toString();
            else {
                Uri uri = Uri.parse(Constants.DRAWABLE_PATH + R.drawable.pill_white_background);
                path = uri.toString();
            }
            mPill = new Pill(mName, mDesc, mCount, mDosage, path, activeSubstance, price, barcode);
            DatabaseRepository.addPill(this, mPill);
            Intent i = new Intent(PillActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
        } else if (mState == State.EDIT) {
            mPill.setName(mName);
            mPill.setDescription(mDesc);
            mPill.setDosage(mDosage);
            mPill.setPillsCount(mCount);
            mPill.setActiveSubstance(activeSubstance);
            mPill.setPrice(price);
            mPill.setBarcodeNumber(barcode);
            mPill.setPhoto(getImageUri().toString());
            DatabaseHelper.getInstance(this).getPillDao().update(mPill);
            Intent i = new Intent(PillActivity.this, MainActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(i);
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
        final CharSequence[] items = {getString(R.string.dialog_take_photo), getString(R.string.dialog_choose_from_library), getString(R.string.cancel)};

        AlertDialog.Builder builder = new AlertDialog.Builder(PillActivity.this);
        builder.setTitle(getString(R.string.add_photo));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals(getString(R.string.dialog_take_photo))) {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, Constants.REQUEST_CAMERA);
                } else if (items[item].equals(getString(R.string.dialog_choose_from_library))) {
                    Intent intent = new Intent(
                            Intent.ACTION_PICK,
                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(
                            Intent.createChooser(intent, getString(R.string.dialog_select_file)), Constants.SELECT_FILE);
                } else if (items[item].equals(getString(R.string.cancel))) {
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
                mImageUri = Uri.fromFile(destination);

            } else if (requestCode == Constants.SELECT_FILE) {
                Uri selectedImageUri = data.getData();
                setImageUri(selectedImageUri);
            }
        }
    }

    public Uri getImageUri() {
        return mImageUri;
    }

    public void setImageUri(Uri mImageUri) {
        this.mImageUri = mImageUri;
    }

    public List<String> getPillNameList() {
        List<String> arrayList = new ArrayList<>();
        Cursor cursor = mOuterPillDatabase.getReadableDatabase()
                .rawQuery("SELECT nazwa FROM " + Constants.OUTER_TABLE_NAME + ";", null);

        if (cursor.getCount() != 0) {
            while (cursor.moveToNext()) {
                arrayList.add(cursor.getString(0));

            }
            cursor.close();
        }
        return arrayList;
    }
}