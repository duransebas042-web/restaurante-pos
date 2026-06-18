package restaurante_pos.service;

import org.springframework.stereotype.Service;
import restaurante_pos.model.Plato;
import java.util.ArrayList;
import java.util.List;

@Service
public class PlatoService {

    public List<Plato> obtenerListaPlatos() {
        List<Plato> platos = new ArrayList<>();
        platos.add(new Plato("Asado Huilense", 32000, List.of("Sopa", "Carne de Cerdo", "Arroz", "Plátano", "Arepa", "Envuelto", "Insulso")));
        platos.add(new Plato("Mojarra Asada a la Parrilla", 35000, List.of("Sopa", "Mojarra Roja", "Arroz", "Patacón", "Ensalada")));
        platos.add(new Plato("Mojarra Frita", 38000, List.of("Sopa", "Mojarra", "Arroz", "Patacón", "Ensalada")));
        platos.add(new Plato("Corte de Lomo de Res", 38000, List.of("Sopa", "Res (300g)", "Arroz", "Papa Salada", "Yuca", "Plátano", "Guacamole")));
        platos.add(new Plato("Lomo de Cerdo a la Plancha", 27000, List.of("Sopa", "Cerdo (250g)", "Arroz", "Papa Salada", "Yuca", "Plátano", "Guacamole")));
        platos.add(new Plato("Pechuga a la Plancha", 27000, List.of("Sopa", "Pechuga (250g)", "Arroz", "Papas a la Francesa", "Ensalada")));
        platos.add(new Plato("Pechuga Gratinada", 32000, List.of("Sopa", "Pechuga", "Queso Mozzarella", "Arroz", "Papas a la Francesa", "Ensalada")));
        platos.add(new Plato("Ajiaco de Pollo", 25000, List.of("Sopa", "Arroz", "Tajada de Aguacate", "Patacón", "Crema de Leche")));
        platos.add(new Plato("Arroz Sanjuanero", 27000, List.of("Sopa", "Carnes Mixtas", "Verduras", "Papas a la Francesa", "Ensalada")));
        platos.add(new Plato("Sancocho de Pollo", 28000, List.of("Sopa", "Pierna Pernil", "Arroz", "Papa", "Mazorca", "Yuca", "Ensalada")));
        platos.add(new Plato("Sancocho de Gallina Criolla", 35000, List.of("Sopa", "Pierna Pernil", "Arroz", "Papa", "Mazorca", "Yuca", "Ensalada")));
        platos.add(new Plato("Bocachico Sudado", 35000, List.of("Sopa", "Bocachico en Salsa", "Papa", "Yuca", "Mazorca", "Ensalada")));
        return platos;
    }
}
