package com.example.usuario.redsports.fragments;


import android.content.Intent;
import android.os.Bundle;

import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.usuario.redsports.AdaptadorEncuentros;
import com.example.usuario.redsports.AltaEncuentro;
import com.example.usuario.redsports.POJO.Encuentro;
import com.example.usuario.redsports.R;

import java.util.ArrayList;


public class FragmentEncuentros1 extends Fragment {

    private static ArrayList<Encuentro> encuentros;
    private RecyclerView recView;
    private FloatingActionButton fab;
    private static int deporte_id;
    View viewFragment;

    public static FragmentEncuentros1 newInstance(ArrayList<Encuentro> e, int id) {
        FragmentEncuentros1 fragment = new FragmentEncuentros1();
        encuentros = e;
        deporte_id = id;
        return fragment;
    }

    public FragmentEncuentros1() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewFragment = inflater.inflate(R.layout.fragment_encuentros,container,false);
        fab = (FloatingActionButton)viewFragment.findViewById(R.id.floatingButton);
        recView = (RecyclerView)viewFragment.findViewById(R.id.RecView);

        final AdaptadorEncuentros adaptador = new AdaptadorEncuentros(encuentros);
        adaptador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recView.getChildAdapterPosition(v);
                Encuentro item = encuentros.get(itemPosition);
                Log.v("seleccionado",item.toString());

                //Intent para ver ese encuentro
            }
        });
        recView.setAdapter(adaptador);

        recView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), AltaEncuentro.class);
                Log.v("deporte_id antes",":" + deporte_id);
                i.putExtra("deporte_id",deporte_id);
                startActivity(i);
            }
        });

        return viewFragment;
    }


}
