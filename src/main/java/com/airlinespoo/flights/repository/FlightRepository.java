package com.airlinespoo.flights.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

/**
 * Implementa el almacenamiento y las consultas de vuelos mediante JDBC y PostgreSQL.
 * <p>
 * Las reglas de negocio (código duplicado, existencia de aerolíneas y aeropuertos,
 * orden de las fechas, etc.) corresponden a FlightService y Flight. Este
 * repositorio se ocupa exclusivamente del acceso a datos.
 * </p>
 */
public class FlightRepository implements IFlightRepository {

    private static final String CONNECTION_URL = "jdbc:postgresql://localhost:5432/aerolines";
    private static final String CONNECTION_USER = "postgres";
    private static final String CONNECTION_PASSWORD = "postgres";

    /*
     * Al recuperar un vuelo, se reconstruyen las referencias del modelo:
     * Flight -> Airline, Flight -> Airport y Airport -> City.
     * Los alias evitan ambigüedades entre las columnas name y cityid.
     */
    private static final String FLIGHT_SELECT = """
            SELECT f.flightid, f.flightname,
                   f.datedeparture, f.hourdeparture,
                   f.datearrival, f.hourarrival,
                   al.airlineid, al.name AS airline_name,
                   dep.airportid AS departure_id, dep.name AS departure_name,
                   dep_city.cityid AS departure_city_id,
                   dep_city.name AS departure_city_name,
                   arr.airportid AS arrival_id, arr.name AS arrival_name,
                   arr_city.cityid AS arrival_city_id,
                   arr_city.name AS arrival_city_name
            FROM tblflights f
            JOIN tblairlines al ON al.airlineid = f.airlineid
            JOIN tblairports dep ON dep.airportid = f.departure
            JOIN tblcities dep_city ON dep_city.cityid = dep.cityid
            JOIN tblairports arr ON arr.airportid = f.arrival
            JOIN tblcities arr_city ON arr_city.cityid = arr.cityid
            """;

    private static final String ORDER_BY_DEPARTURE =
        " ORDER BY f.datedeparture, f.hourdeparture, f.flightid";

    /** Obtiene una conexión nueva a la base de datos. */
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(CONNECTION_URL, CONNECTION_USER, CONNECTION_PASSWORD);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Flight create(Flight flight) {
        Flight validFlight = Validator.requireNotNull(flight, "vuelo");
        if (validFlight.getId() != null) {
            throw new IllegalArgumentException("El vuelo nuevo no debe tener un ID asignado.");
        }

        String sql = """
                INSERT INTO tblflights
                    (flightname, departure, arrival, datedeparture, hourdeparture,
                     datearrival, hourarrival, airlineid)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                RETURNING flightid
                """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindFlightFields(statement, validFlight);
            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    throw new SQLException("PostgreSQL no devolvió el ID del vuelo creado.");
                }
                return withId(validFlight, result.getInt("flightid"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar el vuelo.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Flight> findById(Integer id) {
        int validId = requireFlightId(id);
        String sql = FLIGHT_SELECT + " WHERE f.flightid = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validId);
            return readOne(statement);
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el vuelo por ID.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Optional<Flight> findByFlightName(String flightName) {
        String validName = Validator.requireText(flightName, "código del vuelo", 8);
        String sql = FLIGHT_SELECT + " WHERE f.flightname = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, validName);
            return readOne(statement);
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar el vuelo por código.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Flight> findAll() {
        String sql = FLIGHT_SELECT + ORDER_BY_DEPARTURE;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            return readMany(statement);
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los vuelos.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public Flight update(Flight flight) {
        Flight validFlight = Validator.requireNotNull(flight, "vuelo");
        int id = requireFlightId(validFlight.getId());
        String sql = """
                UPDATE tblflights
                SET flightname = ?, departure = ?, arrival = ?,
                    datedeparture = ?, hourdeparture = ?, datearrival = ?,
                    hourarrival = ?, airlineid = ?
                WHERE flightid = ?
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            bindFlightFields(statement, validFlight);
            statement.setInt(9, id);
            if (statement.executeUpdate() == 0) {
                throw new NotFoundException("vuelo", id);
            }
            return validFlight;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el vuelo.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean deleteById(Integer id) {
        int validId = requireFlightId(id);
        String sql = "DELETE FROM tblflights WHERE flightid = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el vuelo.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsById(Integer id) {
        int validId = requireFlightId(id);
        String sql = "SELECT EXISTS(SELECT 1 FROM tblflights WHERE flightid = ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validId);
            return readExists(statement);
        } catch (SQLException e) {
            throw new RuntimeException("Error al comprobar la existencia del vuelo.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM tblflights";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar los vuelos.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Flight> findByAirline(Airline airline) {
        Airline validAirline = Validator.requireNotNull(airline, "aerolínea");
        String sql = FLIGHT_SELECT + " WHERE f.airlineid = ?" + ORDER_BY_DEPARTURE;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validAirline.getId());
            return readMany(statement);
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los vuelos de la aerolínea.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Flight> findByDepartureDateBetween(LocalDate start, LocalDate end) {
        LocalDate validStart = Validator.requireNotNull(start, "fecha inicial");
        LocalDate validEnd = Validator.requireNotNull(end, "fecha final");
        if (validStart.isAfter(validEnd)) {
            throw new IllegalArgumentException("La fecha inicial no puede ser posterior a la final.");
        }
        String sql = FLIGHT_SELECT +
            " WHERE f.datedeparture BETWEEN ? AND ?" + ORDER_BY_DEPARTURE;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(validStart));
            statement.setDate(2, Date.valueOf(validEnd));
            return readMany(statement);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar vuelos por rango de fechas.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public List<Flight> findByRoute(Airport departure, Airport arrival) {
        Airport validDeparture = Validator.requireNotNull(departure, "aeropuerto de salida");
        Airport validArrival = Validator.requireNotNull(arrival, "aeropuerto de llegada");
        String sql = FLIGHT_SELECT +
            " WHERE f.departure = ? AND f.arrival = ?" + ORDER_BY_DEPARTURE;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, validDeparture.getId());
            statement.setObject(2, validArrival.getId());
            return readMany(statement);
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar vuelos por ruta.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public long countByAirline(Airline airline) {
        Airline validAirline = Validator.requireNotNull(airline, "aerolínea");
        String sql = "SELECT COUNT(*) FROM tblflights WHERE airlineid = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validAirline.getId());
            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar vuelos de la aerolínea.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByAirline(Airline airline) {
        Airline validAirline = Validator.requireNotNull(airline, "aerolínea");
        String sql = "SELECT EXISTS(SELECT 1 FROM tblflights WHERE airlineid = ?)";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validAirline.getId());
            return readExists(statement);
        } catch (SQLException e) {
            throw new RuntimeException("Error al comprobar los vuelos de la aerolínea.", e);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean existsByAirport(Airport airport) {
        Airport validAirport = Validator.requireNotNull(airport, "aeropuerto");
        String sql = """
                SELECT EXISTS(
                    SELECT 1 FROM tblflights
                    WHERE departure = ? OR arrival = ?
                )
                """;
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, validAirport.getId());
            statement.setObject(2, validAirport.getId());
            return readExists(statement);
        } catch (SQLException e) {
            throw new RuntimeException("Error al comprobar los vuelos del aeropuerto.", e);
        }
    }

    /** Escribe los ocho atributos persistidos de Flight en los parámetros 1 a 8. */
    private void bindFlightFields(PreparedStatement statement, Flight flight) throws SQLException {
        statement.setString(1, flight.getFlightName());
        statement.setObject(2, flight.getDeparture().getId());
        statement.setObject(3, flight.getArrival().getId());
        statement.setDate(4, Date.valueOf(flight.getDepartureDate()));
        statement.setTime(5, Time.valueOf(flight.getDepartureTime()));
        statement.setDate(6, Date.valueOf(flight.getArrivalDate()));
        statement.setTime(7, Time.valueOf(flight.getArrivalTime()));
        statement.setInt(8, flight.getAirline().getId());
    }

    /** Devuelve una copia del vuelo con el ID asignado por PostgreSQL. */
    private Flight withId(Flight flight, int id) {
        return new Flight(id, flight.getFlightName(), flight.getDeparture(),
            flight.getArrival(), flight.getDepartureDate(), flight.getDepartureTime(),
            flight.getArrivalDate(), flight.getArrivalTime(), flight.getAirline());
    }

    /** Reconstruye un Flight con sus entidades asociadas a partir de una fila SQL. */
    private Flight mapFlight(ResultSet result) throws SQLException {
        City departureCity = new City(result.getInt("departure_city_id"),
            result.getString("departure_city_name"));
        City arrivalCity = new City(result.getInt("arrival_city_id"),
            result.getString("arrival_city_name"));

        Airport departure = new Airport(
            result.getObject("departure_id", UUID.class),
            result.getString("departure_name"), departureCity);
        Airport arrival = new Airport(
            result.getObject("arrival_id", UUID.class),
            result.getString("arrival_name"), arrivalCity);
        Airline airline = new Airline(result.getInt("airlineid"),
            result.getString("airline_name"));

        return new Flight(result.getInt("flightid"),
            result.getString("flightname"), departure, arrival,
            result.getDate("datedeparture").toLocalDate(),
            result.getTime("hourdeparture").toLocalTime(),
            result.getDate("datearrival").toLocalDate(),
            result.getTime("hourarrival").toLocalTime(), airline);
    }

    /** Ejecuta un SELECT preparado que devuelve cero o una fila de vuelo. */
    private Optional<Flight> readOne(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            return result.next() ? Optional.of(mapFlight(result)) : Optional.empty();
        }
    }

    /** Ejecuta un SELECT preparado y reconstruye los vuelos de todas sus filas. */
    private List<Flight> readMany(PreparedStatement statement) throws SQLException {
        List<Flight> flights = new ArrayList<>();
        try (ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                flights.add(mapFlight(result));
            }
        }
        return flights;
    }

    /** Ejecuta una consulta SELECT EXISTS. */
    private boolean readExists(PreparedStatement statement) throws SQLException {
        try (ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getBoolean(1);
        }
    }

    /** Rechaza IDs nulos, cero o negativos antes de consultar PostgreSQL. */
    private int requireFlightId(Integer id) {
        Validator.requireNotNull(id, "id del vuelo");
        return Math.toIntExact(Validator.requirePositive(id, "id del vuelo"));
    }
}
