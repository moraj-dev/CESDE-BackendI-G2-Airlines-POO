package com.airlinespoo.locations.repository;

import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IAirportRepository {

    Airport create(Airport airport);

    Optional<Airport> findById(UUID id);

    List<Airport> findAll();

    Airport update(Airport airport);

    boolean deleteById(UUID id);

    boolean existsById(UUID id);

    long count();

    List<Airport> listAirportsByCity(City city);

    List<Airport> searchAirportsByName(String name);

    long countAirportsByCity(City city);

    boolean existsByCity(City city);
}
