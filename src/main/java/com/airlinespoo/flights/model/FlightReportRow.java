package com.airlinespoo.flights.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

/**
 * Representa una fila de solo lectura del reporte mensual de vuelos por aerolínea.
 * <p>
 * Contiene los datos que el reporte necesita mostrar, sin implementar reglas de
 * negocio ni operaciones de persistencia. Se utiliza como objeto de transferencia
 * de datos (DTO).
 * </p>
 *
 * @param airlineName nombre de la aerolínea
 * @param flightName código del vuelo
 * @param departureCity ciudad de salida
 * @param departureAirport aeropuerto de salida
 * @param departureDate fecha de salida
 * @param departureTime hora de salida
 * @param arrivalCity ciudad de llegada
 * @param arrivalAirport aeropuerto de llegada
 * @param arrivalDate fecha de llegada
 * @param arrivalTime hora de llegada
 */
public record FlightReportRow(
    String airlineName,
    String flightName,
    String departureCity,
    String departureAirport,
    LocalDate departureDate,
    LocalTime departureTime,
    String arrivalCity,
    String arrivalAirport,
    LocalDate arrivalDate,
    LocalTime arrivalTime
) {
    /**
     * Rechaza campos nulos para que una fila del reporte tenga todos sus datos.
     */
    public FlightReportRow {
        Objects.requireNonNull(airlineName, "nombre de aerolínea");
        Objects.requireNonNull(flightName, "código del vuelo");
        Objects.requireNonNull(departureCity, "ciudad de salida");
        Objects.requireNonNull(departureAirport, "aeropuerto de salida");
        Objects.requireNonNull(departureDate, "fecha de salida");
        Objects.requireNonNull(departureTime, "hora de salida");
        Objects.requireNonNull(arrivalCity, "ciudad de llegada");
        Objects.requireNonNull(arrivalAirport, "aeropuerto de llegada");
        Objects.requireNonNull(arrivalDate, "fecha de llegada");
        Objects.requireNonNull(arrivalTime, "hora de llegada");
    }
}
