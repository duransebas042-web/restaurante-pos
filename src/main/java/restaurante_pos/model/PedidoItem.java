package restaurante_pos.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
@Table(name = "pedido_items")
public class PedidoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String mesa;
    private String plato;
    private String bebida;
    private String observacion;
    private String atendidoPor; // 🚀 Guardará el nombre real del empleado (ej: "Carlos")

    @Enumerated(EnumType.STRING)
    private EstadoPedido estado;

    private String estadoComida;
    private String estadoBebida;

    @ElementCollection
    private List<String> acompanamientos;

    public PedidoItem() {}

    public PedidoItem(String mesa, String plato, List<String> acompanamientos, String bebida, String observacion) {
        this.mesa = mesa;
        this.plato = plato;
        this.acompanamientos = acompanamientos;
        this.bebida = bebida;
        this.observacion = observacion;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getMesa() { return mesa; }
    public void setMesa(String mesa) { this.mesa = mesa; }
    public String getPlato() { return plato; }
    public void setPlato(String plato) { this.plato = plato; }
    public String getBebida() { return bebida; }
    public void setBebida(String bebida) { this.bebida = bebida; }
    public String getObservacion() { return observacion; }
    public void setObservacion(String observacion) { this.observacion = observacion; }
    public String getAtendidoPor() { return atendidoPor; }
    public void setAtendidoPor(String atendidoPor) { this.atendidoPor = atendidoPor; }
    public EstadoPedido getEstado() { return estado; }
    public void setEstado(EstadoPedido estado) { this.estado = estado; }
    public String getEstadoComida() { return estadoComida; }
    public void setEstadoComida(String estadoComida) { this.estadoComida = estadoComida; }
    public String getEstadoBebida() { return estadoBebida; }
    public void setEstadoBebida(String estadoBebida) { this.estadoBebida = estadoBebida; }
    public List<String> getAcompanamientos() { return acompanamientos; }
    public void setAcompanamientos(List<String> acompanamientos) { this.acompanamientos = acompanamientos; }
}
