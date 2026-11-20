package com.airlinespoo.flights.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.flights.service.FlightService.AirlineLookup;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Adaptador JDBC temporal que permite al módulo de vuelos consultar aerolíneas
 * almacenadas en PostgreSQL, sin depender de AirlineRepository en memoria.
 *
 * <p>Cuando el módulo Airlines disponga de un servicio con persistencia JDBC,
 * este adaptador se podrá sustituir por otro que implemente {@link AirlineLookup}.</p>
 */
public class JdbcAirlineLookup implements AirlineLookup {

    private static final String CONNECTION_URL = "jdbc:postgresql://localhost:5432/aerolines";
    private static final String CONNECTION_USER = "postgres";
    private static final String CONNECTION_PASSWORD = "postgres";

    /** Obtiene una conexión JDBC a la misma base de datos que FlightRepository. */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL, CONNECTION_USER, CONNECTION_PASSWORD);
    }

    /**
     * Recupera una aerolínea existente por su identificador.
     *
     * @param id identificador positivo de la aerolínea
     * @return aerolínea encontrada
     * @throws NotFoundException si no existe una aerolínea con el ID indicado
     */
    @Override
    public Airline getAirline(int id) {
        int validId = Math.toIntExact(Validator.requirePositive(id, "id de aerolínea"));
        String sql = "SELECT airlineid, name FROM tblairlines WHERE airlineid = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return mapAirline(result);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar la aerolínea en PostgreSQL.", e);
        }

        throw new NotFoundException("aerolínea", validId);
    }

    /**
     * Recupera todas las aerolíneas existentes, ordenadas por nombre.
     *
     * @return lista de aerolíneas, vacía si no hay registros
     */
    @Override
    public List<Airline> listAirlines() {
        String sql = "SELECT airlineid, name FROM tblairlines ORDER BY LOWER(name), airlineid";
        List<Airline> airlines = new ArrayList<>();

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                airlines.add(mapAirline(result));
            }
            return airlines;
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las aerolíneas en PostgreSQL.", e);
        }
    }

    /** Convierte una fila de tblairlines en el modelo compartido Airline. */
    private Airline mapAirline(ResultSet result) throws SQLException {
        return new Airline(result.getInt("airlineid"), result.getString("name"));
    }
}
