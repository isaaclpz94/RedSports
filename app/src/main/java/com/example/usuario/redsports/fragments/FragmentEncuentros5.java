package com.example.usuario.redsports.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.usuario.redsports.AdaptadorEncuentros;
import com.example.usuario.redsports.POJO.Encuentro;
import com.example.usuario.redsports.R;

import java.util.ArrayList;


public class FragmentEncuentros5 extends Fragment {

    private static ArrayList<Encuentro> encuentros;
    private RecyclerView recView;
    private static int deporte_id;
    View viewFragment;

    public static FragmentEncuentros5 newInstance(ArrayList<Encuentro> e, int id) {
        FragmentEncuentros5 fragment = new FragmentEncuentros5();
        encuentros = e;
        deporte_id = id;
        return fragment;
    }

    public FragmentEncuentros5() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        viewFragment = inflater.inflate(R.layout.fragment_encuentros,container,false);
        recView = (RecyclerView)viewFragment.findViewById(R.id.RecView);

        final AdaptadorEncuentros adaptador = new AdaptadorEncuentros(encuentros);
        adaptador.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int itemPosition = recView.getChildAdapterPosition(v);
                Encuentro item = encuentros.get(itemPosition);

                Intent i = new Intent(getActivity(), com.example.usuario.redsports.Encuentro.class);
                i.putExtra("encuentro", item);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.right_in, R.anim.left_out);
            }
        });
        recView.setAdapter(adaptador);

        recView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,false));

        return viewFragment;
    }


}
