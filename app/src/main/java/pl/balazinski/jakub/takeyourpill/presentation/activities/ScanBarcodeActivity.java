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
import pl.balazinski.jakub.takeyourpill.data.Constants;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;
import pl.balazinski.jakub.takeyourpill.utilities.camera.CameraPreview;

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
        setContentView(R.layout.activity_scan_barcode);
        ButterKnife.bind(this);
        outputProvider = new OutputProvider(this);

        setupContent();
        setupView();
        setPreview();
    }

    private void setupContent() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        autoFocusHandler = new Handler();
        int i = getCameraInformation();
        mCamera = getCameraInstance(i);
        if (mCamera == null) {
            outputProvider.displayShortToast(getString(R.string.toast_no_rear_camera_available));
            finish();
        }
        /* Instance barcode scanner */
        scanner = new ImageScanner();
        scanner.setConfig(0, Config.X_DENSITY, 3);
        scanner.setConfig(0, Config.Y_DENSITY, 3);
    }

    private void setupView() {
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.notification_bar));

        scanText.setText(getString(R.string.scan_barcode_click));

        if (Constants.VERSION >= Build.VERSION_CODES.M) {
            addBarcodeManually.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
            addPillManually.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_background));
        } else {
            addBarcodeManually.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
            addPillManually.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.button_background));
        }

    }

    private void setPreview() {
        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);
    }

    @OnClick(R.id.scanText)
    public void onScanTextClick(View v) {
        outputProvider.displayLog(TAG, "outside clicked");
        if (isClicked) {
            setPreview();
            isClicked = false;
        }
        scanText.setText(getString(R.string.scanning));

    }

    @OnClick(R.id.add_barcode_manually)
    public void onAddBarcodeClick(View v) {
        Intent intent = new Intent(getApplicationContext(), ScanBarcodeChooserActivity.class);
        intent.putExtra(Constants.ADD_BARCODE_MANUALLY, true);
        startActivity(intent);
    }

    @OnClick(R.id.add_pill_manually)
    public void onAddPillClick(View v) {
        startActivity(new Intent(getApplicationContext(), PillActivity.class));
    }

    @Override
    protected void onResume() {
        mPreview.setVisibility(View.VISIBLE);
        if (this.mCamera == null) {
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
            e.printStackTrace();
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
            scanText.setText(getString(R.string.scan_barcode_click));
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
                scanText.setText(getString(R.string.scanning));
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
                    scanText.setText(getString(R.string.barcode_result));
                    scanText.append(sym.getData());
                    outputProvider.displayLog(TAG, "barcode = " + sym.getData());
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra(Constants.SCAN_BARCODE_RESULT, sym.getData());
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
