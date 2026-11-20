package com.airlinespoo.reservations.ui;

import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.reservations.model.*;
import com.airlinespoo.reservations.service.ReservationService;
import java.util.*;

/** Menú de reservas con datos reales obtenidos de Customers y Flights. */
public class ReservationMenu implements Menu {
    private final ReservationService reservations;
    private final CustomerService customers;
    private final FlightService flights;
    private final ConsoleInput input;

    public ReservationMenu(ReservationService reservations, CustomerService customers,
                           FlightService flights, ConsoleInput input) {
        this.reservations = Objects.requireNonNull(reservations);
        this.customers = Objects.requireNonNull(customers);
        this.flights = Objects.requireNonNull(flights);
        this.input = Objects.requireNonNull(input);
    }

    @Override public String title() { return "Reservas"; }

    @Override public void run() {
        int option;
        do {
            System.out.println("\n=== RESERVAS ===");
            System.out.println("1. Crear reserva");
            System.out.println("2. Buscar por cliente");
            System.out.println("3. Buscar por vuelo");
            System.out.println("4. Consultar sillas ocupadas");
            System.out.println("5. Cancelar reserva");
            System.out.println("6. Agregar pasajero a una reserva");
            System.out.println("0. Volver");
            option = input.readInt("Opción", 0, 6);
            try {
                switch (option) {
                    case 1 -> create();
                    case 2 -> print(reservations.getReservationsByCustomer(input.readRequiredText("Documento cliente")));
                    case 3 -> print(reservations.getReservationsByFlight(input.readInt("ID del vuelo", 1, Integer.MAX_VALUE)));
                    case 4 -> System.out.println(reservations.getOccupiedSeatsForFlight(input.readInt("ID del vuelo", 1, Integer.MAX_VALUE)));
                    case 5 -> System.out.println(reservations.cancelReservation(input.readInt("ID de reserva", 1, Integer.MAX_VALUE)) ? "Reserva cancelada" : "No se canceló");
                    case 6 -> addPassenger();
                }
            } catch (RuntimeException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        } while (option != 0);
    }

    private void create() {
        int id = input.readInt("ID de reserva", 1, Integer.MAX_VALUE);
        Customer customer = customers.getCustomer(input.readRequiredText("Documento del cliente existente"));
        Flight flight = flights.getFlight(input.readInt("ID del vuelo existente", 1, Integer.MAX_VALUE));
        List<ReservationDetail> passengers = new ArrayList<>();
        do {
            passengers.add(readPassenger());
        } while (passengers.size() < 9 && input.confirm("¿Agregar otro pasajero?"));
        reservations.createReservation(new Reservation(id, customer, flight, passengers));
        System.out.println("Reserva guardada correctamente.");
    }

    private void addPassenger() {
        int reservationId = input.readInt("ID de reserva existente", 1, Integer.MAX_VALUE);
        reservations.addPassengerToReservation(reservationId, readPassenger());
        System.out.println("Pasajero agregado correctamente.");
    }

    private ReservationDetail readPassenger() {
        int detailId = input.readInt("ID del detalle", 1, Integer.MAX_VALUE);
        String name = input.readRequiredText("Nombre completo");
        String document = input.readRequiredText("Documento pasajero");
        String row = input.readRequiredText("Fila (ej. A)");
        int seatNumber = input.readInt("Número de silla", 1, 99);
        return new ReservationDetail(detailId, name, document, new Seat(row, seatNumber));
    }

    private void print(List<Reservation> found) {
        if (found.isEmpty()) System.out.println("No hay reservas para el criterio seleccionado.");
        else for (Reservation reservation : found) System.out.println(reservation);
    }
}
