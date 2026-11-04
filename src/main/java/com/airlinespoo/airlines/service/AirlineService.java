package com.airlinespoo.airlines.service;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.util.Validator;
import java.util.Optional;

public class AirlineService {

    private final AirlineRepository airlineRepository;
    private final IdGenerator idGenerator;

    public AirlineService(AirlineRepository airlineRepository, IdGenerator idGenerator) {
        this.airlineRepository = airlineRepository;
        this.idGenerator = idGenerator;
    }

    public Airline createAirline(String name) {
        String cleanName = Validator.normalize(name);
        if (airlineRepository.existsByName(cleanName)) {
            throw new DuplicateException("Ya existe una aerolínea con el nombre '" +  cleanName + "'.");
        }

        int id = idGenerator.next();
        Airline airline = new Airline(id, cleanName);
        return airlineRepository.create(airline);
    }

    public Airline getAirline(Integer id) {
        Optional<Airline> found = airlineRepository.findById(id);
        if (found.isEmpty()) {
            throw new NotFoundException("No se encontro el airline con el id: " + id);
        }
        return found.get();
    }
}

