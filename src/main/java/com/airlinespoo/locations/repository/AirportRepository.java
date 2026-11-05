package com.airlinespoo.locations.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AirportRepository extends InMemoryRepository<Airport, UUID> {
    @Override
    protected String entityName() {
        return "aeropuerto";
    }

    public List<Airport> listAirportsByCity(City city){
        City validCity = Validator.requireNotNull(city, "ciudad");
        return values().stream()
            .filter(airport -> airport.getCity().equals(validCity))
            .collect(Collectors.toList());
    }

    public List<Airport> searchAirportsByName(String name) {
        String validName = Validator.requireText(name, "nombre de aeropuerto");
        return values().stream()
            .filter(airport -> airport.getName().toLowerCase().contains(validName.toLowerCase()))
            .collect(Collectors.toList());
    }

    public long countAirportsByCity(City city){
        City validCity = Validator.requireNotNull(city, "ciudad");
        return values().stream()
            .filter(airport -> airport.getCity().equals(validCity))
            .count();
    }

    public boolean existsByCity(City city){
        City validCity = Validator.requireNotNull(city, "ciudad");
        return values().stream()
            .anyMatch(airport -> airport.getCity().equals(validCity));
    }
}
