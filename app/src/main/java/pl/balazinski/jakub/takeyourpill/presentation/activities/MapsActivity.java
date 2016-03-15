package pl.balazinski.jakub.takeyourpill.presentation.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.balazinski.jakub.takeyourpill.R;
import pl.balazinski.jakub.takeyourpill.utilities.map.Place;
import pl.balazinski.jakub.takeyourpill.utilities.map.PlacesService;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

/**
 * Activity that creates map fragment
 */
public class MapsActivity extends AppCompatActivity implements LocationListener {

    private final String TAG = getClass().getSimpleName();
    private static final String API_KEY = "AIzaSyD7P7G-ebIiLwuxlFoY2xR5BitJnljRjjk";

    @Bind(R.id.mapToolbar)
    public Toolbar toolbar;

    private GoogleMap mMap;
    private String mPlaces;
    private Location mLocation;
    private LocationManager mLocationManager;
    private OutputProvider mOutputProvider;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);
        mOutputProvider = new OutputProvider(this);

        setupContent();
        setupView();
        mPlaces = getResources().getString(R.string.place);
        checkEnabled();

    }

    private void setupContent() {
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.maps))
                .getMap();
        mMap.setMyLocationEnabled(true);

    }

    private void setupView(){
        Window window = getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.notification_bar));

        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @OnClick(R.id.toolbar_refresh_button)
    public void onRefreshClick(View v) {
        checkEnabled();
        mOutputProvider.displayLog(TAG, "onclick ckiled!");
    }

    private void checkEnabled() {
        //TODO Repair connection

        mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean isInternetOn = isNetworkConnected();

        if(!isInternetOn) createNetErrorDialog();
        else currentLocation();

        boolean isGpsOn = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkOn = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!isGpsOn && (!isNetworkOn||!isInternetOn)) createGPSErrorDialog();

        if((isGpsOn&&isInternetOn) || (!isGpsOn&&isInternetOn)) currentLocation();
    }



    private boolean isNetworkConnected() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo() != null;
    }

    protected void createGPSErrorDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_gps_error_message))
                .setTitle(getString(R.string.dialog_gps_error_title))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.settings),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        }
                )
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MapsActivity.this.finish();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }

    protected void createNetErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getString(R.string.dialog_internet_error_message))
                .setTitle(getString(R.string.dialog_internet_error_title))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.settings),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(i);
                            }
                        }
                )
                .setNegativeButton(getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                MapsActivity.this.finish();
                            }
                        }
                );
        AlertDialog alert = builder.create();
        alert.show();
    }


    private void currentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        mOutputProvider.displayLog(TAG, "Current mLocation inside");

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Getting the name of the best provider
        String provider = mLocationManager.getBestProvider(criteria, true);
        // Getting Current Location
        //Location mLocation = mLocationManager.getLastKnownLocation(provider);
        Location location = getMyLocation();

        mOutputProvider.displayLog(TAG, "map getmylocation" + mMap.getMyLocation());

        if (mMap.getMyLocation() != null) {
            location = mMap.getMyLocation();
            this.mLocation = location;
            new GetPlaces(MapsActivity.this, mPlaces).execute();
        }
        if (location == null) {
            mOutputProvider.displayLog(TAG, "mLocation ==null");
            onLocationChanged(location);
            mLocationManager.requestLocationUpdates(provider, 20000, 0, this);
        } else {
            this.mLocation = location;
            new GetPlaces(MapsActivity.this, mPlaces).execute();
        }

    }

    private Location getMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null)
            myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = locationManager.getBestProvider(criteria, true);
            myLocation = locationManager.getLastKnownLocation(provider);
        }

        return myLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        mOutputProvider.displayLog(TAG, "mLocation update : " + location);

        this.mLocation = location;

        if (location == null)
            return;

        if (this.mLocation.getLatitude() == location.getLatitude() && this.mLocation.getLatitude() == location.getLongitude()) {
            mOutputProvider.displayLog(TAG, "mLocation not changed.");
            return;
        }

        this.mLocation.setLatitude(location.getLatitude());
        this.mLocation.setLongitude(location.getLongitude());
        mOutputProvider.displayLog(TAG, "Location changed to (" + this.mLocation.getLatitude() + ", " + this.mLocation.getLatitude() + ")");
        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            //   ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},REQ);
            return;
        }
        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkEnabled();
    }

    private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {

        private ProgressDialog mDialog;
        private Context mContext;
        private String places;

        public GetPlaces(Context context, String places) {
            this.mContext = context;
            this.places = places;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mDialog = new ProgressDialog(mContext);
            mDialog.setCancelable(false);
            mDialog.setMessage(getString(R.string.loading));
            mDialog.isIndeterminate();
            mDialog.show();
        }

        @Override
        protected ArrayList<Place> doInBackground(Void... arg0) {
            PlacesService service = new PlacesService(API_KEY);
            ArrayList<Place> findPlaces = service.findPlaces(mLocation.getLatitude(),
                    mLocation.getLongitude(), places);
            for (int i = 0; i < findPlaces.size(); i++) {
                Place placeDetail = findPlaces.get(i);
                mOutputProvider.displayLog(TAG, "mPlaces : " + placeDetail.getName());
            }
            return findPlaces;
        }

        @Override
        protected void onPostExecute(ArrayList<Place> result) {
            super.onPostExecute(result);
            if (mDialog.isShowing()) {
                mDialog.dismiss();
            }
            for (int i = 0; i < result.size(); i++) {
                mMap.addMarker(new MarkerOptions()
                        .title(result.get(i).getName())
                        .position(
                                new LatLng(result.get(i).getLatitude(), result
                                        .get(i).getLongitude()))
                        .icon(BitmapDescriptorFactory
                                .fromResource(R.drawable.pin))
                        .snippet(result.get(i).getVicinity()));
            }

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(mLocation.getLatitude(), mLocation.getLongitude())) // Sets the center of the map to
                            // Mountain View
                    .zoom(14) // Sets the zoom
                    .tilt(30) // Sets the tilt of the camera to 30 degrees
                    .build(); // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }

    }

}
