package com.airlinespoo.locations.repository;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.City;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Gestiona la persistencia y las consultas de ciudades en PostgreSQL
 * mediante JDBC.
 * Implementa las operaciones CRUD y permite realizar búsquedas por nombre.
 */
public class CityRepository implements ICityRepository {

    private Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/aerolines";
        String user = "postgres";
        String password = "postgres";

        return DriverManager.getConnection(url, user, password);
    }

    /**
     * Registra una nueva ciudad en la base de datos.
     *
     * @param city ciudad que se desea registrar.
     * @return ciudad creada con el identificador generado por PostgreSQL.
     * @throws ValidationException si la ciudad es null.
     * @throws RuntimeException si ocurre un error durante la operación SQL.
     */
    @Override
    public City create(City city) {
        City validCity = Validator.requireNotNull(
            city, "ciudad"
        );

        String sql = "INSERT INTO tblcities (name) VALUES (?) RETURNING cityid";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, validCity.getName());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    int id = result.getInt("cityid");
                    return new City(id, validCity.getName());
                }
            }
            throw new IllegalStateException("No se pudo obtener el ID de la ciudad creada.");
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar la ciudad", e);
        }
    }

    /**
     * Busca una ciudad por su identificador.
     *
     * @param id identificador de la ciudad.
     * @return Optional con la ciudad encontrada, o vacío si no existe.
     * @throws ValidationException si el identificador no es válido.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public Optional<City> findById(Integer id) {
        Integer validId = Validator.requireNotNull(id, "id de ciudad");
        String sql = "SELECT cityid, name FROM tblcities WHERE cityid = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validId);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    City city = new City(
                        result.getInt("cityid"),
                        result.getString("name")
                    );
                    return Optional.of(city);
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar la ciudad", e);
        }
    }

    /**
     * Obtiene todas las ciudades registradas en la base de datos.
     *
     * @return lista de ciudades registradas.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public List<City> findAll() {
        List<City> cities = new ArrayList<>();
        String sql = "SELECT cityid, name FROM tblcities";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                City city = new City(
                    result.getInt("cityid"),
                    result.getString("name")
                );
                cities.add(city);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al listar las ciudades", e);
        }
        return cities;
    }

    /**
     * Actualiza los datos de una ciudad existente.
     *
     * @param city ciudad con los datos que se desean actualizar.
     * @return ciudad actualizada.
     * @throws ValidationException si la ciudad es null.
     * @throws NotFoundException si la ciudad no existe.
     * @throws RuntimeException si ocurre un error durante la operación SQL.
     */
    @Override
    public City update(City city) {
        City validCity = Validator.requireNotNull(city, "ciudad");
        String sql = "UPDATE tblcities SET name = ? WHERE cityid = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, validCity.getName());
            statement.setInt(2, validCity.getId());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new NotFoundException("ciudad", validCity.getId());
            }
            return city;
        } catch (SQLException e) {
            throw new RuntimeException("Error al actualizar la ciudad", e);
        }
    }

    /**
     * Elimina una ciudad mediante su identificador.
     *
     * @param id identificador de la ciudad.
     * @return true si la ciudad fue eliminada; false si no existe.
     * @throws ValidationException si el identificador no es válido.
     * @throws RuntimeException si ocurre un error durante la operación SQL.
     */
    @Override
    public boolean deleteById(Integer id) {
        Integer validId = Validator.requireNotNull(id, "id de ciudad");
        String sql = "DELETE FROM tblcities WHERE cityid = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la ciudad", e);
        }
    }

    /**
     * Comprueba si existe una ciudad con el identificador indicado.
     *
     * @param id identificador de la ciudad.
     * @return true si la ciudad existe; false en caso contrario.
     * @throws ValidationException si el identificador no es válido.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public boolean existsById(Integer id) {
        Integer validId = Validator.requireNotNull(id, "id de ciudad");
        String sql = "SELECT EXISTS(SELECT 1 FROM tblcities WHERE cityid = ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, validId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getBoolean(1);
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar la existencia de la ciudad", e);
        }
    }

    /**
     * Cuenta las ciudades registradas en la base de datos.
     *
     * @return cantidad total de ciudades registradas.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM tblcities";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                return result.getLong(1);
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al contar las ciudades", e);
        }
    }

    /**
     * Busca ciudades cuyo nombre contenga el texto indicado,
     * sin distinguir entre mayúsculas y minúsculas.
     *
     * @param name texto utilizado para realizar la búsqueda.
     * @return lista de ciudades que coinciden con el criterio.
     * @throws ValidationException si el texto de búsqueda no es válido.
     * @throws RuntimeException si ocurre un error durante la consulta SQL.
     */
    @Override
    public List<City> searchCitiesByName(String name) {
        String validName = Validator.requireText(name, "nombre de ciudad");
        List<City> cities = new ArrayList<>();

        String sql = "SELECT cityid, name FROM tblcities WHERE name ILIKE ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, "%" + validName + "%");

            try (ResultSet result = statement.executeQuery()) {
                while (result.next()) {
                    City city = new City(
                        result.getInt("cityid"),
                        result.getString("name")
                    );

                    cities.add(city);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar ciudades por nombre", e);
        }
        return cities;
    }
}
