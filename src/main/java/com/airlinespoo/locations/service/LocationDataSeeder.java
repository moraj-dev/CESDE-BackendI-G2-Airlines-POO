package com.airlinespoo.locations.service;


import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.util.List;

/**
 * Carga datos de ejemplo de ciudades y aeropuertos en PostgreSQL.
 * Evita insertar registros que ya existen en la base de datos.
 */
public class LocationDataSeeder {
    private final LocationService locationService;

    /**
     * Inicializa el cargador con el servicio de ubicaciones.
     *
     * @param locationService servicio de ciudades y aeropuertos
     */
    public LocationDataSeeder(LocationService locationService) {
        this.locationService = Validator.requireNotNull(locationService, "servicio de ubicaciones.");
    }

    /**
     * Carga cinco ciudades y seis aeropuertos de ejemplo.
     * Si un registro ya existe, conserva el existente.
     */
    public void loadSampleData() {
        int citiesCreated = 0;
        int airportsCreated = 0;

        String[] cities = {
            "Bogotá",
            "Medellín",
            "Cartagena",
            "Santa Marta",
            "Leticia"
        };

//      Carga de las ciudades
        for (String cityName : cities) {
            if (findCity(cityName) == null) {
                locationService.createCity(cityName);
                citiesCreated++;
            }
        }

//      Carga de los areopuertos
        airportsCreated += loadAirport("El Dorado", "Bogotá");
        airportsCreated += loadAirport("Guaymaral", "Bogotá");
        airportsCreated += loadAirport("Olaya Herrera", "Medellín");
        airportsCreated += loadAirport("Rafael Nuñez", "Cartagena");
        airportsCreated += loadAirport("Simón Bolívar", "Santa Marta");
        airportsCreated += loadAirport("Alfredo Vásquez Cobo", "Leticia");

        System.out.println("Carga de Locations finalizada");
        System.out.println("Ciudades creadas: " + citiesCreated);
        System.out.println("Aeropuertos creados: " + airportsCreated);
    }

    /**
     * Busca una ciudad por su nombre exacto, sin distinguir
     * mayúsculas y minúsculas.
     *
     * @param name nombre de la ciudad
     * @return ciudad encontrada o null si no existe
     */
    private City findCity(String name) {
        List<City> cities = locationService.listCities();

        for (City city : cities) {
            if (city.getName().equalsIgnoreCase(name)) {
                return city;
            }
        }
        return null;
    }

    /**
     * Crea un aeropuerto si todavía no existe en la ciudad indicada.
     *
     * @param airportName nombre del aeropuerto
     * @param cityName nombre de la ciudad
     * @return 1 si se creó el aeropuerto, 0 si ya existía
     */
    private int loadAirport(String airportName, String cityName) {
        City city = findCity(cityName);

        if (city == null) {
            throw new IllegalStateException("No se encontró la ciuad" + cityName);
        }

        List<Airport> airports = locationService.listAirportsByCity(city.getId());

        for (Airport airport : airports) {
            if (airport.getName().equalsIgnoreCase(airportName)) {
                return 0;
            }
        }

        locationService.createAirport(airportName, city.getId());
        return 1;
    }
}
