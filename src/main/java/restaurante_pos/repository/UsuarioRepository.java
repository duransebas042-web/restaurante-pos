package restaurante_pos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import restaurante_pos.model.Usuario;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    // Nos servirá para buscar un usuario por su nombre o coincidencia en el login
    Optional<Usuario> findByNombre(String nombre);
}
