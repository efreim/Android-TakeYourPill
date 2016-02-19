package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;

import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.data.camera.CameraPreview;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

public class ScanBarcodeActivity extends Activity {
    private final String TAG = getClass().getSimpleName();

    private Camera mCamera;
    private CameraPreview mPreview;
    private Handler autoFocusHandler;
    private OutputProvider outputProvider;

    TextView scanText;
    Button scanButton;

    ImageScanner scanner;

    private boolean barcodeScanned = false;
    private boolean previewing = true;

    static {
        System.loadLibrary("iconv");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        outputProvider = new OutputProvider(this);

        setContentView(R.layout.activity_scan_barcode);

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

        mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);
        FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
        preview.addView(mPreview);

        scanText = (TextView) findViewById(R.id.scanText);

        scanButton = (Button) findViewById(R.id.ScanButton);

        scanButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (barcodeScanned) {
                    barcodeScanned = false;
                    scanText.setText("Scanning...");
                    mCamera.setPreviewCallback(previewCb);
                    mCamera.startPreview();
                    previewing = true;
                    mCamera.autoFocus(autoFocusCB);
                }
            }
        });
    }

    public void onPause() {
        super.onPause();
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
            previewing = false;
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }

    private Runnable doAutoFocus = new Runnable() {
        public void run() {
            if (previewing)
                mCamera.autoFocus(autoFocusCB);
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
                    barcodeScanned = true;
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
