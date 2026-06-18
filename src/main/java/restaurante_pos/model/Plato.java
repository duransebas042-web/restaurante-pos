package restaurante_pos.model;

import java.util.List;

public class Plato {
    private String nombre;
    private double precio;
    private List<String> acompanamientosPorDefecto; // 👈 NUEVO: Lista propia del plato

    public Plato() {}

    public Plato(String nombre, double precio, List<String> acompanamientosPorDefecto) {
        this.nombre = nombre;
        this.precio = precio;
        this.acompanamientosPorDefecto = acompanamientosPorDefecto;
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public List<String> getAcompanamientosPorDefecto() { return acompanamientosPorDefecto; }
    public void setAcompanamientosPorDefecto(List<String> acompanamientosPorDefecto) { 
        this.acompanamientosPorDefecto = acompanamientosPorDefecto; 
    }
}
