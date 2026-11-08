package com.airlinespoo.customers.repository;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.customers.model.DocumentType;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DocumentTypeRepository implements IDocumentTypeRepository {

    private Connection getConnection() throws SQLException {
        String url = "jdbc:postgresql://localhost:5432/aerolines";
        String user = "postgres";
        String password = "1692040402";
        return DriverManager.getConnection(url, user, password);
    }

    @Override
    public void create(DocumentType documentType) {
        String sql = "INSERT INTO tblDocumentsType (TypeId, Description) VALUES (?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, documentType.getId());
            pstmt.setString(2, documentType.getDescription());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new AirlinesException("Error al guardar el tipo de documento.", e);
        }
    }

    @Override
    public Optional<DocumentType> findById(String id) {
        String sql = "SELECT TypeId, Description FROM tblDocumentsType WHERE TypeId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new DocumentType(rs.getString("TypeId"), rs.getString("Description")));
                }
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al buscar el tipo de documento.", e);
        }
        return Optional.empty();
    }

    @Override
    public List<DocumentType> findAll() {
        List<DocumentType> types = new ArrayList<>();
        String sql = "SELECT TypeId, Description FROM tblDocumentsType ORDER BY Description";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                types.add(new DocumentType(rs.getString("TypeId"), rs.getString("Description")));
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al listar los tipos de documento.", e);
        }
        return types;
    }

    @Override
    public void update(DocumentType documentType) {
        String sql = "UPDATE tblDocumentsType SET Description = ? WHERE TypeId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, documentType.getDescription());
            pstmt.setString(2, documentType.getId());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new AirlinesException("Error al actualizar el tipo de documento.", e);
        }
    }

    @Override
    public void deleteById(String id) {
        String sql = "DELETE FROM tblDocumentsType WHERE TypeId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            throw new AirlinesException("Error al eliminar el tipo de documento.", e);
        }
    }

    @Override
    public boolean existsById(String id) {
        String sql = "SELECT 1 FROM tblDocumentsType WHERE TypeId = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new AirlinesException("Error al verificar el tipo de documento.", e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM tblDocumentsType";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new AirlinesException("Error al contar los tipos de documento.", e);
        }
    }
}
