package com.airlinespoo.customers.ui;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.service.CountryService;

import java.util.List;

/**
 * Menú de consola de países: registrar, listar, cambiar nombre y eliminar.
 */
public class CountryMenu implements Menu {

    private static final int OPTION_REGISTER = 1;
    private static final int OPTION_LIST = 2;
    private static final int OPTION_UPDATE = 3;
    private static final int OPTION_DELETE = 4;
    private static final int OPTION_BACK = 5;
    private static final int MAX_COUNTRY_ID = 32767; // la columna CountryId es SMALLINT

    private final ConsoleInput input;
    private final CountryService countryService;

    /**
     * Crea el menú con el lector de consola y el servicio de países.
     *
     * @param input          lector de consola compartido
     * @param countryService servicio de países
     */
    public CountryMenu(ConsoleInput input, CountryService countryService) {
        this.input = input;
        this.countryService = countryService;
    }

    @Override
    public String title() {
        return "Países";
    }

    /** Muestra el menú y atiende al usuario hasta que elige volver. */
    @Override
    public void run() {
        boolean back = false;
        while (!back) {
            printOptions();
            int option = input.readInt("Elija una opción", OPTION_REGISTER, OPTION_BACK);

            if (option == OPTION_BACK) {
                back = true;
            } else {
                execute(option);
                input.pause();
            }
        }
    }

    private void printOptions() {
        System.out.println();
        System.out.println("------ PAÍSES ------");
        System.out.println(OPTION_REGISTER + ". Registrar país");
        System.out.println(OPTION_LIST + ". Listar países");
        System.out.println(OPTION_UPDATE + ". Cambiar nombre de un país");
        System.out.println(OPTION_DELETE + ". Eliminar país");
        System.out.println(OPTION_BACK + ". Volver");
        System.out.println("--------------------");
    }

    private void execute(int option) {
        try {
            switch (option) {
                case OPTION_REGISTER:
                    registerCountry();
                    break;
                case OPTION_LIST:
                    listCountries();
                    break;
                case OPTION_UPDATE:
                    updateCountry();
                    break;
                case OPTION_DELETE:
                    deleteCountry();
                    break;
                default:
                    break;
            }
        } catch (AirlinesException e) {
            System.out.println("Error: " + e.getMessage());
            if (e.getCause() != null) {
                System.out.println("Causa: " + e.getCause().getMessage());
            }
        }
    }

    private void registerCountry() {
        System.out.println("---- Registrar país ----");
        int id = input.readInt("Id del país", 1, MAX_COUNTRY_ID);
        String name = input.readRequiredText("Nombre del país");
        Country country = countryService.createCountry(id, name);
        System.out.println("País registrado: " + country);
    }

    private void listCountries() {
        System.out.println("---- Lista de países ----");
        List<Country> countries = countryService.listCountries();
        if (countries.isEmpty()) {
            System.out.println("No hay países registrados.");
            return;
        }
        for (Country country : countries) {
            System.out.println(country.getId() + " - " + country.getName());
        }
    }

    private void updateCountry() {
        System.out.println("---- Cambiar nombre ----");
        int id = input.readInt("Id del país", 1, MAX_COUNTRY_ID);
        String name = input.readRequiredText("Nuevo nombre");
        Country country = countryService.updateCountry(id, name);
        System.out.println("País actualizado: " + country);
    }

    private void deleteCountry() {
        System.out.println("---- Eliminar país ----");
        int id = input.readInt("Id del país", 1, MAX_COUNTRY_ID);
        Country country = countryService.getCountry(id);
        if (input.confirm("¿Seguro que quieres eliminar " + country.getName() + "?")) {
            countryService.deleteCountry(id);
            System.out.println("País eliminado.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }
}
