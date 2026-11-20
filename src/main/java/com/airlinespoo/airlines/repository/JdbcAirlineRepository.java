package com.airlinespoo.airlines.repository;

import com.airlinespoo.airlines.model.Airline;

import java.sql.*;
import java.util.List;
import java.util.Optional;

public class JdbcAirlineRepository implements AirlineRepository {

    private final String url;
    private final String user;
    private final String password;

    public JdbcAirlineRepository(String url, String user, String password) {
        this.url = url;
        this.user = user;
        this.password = password;
    }

    @Override
    public Airline create(Airline entity) {
        return null;
    }

    @Override
    public Optional<Airline> findById(Integer integer) {
        return Optional.empty();
    }

    @Override
    public List<Airline> findAll() {
        return List.of();
    }

    @Override
    public Airline update(Airline entity) {
        return null;
    }

    @Override
    public boolean deleteById(Integer integer) {
        return false;
    }

    @Override
    public boolean existsById(Integer integer) {
        return false;
    }

    @Override
    public long count() {
        String sql ="SELECT COUNT(*) FROM tblairlines";
        try (Connection connection = DriverManager.getConnection(url, user, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {
            result.next();
            return result.getLong(1);
        } catch (SQLException e) {
            throw new IllegalStateException("No se pudo contar las aerolineas", e);
        }
    }

    @Override
    public Optional<Airline> findByName(String name) {
        return Optional.empty();
    }

    @Override
    public boolean existsByName(String name) {
        return false;
    }
}
