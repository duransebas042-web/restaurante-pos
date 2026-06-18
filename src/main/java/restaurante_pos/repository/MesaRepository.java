package restaurante_pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import restaurante_pos.model.Mesa;
import java.util.Optional;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
    
    // Método personalizado para buscar una mesa directamente por su nombre (ej: "Mesa 1")
    Optional<Mesa> findByNombre(String nombre);
}
