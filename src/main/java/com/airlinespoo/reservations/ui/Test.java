/**package com.airlinespoo.reservations.ui;

import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.model.Seat;

import java.util.ArrayList;
import java.util.List;

public class Test {
    public static void main(String[] args) {
        System.out.println("=== INICIANDO PRUEBAS DEL MÓDULO DE RESERVAS ===");

        try {
            // 1. Creamos objetos simulados
            Customer customer = new Customer();
            Flight flight = new Flight();

            // 2. Creamos sillas adaptadas al constructor de Seat (String, int)
            Seat seat1 = new Seat("A", 1);
            Seat seat2 = new Seat("B", 1);

            // 3. Creamos detalles de pasajeros
            ReservationDetail detail1 = new ReservationDetail(1, "Carlos Pérez", "12345678", seat1);
            ReservationDetail detail2 = new ReservationDetail(2, "Ana Gómez", "87654321", seat2);

            List<ReservationDetail> initialPassengers = new ArrayList<>();
            initialPassengers.add(detail1);
            initialPassengers.add(detail2);

            // 4. Intentamos crear la reserva
            Reservation reservation = new Reservation(1, customer, flight, initialPassengers);
            System.out.println("✅ ¡Reserva creada exitosamente!");
            System.out.println(reservation);

            // 5. Probando regla de negocio: Intentar agregar un pasajero con silla repetida
            System.out.println("\n--- Probando regla: Silla repetida ---");
            Seat duplicateSeat = new Seat("A", 1); // Silla A1 ya está ocupada por Carlos
            ReservationDetail detailDuplicate = new ReservationDetail(3, "Pedro Picapiedra", "11112222", duplicateSeat);

            reservation.addPassenger(detailDuplicate);

        } catch (IllegalArgumentException e) {
            System.out.println("EXCEPCIÓN ESPERADA (La regla de negocio funcionó): " + e.getMessage());
        }

        System.out.println("=== FIN DE LAS PRUEBAS ===");
    }
}**/

