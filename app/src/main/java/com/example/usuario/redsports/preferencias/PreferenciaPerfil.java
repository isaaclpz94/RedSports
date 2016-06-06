package com.example.usuario.redsports.preferencias;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.usuario.redsports.POJO.Preferencia;
import com.example.usuario.redsports.R;
import com.example.usuario.redsports.splash.SplashScreen;
import com.example.usuario.redsports.util.Dialogo;
import com.example.usuario.redsports.util.OnDialogoListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by USUARIO on 03/06/2016.
 */
public class PreferenciaPerfil extends AppCompatActivity {

    private RecyclerView rv;
    private TextView tvPreferencias;
    private AdaptadorPreferencias adaptador;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private AlertDialog alert = null;
    private String username, nombre, contraseña, id;
    private ProgressDialog progressDialog;
    private final int CAMBIARUSERNAME = 1, CAMBIARCONTRASEÑA = 2, CAMBIARNOMBRE = 3;
    private final String IP = "http://webservicesports.esy.es";
    private final String UPDATE_USERNAME = IP + "/update_usuario.php";
    private final String UPDATE_NOMBRE = IP + "/update_nombre.php";
    private final String UPDATE_CONTRASENA = IP + "/update_contrasena.php";
    private final String DELETE_USER = IP + "/borrar_usuario.php";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferencias);

        prefs = getSharedPreferences("login_preferences", Context.MODE_PRIVATE);
        editor = prefs.edit();
        nombre = prefs.getString("nombre", "");
        contraseña = prefs.getString("contrasena", "");
        username = prefs.getString("username", "");

        id = prefs.getString("ID", "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        TextView tvToolbar = (TextView) toolbar.findViewById(R.id.tvTituloToolbar);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RockSalt.ttf");
        tvToolbar.setTypeface(tf);
        tvToolbar.setText(getResources().getString(R.string.perfil));
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

        rv = (RecyclerView) findViewById(R.id.recycler);

        final ArrayList<Preferencia> prefs = new ArrayList<>();
        prefs.add(new Preferencia(getResources().getString(R.string.cambiar_username), BitmapFactory.decodeResource(getResources(), R.drawable.person)));
        prefs.add(new Preferencia(getResources().getString(R.string.cambiar_nombre), BitmapFactory.decodeResource(getResources(), R.drawable.person)));
        prefs.add(new Preferencia(getResources().getString(R.string.cambiar_contraseña), BitmapFactory.decodeResource(getResources(), R.drawable.password)));
        prefs.add(new Preferencia(getResources().getString(R.string.eliminar_cuenta), BitmapFactory.decodeResource(getResources(), R.drawable.delete)));

        adaptador = new AdaptadorPreferencias(prefs);
        rv.setAdapter(adaptador);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(PreferenciaPerfil.this, LinearLayoutManager.VERTICAL, false));
        adaptador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = rv.getChildAdapterPosition(v);
               if(prefs.get(itemPosition).getPreferencia().equals(getResources().getString(R.string.cambiar_username))){
                   final OnDialogoListener odl = new OnDialogoListener() {
                       @Override
                       public void onPreShow(View v) {
                           EditText et = (EditText) v.findViewById(R.id.editText);
                           et.setText(username);
                       }

                       @Override
                       public void onOkSelecter(View v) {
                           EditText et = (EditText) v.findViewById(R.id.editText);
                           if (et.getText().toString().length() >= 4) {
                               new UpdateTask().execute(UPDATE_USERNAME, et.getText().toString().trim(), CAMBIARUSERNAME + "");
                               showProgressDialogWithTitle();
                           } else {
                               Toast.makeText(PreferenciaPerfil.this, getResources().getString(R.string.min_caracteres), Toast.LENGTH_SHORT).show();
                           }
                       }

                       @Override
                       public void onCancelSelecter(View v) {
                       }
                   };
                   Dialogo d = new Dialogo(PreferenciaPerfil.this, R.layout.dialogo, odl);
                   d.show();
               }else if(prefs.get(itemPosition).getPreferencia().equals(getResources().getString(R.string.cambiar_contraseña))){
                   final OnDialogoListener odl3 = new OnDialogoListener() {
                       @Override
                       public void onPreShow(View v) {
                           EditText et = (EditText) v.findViewById(R.id.editText);
                           et.setHint(getResources().getString(R.string.su_contraseña));
                           et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                           et.setTransformationMethod(PasswordTransformationMethod.getInstance());
                       }

                       @Override
                       public void onOkSelecter(View v) {
                           EditText et = (EditText) v.findViewById(R.id.editText);

                           if (et.getText().toString().trim().equals(contraseña) && et.getText().toString().length() >= 4) {
                               final OnDialogoListener odl3 = new OnDialogoListener() {
                                   @Override
                                   public void onPreShow(View v) {
                                       EditText et = (EditText) v.findViewById(R.id.editText);
                                       EditText et2 = (EditText) v.findViewById(R.id.editText2);
                                       et2.setVisibility(View.VISIBLE);
                                       et.setHint(getResources().getString(R.string.alta_contrasena));
                                       et2.setHint(getResources().getString(R.string.alta_contrasena2));
                                       et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                       et.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                       et2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                       et2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                   }

                                   @Override
                                   public void onOkSelecter(View v) {
                                       EditText et = (EditText) v.findViewById(R.id.editText);
                                       EditText et2 = (EditText) v.findViewById(R.id.editText2);

                                       et.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                       et.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                       et2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                       et2.setTransformationMethod(PasswordTransformationMethod.getInstance());
                                       if (et.getText().toString().length() >= 4 && et2.getText().toString().length() >= 4) {
                                           if (et.getText().toString().trim().equals(et2.getText().toString().trim())) {
                                               new UpdateTask().execute(UPDATE_CONTRASENA, et.getText().toString().trim(), CAMBIARCONTRASEÑA + "");
                                               showProgressDialogWithTitle();
                                           } else {
                                               Toast.makeText(PreferenciaPerfil.this, getResources().getString(R.string.contraseñas_no_coincidenn), Toast.LENGTH_SHORT).show();
                                           }
                                       } else {
                                           Toast.makeText(PreferenciaPerfil.this, getResources().getString(R.string.min_caracteres), Toast.LENGTH_SHORT).show();
                                       }
                                   }

                                   @Override
                                   public void onCancelSelecter(View v) {
                                   }
                               };
                               Dialogo d3 = new Dialogo(PreferenciaPerfil.this, R.layout.dialogo, odl3);
                               d3.show();
                           } else {
                               Toast.makeText(PreferenciaPerfil.this, getResources().getString(R.string.contraseñas_no_coincidenn), Toast.LENGTH_SHORT).show();
                           }
                       }

                       @Override
                       public void onCancelSelecter(View v) {
                       }
                   };
                   Dialogo d3 = new Dialogo(PreferenciaPerfil.this, R.layout.dialogo, odl3);
                   d3.show();
               }else if(prefs.get(itemPosition).getPreferencia().equals(getResources().getString(R.string.cambiar_nombre))){
                   final OnDialogoListener odl2 = new OnDialogoListener() {
                       @Override
                       public void onPreShow(View v) {
                           EditText et = (EditText) v.findViewById(R.id.editText);
                           et.setText(nombre);
                       }

                       @Override
                       public void onOkSelecter(View v) {
                           EditText et = (EditText) v.findViewById(R.id.editText);
                           if (et.getText().toString().length() >= 4) {
                               new UpdateTask().execute(UPDATE_NOMBRE, et.getText().toString().trim(), CAMBIARNOMBRE + "");
                               showProgressDialogWithTitle();
                           } else {
                               Toast.makeText(PreferenciaPerfil.this, getResources().getString(R.string.min_caracteres), Toast.LENGTH_SHORT).show();
                           }
                       }

                       @Override
                       public void onCancelSelecter(View v) {
                       }
                   };
                   Dialogo d2 = new Dialogo(PreferenciaPerfil.this, R.layout.dialogo, odl2);
                   d2.show();
               }else if(prefs.get(itemPosition).getPreferencia().equals(getResources().getString(R.string.eliminar_cuenta))){
                   final AlertDialog.Builder builder = new AlertDialog.Builder(PreferenciaPerfil.this);
                   builder.setMessage(getResources().getString(R.string.borrar_cuenta))
                           .setCancelable(false)
                           .setPositiveButton(getResources().getString(R.string.si), new DialogInterface.OnClickListener() {
                               public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int idd) {
                                   new DeleteAccount().execute(DELETE_USER, id);
                                   showProgressDialogWithTitle();
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
        });
    }

    private void showProgressDialogWithTitle() {
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressDialog.setCancelable(false);
        progressDialog.setTitle(getResources().getString(R.string.espera));
        progressDialog.setMessage(getResources().getString(R.string.procesando));
        progressDialog.show();

        // To Dismiss progress dialog
        //progressDialog.dismiss();
    }

    /****************************
     * CAMBIAR INFO USUARIO
     ****************************************/
    public class UpdateTask extends AsyncTask<String, Void, String> {
        private String texto;
        private String opcion;

        @Override
        protected String doInBackground(String... params) {
            String cadena = params[0];
            texto = params[1];
            opcion = params[2];
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
                jsonParam.put("id", id);
                switch (opcion) {
                    case CAMBIARUSERNAME + "":
                        jsonParam.put("usuario", texto);
                        break;
                    case CAMBIARNOMBRE + "":
                        jsonParam.put("nombre", texto);
                        break;
                    case CAMBIARCONTRASEÑA + "":
                        jsonParam.put("contrasena", texto);
                        break;
                }

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
            progressDialog.dismiss();
            if (s.equals("1")) {
                switch (opcion) {
                    case CAMBIARUSERNAME + "":
                        editor.putString("username", texto);
                        break;
                    case CAMBIARNOMBRE + "":
                        editor.putString("nombre", texto);
                        break;
                    case CAMBIARCONTRASEÑA + "":
                        editor.putString("contrasena", texto);
                        break;
                }
                editor.commit();
                Toast.makeText(PreferenciaPerfil.this, getResources().getString(R.string.info_actualizada), Toast.LENGTH_SHORT).show();
            } else if (s.equals("2")) { //Error en la consulta
                Toast.makeText(PreferenciaPerfil.this, getResources().getString(R.string.mas_tarde), Toast.LENGTH_SHORT).show();
            } else if (s.equals("")) {
                Toast.makeText(PreferenciaPerfil.this, getResources().getString(R.string.no_conexion), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public class DeleteAccount extends AsyncTask<String, Void, String> {
        private String id;

        @Override
        protected String doInBackground(String... params) {
            String cadena = params[0];
            id = params[1];
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
                jsonParam.put("idusuario", id);

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
            progressDialog.dismiss();
            if (s.equals("1")) {
                editor.clear();
                editor.commit();

                Intent i = new Intent(PreferenciaPerfil.this, SplashScreen.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
            } else if (s.equals("2")) { //Error en la consulta
                Toast.makeText(PreferenciaPerfil.this, getResources().getString(R.string.mas_tarde), Toast.LENGTH_SHORT).show();
            } else if (s.equals("")) {
                Toast.makeText(PreferenciaPerfil.this, getResources().getString(R.string.no_conexion), Toast.LENGTH_SHORT).show();
            }
        }
    }

}
