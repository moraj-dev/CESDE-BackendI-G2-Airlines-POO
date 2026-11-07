package com.airlinespoo.customers.repository;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.customers.model.Country;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CountryRepository implements ICountryRepository {

    private Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=NOMBRE_BD;encrypt=false;loginTimeout=5";
        String user = "USUARIO";
        String password = "PASSWORD";
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public void create(Country country) {
        String sql = "INSERT INTO dbo.tblCountries (CountryId, Name) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, country.getId());
            pstmt.setString(2, country.getName());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new AirlinesException("Error al guardar el país.", e);
        }
    }

    @Override
    public Optional<Country> findById(Integer id) {
        String sql = "SELECT CountryId, Name FROM dbo.tblCountries WHERE CountryId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Country(rs.getInt("CountryId"), rs.getString("Name")));
                }
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al buscar el país.", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Country> findAll() {
        List<Country> countries = new ArrayList<>();
        String sql = "SELECT CountryId, Name FROM dbo.tblCountries ORDER BY Name";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                countries.add(new Country(rs.getInt("CountryId"), rs.getString("Name")));
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al listar los países.", e);
        }
        return countries;
    }

    @Override
    public void update(Country country) {
        String sql = "UPDATE dbo.tblCountries SET Name = ? WHERE CountryId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, country.getName());
            pstmt.setInt(2, country.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new AirlinesException("Error al actualizar el país.", e);
        }
    }

    @Override
    public void deleteById(Integer id) {
        String sql = "DELETE FROM dbo.tblCountries WHERE CountryId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new AirlinesException("Error al eliminar el país.", e);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT 1 FROM dbo.tblCountries WHERE CountryId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al verificar el país.", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM dbo.tblCountries";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new AirlinesException("Error al contar los países.", e);
        }
    }
}
