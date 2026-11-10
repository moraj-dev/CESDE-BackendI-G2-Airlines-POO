package com.airlinespoo.flights.service;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.repository.IFlightRepository;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.service.LocationService;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Gestiona las operaciones de vuelos y sus reglas de negocio.
 * <p>Utiliza PostgreSQL a través de {@link IFlightRepository}, consulta los
 * aeropuertos mediante {@link LocationService} y recibe una abstracción
 * temporal de consulta de aerolíneas. No utiliza {@code InMemoryRepository}.</p>
 */
public class FlightService {

    /**
     * Contrato mínimo que deberá proporcionar la integración del módulo Airlines.
     * Su implementación deberá consultar aerolíneas reales de PostgreSQL.
     * No debe devolver datos solo presentes en memoria.
     */
    public interface AirlineLookup {
        /** @return aerolínea existente con el ID indicado; lanza NotFoundException si no existe */
        Airline getAirline(int id);

        /** @return aerolíneas registradas, para mostrarlas en el menú */
        List<Airline> listAirlines();
    }

    private final IFlightRepository flightRepository;
    private final LocationService locationService;
    private final AirlineLookup airlineLookup;

    /**
     * Inicializa el servicio con las dependencias necesarias.
     *
     * @param flightRepository contrato de persistencia de vuelos
     * @param locationService servicio para validar aeropuertos
     * @param airlineLookup consulta de aerolíneas existentes en PostgreSQL
     */
    public FlightService(IFlightRepository flightRepository,
                         LocationService locationService,
                         AirlineLookup airlineLookup) {
        this.flightRepository = Validator.requireNotNull(flightRepository, "repositorio de vuelos");
        this.locationService = Validator.requireNotNull(locationService, "servicio de ubicaciones");
        this.airlineLookup = Validator.requireNotNull(airlineLookup, "consulta de aerolíneas");
    }

    /**
     * Programa un vuelo y permite que PostgreSQL genere su identificador.
     *
     * @throws DuplicateException si el código ya está registrado
     * @throws NotFoundException si falta alguna entidad relacionada
     */
    public Flight createFlight(String flightName, UUID departureId, UUID arrivalId,
                               LocalDate departureDate, LocalTime departureTime,
                               LocalDate arrivalDate, LocalTime arrivalTime, int airlineId) {
        // El modelo comprueba formato, fechas y que los aeropuertos sean distintos.
        Airport departure = locationService.getAirport(departureId);
        Airport arrival = locationService.getAirport(arrivalId);
        Airline airline = airlineLookup.getAirline(requirePositiveId(airlineId, "id de aerolínea"));

        Flight flight = new Flight(flightName, departure, arrival,
            departureDate, departureTime, arrivalDate, arrivalTime, airline);
        ensureFlightNameAvailable(flight.getFlightName(), null);
        return flightRepository.create(flight);
    }

    /** @return vuelo encontrado por ID; lanza NotFoundException si no existe */
    public Flight getFlight(int id) {
        int validId = requirePositiveId(id, "id de vuelo");
        return flightRepository.findById(validId)
            .orElseThrow(() -> new NotFoundException("vuelo", validId));
    }

    /** @return vuelo encontrado por su código; lanza NotFoundException si no existe */
    public Flight getFlightByName(String flightName) {
        String validName = Validator.requireText(flightName, "código del vuelo", 8);
        return flightRepository.findByFlightName(validName)
            .orElseThrow(() -> new NotFoundException("vuelo con código", validName));
    }

    /** @return todos los vuelos ordenados por salida */
    public List<Flight> listFlights() {
        List<Flight> flights = new ArrayList<>(flightRepository.findAll());
        flights.sort(Comparator.naturalOrder());
        return flights;
    }

    /**
     * Actualiza un vuelo existente y verifica que su código no pertenezca a otro.
     * No cambia el identificador del vuelo.
     */
    public Flight updateFlight(int id, String flightName, UUID departureId, UUID arrivalId,
                               LocalDate departureDate, LocalTime departureTime,
                               LocalDate arrivalDate, LocalTime arrivalTime, int airlineId) {
        Flight current = getFlight(id);
        Airport departure = locationService.getAirport(departureId);
        Airport arrival = locationService.getAirport(arrivalId);
        Airline airline = airlineLookup.getAirline(requirePositiveId(airlineId, "id de aerolínea"));

        // Construir primero un objeto temporal valida todos los campos sin alterar el original.
        Flight validated = new Flight(current.getId(), flightName, departure, arrival,
            departureDate, departureTime, arrivalDate, arrivalTime, airline);
        ensureFlightNameAvailable(validated.getFlightName(), current.getId());
        return flightRepository.update(validated);
    }

    /**
     * Elimina un vuelo existente. PostgreSQL impedirá eliminarlo si ya tiene
     * reservas relacionadas; más adelante se agregará la validación explícita
     * mediante el módulo Reservations.
     *
     * @return true si se eliminó el vuelo
     */
    public boolean deleteFlight(int id) {
        Flight flight = getFlight(id);
        return flightRepository.deleteById(flight.getId());
    }

    /** @return cantidad total de vuelos registrados */
    public long countFlights() {
        return flightRepository.count();
    }

    /** @return aerolíneas disponibles para seleccionar en el menú */
    public List<Airline> listAirlines() {
        List<Airline> airlines = new ArrayList<>(airlineLookup.listAirlines());
        airlines.sort(Comparator.comparing(Airline::getName, String.CASE_INSENSITIVE_ORDER));
        return airlines;
    }

    /** @return aeropuertos disponibles para seleccionar en el menú */
    public List<Airport> listAirports() {
        return locationService.listAirports();
    }

    /** @return vuelos de una aerolínea, ordenados por salida */
    public List<Flight> listFlightsByAirline(int airlineId) {
        Airline airline = airlineLookup.getAirline(requirePositiveId(airlineId, "id de aerolínea"));
        List<Flight> flights = new ArrayList<>(flightRepository.findByAirline(airline));
        flights.sort(Comparator.naturalOrder());
        return flights;
    }

    /**
     * Recupera vuelos con fecha de salida comprendida entre las fechas indicadas
     * (ambos extremos incluidos).
     */
    public List<Flight> listFlightsBetween(LocalDate start, LocalDate end) {
        LocalDate validStart = Validator.requireNotNull(start, "fecha inicial");
        LocalDate validEnd = Validator.requireNotNull(end, "fecha final");
        if (validStart.isAfter(validEnd)) {
            throw new ValidationException("La fecha inicial no puede ser posterior a la fecha final.");
        }
        List<Flight> flights = new ArrayList<>(flightRepository.findByDepartureDateBetween(validStart, validEnd));
        flights.sort(Comparator.naturalOrder());
        return flights;
    }

    /** @return vuelos que siguen la ruta indicada, ordenados por salida */
    public List<Flight> listFlightsByRoute(UUID departureId, UUID arrivalId) {
        Airport departure = locationService.getAirport(departureId);
        Airport arrival = locationService.getAirport(arrivalId);
        if (departure.equals(arrival)) {
            throw new ValidationException("Los aeropuertos de salida y llegada deben ser distintos.");
        }
        List<Flight> flights = new ArrayList<>(flightRepository.findByRoute(departure, arrival));
        flights.sort(Comparator.naturalOrder());
        return flights;
    }

    /** @return cantidad de vuelos registrados para una aerolínea existente */
    public long countFlightsByAirline(int airlineId) {
        Airline airline = airlineLookup.getAirline(requirePositiveId(airlineId, "id de aerolínea"));
        return flightRepository.countByAirline(airline);
    }

    /** @return true si la aerolínea existente tiene vuelos relacionados */
    public boolean airlineHasFlights(int airlineId) {
        Airline airline = airlineLookup.getAirline(requirePositiveId(airlineId, "id de aerolínea"));
        return flightRepository.existsByAirline(airline);
    }

    /**
     * Consulta si un aeropuerto participa como salida o llegada en algún vuelo.
     * Está destinado a la protección de eliminación del módulo Locations.
     */
    public boolean airportHasFlights(UUID airportId) {
        Airport airport = locationService.getAirport(airportId);
        return flightRepository.existsByAirport(airport);
    }

    /**
     * Agrupa los vuelos que salen durante un mes por aerolínea.
     * Las aerolíneas quedan ordenadas por nombre y los vuelos por fecha y hora de salida.
     * Un período sin vuelos produce un mapa vacío.
     *
     * @param year año entre 1900 y 2100
     * @param month mes entre 1 y 12
     * @return mapa ordenado, con una lista de vuelos por aerolínea
     */
    public Map<Airline, List<Flight>> listFlightsByAirlineInMonth(int year, int month) {
        if (year < 1900 || year > 2100) {
            throw new ValidationException("El año debe estar entre 1900 y 2100.");
        }
        if (month < 1 || month > 12) {
            throw new ValidationException("El mes debe estar entre 1 y 12.");
        }
        YearMonth period = YearMonth.of(year, month);
        List<Flight> flights = listFlightsBetween(period.atDay(1), period.atEndOfMonth());
        flights.sort(Comparator
            .comparing((Flight f) -> f.getAirline().getName(), String.CASE_INSENSITIVE_ORDER)
            .thenComparing(Flight::getDepartureDateTime)
            .thenComparing(Flight::getFlightName));

        Map<Airline, List<Flight>> grouped = new LinkedHashMap<>();
        for (Flight flight : flights) {
            grouped.computeIfAbsent(flight.getAirline(), ignored -> new ArrayList<>()).add(flight);
        }
        return grouped;
    }

    private void ensureFlightNameAvailable(String flightName, Integer currentFlightId) {
        flightRepository.findByFlightName(flightName).ifPresent(existing -> {
            if (currentFlightId == null || !existing.getId().equals(currentFlightId)) {
                throw new DuplicateException("Ya existe un vuelo con el código '" + flightName + "'.");
            }
        });
    }

    private static int requirePositiveId(int id, String fieldName) {
        return Math.toIntExact(Validator.requirePositive(id, fieldName));
    }
}

