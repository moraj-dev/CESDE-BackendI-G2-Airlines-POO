package com.airlinespoo.airlines.ui;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;

import java.util.List;

public class AirlineMenu implements Menu {

    private static final String ID_PROMPT = "Id de la aerolínea";
    private static final int MIN_ID = 1;
    private static final int MAX_ID = Integer.MAX_VALUE;

    private final AirlineService airlineService;
    private final ConsoleInput input;

    public AirlineMenu(AirlineService airlineService, ConsoleInput input) {
        this.airlineService = airlineService;
        this.input = input;
    }

    @Override
    public String title() {
        return "Aerolíneas";
    }

    @Override
    public void run() {
        int option;
        do {
            printOptions();
            option = input.readInt("Elige una opción", 0, 6);
            try {
                switch (option) {
                    case 1 -> listAirlines();
                    case 2 -> registerAirline();
                    case 3 -> showAirline();
                    case 4 -> renameAirline();
                    case 5 -> searchAirlines();
                    case 6 -> deleteAirline();
                    case 0 -> System.out.println("Volver al menú principal");
                }
            } catch (AirlinesException e) {
                System.out.println("Error: " + e.getMessage());
            }
        } while (option != 0);
    }

    private void printOptions() {
        System.out.println("\n--- Aerolíneas ---");
        System.out.println("1. Listar aerolíneas");
        System.out.println("2. Registrar aerolínea");
        System.out.println("3. Consultar por id");
        System.out.println("4. Modificar nombre");
        System.out.println("5. Buscar aerolínea");
        System.out.println("6. Eliminar aerolínea");
        System.out.println("0. Volver al menú");
    }

    private void listAirlines() {
        List<Airline> airlines = airlineService.listAirlines();
        if (airlines.isEmpty()) {
            System.out.println("No hay aerolíneas registradas.");
            return;
        }
        for (Airline airline : airlines) {
            System.out.println(airline);
        }
    }

    private void registerAirline() {
        String name = input.readRequiredText("Nombre de la aerolínea");
        Airline created = airlineService.createAirline(name);
        System.out.println("Aerolínea registrada: " + created);
    }

    private void showAirline() {
        int id = input.readInt(ID_PROMPT, MIN_ID, MAX_ID);
        Airline airline = airlineService.getAirline(id);
        System.out.println(airline);
    }

    private void renameAirline() {
        int id = input.readInt(ID_PROMPT, MIN_ID, MAX_ID);
        String newName = input.readRequiredText("Nuevo nombre");
        Airline updated = airlineService.renameAirline(id, newName);
        System.out.println("Aerolínea actualizada: " + updated);
    }

    private void searchAirlines() {
        String text = input.readRequiredText("Aerolínea a buscar");
        List<Airline> found = airlineService.searchAirlinesByName(text);
        if (found.isEmpty()) {
            System.out.println("No se encontraron aerolíneas.");
            return;
        }
        for (Airline airline : found) {
            System.out.println(airline);
        }
    }

    private void deleteAirline() {
        int id = input.readInt(ID_PROMPT, MIN_ID, MAX_ID);
        Airline airline = airlineService.getAirline(id);
        if (input.confirm("¿Eliminar " + airline.getName() + "?")) {
            airlineService.deleteAirline(id);
            System.out.println("Aerolínea eliminada: " + airline.getName());
        } else {
            System.out.println("Operación cancelada.");
        }
    }
}
