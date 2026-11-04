package com.airlinespoo.airlines.ui;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import java.util.List;

public class AirlineMenu implements Menu {

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
            option = input.readInt("Elige una opción", 0, 3);
            try {
                switch (option) {
                    case 1 -> listAirlines();
                    case 2 -> registerAirline();
                    case 3 -> showAirline();
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
        int id = input.readInt("Id de la aerolínea", 1, Integer.MAX_VALUE);
        Airline airline = airlineService.getAirline(id);
        System.out.println(airline);
    }

}
