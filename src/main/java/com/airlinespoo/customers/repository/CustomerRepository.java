package com.airlinespoo.customers.repository;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerRepository implements ICustomerRepository {

    private static final String SELECT_CUSTOMER =
        "SELECT c.customerId, c.FirtName, c.Lastname, c.TelephoneNumber, c.Email, c.Address, "
            + "t.TypeId, t.Description AS TypeDescription, "
            + "p.CountryId, p.Name AS CountryName "
            + "FROM dbo.tblCustomers c "
            + "INNER JOIN dbo.tblDocumentsType t ON c.TypeId = t.TypeId "
            + "INNER JOIN dbo.tblCountries p ON c.CountryId = p.CountryId ";

    private Connection getConnection() throws SQLException {
        String url = "jdbc:sqlserver://localhost:1433;databaseName=NOMBRE_BD;encrypt=false";
        String user = "USUARIO";
        String password = "PASSWORD";
        return DriverManager.getConnection(url, user, password);
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        DocumentType type = new DocumentType(rs.getString("TypeId"), rs.getString("TypeDescription"));
        Country country = new Country(rs.getInt("CountryId"), rs.getString("CountryName"));
        return new Customer(
            rs.getString("customerId"),
            rs.getString("FirtName"),
            rs.getString("Lastname"),
            type,
            rs.getString("TelephoneNumber"),
            rs.getString("Email"),
            country,
            rs.getString("Address"));
    }

    @Override
    public void create(Customer customer) {
        String sql = "INSERT INTO dbo.tblCustomers "
            + "(customerId, FirtName, Lastname, TypeId, TelephoneNumber, Email, CountryId, Address) "
            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getId());
            pstmt.setString(2, customer.getFirstName());
            pstmt.setString(3, customer.getLastName());
            pstmt.setString(4, customer.getDocumentType().getId());
            pstmt.setString(5, customer.getTelephoneNumber());
            pstmt.setString(6, customer.getEmail());
            pstmt.setInt(7, customer.getCountry().getId());
            pstmt.setString(8, customer.getAddress());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new AirlinesException("Error al guardar el cliente.", e);
        }
    }

    @Override
    public Optional<Customer> findById(String id) {
        String sql = SELECT_CUSTOMER + "WHERE c.customerId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al buscar el cliente.", e);
        }
        return Optional.empty();
    }

    @Override
    public List<Customer> findAll() {
        String sql = SELECT_CUSTOMER + "ORDER BY c.Lastname, c.FirtName";
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                customers.add(mapCustomer(rs));
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al listar los clientes.", e);
        }
        return customers;
    }

    @Override
    public void update(Customer customer) {
        String sql = "UPDATE dbo.tblCustomers SET FirtName = ?, Lastname = ?, TypeId = ?, "
            + "TelephoneNumber = ?, Email = ?, CountryId = ?, Address = ? "
            + "WHERE customerId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, customer.getFirstName());
            pstmt.setString(2, customer.getLastName());
            pstmt.setString(3, customer.getDocumentType().getId());
            pstmt.setString(4, customer.getTelephoneNumber());
            pstmt.setString(5, customer.getEmail());
            pstmt.setInt(6, customer.getCountry().getId());
            pstmt.setString(7, customer.getAddress());
            pstmt.setString(8, customer.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new AirlinesException("Error al actualizar el cliente.", e);
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM dbo.tblCustomers WHERE customerId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new AirlinesException("Error al eliminar el cliente.", e);
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM dbo.tblCustomers WHERE customerId = ?";
        return exists(sql, id);
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM dbo.tblCustomers";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new AirlinesException("Error al contar los clientes.", e);
        }
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        String sql = SELECT_CUSTOMER + "WHERE LOWER(c.Email) = LOWER(?)";
        List<Customer> found = query(sql, email);
        return found.isEmpty() ? Optional.empty() : Optional.of(found.get(0));
    }

    @Override
    public boolean existsByEmail(String email) {
        if (email == null) {
            return false;
        }
        String sql = "SELECT 1 FROM dbo.tblCustomers WHERE LOWER(Email) = LOWER(?)";
        return exists(sql, email);
    }

    @Override
    public List<Customer> findByLastNameContaining(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        String sql = SELECT_CUSTOMER + "WHERE LOWER(c.Lastname) LIKE ?";
        return query(sql, "%" + text.trim().toLowerCase() + "%");
    }

    @Override
    public List<Customer> findByCountry(Country country) {
        String sql = SELECT_CUSTOMER + "WHERE c.CountryId = ?";
        return query(sql, country.getId());
    }

    @Override
    public List<Customer> findByDocumentType(DocumentType documentType) {
        String sql = SELECT_CUSTOMER + "WHERE c.TypeId = ?";
        return query(sql, documentType.getId());
    }

    @Override
    public boolean existsByCountry(Country country) {
        String sql = "SELECT 1 FROM dbo.tblCustomers WHERE CountryId = ?";
        return exists(sql, country.getId());
    }

    @Override
    public boolean existsByDocumentType(DocumentType documentType) {
        String sql = "SELECT 1 FROM dbo.tblCustomers WHERE TypeId = ?";
        return exists(sql, documentType.getId());
    }

    private List<Customer> query(String sql, Object parameter) {
        List<Customer> customers = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, parameter);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    customers.add(mapCustomer(rs));
                }
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al consultar los clientes.", e);
        }
        return customers;
    }

    private boolean exists(String sql, Object parameter) {
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setObject(1, parameter);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al verificar el cliente.", e);
        }
    }
}
