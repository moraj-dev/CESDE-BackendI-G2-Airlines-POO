package com.airlinespoo.airlines.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.repository.Repository;

import java.util.Optional;

public interface AirlineRepository extends Repository<Airline, Integer> {

    Optional<Airline> findByName(String title);
    boolean existsByName(String title);
}
