package com.example.gus1430.mymapsapp_sg;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private EditText locationSearch;
    private LocationManager locationManager;
    private Location myLocation;

    private boolean gotmyLocationOneTime;
    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private boolean notTrackingMyLocation = false;

    private static final long MIN_TIME_BW_UPDATES = 1000 * 5;
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 0.0f;
    private static final int MY_LOC_ZOOM_FACTOR = 17;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        //Add a marker on the map that shows your place of birth
        //displays the message "born here" when tapped
        LatLng portland = new LatLng(46, -123);
        mMap.addMarker(new MarkerOptions().position(portland).title("born here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(portland));

        /*if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed FINE permission check");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 2);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("MyMapsApp", "Failed COARSE permission check");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 2);
        }

        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
                || (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            mMap.setMyLocationEnabled(true);
        } */

        locationSearch = (EditText) findViewById(R.id.editText_addr);

        gotmyLocationOneTime = false;
        getLocation();

    }

    public void onSearch(View v){
        String location = locationSearch.getText().toString();
        List<Address> addressList = null;

        //Use locationManager for user location info
        LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = service.getBestProvider(criteria, false);

        Log.d("MyMapsApp", "OnSearch: location = " + location);
        Log.d("MyMapsApp", "OnSearch: provider = " + provider);

        LatLng userLocation = null;
        try{
            //Check the last known location, need to specifically list the provider
            //(network or gps)
            if(locationManager != null){
                if ((myLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "OnSearch: using NETWORK PROVIDER userLocation is: " + myLocation.getLatitude()+ " " + myLocation.getLongitude());
                    Toast.makeText(this, "Userloc: " + myLocation.getLatitude()+ " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                }
                else if((myLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)) != null){
                    userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                    Log.d("MyMapsApp", "OnSearch: using GPS PROVIDER userLocation is: " + myLocation.getLatitude()+ " " + myLocation.getLongitude());
                    Toast.makeText(this, "Userloc: " + myLocation.getLatitude()+ " " + myLocation.getLongitude(), Toast.LENGTH_SHORT).show();
                }
                else{
                    Log.d("MyMapsApp", "OnSearch: myLocation is null");
                }
            }

        }
        catch (SecurityException | IllegalArgumentException e){
            Log.d("MyMapsApp", "OnSearch: Exception on getLastKnownLocation");
            Toast.makeText(this, "Exception on getLastKnownLocation", Toast.LENGTH_SHORT).show();
        }

        if(!location.matches("")){
            //Create Geocoder
            Geocoder geocoder = new Geocoder(this, Locale.US);

            try{
                //get a list of addresses
                addressList = geocoder.getFromLocationName(location, 100,
                        userLocation.latitude - (5.0 / 60),
                        userLocation.longitude - (5.0 / 60),
                        userLocation.latitude + (5.0 / 60),
                        userLocation.longitude + (5.0 / 60));

                Log.d("MyMapsApp", "Created AddressList");
                Log.d("MyMapsApp", "Address list size: " + addressList.size());

            }catch(IOException e){
                e.printStackTrace();
            }

            if(!addressList.isEmpty()){
                Log.d("MyMapsApp", "Address list size: " + addressList.size());

                for(int i = 0; i < addressList.size(); i++){
                    Address address = addressList.get(i);
                    LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                    mMap.addMarker(new MarkerOptions().position(latLng).title(i + ": " + address.getSubThoroughfare() + " " + address.getThoroughfare() + " " + address.getPostalCode()));
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                }
            }
        }

    }

    //method getLocation to place a marker at current location
    public void getLocation(){
        try{
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            //get GPS status
            //isProviderEnabled returns true if user has enabled GPS on phone
            isGPSEnabled = locationManager.isProviderEnabled(locationManager.GPS_PROVIDER);
            if(isGPSEnabled)
                Log.d("MyMapsApp", "getLocation: GPS Enabled");

            isNetworkEnabled = locationManager.isProviderEnabled(locationManager.NETWORK_PROVIDER);
            if(isNetworkEnabled)
                Log.d("MyMapsApp", "getLocation: Network Enabled");

            if(!isGPSEnabled && !isNetworkEnabled){
                Log.d("MyMapsApp", "getLocation: No provider is enabled");
            }
            else {
                if(isNetworkEnabled){
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                }
                if(isGPSEnabled){

                    //launch locationListenerGPS
                    if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerGPS);

                }

            }
        }catch(Exception e){
            Log.d("MyMapsApp", "getLocation: Caught an exception");
            e.printStackTrace();
        }
    }

    //LocationListener is an anonymous inner class
    //Setup for callbacks from the requestLocationUpdates
    LocationListener locationListenerNetwork = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            dropAMarker(LocationManager.NETWORK_PROVIDER);
            //Check if doing one time via onMapReady, if so, remove update to both GPS and network
            if(gotmyLocationOneTime == false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerGPS);
                gotmyLocationOneTime = true;
            }
            else{
                //If here, then tracking, so relaunch request for network
                if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.d("MyMapsApp", "locationListener: status change");
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    LocationListener locationListenerGPS = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            //if doing one time remove updates to both grps and network
            //else do nothing
            dropAMarker(LocationManager.GPS_PROVIDER);
            //Check if doing one time via onMapReady, if so, remove update to both GPS and network
            if(gotmyLocationOneTime == false){
                locationManager.removeUpdates(this);
                locationManager.removeUpdates(locationListenerNetwork);
                gotmyLocationOneTime = true;
            }
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            switch(status){
                case LocationProvider.AVAILABLE:
                    Log.d("MyMapsApp", "LocationListenerGPS: LocationProvider is Available");
                    Toast.makeText(MapsActivity.this, "LocationProvider is Available", Toast.LENGTH_SHORT).show();
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    //enable network updates
                    Log.d("MyMapsApp", "LocationListenerGPS: LocationProvider out of service");
                    Toast.makeText(MapsActivity.this, "LocationProvider out of service", Toast.LENGTH_SHORT).show();
                    if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        return;
                    }
                    if(!notTrackingMyLocation){
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    //enable GPS and Network
                    if(!notTrackingMyLocation){
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }

                    break;
                default:
                    //enable GPS amd Network
                    if(!notTrackingMyLocation){
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListenerNetwork);
                    }

                    break;
            }


        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void dropAMarker(String provider){
        if(locationManager != null){
            if(ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                return;
            myLocation = locationManager.getLastKnownLocation(provider);
        }

        LatLng userLocation = null;
        if(myLocation == null){
            Toast.makeText(this, "myLocation is null", Toast.LENGTH_SHORT).show();

        }
        else{
            userLocation = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            CameraUpdate update = CameraUpdateFactory.newLatLngZoom(userLocation, MY_LOC_ZOOM_FACTOR);
            if(provider == LocationManager.GPS_PROVIDER){
                //add circle for the marker with 2 outer rings (red)
                mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.RED));
                mMap.addCircle(new CircleOptions().center(userLocation).radius(2).strokeColor(Color.RED).strokeWidth(2).fillColor(Color.TRANSPARENT));
            }
            else if(provider == LocationManager.NETWORK_PROVIDER){
                //add circle for the marker with 2 outer rings (blue)
                mMap.addCircle(new CircleOptions().center(userLocation).radius(1).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.BLUE));
                mMap.addCircle(new CircleOptions().center(userLocation).radius(2).strokeColor(Color.BLUE).strokeWidth(2).fillColor(Color.TRANSPARENT));

            }
            mMap.animateCamera(update);
        }
    }


    public void trackMyLocation(View view){
        //kick off the location tracker using getLocation to start the locationListeners
        if(notTrackingMyLocation){
            getLocation();
            notTrackingMyLocation = false;
        }
        else{
            locationManager.removeUpdates(locationListenerNetwork);
            locationManager.removeUpdates(locationListenerGPS);
            //removeUpdates for both network and GPS;
            notTrackingMyLocation = true;
        }
    }
    //Add View Button and method (changeView) to switch between
    //satellite and map views
    public void changeView(View view ){
        if(mMap.getMapType() == 1){
            mMap.setMapType(2);
        }
        else if(mMap.getMapType() == 2){
            mMap.setMapType(1);
        }
    }

    public void clearMarkers(View view){
        mMap.clear();
    }
}
