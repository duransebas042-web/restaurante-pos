package restaurante_pos.service;

import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import restaurante_pos.model.Mesa;
import restaurante_pos.repository.MesaRepository;
import java.util.List;

@Service
public class MesaService {

    // 1. Atributo inmutable sin @Autowired
    private final MesaRepository mesaRepository;

    // 2. Inyección por constructor obligatoria
    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    /**
     * Este método se ejecuta automáticamente al iniciar la aplicación.
     * Si la tabla 'mesas' está vacía en la base de datos, inserta el salón inicial.
     */
    @PostConstruct
    public void inicializarMesasDefecto() {
        if (mesaRepository.count() == 0) {
            mesaRepository.save(new Mesa("Mesa 1", "LIBRE"));
            mesaRepository.save(new Mesa("Mesa 2", "LIBRE"));
            mesaRepository.save(new Mesa("Mesa 3", "LIBRE"));
            mesaRepository.save(new Mesa("Mesa 4", "LIBRE"));
            mesaRepository.save(new Mesa("Mesa 5", "LIBRE"));
            System.out.println("🚀 Base de datos vacía: Se han creado las mesas iniciales del restaurante.");
        }
    }

    // Obtiene todas las mesas directamente desde la base de datos relacional
    public List<Mesa> obtenerMesas() {
        return mesaRepository.findAll();
    }

    // Permite guardar o actualizar una mesa (útil para cuando el sistema cree mesas especiales)
    public void guardarMesa(Mesa mesa) {
        mesaRepository.save(mesa);
    }
}
