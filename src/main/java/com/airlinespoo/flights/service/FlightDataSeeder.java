package com.airlinespoo.flights.service;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.flights.repository.IFlightRepository;
import com.airlinespoo.locations.model.Airport;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/** Crea ocho vuelos de ejemplo una sola vez, usando aeropuertos y aerolíneas reales. */
public class FlightDataSeeder {
    private final FlightService flightService;
    private final IFlightRepository flightRepository;

    public FlightDataSeeder(FlightService flightService, IFlightRepository flightRepository) {
        this.flightService = Objects.requireNonNull(flightService);
        this.flightRepository = Objects.requireNonNull(flightRepository);
    }

    /** Inserta solamente los vuelos que no existen por su código. */
    public void loadSampleData() {
        List<Airport> airports = flightService.listAirports().stream()
            .sorted(Comparator.comparing(Airport::getName)).toList();
        List<Airline> airlines = flightService.listAirlines();
        if (airports.size() < 2 || airlines.isEmpty()) {
            System.out.println("Datos de vuelos omitidos: se requieren aeropuertos y aerolíneas.");
            return;
        }
        LocalDate first = LocalDate.now().plusMonths(1).withDayOfMonth(10);
        LocalDate second = first.plusMonths(1);
        int inserted = 0;
        for (int i = 0; i < 8; i++) {
            String code = "DEM" + String.format("%05d", i + 1);
            if (flightRepository.findByFlightName(code).isPresent()) continue;
            Airport from = airports.get(i % airports.size());
            Airport to = airports.get((i + 1) % airports.size());
            Airline airline = airlines.get(i % airlines.size());
            LocalDate date = (i < 4 ? first : second).plusDays(i % 4);
            LocalTime start = LocalTime.of(7 + (i % 4) * 3, 0);
            flightService.createFlight(code, from.getId(), to.getId(), date, start,
                date, start.plusHours(2), airline.getId());
            inserted++;
        }
        System.out.println("Vuelos de ejemplo creados: " + inserted);
    }
}
