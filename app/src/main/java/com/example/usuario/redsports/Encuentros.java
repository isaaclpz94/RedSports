package com.example.usuario.redsports;

import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.example.usuario.redsports.POJO.Deporte;
import com.example.usuario.redsports.POJO.Encuentro;
import java.util.ArrayList;

public class Encuentros extends AppCompatActivity {

    private ArrayList<Deporte> deportes = new ArrayList<>();
    private ArrayList<Encuentro> encuentros = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_encuentros);

        Bundle extras = getIntent().getExtras();
        if(extras != null){
             deportes = extras.getParcelableArrayList("deportes");
             encuentros = extras.getParcelableArrayList("encuentros");
        }else{
            Intent i = new Intent(this,Principal.class);
            startActivity(i);
            finish();
        }

        //obtengo la toolbar, le doy estilo y le pongo el boton de atrás
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        assert toolbar != null;
        TextView tvToolbar = (TextView)toolbar.findViewById(R.id.tvTituloToolbar);
        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/RockSalt.ttf");
        tvToolbar.setTypeface(tf);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() { //el botón de atras mata la actividad
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        AdaptadorPestañas pagerAdapter = new AdaptadorPestañas(getSupportFragmentManager(), Encuentros.this,deportes,encuentros);
        assert viewPager != null;
        viewPager.setAdapter(pagerAdapter);


        TabLayout tabLayout = (TabLayout) findViewById(R.id.appbartabs);
        assert tabLayout != null;
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setupWithViewPager(viewPager);

        // Iterate over all tabs and set the custom view
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            tab.setCustomView(pagerAdapter.getTabView(i));
        }
    }
}
