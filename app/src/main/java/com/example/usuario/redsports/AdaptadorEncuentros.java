package com.example.usuario.redsports;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.usuario.redsports.POJO.Encuentro;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

/**
 * Created by USUARIO on 09/05/2016.
 */
public class AdaptadorEncuentros extends RecyclerView.Adapter<AdaptadorEncuentros.EncuentrosViewHolder> implements View.OnClickListener, View.OnLongClickListener {

    private View.OnClickListener listener;
    public static Resources recursos;

    private View.OnLongClickListener longlistener;

    private ArrayList<Encuentro> datos;

    public AdaptadorEncuentros(ArrayList<Encuentro> datos) {
        this.datos = datos;
    }

    public AdaptadorEncuentros(){
        datos = new ArrayList<>();
        datos.add(new Encuentro());
    }

    @Override
    public EncuentrosViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_encuentro, viewGroup, false);
        recursos = viewGroup.getResources();
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);

        EncuentrosViewHolder tvh = new EncuentrosViewHolder(itemView);

        return tvh;
    }

    @Override
    public void onBindViewHolder(EncuentrosViewHolder viewHolder, int pos) {
        Encuentro item = datos.get(pos);

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

    public void clear(){
        datos.clear();
        notifyDataSetChanged();
    }

    public void addAll(ArrayList<Encuentro> lista){
        datos.addAll(lista);
        notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if(listener != null)
            listener.onClick(view);
    }

    @Override
    public boolean onLongClick(View v) {
        if(longlistener != null) {
            longlistener.onLongClick(v);
        }
        return true;
    }

    /*************** VIEWHOLDER *********************/
    public static class EncuentrosViewHolder  extends RecyclerView.ViewHolder {

    private TextView tvDescripcion, tvFecha, tvCapacidad;
        private ImageView imgVoy;

    public EncuentrosViewHolder(View itemView) {
        super(itemView);

        tvDescripcion = (TextView)itemView.findViewById(R.id.tvDesc);
        tvFecha = (TextView)itemView.findViewById(R.id.tvFecha);
        tvCapacidad = (TextView)itemView.findViewById(R.id.tvCapacidad);
        imgVoy = (ImageView)itemView.findViewById(R.id.imgVoy);
    }

    public void bindTitular(Encuentro e) {
        if(!e.equals(new Encuentro())){
            String[] fechaHora = obtenerFechayHora(e);
            tvDescripcion.setText(e.getDescripcion());
            tvCapacidad.setText(e.getApuntados() + "/" + e.getCapacidad());
            if(e.getApuntados() == e.getCapacidad()){
                tvCapacidad.setTextColor(recursos.getColor(R.color.colorPrimaryDark));
            }
            tvFecha.setText(fechaHora[0] + " " + fechaHora[1]);
            if(e.isAsistente()){
                imgVoy.setImageResource(R.drawable.voy);
            }
        }
    }

        private String[] obtenerFechayHora(Encuentro e){ // Formatea la fecha y la hora en el formato adecuado para mostrarla
            String fechaa = e.getFecha();
            String horaa = e.getHora();

            Date fecha = Date.valueOf(fechaa);
            SimpleDateFormat dateFormat = new SimpleDateFormat("d MMMM");

            String[] result = new String[2];

            result[0] = dateFormat.format(fecha);
            result[1] = horaa.substring(0,horaa.length()-3);

            return result;
        }
}

}