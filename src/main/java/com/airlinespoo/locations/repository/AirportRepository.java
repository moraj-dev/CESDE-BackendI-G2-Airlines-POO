package com.airlinespoo.locations.repository;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * Gestiona el almacenamiento y las consultas de aeropuertos en memoria.
 * Extiende las operaciones CRUD del repositorio genérico y proporciona
 * búsquedas específicas para el módulo de ubicaciones.
 */
public class AirportRepository extends InMemoryRepository<Airport, UUID> {
    /** @return nombre de la entidad gestionada por el repositorio. */
    @Override
    protected String entityName() {
        return "aeropuerto";
    }
    /**
     * Obtiene los aeropuertos asociados a una ciudad determinada.
     *
     * @param city ciudad cuyos aeropuertos se desean consultar.
     * @return lista de aeropuertos pertenecientes a la ciudad.
     * @throws ValidationException si la ciudad es null.
     */
    public List<Airport> listAirportsByCity(City city){
        City validCity = Validator.requireNotNull(city, "ciudad");
        return values().stream()
            .filter(airport -> airport.getCity().equals(validCity))
            .collect(Collectors.toList());
    }
    /**
     * Busca aeropuertos cuyo nombre contenga el texto indicado,
     * sin distinguir entre mayúsculas y minúsculas.
     *
     * @param name texto utilizado para realizar la búsqueda.
     * @return lista de aeropuertos que coinciden con el criterio.
     * @throws ValidationException si el texto de búsqueda no es válido.
     */
    public List<Airport> searchAirportsByName(String name) {
        String validName = Validator.requireText(name, "nombre de aeropuerto");
        return values().stream()
            .filter(airport -> airport.getName().toLowerCase().contains(validName.toLowerCase()))
            .collect(Collectors.toList());
    }
    /**
     * Cuenta los aeropuertos asociados a una ciudad.
     *
     * @param city ciudad cuyos aeropuertos se desean contar.
     * @return cantidad de aeropuertos asociados a la ciudad.
     * @throws ValidationException si la ciudad es null.
     */
    public long countAirportsByCity(City city){
        City validCity = Validator.requireNotNull(city, "ciudad");
        return values().stream()
            .filter(airport -> airport.getCity().equals(validCity))
            .count();
    }
    /**
     * Comprueba si una ciudad tiene aeropuertos asociados.
     *
     * @param city ciudad que se desea verificar.
     * @return true si existe al menos un aeropuerto asociado; false en caso contrario.
     * @throws ValidationException si la ciudad es null.
     */
    public boolean existsByCity(City city){
        City validCity = Validator.requireNotNull(city, "ciudad");
        return values().stream()
            .anyMatch(airport -> airport.getCity().equals(validCity));
    }
}
