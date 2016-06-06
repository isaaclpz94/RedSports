package com.example.usuario.redsports;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import fr.castorflex.android.smoothprogressbar.SmoothProgressBar;

/**
 * Created by USUARIO on 28/04/2016.
 */
public class AltaUsuario extends AppCompatActivity{
    private TextView tvTitulo;
    private EditText etUser,etNombre, etPass, etPass2;
    private Button btnOk, btnImg;
    private SmoothProgressBar barra;
    private final String IP = "http://webservicesports.esy.es";
    private final String INSERT = IP + "/insertar_usuario.php";
    private boolean rellenado = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_altausuario);

        etUser = (EditText)findViewById(R.id.etUserN);
        etNombre = (EditText)findViewById(R.id.etNombreN);
        etPass = (EditText)findViewById(R.id.etContrasenaN);
        etPass2 = (EditText)findViewById(R.id.etContrasenaN2);
        barra = (SmoothProgressBar)findViewById(R.id.progressbar);
        tvTitulo = (TextView)findViewById(R.id.tvTitulo);
        Typeface tf = Typeface.createFromAsset(getAssets(),"fonts/RockSalt.ttf");
        tvTitulo.setTypeface(tf);
        btnOk = (Button)findViewById(R.id.btnOkN);

        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etUser.getText().length() >= 4){ //Comprobar que el campo usuario no esta vacío
                    if(etNombre.getText().length() >= 4) { //Comprobar que el campo nombre no esta vacío
                        if (etPass.getText().length() >= 4) { //Comprobar que el campo contraseña no esta vacío
                            if (etPass2.getText().length() >= 4) { //Comprobar que el campo contraseña2 no esta vacío
                                //Todo esta rellenado, ahora compruebo que las contraseñas coinciden

                                if (etPass.getText().toString().trim().equals(etPass2.getText().toString().trim())) { //Todo ok, recojo las variables y ejecuto la hebra
                                    String usuario = etUser.getText().toString().trim();
                                    String contraseña = etPass.getText().toString().trim();
                                    String nombre = etNombre.getText().toString().trim();
                                    rellenado = true;
                                    barra.setVisibility(View.VISIBLE);

                                    new InsertTask().execute(INSERT, usuario, nombre, contraseña);

                                } else {
                                    etPass2.setError(getResources().getString(R.string.contraseñas_no_coincidenn));
                                }
                            } else {
                                etPass2.setError(getResources().getString(R.string.min_caracteres));
                            }
                        } else {
                            etPass.setError(getResources().getString(R.string.min_caracteres));
                        }
                    }else{
                        etNombre.setError(getResources().getString(R.string.min_caracteres));
                    }
                }else{
                    etUser.setError(getResources().getString(R.string.min_caracteres));
                }
            }
        });
    }

    public class InsertTask extends AsyncTask<String,Void,String>{
        private String user, pass, name;

        @Override
        protected String doInBackground(String... params) {
            String cadena = params[0];
            user = params[1];
            name = params[2];
            pass = params[3];
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
                jsonParam.put("nombre", name);
                jsonParam.put("usuario", user);
                jsonParam.put("contrasena", pass);
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

            } catch (IOException | JSONException e) {
                e.printStackTrace();
            }
            return resultJSON;

        }

        @Override
        protected void onPostExecute(String s) {
            barra.setVisibility(View.INVISIBLE);
            if (s.equals("1")) { //La consulta se ha realizado correctamente
                Intent i = new Intent(AltaUsuario.this,Login.class);
                i.putExtra("user",user);
                i.putExtra("name", name);
                i.putExtra("pass",pass);
                startActivity(i);
                overridePendingTransition(R.anim.right_in, R.anim.left_out);
            } else if (s.equals("2")) { //Error en la consulta
                Snackbar.make(btnOk,getResources().getString(R.string.mas_tarde), Snackbar.LENGTH_LONG).show();
            }else if(s.equals("3")){ //El usuario ya existe
                Snackbar.make(btnOk,getResources().getString(R.string.usuario_existe), Snackbar.LENGTH_LONG).show();
            }
        }
    }
}