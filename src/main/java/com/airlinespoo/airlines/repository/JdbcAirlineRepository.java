package com.airlinespoo.airlines.repository;

import com.airlinespoo.airlines.model.Airline;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implementación JDBC de {@link AirlineRepository} para la tabla {@code tblairlines} en PostgreSQL.
 */
public class JdbcAirlineRepository implements AirlineRepository {

    private static final String SQL_INSERT =
        "INSERT INTO tblairlines (name) VALUES (?)";
    private static final String SQL_SELECT_BY_ID =
        "SELECT airlineid, name FROM tblairlines WHERE airlineid = ?";
    private static final String SQL_SELECT_ALL =
        "SELECT airlineid, name FROM tblairlines";
    private static final String SQL_UPDATE =
        "UPDATE tblairlines SET name = ? WHERE airlineid = ?";
    private static final String SQL_DELETE_BY_ID =
        "DELETE FROM tblairlines WHERE airlineid = ?";
    private static final String SQL_EXISTS_BY_ID =
        "SELECT 1 FROM tblairlines WHERE airlineid = ?";
    private static final String SQL_COUNT =
        "SELECT COUNT(*) FROM tblairlines";
    private static final String SQL_SELECT_BY_NAME =
        "SELECT airlineid, name FROM tblairlines WHERE LOWER(name) = LOWER(?)";
    private static final String SQL_EXISTS_BY_NAME =
        "SELECT 1 FROM tblairlines WHERE LOWER(name) = LOWER(?)";

    private final String url;
    private final String user;
    private final String password;

    /**
     * Crea un repositorio JDBC con los datos de conexión a PostgreSQL.
     *
     * @param url      URL JDBC de PostgreSQL (ej. {@code jdbc:postgresql://localhost:5432/tu_bd})
     * @param user     usuario de la base de datos
     * @param password contraseña de la base de datos
     */
    public JdbcAirlineRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    /**
     * Inserta una nueva aerolínea y asigna el id generado por PostgreSQL.
     *
     * @param entity aerolínea a registrar
     * @return aerolínea creada con su id generado
     */
    @Override
    public Airline create(Airline entity) {
        validateEntity(entity);
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(
                 SQL_INSERT, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, entity.getName());
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    int generatedId = keys.getInt("airlineid");
                    return new Airline(generatedId, entity.getName());
                }
            }
            throw new IllegalStateException("PostgreSQL no devolvió el id generado para la aerolínea");
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo registrar la aerolínea", e);
        }
    }

    /**
     * Busca una aerolínea por su {@code airlineid}.
     *
     * @param id identificador de la aerolínea
     * @return {@link Optional} con la aerolínea si existe, o vacío en caso contrario
     */
    @Override
    public Optional<Airline> findById(Integer id) {
        if (id == null) {
            return Optional.empty();
        }
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapRow(result));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo consultar la aerolínea por id", e);
        }
    }

    /**
     * Lista todas las aerolíneas registradas en la tabla.
     *
     * @return lista de aerolíneas
     */
    @Override
    public List<Airline> findAll() {
        List<Airline> airlines = new ArrayList<>();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_ALL);
             ResultSet result = statement.executeQuery()) {
            while (result.next()) {
                airlines.add(mapRow(result));
            }
            return airlines;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron listar las aerolíneas", e);
        }
    }

    /**
     * Actualiza el nombre de una aerolínea existente por su {@code airlineid}.
     *
     * @param entity aerolínea con los datos actualizados
     * @return aerolínea actualizada
     */
    @Override
    public Airline update(Airline entity) {
        validateEntity(entity);
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_UPDATE)) {
            statement.setString(1, entity.getName());
            statement.setInt(2, entity.getId());
            int rowsAffected = statement.executeUpdate();
            if (rowsAffected == 0) {
                throw new IllegalStateException("No existe una aerolínea con id " + entity.getId() + " para actualizar");
            }
            return entity;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo actualizar la aerolínea", e);
        }
    }

    /**
     * Elimina una aerolínea por su {@code airlineid}.
     *
     * @param id identificador de la aerolínea
     * @return {@code true} si se eliminó, {@code false} si no existía
     */
    @Override
    public boolean deleteById(Integer id) {
        if (id == null) {
            return false;
        }
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_DELETE_BY_ID)) {
            statement.setInt(1, id);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo eliminar la aerolínea", e);
        }
    }

    /**
     * Indica si existe una aerolínea con el {@code airlineid} dado.
     *
     * @param id identificador de la aerolínea
     * @return {@code true} si existe, {@code false} en caso contrario
     */
    @Override
    public boolean existsById(Integer id) {
        if (id == null) {
            return false;
        }
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_EXISTS_BY_ID)) {
            statement.setInt(1, id);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo verificar la existencia de la aerolínea", e);
        }
    }

    /**
     * Cuenta el total de aerolíneas registradas.
     *
     * @return cantidad total de aerolíneas
     */
    @Override
    public long count() {
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_COUNT);
             ResultSet result = statement.executeQuery()) {
            if (result.next()) {
                return result.getLong(1);
            }
            return 0L;
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudieron contar las aerolíneas", e);
        }
    }

    /**
     * Busca una aerolínea por nombre ignorando mayúsculas y minúsculas.
     *
     * @param name nombre de la aerolínea
     * @return {@link Optional} con la aerolínea si coincide, o vacío si no existe
     */
    @Override
    public Optional<Airline> findByName(String name) {
        if (name == null || name.isBlank()) {
            return Optional.empty();
        }
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_SELECT_BY_NAME)) {
            statement.setString(1, name.trim());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return Optional.of(mapRow(result));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo buscar la aerolínea por nombre", e);
        }
    }

    /**
     * Verifica si ya existe una aerolínea con ese nombre sin distinguir mayúsculas.
     *
     * @param name nombre de la aerolínea
     * @return {@code true} si ya existe, {@code false} en caso contrario
     */
    @Override
    public boolean existsByName(String name) {
        if (name == null || name.isBlank()) {
            return false;
        }
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(SQL_EXISTS_BY_NAME)) {
            statement.setString(1, name.trim());
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo verificar si existe el nombre de la aerolínea", e);
        }
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    private Airline mapRow(ResultSet result) throws SQLException {
        int id = result.getInt("airlineid");
        String name = result.getString("name");
        return new Airline(id, name);
    }

    private void validateEntity(Airline entity) {
        if (entity == null) {
            throw new IllegalArgumentException("La aerolínea no puede ser nula");
        }
    }
}
