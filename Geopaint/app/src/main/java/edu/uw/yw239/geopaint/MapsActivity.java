package edu.uw.yw239.geopaint;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.xdty.preference.colorpicker.ColorPickerDialog;
import org.xdty.preference.colorpicker.ColorPickerSwatch;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String[] RequiredPermissions = new String[] {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE    };

    public Activity thisActivity = MapsActivity.this;

    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private int permissionCheck;

    private Polyline curPolyline;
    private List<Polyline> listOfPolylines = new LinkedList<>();
    private boolean isPenUp;
    private Float size;
    private int curColor = -1;
    private String fileName;
    private File file;

    private static final int LOCATION_REQUEST_CODE = 1;
    private static final int PERMISSION_REQUEST_CODE = 10;

    public static final String ACTIVITY_NAME = "maps activity";

    public static final String PREF_PEN_SIZE = "pref_pen_size";

    public static final String DEFAULT_SIZE = "18";

    public static final String PREF_PEN_COLOR = "pref_pen_color";

    public static final String PREF_FILE_NAME = "pref_file_name";

    public static final String DEFAULT_FILE_NAME = "myDrawings";

    public static final String CREATE_NEW_FILE = "create_new_file";

    public static final String UPDATE_FILE_KEY = "update_file_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        // set the status of the pen
        isPenUp = true;
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        // get the value of size form sharedPreference
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        size = Float.valueOf(sharedPref.getString(PREF_PEN_SIZE, DEFAULT_SIZE));
        fileName = sharedPref.getString(PREF_FILE_NAME, DEFAULT_FILE_NAME);

        if(isAllPermissionGranted()) {
            Initialize();
        }
        else { //if we're missing permission.
            askForPermission();
        }
    }

    private boolean isAllPermissionGranted() {
        for (String permission : RequiredPermissions) {
            if (ContextCompat.checkSelfPermission(MapsActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    private void askForPermission() {
        for (String permission : RequiredPermissions) {
            if (ContextCompat.checkSelfPermission(MapsActivity.this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MapsActivity.this, new String[] { permission }, PERMISSION_REQUEST_CODE);
                return;
            }
        }
    }

    private void Initialize() {
        Toast.makeText(this, getResources().getString(R.string.file_location_notification) + " " + fileName, Toast.LENGTH_LONG).show();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }
        mGoogleApiClient.connect();

        // instantiate the file that is saved privately on external storage
        //file = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), MapsActivity.DEFAULT_FILE_NAME + ".geojson");
    }

    @Override
    protected void onStart() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }

        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){

        // set the status of option menu items
        if(isPenUp == true) {
            menu.findItem(R.id.pen_status_to_lower).setVisible(true);
            menu.findItem(R.id.pen_status_to_raise).setVisible(false);
        }
        else {
            menu.findItem(R.id.pen_status_to_lower).setVisible(false);
            menu.findItem(R.id.pen_status_to_raise).setVisible(true);
        }

        // set up the share intent
        MenuItem item = menu.findItem(R.id.menu_item_share);
        ShareActionProvider shareProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        Intent myShareIntent = new Intent(Intent.ACTION_SEND);
        myShareIntent.setType("text/plain");

        String filePath = getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS) + "/" + fileName + ".geojson";
        Uri fileUri = Uri.fromFile(new File(filePath));
        myShareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
        shareProvider.setShareIntent(myShareIntent);

        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        MenuItem mItem;

        switch(item.getItemId()){
            case R.id.pen_status_to_raise:
                // set the status of the pen to up
                isPenUp = true;

                break;

            case R.id.pen_status_to_lower:
                // set the status of the pen to down and instantiate a new Polyline for the current polyline
                isPenUp = false;
                curPolyline = mMap.addPolyline(new PolylineOptions().width(size).color(curColor));
                listOfPolylines.add(curPolyline);

                break;

            case R.id.settings:
                Intent intent = new Intent(MapsActivity.this, SettingsActivity.class);
                intent.putExtra(SettingsActivity.PARENT_ACTIVITY_KEY, ACTIVITY_NAME);
                startActivity(intent);
                
                break;
            
            case R.id.pen_color:
                pickColor(item);
                break;

            default:
                return super.onOptionsItemSelected(item);        
        }
        
        return true;
    }

    public void pickColor(final MenuItem item) {

        // change color
        if (curColor == -1) {
            curColor = ContextCompat.getColor(this, R.color.flamingo);
        }

        int[] mColors = getResources().getIntArray(R.array.default_rainbow);

        ColorPickerDialog dialog = ColorPickerDialog.newInstance(R.string.color_picker_default_title,
                mColors,
                curColor,
                5, // Number of columns
                ColorPickerDialog.SIZE_SMALL);

        dialog.setOnColorSelectedListener(new ColorPickerSwatch.OnColorSelectedListener() {

            @Override
            public void onColorSelected(int color) {
                // take action if the newly selected color is different from the current color
                if(curColor != color){

                    // update the current color
                    curColor = color;

                    // add the current polyline to the list and instantiate a new polyline
                    curPolyline = mMap.addPolyline(new PolylineOptions().color(curColor).width(size));
                    listOfPolylines.add(curPolyline);

                    // add the current
                    SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(MapsActivity.this);
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putInt(PREF_PEN_COLOR, curColor);
                    editor.commit();
                }

            }

        });

        dialog.show(getFragmentManager(), "color_dialog_test");
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick()
            {
                if (mLocation != null) {
                    mMap.moveCamera(CameraUpdateFactory.newLatLng(
                          new LatLng(mLocation.getLatitude(), mLocation.getLongitude())));
                }
                return true;
            }
        });

        // instantiate the current location
        curPolyline = mMap.addPolyline(new PolylineOptions().width(size).color(curColor));
        listOfPolylines.add(curPolyline);

        // Set some UI settings
        UiSettings mSetting = mMap.getUiSettings();
        mSetting.setCompassEnabled(true);
        mSetting.setZoomControlsEnabled(true);

        LatLng UW = new LatLng(47.6553, -122.3078);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(UW));
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //have permission, can go ahead and do stuff
            if (mMap != null)
            {
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            //request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        permissionCheck = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if(permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //have permission, can go ahead and do stuff

            //assumes location settings enabled
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
        else {
            //request permission
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLocation = location;

        // get the lat and lng of the current location
        LatLng curLocation = new LatLng(location.getLatitude(), location.getLongitude());

        // check the status of the pen
        if(!isPenUp) {
            // the pen is down, do the stuff
            // add the current location to the current polyline
            List<LatLng> listOfPositions = curPolyline.getPoints();
            listOfPositions.add(curLocation);
            curPolyline.setPoints(listOfPositions);

            // parse the list to string
            String strToSave = GeoJsonConverter.convertToGeoJson(listOfPolylines);
            Intent intent = new Intent(MapsActivity.this, MapSavingService.class);
            String[] nameAndContent = new String[2];
            nameAndContent[0] = fileName;
            nameAndContent[1] = strToSave;
            intent.putExtra(UPDATE_FILE_KEY, nameAndContent);
            startService(intent);
        }

        // move the map marker to the current location
        //mMap.addMarker(new MarkerOptions().position(curLocation).title("Current location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(curLocation));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode){
            case LOCATION_REQUEST_CODE: { //if asked for location
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    onConnected(null);
                }
            }
            case MapSavingService.WRITE_REQUEST_CODE:{ //if asked for write
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    onLocationChanged(mLocation);
                }
            }
            case PERMISSION_REQUEST_CODE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (isAllPermissionGranted()) {
                        Initialize();
                    } else {
                        askForPermission();
                    }
                } else if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    finish();
                }
                return;
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(PREF_PEN_SIZE)) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            Float newSize = Float.valueOf(sharedPref.getString(PREF_PEN_SIZE, DEFAULT_SIZE));

            // Update size when go back from settings activity
            if (mMap != null && newSize != size) {
                size = newSize;

                curPolyline = mMap.addPolyline(new PolylineOptions().width(size).color(curColor));
                listOfPolylines.add(curPolyline);
            }
        }

        if(key.equals(PREF_FILE_NAME)){
            // file name has been changed, do something
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            String newName = sharedPref.getString(PREF_FILE_NAME, DEFAULT_FILE_NAME);

            if(newName != fileName){
                // save the current file and create a new file by using service
                Intent intent = new Intent(MapsActivity.this, MapSavingService.class);
                intent.putExtra(CREATE_NEW_FILE, newName);
                startService(intent);

                // change the current file name
                fileName = newName;

                // clear the content in the listOfPolylines and current polyline
                curPolyline = mMap.addPolyline(new PolylineOptions().width(size).color(curColor));
                listOfPolylines = new LinkedList<>();
                listOfPolylines.add(curPolyline);

                // if the pen is down add the new current polyline to the list
                if(!isPenUp) {
                    listOfPolylines.add(curPolyline);
                }
            }

        }
    }


}
