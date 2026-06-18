package restaurante_pos.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nombre;
    private String loginPin; // Aquí guardaremos el PIN ya encriptado 🔒
    private String rol;      // ADMINISTRADOR, MESERO, CAJERO, COCINA

    public Usuario() {
    }

    public Usuario(String nombre, String loginPin, String rol) {
        this.nombre = nombre;
        this.loginPin = loginPin;
        this.rol = rol;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getLoginPin() { return loginPin; }
    public void setLoginPin(String loginPin) { this.loginPin = loginPin; }

    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
}
