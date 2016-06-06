package com.example.usuario.redsports.preferencias;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.usuario.redsports.POJO.Preferencia;
import com.example.usuario.redsports.R;

import java.util.ArrayList;

/**
 * Created by USUARIO on 09/05/2016.
 */
public class AdaptadorPreferencias extends RecyclerView.Adapter<AdaptadorPreferencias.PreferenciasViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private View.OnClickListener listener;

    private View.OnLongClickListener longlistener;

    private ArrayList<Preferencia> datos;

    public AdaptadorPreferencias(ArrayList<Preferencia> datos) {
        this.datos = datos;
    }

    public AdaptadorPreferencias() {
        datos = new ArrayList<>();
    }

    @Override
    public PreferenciasViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_preferencias, viewGroup, false);
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        PreferenciasViewHolder tvh = new PreferenciasViewHolder(itemView);

        return tvh;
    }

    @Override
    public void onBindViewHolder(PreferenciasViewHolder viewHolder, int pos) {
        Preferencia item = datos.get(pos);

        viewHolder.bindTitular(item);
    }

    public void setOnClickListener(View.OnClickListener listener) {
        this.listener = listener;
    }

    public void setOnLongClickListener(View.OnLongClickListener longlistener) {
        this.longlistener = longlistener;
    }

    @Override
    public int getItemCount() {
        return datos.size();
    }

    @Override
    public void onClick(View view) {
        if (listener != null)
            listener.onClick(view);
    }

    @Override
    public boolean onLongClick(View v) {
        if (longlistener != null) {
            longlistener.onLongClick(v);
        }
        return true;
    }

    /***************
     * VIEWHOLDER
     *********************/
    public static class PreferenciasViewHolder extends RecyclerView.ViewHolder {

        private TextView tvPreferencia;
        private ImageView imgPref;

        public PreferenciasViewHolder(View itemView) {
            super(itemView);

            tvPreferencia = (TextView) itemView.findViewById(R.id.tvPreferencia);
            imgPref = (ImageView) itemView.findViewById(R.id.imgPreferencia);
        }

        public void bindTitular(Preferencia e) {
            tvPreferencia.setText(e.getPreferencia());
            imgPref.setImageBitmap(e.getImagen());
        }
    }

}