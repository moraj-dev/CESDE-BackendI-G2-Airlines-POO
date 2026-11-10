package com.airlinespoo.locations.ui;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;
import com.airlinespoo.locations.service.LocationService;

import java.sql.SQLException;
import java.util.List;

/**
 * Gestiona la interacción por consola del módulo de ubicaciones.
 * Permite registrar, consultar, actualizar y eliminar ciudades y aeropuertos
 * utilizando el servicio de ubicaciones conectado a PostgreSQL.
 */
public class LocationMenu implements Menu {

    private final LocationService locationService;
    private final ConsoleInput consoleInput;

    /**
     * Crea el menú de ubicaciones con las dependencias necesarias.
     * No inserta datos de ejemplo, ya que los registros persisten en PostgreSQL.
     *
     * @param locationService servicio de ciudades y aeropuertos.
     * @param consoleInput componente para leer entradas por consola.
     * @throws ValidationException si alguna dependencia es null.
     */
    public LocationMenu(LocationService locationService, ConsoleInput consoleInput) {
        this.locationService = Validator.requireNotNull(locationService, "servicio de ubicaciones");
        this.consoleInput = Validator.requireNotNull(consoleInput, "entrada de consola");
    }

    /**
     * Obtiene el título del menú de ubicaciones.
     *
     * @return título del menú.
     */
    @Override
    public String title() {
        return "Gestión de ubicaciones";
    }

    /**
     * Ejecuta las opciones del menú hasta que el usuario decide regresar.
     * Presenta los errores de negocio y de persistencia sin cerrar el menú.
     */
    @Override
    public void run() {
        try {
            System.out.println("\nAeropuertos registrados: " + locationService.countAirports());
        } catch (RuntimeException e) {
            handleError(e);
        }

        int option;
        do {
            System.out.println("\n=== GESTIÓN DE UBICACIONES ===");
            System.out.println("1. Registrar ciudad");
            System.out.println("2. Registrar aeropuerto");
            System.out.println("3. Listar ciudades");
            System.out.println("4. Listar aeropuertos por ciudad");
            System.out.println("5. Buscar aeropuerto");
            System.out.println("6. Renombrar ciudad");
            System.out.println("7. Renombrar aeropuerto");
            System.out.println("8. Eliminar ciudad");
            System.out.println("9. Eliminar aeropuerto");
            System.out.println("0. Volver");

            option = consoleInput.readInt("Seleccione una opción: ", 0, 9);

            try {
                switch (option) {
                    case 1 -> registerCity();
                    case 2 -> registerAirport();
                    case 3 -> listCities();
                    case 4 -> listAirportsByCity();
                    case 5 -> searchAirports();
                    case 6 -> renameCity();
                    case 7 -> updateAirport();
                    case 8 -> deleteCity();
                    case 9 -> deleteAirport();
                    case 0 -> System.out.println("Volviendo al menú principal...");
                }
            } catch (RuntimeException e) {
                handleError(e);
            }
        } while (option != 0);
    }

    /** Registra una ciudad y muestra su identificador generado en PostgreSQL. */
    private void registerCity() {
        System.out.println("\n=== REGISTRAR CIUDAD ===");
        String name = consoleInput.readRequiredText("Ingrese el nombre de la ciudad: ");
        City city = locationService.createCity(name);
        System.out.println("\n¡Ciudad registrada correctamente!");
        System.out.println("ID: " + city.getId());
        System.out.println("Nombre: " + city.getName());
    }

    /** Registra un aeropuerto asociado a una ciudad seleccionada. */
    private void registerAirport() {
        System.out.println("\n=== REGISTRAR AEROPUERTO ===");
        List<City> cities = locationService.listCities();
        if (cities.isEmpty()) {
            System.out.println("No hay ciudades registradas.");
            return;
        }

        City city = selectCity(cities, "Seleccione una ciudad:");
        String name = consoleInput.readRequiredText("Ingrese el nombre del aeropuerto: ");
        Airport airport = locationService.createAirport(name, city.getId());
        System.out.println("\n¡Aeropuerto registrado correctamente!");
        System.out.println("Nombre: " + airport.getName());
        System.out.println("Ciudad: " + airport.getCity().getName());
    }

    /** Lista las ciudades y muestra la cantidad total registrada. */
    private void listCities() {
        System.out.println("\n=== LISTA DE CIUDADES ===");
        List<City> cities = locationService.listCities();
        if (cities.isEmpty()) {
            System.out.println("No hay ciudades registradas.");
        } else {
            for (City city : cities) {
                System.out.println("ID: " + city.getId() + " | Nombre: " + city.getName());
            }
        }
        System.out.println("Total de ciudades registradas: " + locationService.countCities());
    }

    /** Lista los aeropuertos de una ciudad sin repetir su nombre en cada fila. */
    private void listAirportsByCity() {
        System.out.println("\n=== AEROPUERTOS POR CIUDAD ===");
        List<City> cities = locationService.listCities();
        if (cities.isEmpty()) {
            System.out.println("No hay ciudades registradas.");
            return;
        }

        City city = selectCity(cities, "Seleccione una ciudad:");
        List<Airport> airports = locationService.listAirportsByCity(city.getId());
        System.out.println("\nAeropuertos de " + city.getName() + ":");
        if (airports.isEmpty()) {
            System.out.println("No hay aeropuertos registrados.");
        } else {
            for (Airport airport : airports) {
                System.out.println("- " + airport.getName());
            }
        }
        System.out.println("Total de aeropuertos: " + locationService.countAirportsByCity(city.getId()));
    }

    /** Busca aeropuertos por coincidencia parcial de nombre. */
    private void searchAirports() {
        System.out.println("\n=== BUSCAR AEROPUERTO ===");
        String name = consoleInput.readRequiredText("Ingrese el nombre del aeropuerto o parte de él: ");
        List<Airport> airports = locationService.searchAirportsByName(name);
        if (airports.isEmpty()) {
            System.out.println("No se encontraron aeropuertos.");
            return;
        }

        System.out.println("\nResultados de búsqueda:");
        for (Airport airport : airports) {
            System.out.println("Aeropuerto: " + airport.getName()
                + " | Ciudad: " + airport.getCity().getName());
        }
    }

    /** Cambia el nombre de una ciudad existente. */
    private void renameCity() {
        System.out.println("\n=== RENOMBRAR CIUDAD ===");
        List<City> cities = locationService.listCities();
        if (cities.isEmpty()) {
            System.out.println("No hay ciudades registradas.");
            return;
        }

        City city = selectCity(cities, "Seleccione la ciudad:");
        String newName = consoleInput.readRequiredText("Ingrese el nuevo nombre: ");
        City updated = locationService.updateCity(city.getId(), newName);
        System.out.println("\n¡Ciudad renombrada correctamente!");
        System.out.println("Nuevo nombre: " + updated.getName());
    }

    /** Actualiza el nombre y la ciudad asociada a un aeropuerto. */
    private void updateAirport() {
        System.out.println("\n=== RENOMBRAR AEROPUERTO ===");
        List<Airport> airports = locationService.listAirports();
        if (airports.isEmpty()) {
            System.out.println("No hay aeropuertos registrados.");
            return;
        }

        Airport airport = selectAirport(airports, "Seleccione el aeropuerto:");
        String newName = consoleInput.readRequiredText("Ingrese el nuevo nombre: ");

        List<City> cities = locationService.listCities();
        if (cities.isEmpty()) {
            System.out.println("No hay ciudades registradas.");
            return;
        }
        City city = selectCity(cities, "Seleccione la nueva ciudad:");
        Airport updated = locationService.updateAirport(airport.getId(), newName, city.getId());
        System.out.println("\n¡Aeropuerto actualizado correctamente!");
        System.out.println("Nombre: " + updated.getName());
        System.out.println("Ciudad: " + updated.getCity().getName());
    }

    /** Elimina una ciudad después de solicitar confirmación. */
    private void deleteCity() {
        System.out.println("\n=== ELIMINAR CIUDAD ===");
        List<City> cities = locationService.listCities();
        if (cities.isEmpty()) {
            System.out.println("No hay ciudades registradas.");
            return;
        }

        City city = selectCity(cities, "Seleccione la ciudad que desea eliminar:");
        if (!consoleInput.confirm("¿Está seguro de eliminar " + city.getName() + "? ")) {
            System.out.println("Operación cancelada.");
            return;
        }

        boolean deleted = locationService.deleteCity(city.getId());
        System.out.println(deleted ? "Ciudad eliminada correctamente."
            : "No se eliminó la ciudad; es posible que ya no exista.");
    }

    /** Elimina un aeropuerto después de solicitar confirmación. */
    private void deleteAirport() {
        System.out.println("\n=== ELIMINAR AEROPUERTO ===");
        List<Airport> airports = locationService.listAirports();
        if (airports.isEmpty()) {
            System.out.println("No hay aeropuertos registrados.");
            return;
        }

        Airport airport = selectAirport(airports, "Seleccione el aeropuerto que desea eliminar:");
        if (!consoleInput.confirm("¿Está seguro de eliminar " + airport.getName() + "? ")) {
            System.out.println("Operación cancelada.");
            return;
        }

        boolean deleted = locationService.deleteAirport(airport.getId());
        System.out.println(deleted ? "Aeropuerto eliminado correctamente."
            : "No se eliminó el aeropuerto; es posible que ya no exista.");
    }

    /**
     * Muestra las ciudades disponibles y obtiene la seleccionada.
     *
     * @param cities lista no vacía de ciudades.
     * @param message mensaje que encabeza la selección.
     * @return ciudad seleccionada.
     */
    private City selectCity(List<City> cities, String message) {
        System.out.println("\n" + message);
        for (int i = 0; i < cities.size(); i++) {
            System.out.println((i + 1) + ". " + cities.get(i).getName());
        }
        int selection = consoleInput.readInt("Seleccione una opción: ", 1, cities.size());
        return cities.get(selection - 1);
    }

    /**
     * Muestra los aeropuertos disponibles y obtiene el seleccionado.
     *
     * @param airports lista no vacía de aeropuertos.
     * @param message mensaje que encabeza la selección.
     * @return aeropuerto seleccionado.
     */
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

    /**
     * Muestra errores esperados de negocio o de persistencia.
     * No oculta excepciones de programación ajenas a estas categorías.
     *
     * @param error excepción producida durante una operación del menú.
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
                if ("23503".equals(sqlError.getSQLState())) {
                    System.out.println("No se puede completar la operación porque existen registros relacionados en PostgreSQL.");
                } else if ("23505".equals(sqlError.getSQLState())) {
                    System.out.println("No se puede completar la operación porque ya existe un registro con esos datos únicos.");
                } else {
                    System.out.println("Error de base de datos: " + error.getMessage());
                }
                return;
            }
            cause = cause.getCause();
        }
        throw error;
    }
}
