package com.airlinespoo.reservations.service;

import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.model.Seat;
import com.airlinespoo.reservations.repository.ReservationRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

//Servicio encargado de gestionar la lógica de negocio
public class ReservationService {

    private final ReservationRepository reservationRepository;

    // Conectar modulos


    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    //se crear reserva (Valida existencia, fecha del vuelo y disponibilidad global de sillas).
    public void createReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reserva no kan ser nula.");
        }

        int flightId = reservation.getFlight().getFlightId();
        Set<Seat> occupiedSeatsInFlight = getOccupiedSeatsForFlight(flightId);

        for (ReservationDetail detail : reservation.getDetails()) {
            if (occupiedSeatsInFlight.contains(detail.getSeat())) {
                throw new IllegalArgumentException("La silla " + detail.getSeat() + " ya se encuentra ocupada en este vuelo por otra reserva.");
            }
        }

        // Si pasa todas las validaciones globales, se guarda en el repositorio
        reservationRepository.save(reservation);
    }

     //Obtener un conjunto (Set) con todas las sillas ya ocupadas en un vuelo específico,
     //evitar error de la pista4
    public Set<Seat> getOccupiedSeatsForFlight(int flightId) {
        List<Reservation> reservationsForFlight = reservationRepository.findByFlightId(flightId);
        Set<Seat> occupiedSeats = new HashSet<>();

        for (Reservation res : reservationsForFlight) {
            for (ReservationDetail detail : res.getDetails()) {
                occupiedSeats.add(detail.getSeat());
            }
        }
        return occupiedSeats;
    }

    //Tabla: Agregar un pasajero a una reserva existente validando la silla a nivel global del vuelo.
    public void addPassengerToReservation(int reservationId, ReservationDetail newDetail) {
        Reservation reservation = reservationRepository.findById(reservationId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró la reserva con ID: " + reservationId));

        // Validar que la silla esté libre en todo el vuelo antes de agregarla a la reserva
        int flightId = reservation.getFlight().getFlightId();
        Set<Seat> occupiedSeats = getOccupiedSeatsForFlight(flightId);

        if (occupiedSeats.contains(newDetail.getSeat())) {
            throw new IllegalArgumentException("La silla " + newDetail.getSeat() + " ya está ocupada en este vuelo.");
        }

        // Si está libre, la agregamos usando el método de negocio de la entidad Reservation
        reservation.addPassenger(newDetail);
    }

    //Tabla: Cancelar reserva (Se elimina la reserva con todos sus detalles por composición).
    public boolean cancelReservation(int id) {
        boolean removed = reservationRepository.delete(id);
        if (!removed) {
            throw new IllegalArgumentException("No se pudo cancelar: No existe una reserva con el ID " + id);
        }
        return true;
    }

    // Consultas requeridas en la tabla
    public List<Reservation> getReservationsByCustomer(String customerId) {
        return reservationRepository.findByCustomerId(customerId);
    }

    public List<Reservation> getReservationsByFlight(int flightId) {
        return reservationRepository.findByFlightId(flightId);
    }
}
