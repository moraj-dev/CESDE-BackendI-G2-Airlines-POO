package com.airlinespoo.airlines;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.core.util.IdGenerator;

public class AirlineSampleData {
    private final AirlineRepository  airlineRepository;
    private final IdGenerator idGenerator;

    public AirlineSampleData(AirlineRepository airlineRepository, IdGenerator idGenerator) {
        this.airlineRepository = airlineRepository;
        this.idGenerator = idGenerator;
    }

    public void load() {
        airlineRepository.create(new Airline(100, "Avianca"));
        airlineRepository.create(new Airline(110, "Latam"));
        airlineRepository.create(new Airline(120, "Clic"));
        idGenerator.syncWith(120);
    }
}
