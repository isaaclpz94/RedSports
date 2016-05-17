package com.example.usuario.redsports;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.usuario.redsports.splash.SplashScreenSports;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

public class AltaEncuentro extends AppCompatActivity{

    private final String IP = "http://webservicesports.esy.es";
    private final String INSERT_ENCUENTRO = IP + "/insertar_encuentro.php";
    private final String INSERT_ENCUENTRO_USUARIO = IP + "/insertar_usuario_encuentro.php";
    private NumberPicker num;
    private DatePickerDialog fromDatePickerDialog;
    private TextView tvSubtitulo, tvTitulo;
    private EditText etDescripcion;
    private SimpleDateFormat dateFormatter, format;
    private Button btnFecha, btnHora, btnCancelar, btnCrear;
    private TimePickerDialog mTimePicker;
    private int deporte_id;
    private Spinner spinner;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;

    //Variables para hacer las inserciones
    private int capacidad = 2, apuntados = 1;
    private String fecha, hora, descripcion, lat="latt", lon="lonn", creado_id, usuario_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alta_encuentro);

        //el id del deporte del que vamos a crear un encuentro
        Bundle e = getIntent().getExtras();
        if (e != null) {
            deporte_id = e.getInt("deporte_id");
            Log.v("deporte_id despues", ":" + deporte_id);
        } else {
            finish();
        }

        //Obtenemos la id del usuario logueado para luego hacer la inserción en encuentros_usuarios
        prefs = getSharedPreferences("login_preferences", Context.MODE_PRIVATE);
        editor = prefs.edit();
        usuario_id = prefs.getString("ID", "");

        //findViewById
        spinner = (Spinner)findViewById(R.id.spinner);
        etDescripcion = (EditText)findViewById(R.id.etDesc);
        tvTitulo = (TextView)findViewById(R.id.tvTituloToolbar);
        tvSubtitulo  = (TextView)findViewById(R.id.tvSubtituloToolbar);
        btnFecha = (Button)findViewById(R.id.btnFecha);
        btnHora = (Button)findViewById(R.id.btnHora);
        btnCancelar = (Button)findViewById(R.id.btnCancel);
        btnCrear = (Button)findViewById(R.id.btnCrear);
        num = (NumberPicker) findViewById(R.id.numberPicker);

        //Fuente de letra de la toolbar
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RockSalt.ttf");
        tvTitulo.setTypeface(tf);
        tvSubtitulo.setTypeface(tf);

        //Spinner
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("spinner","posicion " + position);
                if(position == 1){ //Si irá acompañado, mostramos dialogo para ver con cuantos va
                    final Dialog d = new Dialog(AltaEncuentro.this);
                    d.setTitle("¿Con cuántas personas?");
                    d.setContentView(R.layout.number_dialog);
                    Button b1 = (Button) d.findViewById(R.id.button1);
                    Button b2 = (Button) d.findViewById(R.id.button2);
                    final NumberPicker np = (NumberPicker) d.findViewById(R.id.numberPicker1);
                    np.setMaxValue(capacidad-2); //podrá ir acompañado de dos personas menos de la capacidad que tenga el encuentro
                    np.setMinValue(1);
                    np.setWrapSelectorWheel(false);
                    b1.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v) {
                            apuntados = np.getValue() + 1;
                            Log.v("number_spinner", apuntados+"");
                            d.dismiss();
                        }
                    });
                    b2.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v) {
                            d.dismiss();
                        }
                    });
                    d.show();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.v("spinner","nada seleccionado");
            }
        });

        //NumberPicker
        num.setMinValue(2);
        num.setMaxValue(20);
        num.setWrapSelectorWheel(false);
        num.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                capacidad = newVal;
                Log.v("numberpicker",capacidad+"");
            }
        });

        //Date/TimePickers
        fechaDialog();
        timeDialog();

        //OnClicks
        btnFecha.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromDatePickerDialog.show();
            }
        });

        btnHora.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mTimePicker.show();
            }
        });

        btnCancelar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnCrear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(fecha == null || hora == null){
                    Snackbar.make(v,"Selecciona fecha y hora",Snackbar.LENGTH_SHORT).show();
                }else{
                    descripcion = etDescripcion.getText().toString().trim();
                    if(descripcion.equals("")){
                        etDescripcion.setError("Inserta una descripción");
                    }else{
                        etDescripcion.setError(null);
                        new InserEncuentrotTask().execute(INSERT_ENCUENTRO,descripcion, deporte_id+"",apuntados+"", capacidad+"", fecha, hora, lat, lon);
                    }
                }
            }
        });
    }

    /*************** INSERTAR ENCUENTRO OBTENIENDO SU ID **********************************/
    public class InserEncuentrotTask extends AsyncTask<String,Void,String> {
        private String desc, deporte, apuntados, capacidad, fecha, hora, lat, lon;

        @Override
        protected String doInBackground(String... params) {
            String cadena = params[0];
            desc = params[1];
            deporte = params[2];
            apuntados = params[3];
            capacidad = params[4];
            fecha = params[5];
            hora = params[6];
            lat = params[7];
            lon = params[8];
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
                jsonParam.put("descripcion", desc);
                jsonParam.put("deporte_id", deporte);
                jsonParam.put("apuntados", apuntados);
                jsonParam.put("capacidad", capacidad);
                jsonParam.put("fecha", fecha);
                jsonParam.put("hora", hora);
                jsonParam.put("lat", lat);
                jsonParam.put("lon", lon);
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
                    if (resultJSON.equals("1")) {      // hay encuentros que mostrar
                        creado_id = respuestaJSON.getJSONObject("encuentro").getString("ID"); //almaceno el id del partido que acabo de crear
                    }
                    return resultJSON;
                }

            } catch (JSONException | IOException e) {
                e.printStackTrace();
            }
            return resultJSON;

        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("1")) { //La consulta se ha realizado correctamente
                Log.v("encuentro_creado","ID:" + creado_id);
                new InsertEncuentroUsuarioTask().execute(INSERT_ENCUENTRO_USUARIO, usuario_id, creado_id);
            } else if(s.equals("2")) { //Error en la consulta
                Snackbar.make(btnCrear,"Ha habido un error, pruebe de nuevo más tarde", Snackbar.LENGTH_LONG).show();
            }else if(s.equals("")){
                Snackbar.make(btnCrear,"Comprueba tu conexión de internet", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    public class InsertEncuentroUsuarioTask extends AsyncTask<String,Void,String> {
        private String usuario_id, encuentro_id;

        @Override
        protected String doInBackground(String... params) {
            String cadena = params[0];
            URL url;
            String resultJSON = "";

            try {
                HttpURLConnection urlConn;
                usuario_id = params[1];
                encuentro_id = params[2];
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
                        //response+=line;
                    }

                    //Creamos un objeto JSONObject para poder acceder a los atributos (campos) del objeto.
                    JSONObject respuestaJSON = new JSONObject(result.toString());   //Creo un JSONObject a partir del StringBuilder pasado a cadena
                    //Accedemos al vector de resultados

                    resultJSON = respuestaJSON.getString("estado");   // estado es el nombre del campo en el JSON
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return resultJSON;

        }

        @Override
        protected void onPostExecute(String s) {
            if (s.equals("1")) { //La consulta se ha realizado correctamente
                Intent i = new Intent(AltaEncuentro.this, SplashScreenSports.class);
                startActivity(i);
                finish();
            } else if(s.equals("2")) { //Error en la consulta
                Snackbar.make(btnCrear,"Ha habido un error, pruebe de nuevo más tarde", Snackbar.LENGTH_LONG).show();
            }else if(s.equals("")){
                Snackbar.make(btnCrear,"Comprueba tu conexión de internet", Snackbar.LENGTH_LONG).show();
            }
        }
    }

    /********************** DATE/TIMEPICKERS *****************************/
    private void timeDialog(){

        Calendar mcurrentTime = Calendar.getInstance();
        final int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
        int minute = mcurrentTime.get(Calendar.MINUTE);
        mTimePicker = new TimePickerDialog(AltaEncuentro.this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                if(selectedMinute < 10){ //si selecciona las 16:05 sale "16:5", esto lo correjirá
                    //tvHora.setText( selectedHour + ": 0" + selectedMinute);
                    btnHora.setText(selectedHour + ":0" + selectedMinute);
                }else{
                    //tvHora.setText( selectedHour + ":" + selectedMinute);
                    btnHora.setText(selectedHour + ":" + selectedMinute);
                }
                hora = btnHora.getText().toString().trim() + ":00";
                Log.v("timepicker",hora);
            }
        }, hour, minute, true);//Yes 24 hour time
        mTimePicker.setTitle("Selecciona la hora");
    }

    private void fechaDialog(){
        dateFormatter = new SimpleDateFormat("dd-MMM", Locale.getDefault());
        format = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Calendar newCalendar = Calendar.getInstance();
        fromDatePickerDialog = createDialogWithoutDateField();
        /*fromDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                tvFecha.setText(dateFormatter.format(newDate.getTime()));
            }

        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        fromDatePickerDialog.setTitle("Selecciona la fecha");*/
    }

    private DatePickerDialog createDialogWithoutDateField() { //calendario sin seleccion de año
        Calendar newCalendar = Calendar.getInstance();
        DatePickerDialog dpd = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                //tvFecha.setText(dateFormatter.format(newDate.getTime()));
                btnFecha.setText(dateFormatter.format(newDate.getTime()));
                fecha = format.format(newDate.getTime());
                Log.v("datepicker",fecha);
            }
        }, newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));
        try {
            java.lang.reflect.Field[] datePickerDialogFields = dpd.getClass().getDeclaredFields();
            for (java.lang.reflect.Field datePickerDialogField : datePickerDialogFields) {
                if (datePickerDialogField.getName().equals("mDatePicker")) {
                    datePickerDialogField.setAccessible(true);
                    DatePicker datePicker = (DatePicker) datePickerDialogField.get(dpd);
                    java.lang.reflect.Field[] datePickerFields = datePickerDialogField.getType().getDeclaredFields();
                    for (java.lang.reflect.Field datePickerField : datePickerFields) {
                        Log.i("test", datePickerField.getName());
                        if ("mYearSpinner".equals(datePickerField.getName())) {
                            datePickerField.setAccessible(true);
                            Object dayPicker = datePickerField.get(datePicker);
                            ((View) dayPicker).setVisibility(View.GONE);
                        }
                    }
                }
            }
        }
        catch (Exception ex) {
        }
        return dpd;
    }
}
