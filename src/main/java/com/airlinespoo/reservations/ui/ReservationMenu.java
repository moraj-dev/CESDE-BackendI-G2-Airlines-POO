package com.airlinespoo.reservations.ui;

import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.reservations.model.Reservation;
import com.airlinespoo.reservations.model.ReservationDetail;
import com.airlinespoo.reservations.model.Seat;
import com.airlinespoo.reservations.service.ReservationService;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class ReservationMenu {

    private final ReservationService reservationService;
    private final Scanner scanner;

    public ReservationMenu(ReservationService reservationService) {
        this.reservationService = reservationService;
        this.scanner = new Scanner(System.in);
    }

    public void showMenu() {
        int option = 0;
        do {
            System.out.println("\n=== MENÚ GESTIÓN DE RESERVAS (MÓDULO 5) ===");
            System.out.println("1. Crear nueva reserva");
            System.out.println("2. Buscar reservas por ID de cliente");
            System.out.println("3. Buscar reservas por ID de vuelo");
            System.out.println("4. Listar sillas ocupadas de un vuelo");
            System.out.println("5. Cancelar reserva");
            System.out.println("6. Salir");
            System.out.print("Seleccione una opción: ");

            try {
                option = Integer.parseInt(scanner.nextLine());

                switch (option) {
                    case 1:
                        handleCreateReservation();
                        break;
                    case 2:
                        handleSearchByCustomer();
                        break;
                    case 3:
                        handleSearchByFlight();
                        break;
                    case 4:
                        handleListOccupiedSeats();
                        break;
                    case 5:
                        handleCancelReservation();
                        break;
                    case 6:
                        System.out.println("Saliendo del módulo de reservas...");
                        break;
                    default:
                        System.out.println("Opción inválida. Intente de nuevo.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Por favor, ingrese un número válido.");
            } catch (Exception e) {
                System.out.println("Error de negocio: " + e.getMessage());
            }

        } while (option != 6);
    }

    private void handleCreateReservation() {
        System.out.println("\n--- CREAR NUEVA RESERVA ---");
        System.out.print("Ingrese ID de la reserva (número): ");
        int reservationId = Integer.parseInt(scanner.nextLine());

        // Simulación rápida de cliente y vuelo (luego se integran con sus módulos reales)
        Customer customer = new Customer();
        Flight flight = new Flight();

        List<ReservationDetail> details = new ArrayList<>();
        char agregarMas = 's';

        while (agregarMas == 's' && details.size() < 9) {
            System.out.print("ID del detalle: ");
            int detailId = Integer.parseInt(scanner.nextLine());
            System.out.print("Nombre completo del pasajero: ");
            String name = scanner.nextLine();
            System.out.print("Documento del pasajero: ");
            String doc = scanner.nextLine();
            System.out.print("Fila de la silla (Ej: A, B, C): ");
            String row = scanner.nextLine();
            System.out.print("Número de la silla (Ej: 1, 2): ");
            int number = Integer.parseInt(scanner.nextLine());

            Seat seat = new Seat(row, number);
            ReservationDetail detail = new ReservationDetail(detailId, name, doc, seat);
            details.add(detail);

            if (details.size() < 9) {
                System.out.print("¿Desea agregar otro pasajero a esta reserva? (s/n): ");
                agregarMas = scanner.nextLine().trim().toLowerCase().charAt(0);
            }
        }

        Reservation reservation = new Reservation(reservationId, customer, flight, details);
        reservationService.createReservation(reservation);
        System.out.println("¡Reserva creada y guardada con exito!");
    }

    private void handleSearchByCustomer() {
        System.out.print("\nIngrese el documento o ID del cliente: ");
        String customerId = scanner.nextLine();
        List<Reservation> reservations = reservationService.getReservationsByCustomer(customerId);

        if (reservations.isEmpty()) {
            System.out.println("No se encontraron reservas para este cliente.");
        } else {
            reservations.forEach(System.out::println);
        }
    }

    private void handleSearchByFlight() {
        System.out.print("\nIngrese el ID del vuelo: ");
        int flightId = Integer.parseInt(scanner.nextLine());
        List<Reservation> reservations = reservationService.getReservationsByFlight(flightId);

        if (reservations.isEmpty()) {
            System.out.println("No se encontraron reservas para este vuelo.");
        } else {
            reservations.forEach(System.out::println);
        }
    }

    private void handleListOccupiedSeats() {
        System.out.print("\nIngrese el ID del vuelo para ver sillas ocupadas: ");
        int flightId = Integer.parseInt(scanner.nextLine());
        var occupiedSeats = reservationService.getOccupiedSeatsForFlight(flightId);

        if (occupiedSeats.isEmpty()) {
            System.out.println("No hay sillas ocupadas en este vuelo.");
        } else {
            System.out.println("Sillas ocupadas: " + occupiedSeats);
        }
    }

    private void handleCancelReservation() {
        System.out.print("\nIngrese el ID de la reserva a cancelar: ");
        int id = Integer.parseInt(scanner.nextLine());
        reservationService.cancelReservation(id);
        System.out.println("Reserva cancelada exitosamente.");
    }
}


