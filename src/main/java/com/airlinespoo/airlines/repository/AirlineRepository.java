package com.airlinespoo.airlines.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.repository.InMemoryRepository;

import java.util.Optional;

public class AirlineRepository extends InMemoryRepository<Airline, Integer> {

    @Override
    protected String entityName() {
        return "aerolínea";
    }

    public Optional<Airline> findByName(String name) {
        for (Airline airline : this.values()) {
            if (airline.getName().equalsIgnoreCase(name)) {
                return Optional.of(airline);
            }
        }
        return Optional.empty();
    }

    public boolean existsByName(String name) {
        return findByName(name).isPresent();
    }
}
