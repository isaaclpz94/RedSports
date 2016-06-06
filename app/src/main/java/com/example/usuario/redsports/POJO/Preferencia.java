package com.example.usuario.redsports.POJO;

import android.graphics.Bitmap;
import android.widget.ImageView;

/**
 * Created by USUARIO on 03/06/2016.
 */
public class Preferencia {

    private String preferencia;
    private Bitmap imagen;

    public Preferencia(String preferencia, Bitmap imagen) {
        this.preferencia = preferencia;
        this.imagen = imagen;
    }

    public String getPreferencia() {
        return preferencia;
    }

    public void setPreferencia(String preferencia) {
        this.preferencia = preferencia;
    }

    public Bitmap getImagen() {
        return imagen;
    }

    public void setImagen(Bitmap imagen) {
        this.imagen = imagen;
    }

    @Override
    public String toString() {
        return "Preferencia{" +
                "preferencia='" + preferencia + '\'' +
                ", imagen=" + imagen +
                '}';
    }
}
