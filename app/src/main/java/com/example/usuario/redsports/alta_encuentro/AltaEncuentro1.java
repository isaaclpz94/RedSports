package com.example.usuario.redsports.alta_encuentro;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usuario.redsports.POJO.Deporte;
import com.example.usuario.redsports.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;

public class AltaEncuentro1 extends AppCompatActivity implements OnMapReadyCallback {

    private final String IP = "http://webservicesports.esy.es";
    private final String INSERT_ENCUENTRO = IP + "/insertar_encuentro.php";
    private final String INSERT_ENCUENTRO_USUARIO = IP + "/insertar_usuario_encuentro.php";

    private EditText etDesc;
    private TextView tvTituloToolbar, tvSub;
    private Button btnSiguiente;
    private Spinner spinner;
    private int deporte_id;
    private Toolbar toolbar;
    private ArrayList<Deporte> deportes;
    private ArrayList<String> nombreDeportes = new ArrayList<>();

    private GoogleMap mMap;
    private AlertDialog alert = null;
    private boolean seleccionado = false, hecho = false, seleccionadoSpinner = false;
    private String latitud, longitud;



    class Task implements Runnable{

        @Override
        public void run() {
            while (true){
                if(seleccionado){ //si ha elegido el lugar
                    if(etDesc.getText().length() > 0 && seleccionadoSpinner){ //Si esta todo seleccionado
                        hecho = true;
                    }
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alta_encuentro1);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.mapEncuentro);
        mapFragment.getMapAsync(this);

        final LocationManager manager = (LocationManager) getSystemService( Context.LOCATION_SERVICE );
        if ( !manager.isProviderEnabled( LocationManager.GPS_PROVIDER ) ) {
            AlertNoGps();
        }

        new Thread(new Task()).start();

        Bundle e = getIntent().getExtras();

        if(e != null){
            deportes = e.getParcelableArrayList("deportes");
            nombreDeportes.add(getResources().getString(R.string.selecciona_deporte));
            for(Deporte d : deportes){
                nombreDeportes.add(d.getNombre());
            }
        }

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        etDesc = (EditText) findViewById(R.id.etDesc);
        btnSiguiente = (Button) findViewById(R.id.btnSiguiente);
        tvTituloToolbar = (TextView) findViewById(R.id.tvTituloToolbar);
        tvSub = (TextView) findViewById(R.id.tvSubtituloToolbar);
        spinner = (Spinner)findViewById(R.id.spinnerDeporte);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RockSalt.ttf");
        tvTituloToolbar.setTypeface(tf);
        tvSub.setTypeface(tf);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() { //el botón de atras mata la actividad
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        //Spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(AltaEncuentro1.this, android.R.layout.simple_spinner_item, nombreDeportes);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 1:
                        deporte_id = deportes.get(0).getId();
                        seleccionadoSpinner = true;
                        break;
                    case 2:
                        deporte_id = deportes.get(1).getId();
                        seleccionadoSpinner = true;
                        break;
                    case 3:
                        deporte_id = deportes.get(2).getId();
                        seleccionadoSpinner = true;
                        break;
                    case 4:
                        deporte_id = deportes.get(3).getId();
                        seleccionadoSpinner = true;
                        break;
                    case 5:
                        deporte_id = deportes.get(4).getId();
                        seleccionadoSpinner = true;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        btnSiguiente.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hecho){
                    Intent i = new Intent(AltaEncuentro1.this, AltaEncuentro2.class);
                    i.putExtra("descripcion", etDesc.getText().toString().trim());
                    i.putExtra("lat", latitud);
                    i.putExtra("lon", longitud);
                    i.putExtra("deporte", deporte_id);
                    startActivity(i);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }else{
                    if(etDesc.getText().length() == 0 ){
                        etDesc.setError(getResources().getString(R.string.no_vacio));
                    }else if(!seleccionadoSpinner){
                        Toast.makeText(AltaEncuentro1.this, getResources().getString(R.string.selecciona_deporte), Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(AltaEncuentro1.this, getResources().getString(R.string.selecciona_lugar), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng granada = new LatLng(37.1833, -3.6);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(granada,(float)13));
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

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                //Limpio el mapa
                mMap.clear();

                //Agrego el marcador donde ha pulsado
                mMap.addMarker(new MarkerOptions().position(latLng).title(getResources().getString(R.string.encuentro_aqui)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marcador)));

                //Paso las coordenadas a String para luego hacer la inserción
                Double lat = latLng.latitude;
                Double lon = latLng.longitude;
                latitud = lat.toString();
                longitud = lon.toString();

                //Ya tenemos el lugar del evento
                seleccionado = true;


            }
        });
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
