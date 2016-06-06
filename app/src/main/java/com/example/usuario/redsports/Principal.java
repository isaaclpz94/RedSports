package com.example.usuario.redsports;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usuario.redsports.POJO.Encuentro;
import com.example.usuario.redsports.preferencias.Preferencias;
import com.example.usuario.redsports.rv_animator.ScaleInBottomAnimator;
import com.example.usuario.redsports.splash.SplashScreenMap;
import com.example.usuario.redsports.splash.SplashScreenSports;
import com.github.siyamed.shapeimageview.CircularImageView;

import org.json.JSONArray;
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
import java.net.URL;
import java.util.ArrayList;


public class Principal extends AppCompatActivity {
    private String usuario, contraseña, ID;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private DrawerLayout drawer;
    private NavigationView navigationView;
    private TextView tvNombre, tv;
    private CircularImageView imgUser;
    private RecyclerView rv;
    private ArrayList<Encuentro> encuentros;
    private SwipeRefreshLayout refreshLayout;
    private AdaptadorEncuentros adaptador;
    private Encuentro borrarEste;
    private int itemPosition;
    private String IP = "http://webservicesports.esy.es";
    private String ENCUENTROS = IP + "/obtener_encuentro_por_usuario_id.php";
    private String BORRAR_USUARIOENCUENTRO = IP + "/borrar_usuario_encuentro.php";
    private String REDUCIR_APUNTADOS = IP + "/reducir_apuntados.php";

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        getMenuInflater().inflate(R.menu.options_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id){
            case R.id.mnNoIr:
                new BorrarTask().execute(BORRAR_USUARIOENCUENTRO, ID, borrarEste.getId() + "");
                new UpdateTask().execute(REDUCIR_APUNTADOS, encuentros.get(itemPosition).getId() + "", "1");
                encuentros.remove(itemPosition);
                adaptador.notifyItemRemoved(itemPosition);
                break;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        //comprobar si esta logueado y obtener usuario si lo está
        prefs = getSharedPreferences("login_preferences", Context.MODE_PRIVATE);
        editor = prefs.edit();

        usuario = prefs.getString("nombre", "");
        contraseña = prefs.getString("contrasena", "");
        ID = prefs.getString("ID", "");

        if(usuario.equals("") || contraseña.equals("")){
            Intent i = new Intent(Principal.this,Login.class);
            startActivity(i);
            finish();
        }

        //Ahora obtengo los encuentros de ese usuario
        Bundle extras = getIntent().getExtras();
        if(extras != null){ //Si está apuntado algún encuentro muestro la lista
            encuentros = extras.getParcelableArrayList("encuentros");
        }else{
            encuentros = new ArrayList<>();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView tvToolbar = (TextView)toolbar.findViewById(R.id.tvTituloToolbar);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RockSalt.ttf");
        tvToolbar.setTypeface(tf);
        setSupportActionBar(toolbar);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.syncState();

        refreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        assert refreshLayout != null;
        refreshLayout.setColorSchemeResources(R.color.colorPrimaryDark);
        refreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        new ActualizarTask().execute(ENCUENTROS + "?idusuario=" + ID, "no");
                    }
                }
        );

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        assert navigationView != null;
        View header = navigationView.getHeaderView(0);
        imgUser = (CircularImageView)header.findViewById(R.id.imgUser);
        //imgUser.setImageBitmap(); CAMBIAR IMAGEN USUARIO
        tvNombre = (TextView) header.findViewById(R.id.tvNombre);
        tvNombre.setText(usuario);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.ndquedadas:
                        Intent e = new Intent(Principal.this,SplashScreenSports.class);
                        startActivity(e);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        break;
                    case R.id.ndmapa:
                        Intent imap = new Intent(Principal.this, SplashScreenMap.class);
                        startActivity(imap);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        break;
                    case R.id.ndPreferencias:
                        Intent iprefs = new Intent(Principal.this, Preferencias.class);
                        startActivity(iprefs);
                        overridePendingTransition(R.anim.right_in, R.anim.left_out);
                        break;
                    case R.id.ndLogout:
                        editor.clear();
                        editor.commit();
                        Intent i = new Intent(Principal.this,Login.class);
                        startActivity(i);
                        finish();
                        break;

                }
                //drawer.closeDrawers();
                return true;
            }
        });

        rv = (RecyclerView)findViewById(R.id.RecyclerView);
        if(encuentros.size() != 0) {
            adaptador = new AdaptadorEncuentros(encuentros);

        }else{
            ArrayList<Encuentro> array = new ArrayList<>();
            Encuentro e = new Encuentro(0,"no tienes",0,0,0,"1994-07-07","00:00:00","","", false);
            array.add(e);

            adaptador = new AdaptadorEncuentros();

        }
        adaptador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = rv.getChildAdapterPosition(v);
                if(encuentros.size() != 0) {
                    Encuentro item = encuentros.get(itemPosition);

                    Intent i = new Intent(Principal.this, com.example.usuario.redsports.Encuentro.class);
                    i.putExtra("encuentro", item);
                    startActivity(i);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }
            }
        });

        adaptador.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                itemPosition = rv.getChildAdapterPosition(v);
                if(encuentros.size() != 0) {
                    borrarEste = encuentros.get(itemPosition);
                    v.showContextMenu();
                }
                return true;
            }
        });

        rv.setAdapter(adaptador);
        rv.setLayoutManager(new LinearLayoutManager(Principal.this, LinearLayoutManager.VERTICAL,false));
        rv.setItemAnimator(new ScaleInBottomAnimator());
        registerForContextMenu(rv);
    }

    @Override
    protected void onResume() {
        super.onResume();
        new ActualizarTask().execute(ENCUENTROS + "?idusuario=" + ID, "no");
    }

    /*********** SWIPE TO REFRESH ***********************************/
    private class ActualizarTask extends AsyncTask<String,Void,ArrayList<Encuentro>> {
        private String modo;

        @Override
        protected ArrayList<Encuentro> doInBackground(String... params) {

            String cadena = params[0];
            modo = params[1];
            URL url = null; // Url de donde queremos obtener información
            String devuelve = "";
            JSONArray jArray = null;
            Encuentro encuentro = null;

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
                        result.append(line);        // Paso toda la entrada al StringBuilder
                    }
                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados

                    String resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON

                    if (resultJSON.equals("1")) {      // hay encuentros que mostrar
                        jArray = respuestaJSON.getJSONArray("encuentros");
                        ArrayList<Encuentro> arrayEncuentros = new ArrayList<>(); //array en el que los voy a guardar

                        for(int i = 0; i<jArray.length();i++){
                            JSONObject json_respuesta = jArray.getJSONObject(i);
                            arrayEncuentros.add(new Encuentro(json_respuesta.getInt("ID"),json_respuesta.getString("descripcion"),json_respuesta.getInt("deporte_id"),json_respuesta.getInt("apuntados"),json_respuesta.getInt("capacidad"),json_respuesta.getString("fecha"),json_respuesta.getString("hora"),json_respuesta.getString("lat"),json_respuesta.getString("lon"), false));
                        }

                        return arrayEncuentros;
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
        protected void onPostExecute(ArrayList<Encuentro> encuentros) {
            super.onPostExecute(encuentros);
            if(encuentros != null){
                if (encuentros.size() != 0){
                    Principal.this.encuentros = encuentros;
                    adaptador.clear();
                    adaptador.addAll(encuentros);
                }else{
                    adaptador.clear();
                    ArrayList<Encuentro> array = new ArrayList<>();
                    array.add(new Encuentro());
                    adaptador.addAll(array);
                }
            }else{
                adaptador.clear();
                ArrayList<Encuentro> array = new ArrayList<>();
                array.add(new Encuentro());
                adaptador.addAll(array);

            }
            if(modo.equals("si")){
                adaptador.notifyItemRemoved(itemPosition);
            }
            refreshLayout.setRefreshing(false);

        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        assert drawer != null;
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    /********************** BORRAR ASISTENCIA A ENCUENTRO ************************************/
    public class BorrarTask extends AsyncTask<String,Void,String>{
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
                    BufferedReader br=new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
                    while ((line=br.readLine()) != null) {
                        result.append(line);
                        //response+=line;
                    }

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados

                    resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON
                }

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return resultJSON;
        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("1")) { //La consulta se ha realizado correctamente
                new ActualizarTask().execute(ENCUENTROS + "?idusuario=" + ID, "si");
                Toast.makeText(Principal.this, getResources().getString(R.string.no_asistiras), Toast.LENGTH_SHORT).show();
            } else if (s.equals("2")) { //Error en la consulta
                Toast.makeText(Principal.this, getResources().getString(R.string.mas_tarde), Toast.LENGTH_SHORT).show();
            }else if(s.equals("")){
                Toast.makeText(Principal.this, getResources().getString(R.string.no_conexion), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**************************** REDUCIR EL NÚMERO DE APUNTADOS ****************************************/
    class UpdateTask extends AsyncTask<String, Void, Void> {
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

}
