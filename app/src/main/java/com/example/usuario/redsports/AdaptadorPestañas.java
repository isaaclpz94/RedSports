package com.example.usuario.redsports;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.usuario.redsports.POJO.Deporte;
import com.example.usuario.redsports.POJO.Encuentro;
import com.example.usuario.redsports.fragments.FragmentEncuentros1;
import com.example.usuario.redsports.fragments.FragmentEncuentros2;
import com.example.usuario.redsports.fragments.FragmentEncuentros3;
import com.example.usuario.redsports.fragments.FragmentEncuentros4;
import com.example.usuario.redsports.fragments.FragmentEncuentros5;

import java.util.ArrayList;

public class AdaptadorPestañas extends FragmentPagerAdapter {

    private ArrayList<Deporte> deportes;
    private ArrayList<Encuentro> encuentros;
    private ArrayList<Encuentro> encuentrosPorId;
    private Context context;

    public AdaptadorPestañas(FragmentManager fm, Context context, ArrayList<Deporte> deportes, ArrayList<Encuentro> encuentros) {
        super(fm);
        this.deportes = deportes;
        this.encuentros = encuentros;
        this.context = context;
    }

    @Override
    public int getCount() {
        return deportes.size();
    }

    @Override
    public Fragment getItem(int position) { // soporta cinco pestañas distintas

        Fragment f = null;

        switch(position) {
            case 0:
                ArrayList<Encuentro> e1 = getEncuentrosByDeporteId(deportes.get(0).getId());
                f = FragmentEncuentros1.newInstance(e1, deportes.get(0).getId());
                break;
            case 1:
                ArrayList<Encuentro> e2 = getEncuentrosByDeporteId(deportes.get(1).getId());
                f = FragmentEncuentros2.newInstance(e2, deportes.get(1).getId());
                break;
            case 2:
                ArrayList<Encuentro> e3 = getEncuentrosByDeporteId(deportes.get(2).getId());
                f = FragmentEncuentros3.newInstance(e3, deportes.get(2).getId());
                break;
            case 3:
                ArrayList<Encuentro> e4 = getEncuentrosByDeporteId(deportes.get(3).getId());
                f = FragmentEncuentros4.newInstance(e4, deportes.get(3).getId());
                break;
            case 4:
                ArrayList<Encuentro> e5 = getEncuentrosByDeporteId(deportes.get(4).getId());
                f = FragmentEncuentros5.newInstance(e5, deportes.get(4).getId());
                break;
        }
        return f;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return deportes.get(position).getNombre();
    }

    private ArrayList<Encuentro> getEncuentrosByDeporteId(int deporte_id){ // le pasamos el id de un deporte y nos devulve un ArrayList con los encuentros asociados a ese deporte

        ArrayList<Encuentro> result = new ArrayList<>();

        for(Encuentro e : encuentros){
            if(e.esEsteEncuentroDeEsteDeporte(deporte_id))
                result.add(e);
        }
        return result;
    }

    public View getTabView(int position) { //para el layout de cada pestaña
        View v = LayoutInflater.from(context).inflate(R.layout.item_tab, null);
        TextView tv = (TextView) v.findViewById(R.id.tvSports);
        tv.setText(deportes.get(position).getNombre());
        ImageView img = (ImageView) v.findViewById(R.id.imageView2);
        img.setImageBitmap(deportes.get(position).getIcono());
        return v;
    }
}
