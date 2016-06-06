package com.example.usuario.redsports.preferencias;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.example.usuario.redsports.POJO.Preferencia;
import com.example.usuario.redsports.R;
import com.example.usuario.redsports.util.Dialogo;
import com.example.usuario.redsports.util.OnDialogoListener;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by USUARIO on 02/06/2016.
 */
public class Preferencias extends AppCompatActivity {

    private RecyclerView rv;
    private TextView tvPreferencias;
    private AdaptadorPreferencias adaptador;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private String idioma;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preferencias);

        prefs = getSharedPreferences("idioma_preferences", Context.MODE_PRIVATE);
        editor = prefs.edit();
        idioma = prefs.getString("idioma","");


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        TextView tvToolbar = (TextView) toolbar.findViewById(R.id.tvTituloToolbar);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RockSalt.ttf");
        tvToolbar.setTypeface(tf);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        rv = (RecyclerView) findViewById(R.id.recycler);
        final ArrayList<Preferencia> prefs = new ArrayList<>();
        prefs.add(new Preferencia(getResources().getString(R.string.perfil), BitmapFactory.decodeResource(this.getResources(), R.drawable.person)));
        prefs.add(new Preferencia(getResources().getString(R.string.idioma), BitmapFactory.decodeResource(this.getResources(), R.drawable.idioma)));
        prefs.add(new Preferencia(getResources().getString(R.string.acerca_de), BitmapFactory.decodeResource(this.getResources(), R.drawable.info)));
        adaptador = new AdaptadorPreferencias(prefs);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() { //el botón de atras mata la actividad
            @Override
            public void onClick(View v) {
                finish();
                overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
            }
        });

        rv.setAdapter(adaptador);
        rv.setHasFixedSize(true);
        rv.setLayoutManager(new LinearLayoutManager(Preferencias.this, LinearLayoutManager.VERTICAL, false));
        adaptador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = rv.getChildAdapterPosition(v);

                if(prefs.get(itemPosition).getPreferencia().equals(getResources().getString(R.string.perfil))){
                    Intent i = new Intent(Preferencias.this, PreferenciaPerfil.class);
                    startActivity(i);
                    overridePendingTransition(R.anim.right_in, R.anim.left_out);
                }else if(prefs.get(itemPosition).getPreferencia().equals(getResources().getString(R.string.idioma))){
                    final OnDialogoListener odl = new OnDialogoListener() {
                        @Override
                        public void onPreShow(View v) {
                            RadioGroup rg = (RadioGroup)v.findViewById(R.id.rgIdiomas);
                            RadioButton rbEn = (RadioButton)v.findViewById(R.id.rbEn);
                            RadioButton rbEsp = (RadioButton)v.findViewById(R.id.rbEsp);
                            switch (idioma){
                                case "en":
                                    rbEn.setChecked(true);
                                    break;
                                case "es":
                                    rbEsp.setChecked(true);
                                    break;
                                default:
                                    rbEsp.setChecked(true);
                                    break;
                            }

                        }

                        @Override
                        public void onOkSelecter(View v) {
                            RadioGroup rg = (RadioGroup)v.findViewById(R.id.rgIdiomas);
                            int id = rg.getCheckedRadioButtonId();
                            switch (id){
                                case R.id.rbEsp:
                                    Locale locale2 = new Locale("es");
                                    Locale.setDefault(locale2);
                                    Configuration config2 = new Configuration();
                                    config2.locale = locale2;
                                    Preferencias.this.getApplicationContext().getResources().updateConfiguration(config2, null);

                                    editor.putString("idioma","es");
                                    editor.commit();
                                    break;
                                case R.id.rbEn:
                                    Locale locale = new Locale("en_US");
                                    Locale.setDefault(locale);
                                    Configuration config = new Configuration();
                                    config.locale = locale;
                                    Preferencias.this.getApplicationContext().getResources().updateConfiguration(config, null);

                                    editor.putString("idioma","en");
                                    editor.commit();
                                    break;
                            }
                        }

                        @Override
                        public void onCancelSelecter(View v) {
                        }
                    };
                    Dialogo d = new Dialogo(Preferencias.this, R.layout.idiomas, odl);
                    d.show();
                }else if(prefs.get(itemPosition).getPreferencia().equals(getResources().getString(R.string.acerca_de))){
                    new AlertDialog.Builder(Preferencias.this)
                            .setTitle(R.string.acerca_de)
                            .setMessage(getResources().getString(R.string.about_us_texto))
                            .setPositiveButton(android.R.string.yes, null)
                            .setIcon(R.drawable.info)
                            .show();
                }
            }
        });


    }
}