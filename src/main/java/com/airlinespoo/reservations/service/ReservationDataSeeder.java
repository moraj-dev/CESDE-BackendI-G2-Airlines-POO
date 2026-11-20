package com.airlinespoo.reservations.service;

import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.model.Seat;
import com.airlinespoo.reservations.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * Registra cuatro reservas de ejemplo utilizando clientes y vuelos existentes.
 * Los identificadores 900001–900004 y 910001–910008 se reservan para las muestras.
 * La carga puede repetirse sin duplicar reservas previamente creadas por ella.
 */
public final class ReservationDataSeeder {
    private static final int FIRST_RESERVATION_ID = 900001;
    private static final int FIRST_DETAIL_ID = 910001;
    private static final int[] PASSENGER_COUNTS = {1, 2, 3, 2};
    private static final String SAMPLE_DOCUMENT_PREFIX = "DEMORES";

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final CustomerService customerService;
    private final FlightService flightService;

    /** Recibe los servicios y el repositorio que ya utiliza el programa. */
    public ReservationDataSeeder(ReservationService reservationService,
                                 ReservationRepository reservationRepository,
                                 CustomerService customerService,
                                 FlightService flightService) {
        this.reservationService = Objects.requireNonNull(reservationService);
        this.reservationRepository = Objects.requireNonNull(reservationRepository);
        this.customerService = Objects.requireNonNull(customerService);
        this.flightService = Objects.requireNonNull(flightService);
    }

    /**
     * Crea las reservas faltantes, siempre que existan clientes y al menos cuatro
     * vuelos futuros. No modifica las reservas de muestra que ya estén guardadas.
     */
    public void loadSampleData() {
        List<Customer> customers = customerService.listCustomers().stream()
            .sorted(Comparator.comparing(Customer::getId))
            .toList();
        List<Flight> flights = flightService.listFlights().stream()
            .filter(flight -> flight.getDepartureDateTime().isAfter(LocalDateTime.now()))
            .sorted(Comparator.comparing(Flight::getDepartureDateTime)
                .thenComparing(Flight::getFlightName))
            .toList();

        if (customers.isEmpty() || flights.size() < 4) {
            System.out.println("Reservas de ejemplo omitidas: se necesita al menos un cliente "
                + "y cuatro vuelos futuros en PostgreSQL.");
            return;
        }

        int created = 0;
        int detailId = FIRST_DETAIL_ID;
        for (int i = 0; i < PASSENGER_COUNTS.length; i++) {
            int reservationId = FIRST_RESERVATION_ID + i;
            int passengers = PASSENGER_COUNTS[i];
            var existing = reservationRepository.findById(reservationId);

            if (existing.isPresent()) {
                boolean sample = existing.get().getDetails().stream().anyMatch(d ->
                    d.getPassengerDocument().startsWith(SAMPLE_DOCUMENT_PREFIX));
                if (!sample) {
                    throw new IllegalStateException("El ID " + reservationId
                        + " ya pertenece a una reserva ajena a los datos de ejemplo.");
                }
                detailId += passengers;
                continue;
            }

            Flight flight = flights.get(i);
            Customer customer = customers.get(i % customers.size());
            Set<Seat> occupied = new HashSet<>(reservationService.getOccupiedSeatsForFlight(flight.getId()));
            List<ReservationDetail> details = new ArrayList<>();

            for (int p = 0; p < passengers; p++) {
                Seat seat = nextAvailableSeat(occupied);
                occupied.add(seat);
                int passengerNumber = p + 1;
                details.add(new ReservationDetail(
                    detailId++,
                    "Pasajero Demostración " + (i + 1) + "-" + passengerNumber,
                    SAMPLE_DOCUMENT_PREFIX + (i + 1) + passengerNumber,
                    seat));
            }

            reservationService.createReservation(new Reservation(reservationId, customer, flight, details));
            created++;
        }

        System.out.println("Reservas de ejemplo creadas: " + created
            + " (objetivo: 4 reservas, con 1, 2, 3 y 2 pasajeros).");
    }

    /** Busca una silla libre respetando el formato de la clase Seat. */
    private static Seat nextAvailableSeat(Set<Seat> occupied) {
        for (String row : List.of("Z", "Y", "X", "W", "V")) {
            for (int number = 1; number <= 99; number++) {
                Seat candidate = new Seat(row, number);
                if (!occupied.contains(candidate)) {
                    return candidate;
                }
            }
        }
        throw new IllegalStateException("No hay sillas de prueba disponibles para este vuelo.");
    }
}
