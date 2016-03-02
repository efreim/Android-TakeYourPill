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
import android.util.Log;
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
import pl.balazinski.jakub.takeyourpill.data.map.Place;
import pl.balazinski.jakub.takeyourpill.data.map.PlacesService;
import pl.balazinski.jakub.takeyourpill.presentation.OutputProvider;

/**
 * Activity that creates map fragment
 */
public class MapsActivity extends AppCompatActivity implements LocationListener {
    //private GoogleMap map;
    @Bind(R.id.mapToolbar)
    public Toolbar toolbar;

    private final String TAG = getClass().getSimpleName();
    private GoogleMap mMap;
    private String places;
    private Location loc;
    private LocationManager locationManager;
    private OutputProvider outputProvider;

    private static final String API_KEY = "AIzaSyD7P7G-ebIiLwuxlFoY2xR5BitJnljRjjk";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        ButterKnife.bind(this);


        initCompo();
        places = getResources().getString(R.string.place);


        //Setting up notification bar color
        Window window = getWindow();
        // clear FLAG_TRANSLUCENT_STATUS flag:
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        // add FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS flag to the window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        // change the color
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.notification_bar));

        //Setting up toolbar
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        checkEnabled();

        //   String PLACES_SEARCH_URL = "https://maps.googleapis.com/maps/api/place/search/json?location=" + latitude + "," + longtitude + "&radius=100&sensor=true&key=" + API_KEY;
    }

    @OnClick(R.id.toolbar_refresh_button)
    public void onRefreshClick(View v) {
        checkEnabled();
        outputProvider.displayLog(TAG, "onclick ckiled!");
    }

    private void initCompo() {
        outputProvider = new OutputProvider(this);
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.maps))
                .getMap();
        mMap.setMyLocationEnabled(true);

    }

    @Override
    protected void onResume() {
        super.onResume();
        checkEnabled();
    }

    private void checkEnabled() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        boolean isInternetOn = isNetworkConnected();
        if(!isInternetOn)
            createNetErrorDialog();
        else
            currentLocation();


        boolean isGpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkOn = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(!isGpsOn && (!isNetworkOn||!isInternetOn))
            createGPSErrorDialog();


        if((isGpsOn&&isInternetOn) || (!isGpsOn&&isInternetOn))
            currentLocation();

    }



    private boolean isNetworkConnected() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null;
    }

    protected void createGPSErrorDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("You need a gps connection to use this application. Please turn on GPS in Settings.")
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                startActivity(intent);
                            }
                        }
                )
                .setNegativeButton("Cancel",
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
        builder.setMessage("You need a network connection to use this application. Please turn on mobile network or Wi-Fi in Settings.")
                .setTitle("Unable to connect")
                .setCancelable(false)
                .setPositiveButton("Settings",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Intent i = new Intent(Settings.ACTION_WIRELESS_SETTINGS);
                                startActivity(i);
                            }
                        }
                )
                .setNegativeButton("Cancel",
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
        outputProvider.displayLog(TAG, "Current location inside");

        // Creating a criteria object to retrieve provider
        Criteria criteria = new Criteria();
        // Getting the name of the best provider
        String provider = locationManager.getBestProvider(criteria, true);
        // Getting Current Location
        //Location location = locationManager.getLastKnownLocation(provider);
        Location location = getMyLocation();

        outputProvider.displayLog(TAG, "map getmylocation" + mMap.getMyLocation());

        if (mMap.getMyLocation() != null) {
            location = mMap.getMyLocation();
            loc = location;
            new GetPlaces(MapsActivity.this, places).execute();
        }
        if (location == null) {
            outputProvider.displayLog(TAG, "location ==null");
            onLocationChanged(location);
            locationManager.requestLocationUpdates(provider, 20000, 0, this);
        } else {
            loc = location;
            new GetPlaces(MapsActivity.this, places).execute();
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
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location myLocation = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (myLocation == null)
            myLocation = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = lm.getBestProvider(criteria, true);
            myLocation = lm.getLastKnownLocation(provider);
        }

        return myLocation;
    }

    @Override
    public void onLocationChanged(Location location) {
        outputProvider.displayLog(TAG, "location update : " + location);

        loc = location;

        if (location == null)
            return;

        if (loc.getLatitude() == location.getLatitude() && loc.getLatitude() == location.getLongitude()) {
            outputProvider.displayLog(TAG, "location not changed.");
            return;
        }

        loc.setLatitude(location.getLatitude());
        loc.setLongitude(location.getLongitude());
        outputProvider.displayLog(TAG, "Location changed to (" + loc.getLatitude() + ", " + loc.getLatitude() + ")");
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
        locationManager.removeUpdates(this);
        // Ask fragment to get new data and update screen
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

    private class GetPlaces extends AsyncTask<Void, Void, ArrayList<Place>> {

        private ProgressDialog dialog;
        private Context context;
        private String places;

        public GetPlaces(Context context, String places) {
            this.context = context;
            this.places = places;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(context);
            dialog.setCancelable(false);
            dialog.setMessage("Loading..");
            dialog.isIndeterminate();
            dialog.show();
        }

        @Override
        protected ArrayList<Place> doInBackground(Void... arg0) {
            PlacesService service = new PlacesService(API_KEY);
            ArrayList<Place> findPlaces = service.findPlaces(loc.getLatitude(),
                    loc.getLongitude(), places);

            for (int i = 0; i < findPlaces.size(); i++) {

                Place placeDetail = findPlaces.get(i);
                outputProvider.displayLog(TAG, "places : " + placeDetail.getName());
            }
            return findPlaces;
        }

        @Override
        protected void onPostExecute(ArrayList<Place> result) {
            super.onPostExecute(result);
            if (dialog.isShowing()) {
                dialog.dismiss();
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
                    .target(new LatLng(loc.getLatitude(), loc.getLongitude())) // Sets the center of the map to
                            // Mountain View
                    .zoom(14) // Sets the zoom
                    .tilt(30) // Sets the tilt of the camera to 30 degrees
                    .build(); // Creates a CameraPosition from the builder
            mMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition));
        }

    }


}
