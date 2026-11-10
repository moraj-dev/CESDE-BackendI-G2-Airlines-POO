package com.airlinespoo.reservations.model;

import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.flights.model.Flight;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Reservation {

    private final int id;
    private final Customer customer;
    private final Flight flight;
    private final List<ReservationDetail> details;

    private static final int MAX_PASSENGERS = 9;//limite maximo definido

    //Constructor - Restricciones - Validaciones
    public Reservation(int id, Customer customer, Flight flight, List<ReservationDetail> initialDetails) {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID de la reserva debe ser un valor positivo.");
        }
        if (customer == null) {
            throw new IllegalArgumentException("El cliente de la reserva no puede ser nulo.");
        }
        if (flight == null) {
            throw new IllegalArgumentException("El vuelo de la reserva no puede ser nulo.");
        }
        if (initialDetails == null || initialDetails.isEmpty()) {
            throw new IllegalArgumentException("Una reserva debe tener al menos un pasajero.");
        }
        if (initialDetails.size() > MAX_PASSENGERS) {
            throw new IllegalArgumentException("La reserva no puede superar el máximo de " + MAX_PASSENGERS + " pasajeros.");
        }

        this.id = id;
        this.customer = customer;
        this.flight = flight;
        this.details = new ArrayList<>();

        // Agregamos y validamos cada detalle inicial mediante las reglas de la reserva
        for (ReservationDetail detail : initialDetails) {
            addPassengerDetail(detail);
        }
    }

    // Getters
    public int getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Flight getFlight() {
        return flight;
    }


    //Devuelve una copia de la lista (solo para ver) para proteger la composición y evitar que la modifiquen.
    public List<ReservationDetail> getDetails() {
        return Collections.unmodifiableList(new ArrayList<>(details));
    }


     //Agrega un nuevo pasajero validando reglas internas de la reserva.
    public void addPassenger(ReservationDetail detail) {
        if (detail == null) {
            throw new IllegalArgumentException("El detalle del pasajero no puede ser nulo.");
        }
        if (details.size() >= MAX_PASSENGERS) {
            throw new IllegalArgumentException("No se pueden agregar más pasajeros; se alcanzó el límite de " + MAX_PASSENGERS + ".");
        }
        addPassengerDetail(detail);
    }

    // Método privado auxiliar para validar duplicados internos en la reserva
    private void addPassengerDetail(ReservationDetail detail) {
        for (ReservationDetail existing : details) {
            // Un pasajero no puede aparecer dos veces en la misma reserva
            if (existing.getPassengerDocument().equalsIgnoreCase(detail.getPassengerDocument())) {
                throw new IllegalArgumentException("El pasajero con documento " + detail.getPassengerDocument() + " ya está registrado en esta reserva.");
            }
            // Una silla no puede repetirse dentro de la misma reserva
            if (existing.getSeat().equals(detail.getSeat())) {
                throw new IllegalArgumentException("La silla " + detail.getSeat() + " ya está asignada a otro pasajero en esta reserva.");
            }
        }
        details.add(detail);
    }


    //Quita un pasajero de la reserva asegurando que no quede vacía.
    public void removePassenger(String passengerDocument) {
        if (details.size() <= 1) {
            throw new IllegalArgumentException("No se puede eliminar el pasajero porque la reserva debe tener al menos un pasajero.");
        }

        boolean removed = details.removeIf(d -> d.getPassengerDocument().equalsIgnoreCase(passengerDocument));
        if (!removed) {
            throw new IllegalArgumentException("No se encontró ningún pasajero con el documento " + passengerDocument + " en esta reserva.");
        }
    }

    // equals y hashCode basados en el ID de la reserva
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Reservation that = (Reservation) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    // toString para depuración
    @Override
    public String toString() {
        return "Reserva #" + id + " [Cliente: " + customer.getLastName() + ", Pasajeros: " + details.size() + "]";
    }
}
