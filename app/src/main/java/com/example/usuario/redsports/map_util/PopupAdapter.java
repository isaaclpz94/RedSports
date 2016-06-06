package com.example.usuario.redsports.map_util;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.usuario.redsports.POJO.Encuentro;
import com.example.usuario.redsports.R;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.model.Marker;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class PopupAdapter implements InfoWindowAdapter {
    private View popup=null;
    private LayoutInflater inflater=null;

    public PopupAdapter(LayoutInflater inflater) {
        this.inflater=inflater;
    }

    @Override
    public View getInfoWindow(Marker marker) {
        return(null);
    }

    @SuppressLint("InflateParams")
    @Override
    public View getInfoContents(Marker marker) {
        if (popup == null) {
            popup=inflater.inflate(R.layout.popup, null);
        }
        String info[] = marker.getSnippet().split("\\|");
        String capacidad = info[0];
        String fecha = info[1];
        String deporte = info[2];

        fecha = obtenerFechayHora(fecha);

        TextView tv=(TextView)popup.findViewById(R.id.title);
        tv.setText(marker.getTitle());

        tv=(TextView)popup.findViewById(R.id.asistencia);
        tv.setText(popup.getResources().getString(R.string.capacidad) + capacidad);

        tv=(TextView)popup.findViewById(R.id.fecha);
        tv.setText(popup.getResources().getString(R.string.fecha_) + fecha);

        tv=(TextView)popup.findViewById(R.id.deporte);
        tv.setText(popup.getResources().getString(R.string.deporte) + deporte);
        return(popup);
    }

    private String obtenerFechayHora(String e){ // Formatea la fecha y la hora en el formato adecuado para mostrarla

        Date fecha = Date.valueOf(e);
        SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM");

        return dateFormat.format(fecha);
    }
}
