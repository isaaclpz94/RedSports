package com.example.usuario.redsports;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.usuario.redsports.POJO.Deporte;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Date;
import java.text.SimpleDateFormat;

public class Encuentro extends AppCompatActivity implements OnMapReadyCallback {
    private TextView tvDeporte, tvDescripcion, tvHora, tvFecha, tvCapacidad;
    private Toolbar toolbar;
    private com.example.usuario.redsports.POJO.Encuentro en;
    private ImageView img;
    private FloatingActionButton fab;
    private String idusuario;
    private SharedPreferences prefs;
    private ProgressBar progressBar;
    private boolean reducido = false, borrado = false;
    private GoogleMap mMap;

    private final String IP = "http://webservicesports.esy.es";
    private final String INSERT = IP + "/insertar_usuario_encuentro.php";
    private final String GET_DEPORTE = IP + "/obtener_deporte_por_id.php";
    private final String ICONOS_DEPORTES = IP + "/imgs/icons/";
    private final String REDUCIR_APUNTADOS = IP + "/reducir_apuntados.php";
    private final String BORRAR_USUARIOENCUENTRO = IP + "/borrar_usuario_encuentro.php";
    private final String INCREMENTAR_APUNTADOS = IP + "/incrementar_apuntados.php";

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Agregamos los marcadores de los encuentros
        mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(en.getLat()), Double.parseDouble(en.getLon()))).title(getResources().getString(R.string.lugar_encuentro)).icon(BitmapDescriptorFactory.fromResource(R.drawable.marcador)));

        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(en.getLat()), Double.parseDouble(en.getLon())), (float) 14));
    }

    class Task implements Runnable {

        @Override
        public void run() {
            while (true) {
                if (reducido && borrado) {
                    Snackbar.make(fab, getResources().getString(R.string.no_asistiras), Snackbar.LENGTH_LONG).show();
                    reducido = false; //para que no ejecute mas de una vez el getEncuentrosTask()
                    borrado = false;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuentro);

        //obtener id del usuario
        prefs = getSharedPreferences("login_preferences", Context.MODE_PRIVATE);
        idusuario = prefs.getString("ID", "");

        //obtener el encuentro
        Bundle e = getIntent().getExtras();
        if (e != null) {
            en = (com.example.usuario.redsports.POJO.Encuentro) e.get("encuentro");
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapEncuentro);
        mapFragment.getMapAsync(this);

        //findViewByIds
        tvDeporte = (TextView) findViewById(R.id.tvDeporte);
        tvDescripcion = (TextView) findViewById(R.id.tvDesc);
        toolbar = (Toolbar) findViewById(R.id.tlbar);
        tvHora = (TextView) findViewById(R.id.tvHora);
        tvFecha = (TextView) findViewById(R.id.tvFecha);
        img = (ImageView) findViewById(R.id.imgDeporte);
        tvCapacidad = (TextView) findViewById(R.id.tvCapacidad);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        new getDeporteTask().execute(GET_DEPORTE + "?deporte_id=" + en.getDeporte_id());
        progressBar.setVisibility(View.VISIBLE);

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

        //rellenar el layout con los datos del encuentro
        String[] fechahora = obtenerFechayHora(en);
        tvDescripcion.setText(en.getDescripcion());
        tvFecha.setText(fechahora[0]);
        tvHora.setText(fechahora[1]);
        tvCapacidad.setText(en.getApuntados() + "/" + en.getCapacidad());
        if (en.getApuntados() == en.getCapacidad()) {
            tvCapacidad.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
        }
        tvDescripcion.setText(en.getDescripcion());
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (en.getApuntados() == en.getCapacidad()) {
                    Snackbar.make(v, getResources().getString(R.string.max_personas), Snackbar.LENGTH_LONG).show();
                } else {
                    new InsertTask().execute(INSERT, idusuario, en.getId() + "");
                }

            }
        });
    }

    private String[] obtenerFechayHora(com.example.usuario.redsports.POJO.Encuentro e) { // Formatea la fecha y la hora en el formato adecuado para mostrarla
        String fechaa = e.getFecha();
        String horaa = e.getHora();

        Date fecha = Date.valueOf(fechaa);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM");

        String[] result = new String[2];

        result[0] = dateFormat.format(fecha);
        result[1] = horaa.substring(0, horaa.length() - 3);

        return result;
    }

    /**********************
     * INSERTAR ENCUENTRO_USUARIO
     *******************************************/
    public class InsertTask extends AsyncTask<String, Void, String> {
        private String usuario_id, encuentro_id;

        @Override
        protected String doInBackground(String... params) {
            String cadena = params[0];
            usuario_id = params[1];
            encuentro_id = params[2];
            URL url;
            String resultJSON = "";

            try {
                HttpURLConnection urlConn;
                url = new URL(cadena);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);
                urlConn.setUseCaches(false);
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Accept", "application/json");
                urlConn.connect();
                //Creo el Objeto JSON
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("usuario_id", usuario_id);
                jsonParam.put("encuentro_id", encuentro_id);
                // Envio los parámetros post.
                OutputStream os = urlConn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();

                int respuesta = urlConn.getResponseCode();

                StringBuilder result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK) {

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados

                    resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return resultJSON;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("1")) {
                Snackbar.make(fab, getResources().getString(R.string.asistiras), Snackbar.LENGTH_LONG).setAction(getResources().getString(R.string.deshacer), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new Task()).start();
                        new ReducirTask().execute(REDUCIR_APUNTADOS, en.getId() + "", "1");
                        new BorrarTask().execute(BORRAR_USUARIOENCUENTRO, idusuario, en.getId() + "");
                    }
                }).setActionTextColor(getResources().getColor(R.color.colorPrimaryDark)).show();
                new UpdateTask().execute(INCREMENTAR_APUNTADOS, encuentro_id, "1");
            } else if (s.equals("2")) { //Error en la consulta
                Snackbar.make(fab, getResources().getString(R.string.mas_tarde), Snackbar.LENGTH_LONG).show();
            } else if (s.equals("3")) {
                Snackbar.make(fab, getResources().getString(R.string.asistes), Snackbar.LENGTH_LONG).setAction(getResources().getString(R.string.deshacer), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Thread(new Task()).start();
                        new ReducirTask().execute(REDUCIR_APUNTADOS, en.getId() + "", "1");
                        new BorrarTask().execute(BORRAR_USUARIOENCUENTRO, idusuario, en.getId() + "");
                    }
                }).setActionTextColor(getResources().getColor(R.color.colorPrimaryDark)).show();
            } else if (s.equals("")) {
                Snackbar.make(fab, getResources().getString(R.string.no_conexion), Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /**********************
     * OBTENER EL DEPORTE DEL ENCUENTRO
     ******************************/
    class getDeporteTask extends AsyncTask<String, Void, Deporte> {

        @Override
        protected Deporte doInBackground(String... params) {

            String cadena = params[0];
            URL url = null; // Url de donde queremos obtener información
            String nombre = "";
            Deporte sport = null;
            Bitmap icono = null;

            try {
                url = new URL(cadena);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //Abrir la conexión
                connection.setRequestProperty("User-Agent", "Mozilla/5.0" + " (Linux; Android 1.5; es-ES) Ejemplo HTTP");
                //connection.setHeader("content-type", "application/json");

                int respuesta = connection.getResponseCode();
                StringBuilder result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK) {

                    InputStream in = new BufferedInputStream(connection.getInputStream());  // preparo la cadena de entrada

                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));  // la introduzco en un BufferedReader

                    // El siguiente proceso lo hago porque el JSONOBject necesita un String y tengo
                    // que tranformar el BufferedReader a String. Esto lo hago a traves de un
                    // StringBuilder.

                    String line;
                    while ((line = reader.readLine()) != null) {
                        result.append(line); // Paso toda la entrada al StringBuilder
                    }
                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados

                    String resultJSON = respuestaJSON.getString("estado"); // estado es el nombre del campo en el JSON

                    Log.v("nomre", "deporte " + respuestaJSON.getJSONObject("deporte").getString("nombre"));
                    Log.v("nomre", "CAGOENDIOS");

                    if (resultJSON.equals("1")) { // hay deportes que mostrar
                        nombre = respuestaJSON.getJSONObject("deporte").getString("nombre");
                        Log.v("jsonnombre", "el nombre es " + nombre);

                        //obtengo el icono del deporte
                        URL urlicono = new URL(ICONOS_DEPORTES + respuestaJSON.getJSONObject("deporte").getString("icono"));
                        HttpURLConnection conimagen = (HttpURLConnection) urlicono.openConnection();
                        conimagen.connect();
                        icono = BitmapFactory.decodeStream(conimagen.getInputStream());

                        Bitmap nuevo = scaleDownBitmap(icono, 100, Encuentro.this); //escalo la imagen para que luego no me de error

                        sport = new Deporte(en.getId(), nombre, icono);

                        return sport;
                    } else if (resultJSON.equals("2")) {
                        return null;
                    }
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Deporte deporte) {
            super.onPostExecute(deporte);
            if (deporte != null) {
                progressBar.setVisibility(View.GONE);
                img.setImageBitmap(deporte.getIcono());
                tvDeporte.setText(deporte.getNombre());
            }
        }
    }

    //metodo para escalar las imagenes
    public Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) {

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h = (int) (newHeight * densityMultiplier);
        int w = (int) (h * photo.getWidth() / ((double) photo.getHeight()));

        photo = Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }

    /****************************
     * INCREMENTAR EL NÚMERO DE APUNTADOS
     ****************************************/
    public class UpdateTask extends AsyncTask<String, Void, Void> {
        private String encuentro_id, apuntados;

        @Override
        protected Void doInBackground(String... params) {
            String cadena = params[0];
            encuentro_id = params[1];
            apuntados = params[2];
            URL url;
            String resultJSON = "";

            try {
                HttpURLConnection urlConn;
                url = new URL(cadena);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);
                urlConn.setUseCaches(false);
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Accept", "application/json");
                urlConn.connect();
                //Creo el Objeto JSON
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("id", encuentro_id);
                jsonParam.put("numero_apuntados", apuntados);
                // Envio los parámetros post.
                OutputStream os = urlConn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();

                int respuesta = urlConn.getResponseCode();

                StringBuilder result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK) {

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados

                    resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /**********************
     * BORRAR ASISTENCIA A ENCUENTRO
     ************************************/
    public class BorrarTask extends AsyncTask<String, Void, String> {
        private String usuario_id, encuentro_id;

        @Override
        protected String doInBackground(String... params) {
            String cadena = params[0];
            usuario_id = params[1];
            encuentro_id = params[2];
            URL url;
            String resultJSON = "";

            try {
                HttpURLConnection urlConn;

                DataOutputStream printout;
                DataInputStream input;
                url = new URL(cadena);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);
                urlConn.setUseCaches(false);
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Accept", "application/json");
                urlConn.connect();
                //Creo el Objeto JSON
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("idusuario", usuario_id);
                jsonParam.put("idencuentro", encuentro_id);
                // Envio los parámetros post.
                OutputStream os = urlConn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();

                int respuesta = urlConn.getResponseCode();


                StringBuilder result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK) {

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                        //response+=line;
                    }

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados

                    resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON
                }

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return resultJSON;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("1")) { //La consulta se ha realizado correctamente
                borrado = true;
            } else if (s.equals("2")) { //Error en la consulta
                Snackbar.make(fab, getResources().getString(R.string.mas_tarde), Snackbar.LENGTH_SHORT).show();
            } else if (s.equals("")) {
                Snackbar.make(fab, getResources().getString(R.string.no_conexion), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    /****************************
     * REDUCIR EL NÚMERO DE APUNTADOS
     ****************************************/
    class ReducirTask extends AsyncTask<String, Void, String> {
        private String encuentro_id, apuntados;

        @Override
        protected String doInBackground(String... params) {
            String cadena = params[0];
            encuentro_id = params[1];
            apuntados = params[2];
            URL url;
            String resultJSON = "";

            try {
                HttpURLConnection urlConn;
                url = new URL(cadena);
                urlConn = (HttpURLConnection) url.openConnection();
                urlConn.setDoInput(true);
                urlConn.setDoOutput(true);
                urlConn.setUseCaches(false);
                urlConn.setRequestProperty("Content-Type", "application/json");
                urlConn.setRequestProperty("Accept", "application/json");
                urlConn.connect();
                //Creo el Objeto JSON
                JSONObject jsonParam = new JSONObject();
                jsonParam.put("id", encuentro_id);
                jsonParam.put("numero_apuntados", apuntados);
                // Envio los parámetros post.
                OutputStream os = urlConn.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
                writer.write(jsonParam.toString());
                writer.flush();
                writer.close();

                int respuesta = urlConn.getResponseCode();

                StringBuilder result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK) {

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        result.append(line);
                    }

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados

                    resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON
                }
            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return resultJSON;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("1")) {
                reducido = true;
            }
        }
    }
}