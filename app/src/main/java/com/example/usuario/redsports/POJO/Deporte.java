package com.example.usuario.redsports.POJO;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by USUARIO on 08/05/2016.
 */
public class Deporte implements Parcelable{
    private int id;
    private String nombre;
    private Bitmap icono;

    public Deporte(){
    }

    public Deporte(int id, String nombre, Bitmap icono){
        this.id = id;
        this.nombre = nombre;
        this.icono = icono;
    }


    protected Deporte(Parcel in) {
        id = in.readInt();
        nombre = in.readString();
        icono = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nombre);
        dest.writeParcelable(icono, flags);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Deporte> CREATOR = new Creator<Deporte>() {
        @Override
        public Deporte createFromParcel(Parcel in) {
            return new Deporte(in);
        }

        @Override
        public Deporte[] newArray(int size) {
            return new Deporte[size];
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Bitmap getIcono() {
        return icono;
    }

    public void setIcono(Bitmap icono) {
        this.icono = icono;
    }

    @Override
    public String toString() {
        return "Deporte{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", icono=" + icono +
                '}';
    }
}
