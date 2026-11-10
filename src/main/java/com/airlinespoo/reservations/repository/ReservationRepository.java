package com.airlinespoo.reservations.repository;

import com.airlinespoo.reservations.model.Reservation;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


//Repositorio en memoria para la gestión y almacenamiento temporal de reservas.
public class ReservationRepository {

    //SeAlmacenamiento en memoria (estructura de datos base)
    private final List<Reservation> reservations = new ArrayList<>();

    //Guarda una nueva reserva en el repositorio.
    public void save(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("La reserva no puede ser nula.");
        }
        // Validar que no exista otra reserva con el mismo ID
        if (findById(reservation.getId()).isPresent()) {
            throw new IllegalArgumentException("Ya existe una reserva registrada con el ID: " + reservation.getId());
        }
        reservations.add(reservation);
    }

    //Busca una reserva por su ID único.
    public Optional<Reservation> findById(int id) {
        return reservations.stream()
            .filter(r -> r.getId() == id)
            .findFirst();
    }

    //Retorna una copia de todas las reservas registradas.
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations);
    }

    //Permite buscar reservas asociadas a un cliente específico (por ID del cliente).
    public List<Reservation> findByCustomerId(String customerId) {
        if (customerId == null) {
            return new ArrayList<>();
        }
        return reservations.stream()
            .filter(r -> r.getCustomer().getCustomerId().equalsIgnoreCase(customerId))
            .collect(Collectors.toList());
    }

    //Permite buscar reservas asociadas a un vuelo específico (por ID del vuelo).
    public List<Reservation> findByFlightId(int flightId) {
        return reservations.stream()
            .filter(r -> r.getFlight().getFlightId() == flightId)
            .collect(Collectors.toList());
    }

    //Elimina una reserva del repositorio por su ID.
    public boolean delete(int id) {
        return reservations.removeIf(r -> r.getId() == id);
    }
}
