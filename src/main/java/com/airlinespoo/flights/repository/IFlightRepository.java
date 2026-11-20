package com.airlinespoo.flights.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.locations.model.Airport;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Define las operaciones de persistencia y consulta de vuelos.
 * <p>
 * Este contrato no depende de {@code InMemoryRepository}. Su implementación
 * JDBC accederá a la tabla {@code tblflights} de PostgreSQL.
 * Las reglas de negocio y las excepciones por entidades no encontradas
 * corresponden a {@code FlightService}.
 * </p>
 */
public interface IFlightRepository {

    /**
     * Guarda un vuelo nuevo y devuelve el vuelo con el ID generado por la base de datos.
     *
     * @param flight vuelo nuevo, todavía sin ID
     * @return vuelo persistido con su identificador
     */
    Flight create(Flight flight);

    /**
     * Busca un vuelo mediante su identificador.
     *
     * @param id identificador del vuelo
     * @return vuelo encontrado, o vacío si no existe
     */
    Optional<Flight> findById(Integer id);

    /**
     * Busca un vuelo mediante su código único.
     *
     * @param flightName código de vuelo
     * @return vuelo encontrado, o vacío si no existe
     */
    Optional<Flight> findByFlightName(String flightName);

    /**
     * Recupera todos los vuelos ordenados por fecha y hora de salida.
     *
     * @return lista de vuelos
     */
    List<Flight> findAll();

    /**
     * Actualiza un vuelo existente utilizando su identificador.
     *
     * @param flight vuelo con los nuevos datos
     * @return vuelo actualizado
     */
    Flight update(Flight flight);

    /**
     * Elimina un vuelo mediante su identificador.
     *
     * @param id identificador del vuelo
     * @return {@code true} si se eliminó; {@code false} si no existía
     */
    boolean deleteById(Integer id);

    /**
     * Comprueba si existe un vuelo con el identificador indicado.
     *
     * @param id identificador del vuelo
     * @return {@code true} si existe
     */
    boolean existsById(Integer id);

    /**
     * Cuenta todos los vuelos registrados.
     *
     * @return cantidad de vuelos
     */
    long count();

    /**
     * Busca los vuelos de una aerolínea, ordenados por salida.
     *
     * @param airline aerolínea que opera los vuelos
     * @return lista de vuelos de la aerolínea
     */
    List<Flight> findByAirline(Airline airline);

    /**
     * Busca vuelos cuya fecha de salida esté en el rango indicado, incluidos sus extremos.
     *
     * @param start fecha inicial
     * @param end fecha final
     * @return lista de vuelos del rango
     */
    List<Flight> findByDepartureDateBetween(LocalDate start, LocalDate end);

    /**
     * Busca vuelos que cubren una ruta entre dos aeropuertos.
     *
     * @param departure aeropuerto de salida
     * @param arrival aeropuerto de llegada
     * @return lista de vuelos de la ruta
     */
    List<Flight> findByRoute(Airport departure, Airport arrival);

    /**
     * Cuenta los vuelos pertenecientes a una aerolínea.
     *
     * @param airline aerolínea consultada
     * @return número de vuelos
     */
    long countByAirline(Airline airline);

    /**
     * Comprueba si una aerolínea tiene al menos un vuelo asociado.
     *
     * @param airline aerolínea consultada
     * @return {@code true} si tiene vuelos
     */
    boolean existsByAirline(Airline airline);

    /**
     * Comprueba si un aeropuerto se utiliza como salida o llegada de algún vuelo.
     * Esta consulta permitirá impedir su eliminación desde el módulo Locations.
     *
     * @param airport aeropuerto consultado
     * @return {@code true} si participa en algún vuelo
     */
    boolean existsByAirport(Airport airport);
}
