package com.airlinespoo.airlines.service;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.util.Validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
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
            throw new NotFoundException("No se encontro la aerolínea con el id: " + id);
        }
        return found.get();
    }

    public List<Airline> listAirlines() {
        List<Airline> airlines = airlineRepository.findAll();
        airlines.sort(Comparator.comparing(Airline::getName, String.CASE_INSENSITIVE_ORDER));
        return airlines;
    }

    public Airline renameAirline(Integer id, String name) {
        Airline airline = getAirline(id);
        String cleanName = Validator.normalize(name);

        Optional<Airline> found = airlineRepository.findByName(cleanName);
        if (found.isPresent() && !found.get().getId().equals(airline.getId())) {
            throw new DuplicateException("Ya existe una aerolínea con el nombre '" +  cleanName + "'.");
        }

        airline.setName(cleanName);
        return airlineRepository.update(airline);
    }

    public void deleteAirline(Integer id) {
        getAirline(id);
        airlineRepository.deleteById(id);
    }

    public long countAirlines() {
        return airlineRepository.count();
    }

    public List<Airline> searchAirlinesByName(String text) {
        String cleanSearch = Validator.requireText(text, "texto de búsqueda").toLowerCase();
        List<Airline> result = new ArrayList<>();
        for (Airline airline : airlineRepository.findAll()) {
            if (airline.getName().toLowerCase().contains(cleanSearch)) {
                result.add(airline);
            }
        }
        return result;
    }
}

