package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.utilities.camera.CameraPreview;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

public class ScanBarcodeActivity extends AppCompatActivity {
    private final String TAG = getClass().getSimpleName();

    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private OutputProvider outputProvider;

    ImageScanner scanner;

    private boolean isClicked = true;
    private boolean previewing = true;

    @Bind(R.id.scanText)
    TextView scanText;
    @Bind(R.id.add_barcode_manually)
    Button addBarcodeManually;
    @Bind(R.id.add_pill_manually)
    Button addPillManually;
    static {
        System.loadLibrary("iconv");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        outputProvider = new OutputProvider(this);

        setContentView(R.layout.activity_scan_barcode);
        ButterKnife.bind(this);

        //Setting up notification bar color
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.notification_bar));


        scanText.setText("Click to scan barcode!");
        // barcodeScanned=false;

        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            addBarcodeManually.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
            addPillManually.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
        } else {
            addBarcodeManually.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
            addPillManually.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        int i = getCameraInformation();
        mCamera = getCameraInstance(i);
        if (mCamera == null) {
            outputProvider.displayShortToast("No rear camera available.");
            finish();
        }
        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);

        setPreview();


    }

    private void setPreview() {
        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
    }

    @OnClick(R.id.scanText)
    public void onScanTextClick(View v) {
        outputProvider.displayLog(TAG, "outside clicked");
        if(isClicked) {
            setPreview();
            isClicked = false;
        }
        scanText.setText("Scanning...");

    }

    @OnClick(R.id.add_barcode_manually)
    public void onAddBarcodeClick(View v) {
        Intent intent = new Intent(getApplicationContext(), ScanBarcodeChooserActivity.class);
        intent.putExtra("SCAN_MANUALLY", true);
        startActivity(intent);
    }

    @OnClick(R.id.add_pill_manually)
    public void onAddPillClick(View v) {
        startActivity(new Intent(getApplicationContext(), PillActivity.class));
    }

    @Override
    protected void onResume() {
        mPreview.setVisibility(View.VISIBLE);
        if (this.mCamera == null){
            this.mCamera = getCameraInstance(0);
            isClicked = true;
        }
        super.onResume();
    }

    public void onPause() {
        super.onPause();
        mPreview.setVisibility(View.GONE);
        releaseCamera();
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public static Camera getCameraInstance(int i) {
        Camera c = null;
        try {
            //0 is rear camera, else camera is null and barcode cannot be scaned
            if (i == 0)
                c = Camera.open(0);
        } catch (Exception e) {
        }
        return c;
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

    private void releaseCamera() {
        if (mCamera != null) {
            scanText.setText("Click to scan barcode!");
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
            mPreview.getHolder().removeCallback(mPreview);
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing) {
                scanText.setText("Scanning...");
                mCamera.autoFocus(autoFocusCB);
            }
        }
    };

    Camera.PreviewCallback previewCb = new Camera.PreviewCallback() {
        public void onPreviewFrame(byte[] data, Camera camera) {
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size size = parameters.getPreviewSize();

            Image barcode = new Image(size.width, size.height, "Y800");
            barcode.setData(data);

            int result = scanner.scanImage(barcode);

            if (result != 0) {
                previewing = false;
                mCamera.setPreviewCallback(null);
                mCamera.stopPreview();

                SymbolSet syms = scanner.getResults();
                for (Symbol sym : syms) {
                    scanText.setText("barcode result " + sym.getData());
                    outputProvider.displayLog(TAG, "barcode = " + sym.getData());
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", sym.getData());
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
        }
    };

    // Mimic continuous auto-focusing
    Camera.AutoFocusCallback autoFocusCB = new Camera.AutoFocusCallback() {
        public void onAutoFocus(boolean success, Camera camera) {
            autoFocusHandler.postDelayed(doAutoFocus, 1000);
        }
    };
}
