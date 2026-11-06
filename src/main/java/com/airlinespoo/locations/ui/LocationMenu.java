package com.airlinespoo.locations.ui;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.service.LocationService;
import com.airlinespoo.locations.model.City;
import com.airlinespoo.core.exception.DuplicateException;

import java.util.List;
/**
 * Gestiona la interacción por consola del módulo de ubicaciones,
 * permitiendo realizar operaciones sobre ciudades y aeropuertos.
 */
public class LocationMenu implements Menu {

    private final LocationService locationService;
    private final ConsoleInput consoleInput;
    /**
     * Crea el menú de ubicaciones con las dependencias necesarias.
     *
     * @param locationService servicio encargado de las operaciones de ciudades y aeropuertos.
     * @param consoleInput componente encargado de gestionar la entrada por consola.
     */
    public LocationMenu(LocationService locationService, ConsoleInput consoleInput) {
        this.locationService = locationService;
        this.consoleInput = consoleInput;

        loadSampleData();
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
     * Ejecuta el menú de ubicaciones y gestiona las operaciones disponibles
     * hasta que el usuario decide regresar al menú anterior.
     */
    @Override
    public void run() {
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

            switch (option) {
                case 1 -> {
                    System.out.println("\n=== REGISTRAR CIUDAD ===");

                    String name = consoleInput.readRequiredText(
                        "Ingrese el nombre de la ciudad"
                    );

                    try {
                        City city = locationService.createCity(name);

                        System.out.println("\n¡Ciudad registrada correctamente!");
                        System.out.println("ID: " + city.getId());
                        System.out.println("Nombre: " + city.getName());

                    } catch (DuplicateException | IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case 2 -> {
                    System.out.println("\n=== REGISTRAR AEROPUERTO ===");

                    List<City> cities = locationService.listCities();

                    if (cities.isEmpty()) {
                        System.out.println("No hay ciudades registradas.");
                        break;
                    }

                    System.out.println("\nSeleccione una ciudad:");

                    for (int i = 0; i < cities.size(); i++) {
                        System.out.println((i + 1) + ". " + cities.get(i).getName());
                    }

                    int selection = consoleInput.readInt(
                        "Seleccione una opción: ", 1, cities.size()
                    );

                    City selectedCity = cities.get(selection - 1);

                    String name = consoleInput.readRequiredText(
                        "Ingrese el nombre del aeropuerto"
                    );

                    try {
                        Airport airport = locationService.createAirport(
                            name, selectedCity.getId()
                        );

                        System.out.println("\n¡Aeropuerto registrado correctamente!");
                        System.out.println("Nombre: " + airport.getName());
                        System.out.println("Ciudad: " + airport.getCity().getName());

                    } catch (DuplicateException | IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case 3 -> {
                    System.out.println("\n=== LISTA DE CIUDADES ===");

                    List<City> cities = locationService.listCities();

                    if (cities.isEmpty()) {
                        System.out.println("No hay ciudades registradas.");
                        break;
                    }

                    for (City city : cities) {
                        System.out.println(
                            "ID: " + city.getId()
                                + " | Nombre: " + city.getName()
                        );
                    }
                }
                case 4 -> {
                    System.out.println("\n=== AEROPUERTOS POR CIUDAD ===");

                    List<City> cities = locationService.listCities();

                    if (cities.isEmpty()) {
                        System.out.println("No hay ciudades registradas.");
                        break;
                    }

                    System.out.println("\nSeleccione una ciudad:");

                    for (int i = 0; i < cities.size(); i++) {
                        System.out.println((i + 1) + ". " + cities.get(i).getName());
                    }

                    int selection = consoleInput.readInt(
                        "Seleccione una opción: ", 1, cities.size()
                    );

                    City selectedCity = cities.get(selection - 1);

                    List<Airport> airports =
                        locationService.listAirportsByCity(selectedCity.getId());

                    if (airports.isEmpty()) {
                        System.out.println(
                            "No hay aeropuertos registrados en " + selectedCity.getName()
                        );
                        break;
                    }

                    System.out.println("\nAeropuertos de " + selectedCity.getName() + ":");

                    for (Airport airport : airports) {
                        System.out.println("- " + airport.getName());
                    }
                }
                case 5 -> {
                    System.out.println("\n=== BUSCAR AEROPUERTO ===");

                    String name = consoleInput.readRequiredText(
                        "Ingrese el nombre del aeropuerto o parte de él: "
                    );

                    try {
                        List<Airport> airports =
                            locationService.searchAirportsByName(name);

                        if (airports.isEmpty()) {
                            System.out.println("No se encontraron aeropuertos.");
                            break;
                        }

                        System.out.println("\nResultados de búsqueda:");

                        for (Airport airport : airports) {
                            System.out.println(
                                "Aeropuerto: " + airport.getName()
                                    + " | Ciudad: " + airport.getCity().getName()
                            );
                        }

                    } catch (IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case 6 -> {
                    System.out.println("\n=== RENOMBRAR CIUDAD ===");

                    List<City> cities = locationService.listCities();

                    if (cities.isEmpty()) {
                        System.out.println("No hay ciudades registradas.");
                        break;
                    }

                    for (int i = 0; i < cities.size(); i++) {
                        System.out.println(
                            (i + 1) + ". " + cities.get(i).getName()
                        );
                    }

                    int selection = consoleInput.readInt(
                        "Seleccione la ciudad: ", 1, cities.size()
                    );

                    City selectedCity = cities.get(selection - 1);

                    String newName = consoleInput.readRequiredText(
                        "Ingrese el nuevo nombre: "
                    );

                    try {
                        City updatedCity = locationService.updateCity(
                            selectedCity.getId(), newName
                        );

                        System.out.println("\n¡Ciudad renombrada correctamente!");
                        System.out.println("Nuevo nombre: " + updatedCity.getName());

                    } catch (DuplicateException | IllegalArgumentException | NotFoundException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case 7 -> {
                    System.out.println("\n=== RENOMBRAR AEROPUERTO ===");

                    List<Airport> airports = locationService.listAirports();

                    if (airports.isEmpty()) {
                        System.out.println("No hay aeropuertos registrados.");
                        break;
                    }

                    for (int i = 0; i < airports.size(); i++) {
                        Airport airport = airports.get(i);

                        System.out.println(
                            (i + 1) + ". " + airport.getName()
                                + " - " + airport.getCity().getName()
                        );
                    }

                    int selection = consoleInput.readInt(
                        "Seleccione el aeropuerto: ", 1, airports.size()
                    );

                    Airport selectedAirport = airports.get(selection - 1);

                    String newName = consoleInput.readRequiredText(
                        "Ingrese el nuevo nombre"
                    );

                    List<City> cities = locationService.listCities();

                    System.out.println("\nSeleccione la nueva ciudad:");

                    for (int i = 0; i < cities.size(); i++) {
                        System.out.println(
                            (i + 1) + ". " + cities.get(i).getName()
                        );
                    }

                    int citySelection = consoleInput.readInt(
                        "Seleccione la ciudad: ", 1, cities.size()
                    );

                    City selectedCity = cities.get(citySelection - 1);

                    try {
                        Airport updatedAirport = locationService.updateAirport(
                            selectedAirport.getId(),
                            newName,
                            selectedCity.getId()
                        );

                        System.out.println("\n¡Aeropuerto actualizado correctamente!");
                        System.out.println("Nombre: " + updatedAirport.getName());
                        System.out.println("Ciudad: " + updatedAirport.getCity().getName());

                    } catch (DuplicateException | IllegalArgumentException | NotFoundException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case 8 -> {
                    System.out.println("\n=== ELIMINAR CIUDAD ===");

                    List<City> cities = locationService.listCities();

                    if (cities.isEmpty()) {
                        System.out.println("No hay ciudades registradas.");
                        break;
                    }

                    for (int i = 0; i < cities.size(); i++) {
                        System.out.println(
                            (i + 1) + ". " + cities.get(i).getName()
                        );
                    }

                    int selection = consoleInput.readInt(
                        "Seleccione la ciudad que desea eliminar: ",
                        1, cities.size()
                    );

                    City selectedCity = cities.get(selection - 1);

                    boolean confirm = consoleInput.confirm(
                        "¿Está seguro de eliminar " + selectedCity.getName() + "? "
                    );

                    if (!confirm) {
                        System.out.println("Operación cancelada.");
                        break;
                    }

                    try {
                        locationService.deleteCity(selectedCity.getId());
                        System.out.println("Ciudad eliminada correctamente.");

                    } catch (BusinessRuleException | NotFoundException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case 9 -> {
                    System.out.println("\n=== ELIMINAR AEROPUERTO ===");

                    List<Airport> airports = locationService.listAirports();

                    if (airports.isEmpty()) {
                        System.out.println("No hay aeropuertos registrados.");
                        break;
                    }

                    for (int i = 0; i < airports.size(); i++) {
                        Airport airport = airports.get(i);

                        System.out.println(
                            (i + 1) + ". " + airport.getName()
                                + " - " + airport.getCity().getName()
                        );
                    }

                    int selection = consoleInput.readInt(
                        "Seleccione el aeropuerto que desea eliminar: ",
                        1, airports.size()
                    );

                    Airport selectedAirport = airports.get(selection - 1);

                    boolean confirm = consoleInput.confirm(
                        "¿Está seguro de eliminar " + selectedAirport.getName() + "? "
                    );

                    if (!confirm) {
                        System.out.println("Operación cancelada.");
                        break;
                    }

                    try {
                        locationService.deleteAirport(selectedAirport.getId());

                        System.out.println("Aeropuerto eliminado correctamente.");

                    } catch (NotFoundException | IllegalArgumentException e) {
                        System.out.println("Error: " + e.getMessage());
                    }
                }
                case 0 -> System.out.println("Volviendo al menú principal...");
            }

        } while (option != 0);
    }
    /**
     * Carga ciudades y aeropuertos de ejemplo para las pruebas del módulo.
     */
    private void loadSampleData() {
        if (!locationService.listCities().isEmpty()) {
            return;
        }

        City bogota = locationService.createCity("Bogotá");
        City medellin = locationService.createCity("Medellín");
        City cartagena = locationService.createCity("Cartagena");
        City santaMarta = locationService.createCity("Santa Marta");
        City leticia = locationService.createCity("Leticia");

        locationService.createAirport("El Dorado", bogota.getId());
        locationService.createAirport("Guaymaral", bogota.getId());

        locationService.createAirport("Olaya Herrera", medellin.getId());

        locationService.createAirport("Rafael Núñez", cartagena.getId());

        locationService.createAirport("Simón Bolívar", santaMarta.getId());

        locationService.createAirport("Alfredo Vásquez Cobo", leticia.getId());
    }
}
