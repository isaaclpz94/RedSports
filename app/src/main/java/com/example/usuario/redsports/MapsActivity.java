package com.example.usuario.redsports;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.usuario.redsports.POJO.*;
import com.example.usuario.redsports.POJO.Encuentro;
import com.example.usuario.redsports.map_util.PopupAdapter;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private Toolbar toolbar;
    private ArrayList<com.example.usuario.redsports.POJO.Encuentro> encuentros = new ArrayList<>();
    private ArrayList<Deporte> deportes = new ArrayList<>();
    private ArrayList<Marker> marcadores = new ArrayList<>();
    private AlertDialog alert = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            AlertNoGps();
        }

        toolbar = (Toolbar)findViewById(R.id.maptoolbar);

        //titulo y boton de atras de la toolbar
        assert toolbar != null;
        toolbar.setTitle("");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() { //el botón de atras mata la actividad
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        //obtener encuentros y deportes
        Bundle e = getIntent().getExtras();
        if(e != null){
            encuentros = e.getParcelableArrayList("encuentros");
            deportes = e.getParcelableArrayList("deportes");
        }else{
            Toast.makeText(getApplicationContext(),getResources().getString(R.string.no_conexion), Toast.LENGTH_SHORT).show();
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

        // Agregamos los marcadores de los encuentros
        for (Encuentro en : encuentros){
            for(Deporte d: deportes){
                if(en.getDeporte_id() == d.getId()){
                    marcadores.add(mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(en.getLat()), Double.parseDouble(en.getLon()))).title(en.getDescripcion()).snippet(en.getApuntados() + "/" + en.getCapacidad() + "|" + en.getFecha() + "|" + d.getNombre()).icon(BitmapDescriptorFactory.fromResource(R.drawable.marcador))));
                }
            }
        }

        mMap.setInfoWindowAdapter(new PopupAdapter(getLayoutInflater()));
        mMap.setOnInfoWindowClickListener(this);
        LatLng granada = new LatLng(37.1833, -3.6);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(granada,(float)11));
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        for(Encuentro en : encuentros){
            if(marker.getTitle().equals(en.getDescripcion())){
                Intent i = new Intent(MapsActivity.this, com.example.usuario.redsports.Encuentro.class);
                i.putExtra("encuentro", en);
                startActivity(i);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        }
    }

    private void AlertNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(getResources().getString(R.string.alert_gps))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.si), new DialogInterface.OnClickListener() {
                    public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                        dialog.cancel();
                    }
                });
        alert = builder.create();
        alert.show();
    }
}
