package com.airlinespoo.locations.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;
import com.airlinespoo.locations.repository.AirportRepository;
import com.airlinespoo.locations.repository.CityRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
/**
 * Gestiona las operaciones y reglas de negocio relacionadas con ciudades
 * y aeropuertos.
 */
public class LocationService {
    private final CityRepository cityRepository;
    private final AirportRepository airportRepository;
    private final IdGenerator idGenerator;
    /**
     * Crea el servicio con los repositorios y el generador de identificadores necesarios.
     *
     * @param cityRepository repositorio de ciudades.
     * @param airportRepository repositorio de aeropuertos.
     * @param idGenerator generador de identificadores para las ciudades.
     * @throws ValidationException si alguna dependencia es null.
     */
    public LocationService(CityRepository cityRepository, AirportRepository airportRepository, IdGenerator idGenerator) {
        this.cityRepository = Validator.requireNotNull(cityRepository, "repositorio de ciudades");
        this.airportRepository = Validator.requireNotNull(airportRepository, "repositorio de aeropuertos");
        this.idGenerator = Validator.requireNotNull(idGenerator, "generador de ids");
    }
    /**
     * Registra una nueva ciudad.
     *
     * @param name nombre de la ciudad.
     * @return ciudad creada.
     * @throws ValidationException si el nombre no es válido.
     * @throws DuplicateException si ya existe una ciudad con ese nombre.
     */
    public City createCity(String name) {
        String validName = Validator.requireText(name, "nombre de ciudad", 20);
        if (cityRepository.findAll().stream()
            .anyMatch(city -> city.getName().equalsIgnoreCase(validName))){
            throw new DuplicateException("Ya existe una ciudad con el nombre " + validName);
        }
        return cityRepository.create(new City(idGenerator.next(),validName));
    }
    /**
     * Busca una ciudad por su identificador.
     *
     * @param id identificador de la ciudad.
     * @return ciudad encontrada.
     * @throws ValidationException si el identificador no es válido.
     * @throws NotFoundException si la ciudad no existe.
     */
    public City getCity(int id) {
        int validId = Math.toIntExact(Validator.requirePositive(id, "id de ciudad"));
        Optional<City> city = cityRepository.findById(validId);
        if (city.isEmpty()){
            throw new NotFoundException("ciudad", validId);
        }
        return city.get();
    }
    /**
     * Obtiene todas las ciudades ordenadas alfabéticamente por nombre.
     *
     * @return lista de ciudades.
     */
    public List<City> listCities(){
        return cityRepository.findAll().stream()
            .sorted(Comparator.comparing(City::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }
    /**
     * Actualiza el nombre de una ciudad existente.
     *
     * @param id identificador de la ciudad.
     * @param name nuevo nombre de la ciudad.
     * @return ciudad actualizada.
     * @throws ValidationException si el identificador o el nombre no son válidos.
     * @throws NotFoundException si la ciudad no existe.
     * @throws DuplicateException si otra ciudad ya tiene ese nombre.
     */
    public City updateCity(int id, String name) {
        int validId = Math.toIntExact(Validator.requirePositive(id, "id de ciudad"));
        String validName = Validator.requireText(name, "nombre de ciudad", 20);
        Optional<City> foundCity = cityRepository.findById(validId);
        if (foundCity.isEmpty()) {
            throw new NotFoundException("ciudad", validId);
        }
        boolean duplicate = cityRepository.findAll().stream()
            .anyMatch(existingCity ->
                existingCity.getName().equalsIgnoreCase(validName)
                    && !existingCity.getId().equals(validId)
            );
        if (duplicate) {
            throw new DuplicateException(
                "Ya existe una ciudad con el nombre " + validName
            );
        }
        City city = foundCity.get();
        city.setName(validName);

        return cityRepository.update(city);
    }
    /**
     * Elimina una ciudad que no tenga aeropuertos asociados.
     *
     * @param id identificador de la ciudad.
     * @return true si la ciudad fue eliminada.
     * @throws ValidationException si el identificador no es válido.
     * @throws NotFoundException si la ciudad no existe.
     * @throws BusinessRuleException si la ciudad tiene aeropuertos asociados.
     */
    public boolean deleteCity(int id){
        int validId = Math.toIntExact(Validator.requirePositive(id, "id de ciudad"));
        Optional<City> foundCity = cityRepository.findById(validId);
        if (foundCity.isEmpty()){
            throw new NotFoundException("ciudad", validId);
        }
        if (airportRepository.existsByCity(foundCity.get())){
            throw new BusinessRuleException("No se puede eliminar una ciudad que contenga aeropuertos.");
        }
        cityRepository.deleteById(validId);
        return true;
    }
    /**
     * Busca ciudades cuyo nombre contenga el texto indicado.
     *
     * @param name texto utilizado para la búsqueda.
     * @return lista de ciudades coincidentes.
     * @throws ValidationException si el texto de búsqueda no es válido.
     */
    public List<City> searchCitiesByName(String name) {
        String validName = Validator.requireText(name, "nombre de ciudad");
        return cityRepository.searchCitiesByName(validName);
    }
    /**
     * Registra un nuevo aeropuerto asociado a una ciudad existente.
     *
     * @param name nombre del aeropuerto.
     * @param id identificador de la ciudad asociada.
     * @return aeropuerto creado.
     * @throws ValidationException si el nombre o el identificador no son válidos.
     * @throws NotFoundException si la ciudad no existe.
     * @throws DuplicateException si ya existe un aeropuerto con ese nombre en la ciudad.
     */
    public Airport createAirport(String name, int id) {
        String validName = Validator.requireText(name, "nombre de aeropuerto", 50);
        int validId = Math.toIntExact(Validator.requirePositive(id, "id de ciudad"));
        Optional<City> city = cityRepository.findById(validId);
        if (city.isEmpty()){
            throw new NotFoundException("ciudad", validId);
        }
        List<Airport> airports = airportRepository.listAirportsByCity(city.get());
        boolean duplicate = airports.stream()
            .anyMatch(airport -> airport.getName().equalsIgnoreCase(validName));
        if (duplicate) {
            throw new DuplicateException("Ya existe un aeropuerto con el nombre " + validName
                + " en la ciudad " + city.get().getName());
        }
        Airport airport = new Airport(validName, city.get());
        return airportRepository.create(airport);
    }
    /**
     * Busca un aeropuerto por su identificador.
     *
     * @param id identificador UUID del aeropuerto.
     * @return aeropuerto encontrado.
     * @throws ValidationException si el identificador es null.
     * @throws NotFoundException si el aeropuerto no existe.
     */
    public Airport getAirport(UUID id){
        UUID validId = Validator.requireNotNull(id, "id de aeropuerto");
        Optional<Airport> airport = airportRepository.findById(validId);
        if (airport.isEmpty()){
            throw new NotFoundException("aeropuerto", validId);
        }
        return airport.get();
    }
    /**
     * Obtiene todos los aeropuertos ordenados alfabéticamente por nombre.
     *
     * @return lista de aeropuertos.
     */
    public List<Airport> listAirports(){
        return airportRepository.findAll().stream()
            .sorted(Comparator.comparing(Airport::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }
    /**
     * Actualiza el nombre y la ciudad asociada a un aeropuerto existente.
     *
     * @param id identificador UUID del aeropuerto.
     * @param name nuevo nombre del aeropuerto.
     * @param idCity identificador de la nueva ciudad.
     * @return aeropuerto actualizado.
     * @throws ValidationException si alguno de los parámetros no es válido.
     * @throws NotFoundException si el aeropuerto o la ciudad no existen.
     * @throws DuplicateException si ya existe otro aeropuerto con ese nombre en la ciudad destino.
     */
    public Airport updateAirport(UUID id, String name, int idCity){
        UUID validId = Validator.requireNotNull(id, "id de aeropuerto");
        Optional<Airport> airport = airportRepository.findById(validId);
        if (airport.isEmpty()){
            throw new NotFoundException("aeropuerto", validId);
        }
        String validName = Validator.requireText(name,"nombre de aeropuerto", 50);
        int validIdCity = Math.toIntExact(Validator.requirePositive(idCity, "id de ciudad"));
        City city = getCity(validIdCity);
        Airport existingAirport = airport.get();
        boolean duplicate = airportRepository.listAirportsByCity(city)
            .stream().anyMatch(existing -> existing.getName().equalsIgnoreCase(validName)
            && !existing.getId().equals(validId));
        if (duplicate){
            throw new DuplicateException(
                "Ya existe un aeropuerto con el nombre "
                    + validName + " en la ciudad " + city.getName()
            );
        }
        existingAirport.setName(validName);
        existingAirport.setCity(city);

        return airportRepository.update(existingAirport);
    }
    /**
     * Elimina un aeropuerto existente.
     *
     * @param id identificador UUID del aeropuerto.
     * @return true si el aeropuerto fue eliminado.
     * @throws ValidationException si el identificador es null.
     * @throws NotFoundException si el aeropuerto no existe.
     */
    public boolean deleteAirport(UUID id){
        UUID validId = Validator.requireNotNull(id, "id de aeropuerto");
        Optional<Airport> airport = airportRepository.findById(validId);
        if (airport.isEmpty()){
            throw new NotFoundException("aeropuerto", validId);
        }
        airportRepository.deleteById(validId);
        return true;
    }
    /**
     * Obtiene los aeropuertos de una ciudad ordenados alfabéticamente por nombre.
     *
     * @param id identificador de la ciudad.
     * @return lista de aeropuertos asociados a la ciudad.
     * @throws ValidationException si el identificador no es válido.
     * @throws NotFoundException si la ciudad no existe.
     */
    public List<Airport> listAirportsByCity(int id) {
        int validId = Math.toIntExact(Validator.requirePositive(id, "id de ciudad"));
        Optional<City> foundCity = cityRepository.findById(validId);
        if (foundCity.isEmpty()){
            throw new NotFoundException("ciudad", validId);
        }
        return airportRepository.listAirportsByCity(foundCity.get())
            .stream()
            .sorted(Comparator.comparing(
                Airport::getName,
                String.CASE_INSENSITIVE_ORDER
            ))
            .collect(Collectors.toList());
    }
    /**
     * Busca aeropuertos cuyo nombre contenga el texto indicado.
     *
     * @param name texto utilizado para la búsqueda.
     * @return lista de aeropuertos coincidentes.
     * @throws ValidationException si el texto de búsqueda no es válido.
     */
    public List<Airport> searchAirportsByName(String name) {
        String validName = Validator.requireText(name, "nombre de aeropuerto", 50);
        return airportRepository.searchAirportsByName(validName);
    }
}
