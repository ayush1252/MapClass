package com.example.ayush.ninjasmymap;

import android.Manifest;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.GetChars;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap;
    Marker marker;
    Marker originmarker;
    Marker destinationmarker;
    EditText origin;
    EditText destination;
    Button gobutton;
    GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mGoogleApiClient.connect();

        mapFragment.getMapAsync(this);
        origin = (EditText) findViewById(R.id.origin);
        destination = (EditText) findViewById(R.id.destination);
        gobutton = (Button) findViewById(R.id.gobutton);
        gobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str = String.valueOf(origin.getText());
                String dest = String.valueOf(destination.getText());
                if (str == null && dest == null) {
                    Toast.makeText(MapsActivity.this, "Source and Destination cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                    if (destination != null)
                        fetchLocation(str, dest);
                    else
                        fetchLocation(str);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });



    }

    private void startLocationUpdates() {
        final LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(3000);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        LocationSettingsRequest.Builder builder=new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        PendingResult<LocationSettingsResult> result = LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());
        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
                int statusCode = locationSettingsResult.getStatus().getStatusCode();
                if(statusCode== LocationSettingsStatusCodes.RESOLUTION_REQUIRED)
                {
                    try {
                        locationSettingsResult.getStatus().startResolutionForResult(MapsActivity.this,1);
                    } catch (IntentSender.SendIntentException e) {
                        e.printStackTrace();
                    }
                }


            }
        });
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, locationRequest, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                Toast.makeText(MapsActivity.this, String.valueOf(location), Toast.LENGTH_SHORT).show();

            }
        });
    }

    @Override
    protected void onPause() {
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, (LocationListener) this);
        mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    protected void onResume() {
        mGoogleApiClient.connect();
        super.onResume();
    }

    private void fetchLocation(String str, String dest) throws IOException {
        Geocoder gc = new Geocoder(this);
        if (gc.isPresent()) {
            ArrayList<Address> origin = (ArrayList<Address>) gc.getFromLocationName(str, 1);
            ArrayList<Address> destination = (ArrayList<Address>) gc.getFromLocationName(dest, 1);
            LatLng originpos, destinationpos;
            Double latorigin = 0.0, longorigin = 0.0, latdest = 0.0, longdest = 0.0;

            if (origin != null) {
                latorigin = origin.get(0).getLatitude();
                longorigin = origin.get(0).getLongitude();
            }
            if (destination != null) {
                latdest = destination.get(0).getLatitude();
                longdest = destination.get(0).getLongitude();
            }

            originpos = new LatLng(latorigin, longorigin);
            destinationpos = new LatLng(latdest, longdest);
            if (marker != null)
                marker.remove();
            if (originmarker != null)
                originmarker.remove();
            if (destinationmarker != null)
                destinationmarker.remove();
            originmarker = mMap.addMarker(new MarkerOptions()
                    .position(originpos)
                    .draggable(true)
                    .title("Origin"));
            destinationmarker = mMap.addMarker(new MarkerOptions()
                    .position(destinationpos)
                    .draggable(true)
                    .title("Destination"));
            CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(originpos, 15);
            mMap.animateCamera(cam);
            Routing routing = new Routing.Builder()
                    .waypoints(originpos, destinationpos)
                    .withListener(this)
                    .travelMode(AbstractRouting.TravelMode.WALKING)
                    .build();
            routing.execute();
        }
    }

    private void fetchLocation(String str) throws IOException {

        Geocoder gc = new Geocoder(this);
        if (gc.isPresent()) {
            ArrayList<Address> list = (ArrayList<Address>) gc.getFromLocationName(str, 1);
            if (list != null) {
                Address ad = list.get(0);
                double latitude = ad.getLatitude();
                double longitude = ad.getLongitude();
                if (marker != null)
                    marker.remove();
                LatLng fetchedlocation = new LatLng(latitude, longitude);
                marker = mMap.addMarker(new MarkerOptions()
                        .position(fetchedlocation)
                        .draggable(true)
                        .title("Origin"));
                marker.setSnippet(String.valueOf(fetchedlocation));
                CameraUpdate cam = CameraUpdateFactory.newLatLngZoom(fetchedlocation, 5);
                mMap.animateCamera(cam);

            }
        }
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

        if (marker != null)
            marker.remove();
        marker = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

        mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker Mmarker) {
                marker = Mmarker;
                LatLng position = Mmarker.getPosition();
                double longitude = position.longitude;
                double latitude = position.latitude;
                Geocoder gc = new Geocoder(getApplicationContext());
                if (gc.isPresent()) {
                    List<Address> fromLocation = null;
                    try {
                        fromLocation = gc.getFromLocation(latitude, longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (fromLocation != null) {
                        Address ad = fromLocation.get(0);
                        String locality = ad.getLocality();
                        Mmarker.setSnippet(locality);
                    }
                }

            }
        });
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, 1);
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);

    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {

        Route r = arrayList.get(i);
        PolylineOptions polyoptions = new PolylineOptions();
        polyoptions.addAll(r.getPoints());
       Polyline myroute= mMap.addPolyline(polyoptions);

    }

    @Override
    public void onRoutingCancelled() {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    mMap.setMyLocationEnabled(true);
                }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }Location lastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        Toast.makeText(this, String.valueOf(lastLocation), Toast.LENGTH_SHORT).show();

        startLocationUpdates();

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
