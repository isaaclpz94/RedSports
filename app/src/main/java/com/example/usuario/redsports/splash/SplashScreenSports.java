package com.example.usuario.redsports.splash;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Window;
import android.widget.TextView;

import com.example.usuario.redsports.POJO.Deporte;
import com.example.usuario.redsports.POJO.Encuentro;
import com.example.usuario.redsports.Principal;
import com.example.usuario.redsports.Encuentros;
import com.example.usuario.redsports.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by USUARIO on 07/05/2016.
 */
public class SplashScreenSports extends AppCompatActivity {

    private TextView tvCargando;
    private static final String IP = "http://webservicesports.esy.es";
    private static final String OBTENER_DEPORTES = IP + "/obtener_deportes.php";
    private static final String OBTENER_ENCUENTROS = IP + "/obtener_encuentros.php";
    private static final String OBTENER_MIS_ENCUENTROS = IP + "/obtener_encuentro_por_usuario_id.php";
    private static final String ICONOS_DEPORTES = IP + "/imgs/icons/";
    private String ID;
    private Bitmap icono;
    private ArrayList<Deporte> deportes = new ArrayList<>();
    private ArrayList<Encuentro> encuentros = new ArrayList<>();
    private ArrayList<Encuentro> arrayEncuentros;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private boolean tengodeportes = false, tengomisencuentros = false;


    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    class Task implements Runnable{

        @Override
        public void run() {
            while (true){
                if(tengodeportes && tengomisencuentros){
                    new getEncuentrosTask().execute(OBTENER_ENCUENTROS);
                    tengodeportes=false; //para que no ejecute mas de una vez el getEncuentrosTask()
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splashscreen_sports);

        prefs = getSharedPreferences("login_preferences", Context.MODE_PRIVATE);
        editor = prefs.edit();

        ID = prefs.getString("ID", "");

        new Thread(new Task()).start();
        new getDeportesTask().execute(OBTENER_DEPORTES);
        new getMisEncuentrosTask().execute(OBTENER_MIS_ENCUENTROS + "?idusuario=" + ID);


        TextView tvTitulo = (TextView) findViewById(R.id.tvTituloSplashsports);
        tvCargando = (TextView) findViewById(R.id.tvLoading);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RockSalt.ttf");
        assert tvTitulo != null;
        tvTitulo.setTypeface(tf);
        tvCargando.setTypeface(tf);
    }

    /**************************** OBTENER LOS DEPORTES ******************************************************/

    public class getDeportesTask extends AsyncTask<String,Void,ArrayList<Deporte>> {

        @Override
        protected ArrayList<Deporte> doInBackground(String... params) {

            String cadena = params[0];
            URL url = null; // Url de donde queremos obtener información
            String devuelve = "";
            JSONArray jArray = null;
            Deporte sport = null;

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

                    if (resultJSON.equals("1")) {      // hay deportes que mostrar
                        jArray = respuestaJSON.getJSONArray("deportes");
                        ArrayList<Deporte> arrayDeportes = new ArrayList<>();
                        for(int i = 0; i<jArray.length();i++){

                            JSONObject json_respuesta = jArray.getJSONObject(i);

                            //obtengo el icono del deporte
                            URL urlicono = new URL(ICONOS_DEPORTES + json_respuesta.getString("icono"));
                            HttpURLConnection conimagen = (HttpURLConnection)urlicono.openConnection();
                            conimagen.connect();
                            icono = BitmapFactory.decodeStream(conimagen.getInputStream());

                            Bitmap nuevo = scaleDownBitmap(icono, 100, SplashScreenSports.this); //escalo la imagen para que luego no me de error

                            arrayDeportes.add(new Deporte(json_respuesta.getInt("ID"),json_respuesta.getString("nombre"),nuevo));
                            }

                        return arrayDeportes;
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
        protected void onPostExecute(ArrayList<Deporte> sports) {
            super.onPostExecute(sports);
            if(sports!=null){
                tengodeportes = true;
                for(Deporte d : sports){
                    deportes.add(d);
                }
            }else{
                Intent i = new Intent(SplashScreenSports.this, Principal.class);
                startActivity(i);
            }
        }
    }

    public static Bitmap scaleDownBitmap(Bitmap photo, int newHeight, Context context) { //metodo para escalar las imagenes, ya que me daba error

        final float densityMultiplier = context.getResources().getDisplayMetrics().density;

        int h= (int) (newHeight*densityMultiplier);
        int w= (int) (h * photo.getWidth()/((double) photo.getHeight()));

        photo=Bitmap.createScaledBitmap(photo, w, h, true);

        return photo;
    }

    /**************************** OBTENER LOS ID DE LOS ENCUENTROS DEL USUARIO ******************************************************/

    public class getMisEncuentrosTask extends AsyncTask<String,Void,ArrayList<Encuentro>> {

        @Override
        protected ArrayList<Encuentro> doInBackground(String... params) {

            String cadena = params[0];
            URL url = null; // Url de donde queremos obtener información
            String devuelve = "";
            JSONArray jArray = null;
            Encuentro encuentro = null;

            try {
                url = new URL(cadena);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //Abrir la conexión
                connection.setRequestProperty("User-Agent", "Mozilla/5.0" + " (Linux; Android 1.5; es-ES) Ejemplo HTTP");

                int respuesta = connection.getResponseCode();
                StringBuilder result = new StringBuilder();

                if (respuesta == HttpURLConnection.HTTP_OK) {

                    InputStream in = new BufferedInputStream(connection.getInputStream());  // preparo la cadena de entrada
                    BufferedReader reader = new BufferedReader(new InputStreamReader(in));  // la introduzco en un BufferedReader

                    //El siguiente proceso lo hago porque el JSONOBject necesita un String y tengo
                    //que tranformar el BufferedReader a String. Esto lo hago a traves de un
                    //StringBuilder.

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
                        arrayEncuentros = new ArrayList<>(); //array en el que los voy a guardar

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
            if(encuentros == null){
            }
            tengomisencuentros = true;
        }
    }



    /**************************** OBTENER TODOS LOS ENCUENTROS ******************************************************/

    public class getEncuentrosTask extends AsyncTask<String,Void,ArrayList<Encuentro>> {

        @Override
        protected ArrayList<Encuentro> doInBackground(String... params) {

            String cadena = params[0];
            URL url = null; // Url de donde queremos obtener información
            JSONArray jArray = null;

            try {
                url = new URL(cadena);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection(); //Abrir la conexión
                connection.setRequestProperty("User-Agent", "Mozilla/5.0" + " (Linux; Android 1.5; es-ES) Ejemplo HTTP");

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

                    if (resultJSON.equals("1")) { // hay encuentros que mostrar
                        jArray = respuestaJSON.getJSONArray("encuentros");
                        ArrayList<Encuentro> arrayEncuentros = new ArrayList<>(); //array en el que los voy a guardar

                        for(int i = 0 ; i < jArray.length() ; i++){
                            JSONObject json_respuesta = jArray.getJSONObject(i);
                            arrayEncuentros.add(new Encuentro(json_respuesta.getInt("ID"),json_respuesta.getString("descripcion"),json_respuesta.getInt("deporte_id"),json_respuesta.getInt("apuntados"),json_respuesta.getInt("capacidad"),json_respuesta.getString("fecha"),json_respuesta.getString("hora"),json_respuesta.getString("lat"),json_respuesta.getString("lon"),false));
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
            if(encuentros!=null){
                if(arrayEncuentros != null) {
                    if (encuentros.size() > 0 && arrayEncuentros.size() > 0) {
                        //Recorremos los encuentros para ver a cuales de ellos el usuario está apuntado
                        for (Encuentro e : encuentros) {
                            for (Encuentro me : arrayEncuentros) {
                                if (e.getId() == me.getId()) {
                                    e.setAsistente(true);
                                }
                            }
                        }
                    }
                }

                Intent i = new Intent(SplashScreenSports.this, Encuentros.class);
                i.putParcelableArrayListExtra("encuentros", encuentros);
                i.putParcelableArrayListExtra("deportes", deportes);
                startActivity(i);

            }else{
                Intent i = new Intent(SplashScreenSports.this, Principal.class);
                startActivity(i);
            }
        }
    }

}

