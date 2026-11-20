package com.airlinespoo.reservations.repository;

import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.model.Seat;
import core.database.DatabaseConnection;
import java.sql.*;
import java.util.*;

/** Almacena reservas y detalles en PostgreSQL, reutilizando los modelos reales del proyecto. */
public class ReservationRepository {
    private final CustomerService customers;
    private final FlightService flights;

    public ReservationRepository(CustomerService customers, FlightService flights) {
        this.customers = Objects.requireNonNull(customers);
        this.flights = Objects.requireNonNull(flights);
    }

    /** Guarda cabecera y pasajeros en una única transacción. */
    public void save(Reservation reservation) {
        String master = "INSERT INTO tblReservations (ReservationId, CustomerId, FlightId) VALUES (?, ?, ?)";
        String detail = "INSERT INTO tblDetailsReservation (DetailId, ReservationId, PassangerFullname, PassagerDocument, SeatNumer, RowNumber) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement m = connection.prepareStatement(master);
                 PreparedStatement d = connection.prepareStatement(detail)) {
                m.setInt(1, reservation.getId());
                m.setString(2, reservation.getCustomer().getId());
                m.setInt(3, reservation.getFlight().getId());
                m.executeUpdate();
                for (ReservationDetail passenger : reservation.getDetails()) {
                    d.setInt(1, passenger.getId());
                    d.setInt(2, reservation.getId());
                    d.setString(3, passenger.getPassengerFullName());
                    d.setString(4, passenger.getPassengerDocument());
                    d.setShort(5, (short) passenger.getSeat().getSeatNumber());
                    d.setString(6, passenger.getSeat().getRowNumber());
                    d.addBatch();
                }
                d.executeBatch();
                connection.commit();
            } catch (SQLException | RuntimeException ex) {
                connection.rollback();
                throw new IllegalStateException("No se pudo guardar la reserva", ex);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error de conexión al guardar la reserva", ex);
        }
    }

    public Optional<Reservation> findById(int id) {
        return find("WHERE ReservationId = ?", id).stream().findFirst();
    }

    public List<Reservation> findByCustomerId(String customerId) {
        return find("WHERE CustomerId = ?", customerId);
    }

    public List<Reservation> findByFlightId(int flightId) {
        return find("WHERE FlightId = ?", flightId);
    }

    private List<Reservation> find(String filter, Object value) {
        List<Reservation> result = new ArrayList<>();
        String sql = "SELECT ReservationId, CustomerId, FlightId FROM tblReservations " + filter;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setObject(1, value);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt("ReservationId");
                    Customer customer = customers.getCustomer(rs.getString("CustomerId"));
                    Flight flight = flights.getFlight(rs.getInt("FlightId"));
                    result.add(new Reservation(id, customer, flight, readDetails(connection, id)));
                }
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error consultando reservas", ex);
        }
        return result;
    }

    private List<ReservationDetail> readDetails(Connection connection, int reservationId) throws SQLException {
        List<ReservationDetail> details = new ArrayList<>();
        String sql = "SELECT DetailId, PassangerFullname, PassagerDocument, SeatNumer, RowNumber " +
            "FROM tblDetailsReservation WHERE ReservationId = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, reservationId);
            try (ResultSet rs = statement.executeQuery()) {
                while (rs.next()) {
                    Seat seat = new Seat(rs.getString("RowNumber").trim(), rs.getInt("SeatNumer"));
                    details.add(new ReservationDetail(rs.getInt("DetailId"),
                        rs.getString("PassangerFullname"), rs.getString("PassagerDocument"), seat));
                }
            }
        }
        return details;
    }

    /** Elimina detalles y cabecera juntos; no deja pasajeros huérfanos. */
    public boolean delete(int id) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement details = connection.prepareStatement(
                "DELETE FROM tblDetailsReservation WHERE ReservationId = ?");
                 PreparedStatement master = connection.prepareStatement(
                     "DELETE FROM tblReservations WHERE ReservationId = ?")) {
                details.setInt(1, id);
                details.executeUpdate();
                master.setInt(1, id);
                boolean removed = master.executeUpdate() > 0;
                connection.commit();
                return removed;
            } catch (SQLException ex) {
                connection.rollback();
                throw new IllegalStateException("Error eliminando reserva", ex);
            }
        } catch (SQLException ex) {
            throw new IllegalStateException("Error de conexión al cancelar reserva", ex);
        }
    }

    /** Persiste un nuevo pasajero de una reserva existente. */
    public void addDetail(int reservationId, ReservationDetail detail) {
        String sql = "INSERT INTO tblDetailsReservation " +
            "(DetailId, ReservationId, PassangerFullname, PassagerDocument, SeatNumer, RowNumber) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, detail.getId());
            statement.setInt(2, reservationId);
            statement.setString(3, detail.getPassengerFullName());
            statement.setString(4, detail.getPassengerDocument());
            statement.setInt(5, detail.getSeat().getSeatNumber());
            statement.setString(6, detail.getSeat().getRowNumber());
            statement.executeUpdate();
        } catch (SQLException ex) {
            throw new IllegalStateException("Error agregando pasajero", ex);
        }
    }
}
