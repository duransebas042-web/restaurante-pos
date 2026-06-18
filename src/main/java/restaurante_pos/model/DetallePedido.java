package restaurante_pos.model;

import java.util.List;

public class DetallePedido {

    private String nombrePlato;

    private List<String> acompanamientos;

    private String bebida;

    private String cambio;

    public DetallePedido(
            String nombrePlato,
            List<String> acompanamientos,
            String bebida,
            String cambio) {

        this.nombrePlato = nombrePlato;
        this.acompanamientos = acompanamientos;
        this.bebida = bebida;
        this.cambio = cambio;
    }

    public String getNombrePlato() {
        return nombrePlato;
    }

    public List<String> getAcompanamientos() {
        return acompanamientos;
    }

    public String getBebida() {
        return bebida;
    }

    public String getCambio() {
        return cambio;
    }
}