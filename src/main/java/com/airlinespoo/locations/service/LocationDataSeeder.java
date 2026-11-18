package com.airlinespoo.locations.service;


import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.Airport;
import com.airlinespoo.locations.model.City;

import java.util.List;

public class LocationDataSeeder {
    private final LocationService locationService;


    public LocationDataSeeder(LocationService locationService) {
        this.locationService = Validator.requireNotNull(locationService, "servicio de ubicaciones.");
    }

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


        for (String cityName : cities) {
            if (findCity(cityName) == null) {
                locationService.createCity(cityName);
                citiesCreated++;
            }
        }

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

    private City findCity(String name) {
        List<City> cities = locationService.listCities();

        for (City city : cities) {
            if (city.getName().equalsIgnoreCase(name)) {
                return city;
            }
        }
        return null;
    }

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
