package com.airlinespoo.locations.repository;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.City;


import java.util.List;
import java.util.stream.Collectors;
/**
 * Gestiona el almacenamiento y las consultas de ciudades en memoria.
 * Extiende las operaciones CRUD del repositorio genérico y proporciona
 * búsquedas específicas por nombre.
 */
public class CityRepository extends InMemoryRepository<City, Integer> {
    /** @return nombre de la entidad gestionada por el repositorio. */
    @Override
    protected String entityName() {
        return "ciudad";
    }
    /**
     * Busca ciudades cuyo nombre contenga el texto indicado,
     * sin distinguir entre mayúsculas y minúsculas.
     *
     * @param name texto utilizado para realizar la búsqueda.
     * @return lista de ciudades que coinciden con el criterio.
     * @throws ValidationException si el texto de búsqueda no es válido.
     */
    public List<City> searchCitiesByName(String name) {
        String validName = Validator.requireText(name, "nombre de ciudad");
        return values().stream()
            .filter(city -> city.getName().toLowerCase().contains(validName.toLowerCase()))
            .collect(Collectors.toList());
    }
}
