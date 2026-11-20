package com.airlinespoo.reservations.repository;

import core.database.DatabaseConnection;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.model.Seat;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.flights.model.Flight;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReservationRepository {

    // Guarda la reserva y sus detalles en las tablas tblReservations y tblDetailsReservation
    public void save(Reservation reservation) {
        String sqlReservation = "INSERT INTO tblReservations (ReservationId, CustomerId, FlightId) VALUES (?, ?, ?)";
        String sqlDetail = "INSERT INTO tblDetailsReservation (DetailId, ReservationId, PassangerFullname, PassagerDocument, SeatNumer, RowNumber) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Desactivamos el autocommit para manejar una transacción segura (todo o nada)
            conn.setAutoCommit(false);

            try {
                // 1. Inserta el registro principal de la reserva
                try (PreparedStatement pstmtRes = conn.prepareStatement(sqlReservation)) {
                    pstmtRes.setInt(1, reservation.getId());
                    pstmtRes.setString(2, reservation.getCustomer() != null ? reservation.getCustomer().getCustomerId() : null);
                    pstmtRes.setInt(3, reservation.getFlight() != null ? reservation.getFlight().getFlightId() : 0);
                    pstmtRes.executeUpdate();
                }

                // 2. Inserta cada pasajero y su silla asociada en tblDetailsReservation
                try (PreparedStatement pstmtDet = conn.prepareStatement(sqlDetail)) {
                    for (ReservationDetail detail : reservation.getDetails()) {
                        pstmtDet.setInt(1, detail.getId());
                        pstmtDet.setInt(2, reservation.getId());
                        pstmtDet.setString(3, detail.getPassengerFullName());
                        pstmtDet.setString(4, detail.getPassengerDocument());
                        pstmtDet.setShort(5, (short) detail.getSeat().getSeatNumber());
                        pstmtDet.setString(6, detail.getSeat().getRowNumber());
                        pstmtDet.executeUpdate();
                    }
                }

                // Confirma ambos inserts si no hubo errores
                conn.commit();
            } catch (SQLException e) {
                // Si falla un insert (por ejemplo, ID duplicado o llave foránea), revierte todo
                conn.rollback();
                throw new RuntimeException("Error al guardar en la base de datos: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión con la base de datos: " + e.getMessage(), e);
        }
    }

    // Busca una reserva puntual por su ID y devuelve un Optional
    public Optional<Reservation> findById(int id) {
        String sql = "SELECT * FROM tblReservations WHERE ReservationId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int resId = rs.getInt("ReservationId");
                    Customer customer = new Customer();
                    Flight flight = new Flight();
                    List<ReservationDetail> details = findDetailsByReservationId(resId);

                    return Optional.of(new Reservation(resId, customer, flight, details));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar reserva por ID: " + e.getMessage(), e);
        }
        return Optional.empty();
    }

    // Busca todas las reservas asociadas a un cliente específico
    public List<Reservation> findByCustomerId(String customerId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM tblReservations WHERE CustomerId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int resId = rs.getInt("ReservationId");
                    Customer customer = new Customer();
                    Flight flight = new Flight();
                    List<ReservationDetail> details = findDetailsByReservationId(resId);

                    reservations.add(new Reservation(resId, customer, flight, details));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar reservas por cliente: " + e.getMessage(), e);
        }
        return reservations;
    }

    // Busca todas las reservas asociadas a un vuelo en la base de datos
    public List<Reservation> findByFlightId(int flightId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM tblReservations WHERE FlightId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, flightId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int resId = rs.getInt("ReservationId");
                    Customer customer = new Customer();
                    Flight flight = new Flight();
                    List<ReservationDetail> details = findDetailsByReservationId(resId);

                    reservations.add(new Reservation(resId, customer, flight, details));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar reservas por vuelo: " + e.getMessage(), e);
        }
        return reservations;
    }

    // Elimina una reserva de tblReservations por su ID
    public boolean delete(int id) {
        String sql = "DELETE FROM tblReservations WHERE ReservationId = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Error al eliminar la reserva: " + e.getMessage(), e);
        }
    }

    // Método auxiliar privado para reconstruir la lista de pasajeros y sillas de una reserva
    private List<ReservationDetail> findDetailsByReservationId(int reservationId) {
        List<ReservationDetail> details = new ArrayList<>();
        String sql = "SELECT * FROM tblDetailsReservation WHERE ReservationId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, reservationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int detailId = rs.getInt("DetailId");
                    String fullName = rs.getString("PassangerFullname");
                    String document = rs.getString("PassagerDocument");
                    short seatNumber = rs.getShort("SeatNumer");
                    String rowNumber = rs.getString("RowNumber");

                    Seat seat = new Seat(rowNumber != null ? rowNumber.trim() : "", seatNumber);
                    details.add(new ReservationDetail(detailId, fullName, document, seat));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al cargar detalles de la reserva: " + e.getMessage(), e);
        }
        return details;
    }
}
