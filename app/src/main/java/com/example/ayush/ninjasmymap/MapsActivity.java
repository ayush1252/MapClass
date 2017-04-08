package com.example.ayush.ninjasmymap;

import android.graphics.drawable.AnimationDrawable;
import android.location.Address;
import android.location.Geocoder;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    Marker marker;
    Marker originmarker;
    Marker destinationmarker;
    EditText origin;
    EditText destination;
    Button gobutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        origin= (EditText) findViewById(R.id.origin);
        destination= (EditText) findViewById(R.id.destination);
        gobutton= (Button) findViewById(R.id.gobutton);
        gobutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String str= String.valueOf(origin.getText());
                String dest= String.valueOf(destination.getText());
                if(str==null&&dest==null) {
                    Toast.makeText(MapsActivity.this, "Source and Destination cannot be empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                try {
                        if(destination!=null)
                        fetchLocation(str,dest);
                        else
                            fetchLocation(str);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void fetchLocation(String str, String dest) throws IOException {
        Geocoder gc=new Geocoder(this);
        if(gc.isPresent())
        {
            ArrayList<Address> origin= (ArrayList<Address>) gc.getFromLocationName(str,1);
            ArrayList<Address> destination= (ArrayList<Address>) gc.getFromLocationName(dest,1);
            LatLng originpos, destinationpos;
            Double latorigin = 0.0,longorigin=0.0, latdest=0.0,longdest=0.0;

            if(origin!=null) {
                latorigin=origin.get(0).getLatitude();
                longorigin=origin.get(0).getLongitude();
            }
            if(destination!=null)
            {
                latdest=destination.get(0).getLatitude();
                longdest=destination.get(0).getLongitude();
            }

            originpos=new LatLng(latorigin,longorigin);
            destinationpos=new LatLng(latdest,longdest);
            if(marker!=null)
                marker.remove();
            if(originmarker!=null)
                originmarker.remove();
            if(destinationmarker!=null)
                destinationmarker.remove();
           originmarker= mMap.addMarker(new MarkerOptions()
                    .position(originpos)
                    .draggable(true)
                    .title("Origin"));
            destinationmarker=mMap.addMarker(new MarkerOptions()
                    .position(destinationpos)
                    .draggable(true)
                    .title("Destination"));
            CameraUpdate cam=CameraUpdateFactory.newLatLngZoom(originpos,15);
            mMap.animateCamera(cam);
            Routing routing=new Routing.Builder().waypoints(originpos,destinationpos).withListener(this).travelMode(AbstractRouting.TravelMode.WALKING).build();
            routing.execute();
        }
    }

    private void fetchLocation(String str) throws IOException {

        Geocoder gc=new Geocoder(this);
        if(gc.isPresent())
        {
            ArrayList<Address> list= (ArrayList<Address>) gc.getFromLocationName(str, 1);
            if(list!=null)
            {
                Address ad=list.get(0);
                double latitude = ad.getLatitude();
                double longitude = ad.getLongitude();
                if(marker!=null)
                    marker.remove();
                LatLng fetchedlocation=new LatLng(latitude,longitude);
               marker= mMap.addMarker(new MarkerOptions()
                        .position(fetchedlocation)
                        .draggable(true)
                        .title("Origin"));
                marker.setSnippet(String.valueOf(fetchedlocation));
                CameraUpdate cam=CameraUpdateFactory.newLatLngZoom(fetchedlocation,5);
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
        if(marker!=null)
            marker.remove();
        marker=mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
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
                marker=Mmarker;
                LatLng position = Mmarker.getPosition();
                double longitude = position.longitude;
                double latitude = position.latitude;
                Geocoder gc=new Geocoder(getApplicationContext());
                if(gc.isPresent())
                {
                    List<Address> fromLocation=null;
                    try {
                        fromLocation = gc.getFromLocation(latitude, longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if(fromLocation!=null)
                    {
                        Address ad=fromLocation.get(0);
                        String locality = ad.getLocality();
                        Mmarker.setSnippet(locality);
                    }
                }

            }
        });
    }

    @Override
    public void onRoutingFailure(RouteException e) {

    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> arrayList, int i) {

        Route r=arrayList.get(i);
        PolylineOptions polyoptions=new PolylineOptions();
        polyoptions.addAll(r.getPoints());
        mMap.addPolyline(polyoptions);

    }

    @Override
    public void onRoutingCancelled() {

    }
}
