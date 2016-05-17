package com.example.usuario.redsports.POJO;

import android.os.Parcel;
import android.os.Parcelable;


/**
 * Created by USUARIO on 08/05/2016.
 */
public class Encuentro implements Parcelable {
    private int id;
    private String descripcion;
    private int deporte_id;
    private int apuntados;
    private int capacidad;
    private String fecha;
    private String hora;
    private String lat;
    private String lon;
    private boolean asistente;


    public Encuentro() {
    }


    public Encuentro(int id, String descripcion, int deporte_id, int apuntados, int capacidad, String fecha, String hora, String lat, String lon, boolean asiste) {
        this.id = id;
        this.descripcion = descripcion;
        this.deporte_id = deporte_id;
        this.apuntados = apuntados;
        this.capacidad = capacidad;
        this.fecha = fecha;
        this.hora = hora;
        this.lat = lat;
        this.lon = lon;
        this.asistente = asiste;
    }

    protected Encuentro(Parcel in) {
        id = in.readInt();
        descripcion = in.readString();
        deporte_id = in.readInt();
        apuntados = in.readInt();
        capacidad = in.readInt();
        fecha = in.readString();
        hora = in.readString();
        lat = in.readString();
        lon = in.readString();
        asistente = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(descripcion);
        dest.writeInt(deporte_id);
        dest.writeInt(apuntados);
        dest.writeInt(capacidad);
        dest.writeString(fecha);
        dest.writeString(hora);
        dest.writeString(lat);
        dest.writeString(lon);
        dest.writeByte((byte) (asistente ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Encuentro> CREATOR = new Creator<Encuentro>() {
        @Override
        public Encuentro createFromParcel(Parcel in) {
            return new Encuentro(in);
        }

        @Override
        public Encuentro[] newArray(int size) {
            return new Encuentro[size];
        }
    };

    public boolean esEsteEncuentroDeEsteDeporte(int deporte_id) { //Si el encuentro desde el que llamamos al metodo pertenece al deporte que le pasamos por parametro devuelve true
        if (deporte_id == this.deporte_id) {
            return true;
        } else {
            return false;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public int getDeporte_id() {
        return deporte_id;
    }

    public void setDeporte_id(int deporte_id) {
        this.deporte_id = deporte_id;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public String getHora() {
        return hora;
    }

    public void setHora(String hora) {
        this.hora = hora;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }

    public int getApuntados() {
        return apuntados;
    }

    public void setApuntados(int apuntados) {
        this.apuntados = apuntados;
    }

    public boolean isAsistente() {
        return asistente;
    }

    public void setAsistente(boolean asiste) {
        this.asistente = asiste;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Encuentro encuentro = (Encuentro) o;

        if (id != encuentro.id) return false;
        if (deporte_id != encuentro.deporte_id) return false;
        if (apuntados != encuentro.apuntados) return false;
        if (capacidad != encuentro.capacidad) return false;
        if (asistente != encuentro.asistente) return false;
        if (descripcion != null ? !descripcion.equals(encuentro.descripcion) : encuentro.descripcion != null)
            return false;
        if (fecha != null ? !fecha.equals(encuentro.fecha) : encuentro.fecha != null) return false;
        if (hora != null ? !hora.equals(encuentro.hora) : encuentro.hora != null) return false;
        if (lat != null ? !lat.equals(encuentro.lat) : encuentro.lat != null) return false;
        return lon != null ? lon.equals(encuentro.lon) : encuentro.lon == null;

    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (descripcion != null ? descripcion.hashCode() : 0);
        result = 31 * result + deporte_id;
        result = 31 * result + apuntados;
        result = 31 * result + capacidad;
        result = 31 * result + (fecha != null ? fecha.hashCode() : 0);
        result = 31 * result + (hora != null ? hora.hashCode() : 0);
        result = 31 * result + (lat != null ? lat.hashCode() : 0);
        result = 31 * result + (lon != null ? lon.hashCode() : 0);
        result = 31 * result + (asistente ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Encuentro{" +
                "id=" + id +
                ", descripcion='" + descripcion + '\'' +
                ", deporte_id=" + deporte_id +
                ", apuntados=" + apuntados +
                ", capacidad=" + capacidad +
                ", fecha='" + fecha + '\'' +
                ", hora='" + hora + '\'' +
                ", lat='" + lat + '\'' +
                ", lon='" + lon + '\'' +
                ", asiste=" + asistente +
                '}';
    }
}
