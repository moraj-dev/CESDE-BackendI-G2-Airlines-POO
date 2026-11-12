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

public class ReservationRepository {

    //Guarda la reserva y sus detalles en las tablas tblReservations y tblDetailsReservation.
    public void save(Reservation reservation) {
        String sqlReservation = "INSERT INTO tblReservations (ReservationId, CustomerId, FlightId) VALUES (?, ?, ?)";
        String sqlDetail = "INSERT INTO tblDetailsReservation (DetailId, ReservationId, PassangerFullname, PassagerDocument, SeatNumer, RowNumber) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Desactivamos el autocommit para manejar una transacción segura
            conn.setAutoCommit(false);

            try {
                // Guardar la cabecera de la reserva
                try (PreparedStatement pstmtRes = conn.prepareStatement(sqlReservation)) {
                    pstmtRes.setInt(1, reservation.getId());
                    pstmtRes.setString(2, reservation.getCustomer() != null ? reservation.getCustomer().getCustomerId() : null);
                    pstmtRes.setInt(3, reservation.getFlight() != null ? reservation.getFlight().getFlightId() : 0);
                    pstmtRes.executeUpdate();
                }

                // Guardar cada detalle (pasajero y silla) asociado a la reserva
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

                conn.commit(); // Confirmar transacción exitosa
            } catch (SQLException e) {
                conn.rollback(); // Revertir si algo falla
                throw new RuntimeException("Error al guardar en la base de datos: " + e.getMessage(), e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error de conexión con la base de datos: " + e.getMessage(), e);
        }
    }

    //Busca todas las reservas asociadas a un vuelo en la base de datos.

    public List<Reservation> findByFlightId(int flightId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT * FROM tblReservations WHERE FlightId = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, flightId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    int resId = rs.getInt("ReservationId");
                    String customerId = rs.getString("CustomerId");

                    // Reconstruimos objetos básicos para la vista
                    Customer customer = new Customer();
                    // (Opcional: aquí podrías asignar el ID si tu modelo lo permite)

                    Flight flight = new Flight();

                    Reservation res = new Reservation(resId, customer, flight, new ArrayList<>());
                    reservations.add(res);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al buscar reservas por vuelo: " + e.getMessage(), e);
        }
        return reservations;
    }

    //Elimina una reserva de la base de datos.

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
}
