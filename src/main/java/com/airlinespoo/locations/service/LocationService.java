package com.airlinespoo.locations.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
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

public class LocationService {
    private final CityRepository cityRepository;
    private final AirportRepository airportRepository;
    private final IdGenerator idGenerator;

    public LocationService(CityRepository cityRepository, AirportRepository airportRepository, IdGenerator idGenerator) {
        this.cityRepository = Validator.requireNotNull(cityRepository, "repositorio de ciudades");
        this.airportRepository = Validator.requireNotNull(airportRepository, "repositorio de aeropuertos");
        this.idGenerator = Validator.requireNotNull(idGenerator, "generador de ids");
    }

    public City createCity(String name) {
        String validName = Validator.requireText(name, "nombre de ciudad", 20);
        if (cityRepository.findAll().stream()
            .anyMatch(city -> city.getName().equalsIgnoreCase(validName))){
            throw new DuplicateException("Ya existe una ciudad con el nombre " + validName);
        }
        return cityRepository.create(new City(idGenerator.next(),validName));
    }

    public City getCity(int id) {
        int validId = Math.toIntExact(Validator.requirePositive(id, "id de ciudad"));
        Optional<City> city = cityRepository.findById(validId);
        if (city.isEmpty()){
            throw new NotFoundException("ciudad", validId);
        }
        return city.get();
    }

    public List<City> listCities(){
        return cityRepository.findAll().stream()
            .sorted(Comparator.comparing(City::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

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

    public List<City> searchCitiesByName(String name) {
        String validName = Validator.requireText(name, "nombre de ciudad");
        return cityRepository.searchCitiesByName(validName);
    }

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

    public Airport getAirport(UUID id){
        UUID validId = Validator.requireNotNull(id, "id de aeropuerto");
        Optional<Airport> airport = airportRepository.findById(validId);
        if (airport.isEmpty()){
            throw new NotFoundException("aeropuerto", validId);
        }
        return airport.get();
    }

    public List<Airport> listAirports(){
        return airportRepository.findAll().stream()
            .sorted(Comparator.comparing(Airport::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(Collectors.toList());
    }

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

    public boolean deleteAirport(UUID id){
        UUID validId = Validator.requireNotNull(id, "id de aeropuerto");
        Optional<Airport> airport = airportRepository.findById(validId);
        if (airport.isEmpty()){
            throw new NotFoundException("aeropuerto", validId);
        }
        airportRepository.deleteById(validId);
        return true;
    }

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

    public List<Airport> searchAirportsByName(String name) {
        String validName = Validator.requireText(name, "nombre de aeropuerto", 50);
        return airportRepository.searchAirportsByName(validName);
    }
}
