package com.airlinespoo.locations.repository;

import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
/**
 * Gestiona la persistencia y las consultas de aeropuertos
 * en PostgreSQL mediante JDBC.
 *
 * Implementa las operaciones CRUD y las búsquedas específicas
 * relacionadas con ciudades y aeropuertos.
 */
public class AirportRepository implements IAirportRepository {

    private Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/aerolines";
        String user = "postgres";
        String password = "postgres";

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Registra un nuevo aeropuerto asociado a una ciudad.
     *
     * @param airport aeropuerto que se desea registrar.
     * @return aeropuerto registrado.
     * @throws ValidationException si el aeropuerto es null.
     * @throws RuntimeException si ocurre un error durante la operación SQL.
     */
    @Override
    public Airport create(Airport airport) {
        Airport validAirport = Validator.requireNotNull(
            airport, "aeropuerto"
        );

        String sql = "INSERT INTO tblairports (airportid, name, cityid) VALUES (?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, validAirport.getId());
            statement.setString(2, validAirport.getName());
            statement.setInt(3, validAirport.getCity().getId());

            statement.executeUpdate();

            return validAirport;
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar el aeropuerto", e);
        }
    }

    /**
     * Busca un aeropuerto por su identificador UUID.
     *
     * @param id identificador del aeropuerto.
     * @return Optional con el aeropuerto encontrado, o vacío si no existe.
     * @throws ValidationException si el identificador es null.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public Optional<Airport> findById(UUID id) {
        UUID validId = Validator.requireNotNull(id, "id de aeropuerto");

        String sql = """
            SELECT a.airportid, a.name AS airport_name,
                   c.cityid, c.name AS city_name
            FROM tblairports a
            INNER JOIN tblcities c ON a.cityid = c.cityid
            WHERE a.airportid = ?
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, validId);

            try (java.sql.ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    City city = new City(
                        result.getInt("cityid"),
                        result.getString("city_name")
                    );
                    Airport airport = new Airport(
                        result.getObject("airportid", UUID.class),
                        result.getString("airport_name"),
                        city
                    );
                    return Optional.of(airport);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar el aeropuerto", e);
        }
    }

    /**
     * Obtiene todos los aeropuertos con sus ciudades asociadas.
     *
     * @return lista de aeropuertos registrados con ciudad asociada.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public List<Airport> findAll() {
        List<Airport> airports = new ArrayList<>();

        String sql = """
            SELECT a.airportid, a.name AS airport_name,
                   c.cityid, c.name AS city_name
            FROM tblairports a
            INNER JOIN tblcities c ON a.cityid = c.cityid
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                City city = new City(
                    result.getInt("cityid"),
                    result.getString("city_name")
                );
                Airport airport = new Airport(
                    result.getObject("airportid", UUID.class),
                    result.getString("airport_name"),
                    city
                );
                airports.add(airport);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar los aeropuertos", e);
        }
        return airports;
    }

    /**
     * Actualiza el nombre y la ciudad asociada a un aeropuerto.
     *
     * @param airport aeropuerto con los datos actualizados.
     * @return aeropuerto actualizado.
     * @throws ValidationException si el aeropuerto es null.
     * @throws NotFoundException si el aeropuerto no existe.
     * @throws RuntimeException si ocurre un error durante la operación SQL.
     */
    @Override
    public Airport update(Airport airport) {
        Airport validAirport = Validator.requireNotNull(
            airport, "aeropuerto"
        );

        String sql = """
            UPDATE tblairports
            SET name = ?, cityid = ?
            WHERE airportid = ?
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, validAirport.getName());
            statement.setInt(2, validAirport.getCity().getId());
            statement.setObject(3, validAirport.getId());

            int rowsAffected = statement.executeUpdate();

            if (rowsAffected == 0) {
                throw new NotFoundException("aeropuerto", validAirport.getId());
            }
            return validAirport;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar el aeropuerto", e);
        }
    }

    /**
     * Elimina un aeropuerto mediante su identificador UUID.
     *
     * @param id identificador del aeropuerto.
     * @return true si el aeropuerto fue eliminado; false si no existe.
     * @throws ValidationException si el identificador es null.
     * @throws RuntimeException si ocurre un error durante la operación SQL.
     */
    @Override
    public boolean deleteById(UUID id) {
        UUID validId = Validator.requireNotNull(id, "id de aeropuerto");

        String sql = "DELETE FROM tblairports WHERE airportid = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, validId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar el aeropuerto", e);
        }
    }

    /**
     * Comprueba si existe un aeropuerto con el identificador indicado.
     *
     * @param id identificador UUID del aeropuerto.
     * @return true si el aeropuerto existe; false en caso contrario.
     * @throws ValidationException si el identificador es null.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public boolean existsById(UUID id) {
        UUID validId = Validator.requireNotNull(id, "id de aeropuerto");

        String sql = "SELECT EXISTS(SELECT 1 FROM tblairports WHERE airportid = ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, validId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getBoolean(1);
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al verificar la existencia del aeropuerto", e
            );
        }
    }

    /**
     * Cuenta los aeropuertos registrados en la base de datos.
     *
     * @return cantidad total de aeropuertos registrados.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM tblairports";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                return result.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al contar los aeropuertos", e
            );
        }
    }

    /**
     * Obtiene los aeropuertos asociados a una ciudad determinada.
     *
     * @param city ciudad cuyos aeropuertos se desean consultar.
     * @return lista de aeropuertos pertenecientes a la ciudad.
     * @throws ValidationException si la ciudad es null.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public List<Airport> listAirportsByCity(City city) {
        City validCity = Validator.requireNotNull(city, "ciudad");
        List<Airport> airports = new ArrayList<>();

        String sql = """
            SELECT a.airportid, a.name AS airport_name,
                   c.cityid, c.name AS city_name
            FROM tblairports a
            INNER JOIN tblcities c ON a.cityid = c.cityid
            WHERE c.cityid = ?
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, validCity.getId());

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    City foundCity = new City(
                        result.getInt("cityid"),
                        result.getString("city_name")
                    );
                    Airport airport = new Airport(
                        result.getObject("airportid", UUID.class),
                        result.getString("airport_name"),
                        foundCity
                    );
                    airports.add(airport);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al listar los aeropuertos por ciudad", e
            );
        }
        return airports;
    }

    /**
     * Busca aeropuertos cuyo nombre contenga el texto indicado,
     * sin distinguir entre mayúsculas y minúsculas.
     *
     * @param name texto utilizado para realizar la búsqueda.
     * @return lista de aeropuertos que coinciden con el criterio.
     * @throws ValidationException si el texto de búsqueda no es válido.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public List<Airport> searchAirportsByName(String name) {
        String validName = Validator.requireText(
            name, "nombre de aeropuerto"
        );
        List<Airport> airports = new ArrayList<>();

        String sql = """
            SELECT a.airportid, a.name AS airport_name,
                   c.cityid, c.name AS city_name
            FROM tblairports a
            INNER JOIN tblcities c ON a.cityid = c.cityid
            WHERE a.name ILIKE ?
            """;

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + validName + "%");

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    City city = new City(
                        result.getInt("cityid"),
                        result.getString("city_name")
                    );
                    Airport airport = new Airport(
                        result.getObject("airportid", UUID.class),
                        result.getString("airport_name"),
                        city
                    );
                    airports.add(airport);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al buscar aeropuertos por nombre", e
            );
        }
        return airports;
    }

    /**
     * Cuenta los aeropuertos asociados a una ciudad.
     *
     * @param city ciudad cuyos aeropuertos se desean contar.
     * @return cantidad de aeropuertos asociados a la ciudad.
     * @throws ValidationException si la ciudad es null.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public long countAirportsByCity(City city) {
        City validCity = Validator.requireNotNull(city, "ciudad");

        String sql = "SELECT COUNT(*) FROM tblairports WHERE cityid = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, validCity.getId());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getLong(1);
                }
            }

            return 0;
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al contar los aeropuertos por ciudad", e
            );
        }
    }

    /**
     * Comprueba si una ciudad tiene aeropuertos asociados.
     *
     * @param city ciudad que se desea verificar.
     * @return true si existe al menos un aeropuerto asociado;
     *         false en caso contrario.
     * @throws ValidationException si la ciudad es null.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public boolean existsByCity(City city) {
        City validCity = Validator.requireNotNull(city, "ciudad");

        String sql = "SELECT EXISTS(SELECT 1 FROM tblairports WHERE cityid = ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, validCity.getId());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getBoolean(1);
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al verificar aeropuertos asociados a la ciudad", e
            );
        }
    }
}
