package com.airlinespoo.reservations.model;

public class ReservationDetail {

    private final int id;
    private final String passengerFullName;
    private final String passengerDocument;
    private final Seat seat;

    //constructor - validaciones
    public ReservationDetail(int id, String passengerFullName, String passengerDocument, Seat seat) {

        if (id <= 0) {
            throw new IllegalArgumentException("El ID del detalle de reserva debe ser un valor positivo.");
        }

        if (passengerFullName == null || passengerFullName.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre completo del pasajero no puede ser nulo o vacio.");
        }
        String normalizedName = passengerFullName.trim();
        if (normalizedName.length() > 50) {
            throw new IllegalArgumentException("El nombre completo del pasajero no puede superar los 50 caracteres.");
        }

        if (passengerDocument == null || passengerDocument.trim().isEmpty()) {
            throw new IllegalArgumentException("El documento del pasajero no puede ser nulo o vacío.");
        }
        String normalizedDoc = passengerDocument.trim();
        if (normalizedDoc.length() > 20) {
            throw new IllegalArgumentException("El documento del pasajero no puede superar los 20 caracteres.");
        }

        if (seat == null) {
            throw new IllegalArgumentException("La silla asignada al pasajero no puede ser nula.");
        }

        // asignacion valores
        this.id = id;
        this.passengerFullName = normalizedName;
        this.passengerDocument = normalizedDoc;
        this.seat = seat;
    }

    // getters sin setters
    public int getId() {
        return id;
    }

    public String getPassengerFullName() {
        return passengerFullName;
    }

    public String getPassengerDocument() {
        return passengerDocument;
    }

    public Seat getSeat() {
        return seat;
    }

    //equals y hashCode basados en el ID del detalle
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReservationDetail that = (ReservationDetail) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(id);
    }

    // toString - visualisacion datos del pasajero
    @Override
    public String toString() {
        return "Pasajero: " + passengerFullName +
            " (Doc: " + passengerDocument +
            ") - Silla: " + seat;
    }
}
