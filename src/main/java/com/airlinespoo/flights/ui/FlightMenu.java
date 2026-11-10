package com.airlinespoo.flights.ui;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.flights.model.Flight;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.locations.model.Airport;

import java.sql.SQLException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

/**
 * Menú de consola para la gestión de vuelos.
 * <p>La interfaz solo lee datos y presenta resultados; las reglas de negocio
 * corresponden a {@link FlightService}. No carga registros de ejemplo, pues
 * la persistencia se realiza en PostgreSQL.</p>
 */
public class FlightMenu implements Menu {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final FlightService flightService;
    private final ConsoleInput consoleInput;

    /**
     * Construye el menú con sus dependencias.
     *
     * @param flightService servicio de vuelos
     * @param consoleInput lector de consola compartido con los otros menús
     */
    public FlightMenu(FlightService flightService, ConsoleInput consoleInput) {
        this.flightService = Validator.requireNotNull(flightService, "servicio de vuelos");
        this.consoleInput = Validator.requireNotNull(consoleInput, "entrada de consola");
    }

    /** {@inheritDoc} */
    @Override
    public String title() {
        return "Gestión de vuelos";
    }

    /**
     * Muestra las opciones hasta que el usuario decide volver.
     * Captura excepciones de cada operación sin cerrar el menú.
     */
    @Override
    public void run() {
        int option;
        do {
            System.out.println("\n=== GESTIÓN DE VUELOS ===");
            System.out.println("1. Programar vuelo");
            System.out.println("2. Consultar vuelo por código");
            System.out.println("3. Listar todos los vuelos");
            System.out.println("4. Reprogramar / actualizar vuelo");
            System.out.println("5. Cancelar vuelo");
            System.out.println("6. Listar vuelos por aerolínea");
            System.out.println("7. Listar vuelos por rango de fechas");
            System.out.println("8. Reporte mensual por aerolínea");
            System.out.println("9. Listar vuelos por ruta");
            System.out.println("0. Volver");

            option = consoleInput.readInt("Seleccione una opción: ", 0, 9);
            try {
                switch (option) {
                    case 1 -> createFlight();
                    case 2 -> getFlightByName();
                    case 3 -> listFlights();
                    case 4 -> updateFlight();
                    case 5 -> deleteFlight();
                    case 6 -> listFlightsByAirline();
                    case 7 -> listFlightsBetween();
                    case 8 -> monthlyReport();
                    case 9 -> listFlightsByRoute();
                    case 0 -> System.out.println("Volviendo al menú principal...");
                    default -> throw new IllegalStateException("Opción de menú inesperada: " + option);
                }
            } catch (RuntimeException error) {
                handleError(error);
            }
        } while (option != 0);
    }

    /** Programa un vuelo utilizando aerolíneas y aeropuertos previamente registrados. */
    private void createFlight() {
        System.out.println("\n=== PROGRAMAR VUELO ===");
        List<Airline> airlines = flightService.listAirlines();
        List<Airport> airports = flightService.listAirports();
        if (!hasRequiredCatalogs(airlines, airports)) return;

        String code = consoleInput.readRequiredText("Código del vuelo (máximo 8, A-Z y 0-9): ");
        Airline airline = selectAirline(airlines, "Seleccione la aerolínea:");
        Airport departure = selectAirport(airports, "Seleccione el aeropuerto de salida:");
        Airport arrival = selectAirport(airports, "Seleccione el aeropuerto de llegada:");
        LocalDate departureDate = readDate("Fecha de salida (AAAA-MM-DD): ");
        LocalTime departureTime = readTime("Hora de salida (HH:mm): ");
        LocalDate arrivalDate = readDate("Fecha de llegada (AAAA-MM-DD): ");
        LocalTime arrivalTime = readTime("Hora de llegada (HH:mm): ");

        Flight created = flightService.createFlight(code, departure.getId(), arrival.getId(),
            departureDate, departureTime, arrivalDate, arrivalTime, airline.getId());
        System.out.println("\nVuelo registrado correctamente. ID: " + created.getId());
        printFlight(created);
    }

    /** Consulta un vuelo por su código único. */
    private void getFlightByName() {
        System.out.println("\n=== CONSULTAR VUELO POR CÓDIGO ===");
        String code = consoleInput.readRequiredText("Código del vuelo: ");
        printFlight(flightService.getFlightByName(code));
    }

    /** Lista todos los vuelos ordenados por fecha y hora de salida. */
    private void listFlights() {
        System.out.println("\n=== TODOS LOS VUELOS ===");
        printFlights(flightService.listFlights());
        System.out.println("Total de vuelos: " + flightService.countFlights());
    }

    /** Actualiza un vuelo identificado por su ID, permitiendo conservar el mismo código. */
    private void updateFlight() {
        System.out.println("\n=== REPROGRAMAR / ACTUALIZAR VUELO ===");
        List<Flight> flights = flightService.listFlights();
        if (flights.isEmpty()) {
            System.out.println("No hay vuelos registrados.");
            return;
        }
        Flight current = selectFlight(flights, "Seleccione el vuelo a actualizar:");
        List<Airline> airlines = flightService.listAirlines();
        List<Airport> airports = flightService.listAirports();
        if (!hasRequiredCatalogs(airlines, airports)) return;

        System.out.println("Vuelo actual: " + current.getFlightName());
        String code = consoleInput.readRequiredText("Nuevo código (puede ser el actual): ");
        Airline airline = selectAirline(airlines, "Seleccione la aerolínea:");
        Airport departure = selectAirport(airports, "Seleccione el aeropuerto de salida:");
        Airport arrival = selectAirport(airports, "Seleccione el aeropuerto de llegada:");
        LocalDate departureDate = readDate("Fecha de salida (AAAA-MM-DD): ");
        LocalTime departureTime = readTime("Hora de salida (HH:mm): ");
        LocalDate arrivalDate = readDate("Fecha de llegada (AAAA-MM-DD): ");
        LocalTime arrivalTime = readTime("Hora de llegada (HH:mm): ");

        Flight updated = flightService.updateFlight(current.getId(), code,
            departure.getId(), arrival.getId(), departureDate, departureTime,
            arrivalDate, arrivalTime, airline.getId());
        System.out.println("\nVuelo actualizado correctamente.");
        printFlight(updated);
    }

    /** Cancela un vuelo tras solicitar confirmación. */
    private void deleteFlight() {
        System.out.println("\n=== CANCELAR VUELO ===");
        List<Flight> flights = flightService.listFlights();
        if (flights.isEmpty()) {
            System.out.println("No hay vuelos registrados.");
            return;
        }
        Flight selected = selectFlight(flights, "Seleccione el vuelo a cancelar:");
        if (!consoleInput.confirm("¿Cancelar el vuelo " + selected.getFlightName() + "? ")) {
            System.out.println("Operación cancelada.");
            return;
        }
        boolean deleted = flightService.deleteFlight(selected.getId());
        System.out.println(deleted ? "Vuelo cancelado correctamente."
            : "No se eliminó el vuelo; es posible que ya no exista.");
    }

    /** Lista vuelos de una aerolínea, ordenados cronológicamente. */
    private void listFlightsByAirline() {
        System.out.println("\n=== VUELOS POR AEROLÍNEA ===");
        List<Airline> airlines = flightService.listAirlines();
        if (airlines.isEmpty()) {
            System.out.println("No hay aerolíneas registradas.");
            return;
        }
        Airline selected = selectAirline(airlines, "Seleccione la aerolínea:");
        printFlights(flightService.listFlightsByAirline(selected.getId()));
        System.out.println("Total: " + flightService.countFlightsByAirline(selected.getId()));
    }

    /** Busca vuelos cuyas fechas de salida estén dentro del rango inclusivo indicado. */
    private void listFlightsBetween() {
        System.out.println("\n=== VUELOS POR RANGO DE FECHAS ===");
        LocalDate start = readDate("Fecha inicial (AAAA-MM-DD): ");
        LocalDate end = readDate("Fecha final (AAAA-MM-DD): ");
        printFlights(flightService.listFlightsBetween(start, end));
    }

    /** Presenta vuelos agrupados por aerolínea en el mes elegido. */
    private void monthlyReport() {
        System.out.println("\n=== REPORTE MENSUAL DE VUELOS ===");
        int year = consoleInput.readInt("Año (1900-2100): ", 1900, 2100);
        int month = consoleInput.readInt("Mes (1-12): ", 1, 12);
        Map<Airline, List<Flight>> grouped = flightService.listFlightsByAirlineInMonth(year, month);
        if (grouped.isEmpty()) {
            System.out.println("Sin vuelos en ese período.");
            return;
        }
        int total = 0;
        for (Map.Entry<Airline, List<Flight>> entry : grouped.entrySet()) {
            System.out.println("\nAerolínea: " + entry.getKey().getName()
                + " (ID: " + entry.getKey().getId() + ")");
            printFlights(entry.getValue());
            System.out.println("Vuelos de esta aerolínea: " + entry.getValue().size());
            total += entry.getValue().size();
        }
        System.out.println("\nTotal de vuelos del período: " + total);
    }

    /** Lista vuelos para una ruta compuesta por dos aeropuertos distintos. */
    private void listFlightsByRoute() {
        System.out.println("\n=== VUELOS POR RUTA ===");
        List<Airport> airports = flightService.listAirports();
        if (airports.size() < 2) {
            System.out.println("Se necesitan al menos dos aeropuertos registrados.");
            return;
        }
        Airport departure = selectAirport(airports, "Aeropuerto de salida:");
        Airport arrival = selectAirport(airports, "Aeropuerto de llegada:");
        printFlights(flightService.listFlightsByRoute(departure.getId(), arrival.getId()));
    }

    /** Comprueba que haya catálogos suficientes para programar o modificar vuelos. */
    private boolean hasRequiredCatalogs(List<Airline> airlines, List<Airport> airports) {
        if (airlines.isEmpty()) {
            System.out.println("No hay aerolíneas registradas en PostgreSQL.");
            return false;
        }
        if (airports.size() < 2) {
            System.out.println("Se necesitan al menos dos aeropuertos registrados.");
            return false;
        }
        return true;
    }

    /** Selecciona una aerolínea por número visible, sin confundir el índice con el ID. */
    private Airline selectAirline(List<Airline> airlines, String message) {
        System.out.println("\n" + message);
        for (int i = 0; i < airlines.size(); i++) {
            Airline airline = airlines.get(i);
            System.out.println((i + 1) + ". " + airline.getName() + " (ID: " + airline.getId() + ")");
        }
        int selection = consoleInput.readInt("Seleccione una opción: ", 1, airlines.size());
        return airlines.get(selection - 1);
    }

    /** Selecciona un aeropuerto mostrando también su ciudad. */
    private Airport selectAirport(List<Airport> airports, String message) {
        System.out.println("\n" + message);
        for (int i = 0; i < airports.size(); i++) {
            Airport airport = airports.get(i);
            System.out.println((i + 1) + ". " + airport.getName()
                + " - " + airport.getCity().getName());
        }
        int selection = consoleInput.readInt("Seleccione una opción: ", 1, airports.size());
        return airports.get(selection - 1);
    }

    /** Selecciona un vuelo por posición, conservando su ID real de PostgreSQL. */
    private Flight selectFlight(List<Flight> flights, String message) {
        System.out.println("\n" + message);
        for (int i = 0; i < flights.size(); i++) {
            Flight flight = flights.get(i);
            System.out.println((i + 1) + ". " + flight.getFlightName()
                + " | ID: " + flight.getId() + " | salida: " + flight.getDepartureDateTime());
        }
        int selection = consoleInput.readInt("Seleccione una opción: ", 1, flights.size());
        return flights.get(selection - 1);
    }

    /** Lee una fecha ISO, repreguntando cuando el formato o la fecha sean inválidos. */
    private LocalDate readDate(String prompt) {
        while (true) {
            String value = consoleInput.readRequiredText(prompt);
            try {
                return LocalDate.parse(value, DATE_FORMAT);
            } catch (DateTimeParseException error) {
                System.out.println("Fecha inválida. Utilice AAAA-MM-DD (ejemplo: 2026-11-15).");
            }
        }
    }

    /** Lee una hora en formato de 24 horas, repreguntando si es inválida. */
    private LocalTime readTime(String prompt) {
        while (true) {
            String value = consoleInput.readRequiredText(prompt);
            try {
                return LocalTime.parse(value, TIME_FORMAT);
            } catch (DateTimeParseException error) {
                System.out.println("Hora inválida. Utilice HH:mm (ejemplo: 23:30).");
            }
        }
    }

    /** Presenta una lista o un mensaje en caso de ausencia de resultados. */
    private void printFlights(List<Flight> flights) {
        if (flights.isEmpty()) {
            System.out.println("No se encontraron vuelos.");
            return;
        }
        for (Flight flight : flights) {
            printFlight(flight);
        }
    }

    /** Presenta los datos de un vuelo y su duración en horas y minutos. */
    private void printFlight(Flight flight) {
        Duration duration = flight.getDuration();
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        System.out.println("\nID: " + flight.getId() + " | Código: " + flight.getFlightName());
        System.out.println("Aerolínea: " + flight.getAirline().getName());
        System.out.println("Salida: " + flight.getDeparture().getName() + " ("
            + flight.getDeparture().getCity().getName() + ") | "
            + flight.getDepartureDate().format(DATE_FORMAT) + " "
            + flight.getDepartureTime().format(TIME_FORMAT));
        System.out.println("Llegada: " + flight.getArrival().getName() + " ("
            + flight.getArrival().getCity().getName() + ") | "
            + flight.getArrivalDate().format(DATE_FORMAT) + " "
            + flight.getArrivalTime().format(TIME_FORMAT));
        System.out.println("Duración: " + hours + " h " + minutes + " min");
    }

    /**
     * Muestra errores de negocio y restricciones de PostgreSQL sin ocultar
     * excepciones inesperadas de programación.
     */
    private void handleError(RuntimeException error) {
        if (error instanceof ValidationException
            || error instanceof DuplicateException
            || error instanceof NotFoundException
            || error instanceof BusinessRuleException) {
            System.out.println("Error: " + error.getMessage());
            return;
        }

        Throwable cause = error;
        while (cause != null) {
            if (cause instanceof SQLException sqlError) {
                switch (sqlError.getSQLState()) {
                    case "23503" -> System.out.println(
                        "No se puede completar la operación: hay registros relacionados en PostgreSQL.");
                    case "23505" -> System.out.println(
                        "No se puede completar la operación: el código del vuelo ya existe.");
                    case "23502" -> System.out.println(
                        "No se puede completar la operación: falta un dato obligatorio.");
                    default -> System.out.println("Error de PostgreSQL: " + sqlError.getMessage());
                }
                return;
            }
            cause = cause.getCause();
        }
        throw error;
    }
}
