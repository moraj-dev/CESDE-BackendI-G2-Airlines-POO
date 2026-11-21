package com.airlinespoo.flights.model;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Representa un vuelo entre dos aeropuertos, operado por una aerolínea.
 * <p>El identificador lo asigna PostgreSQL al insertar el registro. Un vuelo
 * todavía no persistido tiene {@code id == null}.</p>
 */
public class Flight implements Comparable<Flight> {

    private static final int MAX_FLIGHT_NAME_LENGTH = 8;

    private final Integer id;
    private String flightName;
    private Airport departure;
    private Airport arrival;
    private LocalDate departureDate;
    private LocalTime departureTime;
    private LocalDate arrivalDate;
    private LocalTime arrivalTime;
    private Airline airline;

    /**
     * Construye un vuelo nuevo, aún sin identificador de base de datos.
     *
     * @param flightName código único del vuelo, de hasta ocho caracteres alfanuméricos
     * @param departure aeropuerto de salida
     * @param arrival aeropuerto de llegada
     * @param departureDate fecha de salida
     * @param departureTime hora de salida
     * @param arrivalDate fecha de llegada
     * @param arrivalTime hora de llegada
     * @param airline aerolínea responsable del vuelo
     */
    public Flight(String flightName, Airport departure, Airport arrival,
                  LocalDate departureDate, LocalTime departureTime,
                  LocalDate arrivalDate, LocalTime arrivalTime, Airline airline) {
        this(null, flightName, departure, arrival,
            departureDate, departureTime, arrivalDate, arrivalTime, airline);
    }

    /**
     * Reconstruye un vuelo persistido, por ejemplo a partir de un ResultSet JDBC.
     *
     * @param id identificador generado por PostgreSQL; {@code null} solo antes de insertar
     * @param flightName código del vuelo
     * @param departure aeropuerto de salida
     * @param arrival aeropuerto de llegada
     * @param departureDate fecha de salida
     * @param departureTime hora de salida
     * @param arrivalDate fecha de llegada
     * @param arrivalTime hora de llegada
     * @param airline aerolínea responsable del vuelo
     */
    public Flight(Integer id, String flightName, Airport departure, Airport arrival,
                  LocalDate departureDate, LocalTime departureTime,
                  LocalDate arrivalDate, LocalTime arrivalTime, Airline airline) {
        if (id != null) {
            Validator.requirePositive(id, "id del vuelo");
        }
        this.id = id;
        updateDetails(flightName, departure, arrival, departureDate,
            departureTime, arrivalDate, arrivalTime, airline);
    }

    /** {@inheritDoc} */
    public Integer getId() {
        return id;
    }

    /** @return código del vuelo */
    public String getFlightName() {
        return flightName;
    }

    /** @return aeropuerto de salida */
    public Airport getDeparture() {
        return departure;
    }

    /** @return aeropuerto de llegada */
    public Airport getArrival() {
        return arrival;
    }

    /** @return fecha de salida */
    public LocalDate getDepartureDate() {
        return departureDate;
    }

    /** @return hora de salida */
    public LocalTime getDepartureTime() {
        return departureTime;
    }

    /** @return fecha de llegada */
    public LocalDate getArrivalDate() {
        return arrivalDate;
    }

    /** @return hora de llegada */
    public LocalTime getArrivalTime() {
        return arrivalTime;
    }

    /** @return aerolínea responsable del vuelo */
    public Airline getAirline() {
        return airline;
    }

    /** @return fecha y hora de salida como un único valor */
    public LocalDateTime getDepartureDateTime() {
        return LocalDateTime.of(departureDate, departureTime);
    }

    /** @return fecha y hora de llegada como un único valor */
    public LocalDateTime getArrivalDateTime() {
        return LocalDateTime.of(arrivalDate, arrivalTime);
    }

    /** @return duración programada del vuelo */
    public Duration getDuration() {
        return Duration.between(getDepartureDateTime(), getArrivalDateTime());
    }

    /**
     * Indica si el vuelo ya salió respecto de la fecha y hora indicada.
     * El instante exacto de salida se considera como vuelo ya salido.
     *
     * @param moment fecha y hora de referencia
     * @return {@code true} si la salida es anterior o igual al momento indicado
     */
    public boolean hasDepartedAt(LocalDateTime moment) {
        Validator.requireNotNull(moment, "momento de consulta");
        return !getDepartureDateTime().isAfter(moment);
    }

    /**
     * Reprograma el vuelo y permite actualizar su código, ruta y aerolínea.
     * Todos los valores se validan antes de modificar el estado del objeto;
     * la unicidad del código y la existencia de referencias se verifican en el servicio.
     *
     * @param flightName nuevo código del vuelo
     * @param departure nuevo aeropuerto de salida
     * @param arrival nuevo aeropuerto de llegada
     * @param departureDate nueva fecha de salida
     * @param departureTime nueva hora de salida
     * @param arrivalDate nueva fecha de llegada
     * @param arrivalTime nueva hora de llegada
     * @param airline nueva aerolínea
     */
    public void updateDetails(String flightName, Airport departure, Airport arrival,
                              LocalDate departureDate, LocalTime departureTime,
                              LocalDate arrivalDate, LocalTime arrivalTime, Airline airline) {
        String validName = validateFlightName(flightName);
        Airport validDeparture = Validator.requireNotNull(departure, "aeropuerto de salida");
        Airport validArrival = Validator.requireNotNull(arrival, "aeropuerto de llegada");
        LocalDate validDepartureDate = Validator.requireNotNull(departureDate, "fecha de salida");
        LocalTime validDepartureTime = Validator.requireNotNull(departureTime, "hora de salida");
        LocalDate validArrivalDate = Validator.requireNotNull(arrivalDate, "fecha de llegada");
        LocalTime validArrivalTime = Validator.requireNotNull(arrivalTime, "hora de llegada");
        Airline validAirline = Validator.requireNotNull(airline, "aerolínea");

        if (validDeparture.equals(validArrival)) {
            throw new ValidationException("Los aeropuertos de salida y llegada deben ser distintos.");
        }

        LocalDateTime departureAt = LocalDateTime.of(validDepartureDate, validDepartureTime);
        LocalDateTime arrivalAt = LocalDateTime.of(validArrivalDate, validArrivalTime);
        if (!arrivalAt.isAfter(departureAt)) {
            throw new ValidationException("La fecha y hora de llegada deben ser posteriores a la salida.");
        }

        this.flightName = validName;
        this.departure = validDeparture;
        this.arrival = validArrival;
        this.departureDate = validDepartureDate;
        this.departureTime = validDepartureTime;
        this.arrivalDate = validArrivalDate;
        this.arrivalTime = validArrivalTime;
        this.airline = validAirline;
    }

    private static String validateFlightName(String name) {
        String validName = Validator.requireText(name, "código del vuelo", MAX_FLIGHT_NAME_LENGTH);
        if (!validName.matches("[A-Z0-9]+")) {
            throw new ValidationException("El código del vuelo solo puede contener letras mayúsculas y números.");
        }
        return validName;
    }

    /** Orden natural: fecha y hora de salida; en empate, identificador. */
    @Override
    public int compareTo(Flight other) {
        Objects.requireNonNull(other, "vuelo a comparar");
        int comparison = getDepartureDateTime().compareTo(other.getDepartureDateTime());
        if (comparison != 0) {
            return comparison;
        }
        if (id != null && other.id != null) {
            return id.compareTo(other.id);
        }
        return flightName.compareTo(other.flightName);
    }

    /** Los vuelos persistidos son iguales si tienen el mismo identificador. */
    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (!(other instanceof Flight flight)) return false;
        return id != null && id.equals(flight.id);
    }

    @Override
    public int hashCode() {
        return id == null ? System.identityHashCode(this) : id.hashCode();
    }

    @Override
    public String toString() {
        return "Vuelo " + flightName
            + " | " + departure.getName() + " -> " + arrival.getName()
            + " | salida: " + getDepartureDateTime()
            + " | llegada: " + getArrivalDateTime()
            + " | aerolínea: " + airline.getName();
    }
}

