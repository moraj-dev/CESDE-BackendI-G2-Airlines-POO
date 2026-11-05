package com.airlinespoo.locations.service;

import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.City;
import com.airlinespoo.locations.repository.AirportRepository;
import com.airlinespoo.locations.repository.CityRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
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
}
