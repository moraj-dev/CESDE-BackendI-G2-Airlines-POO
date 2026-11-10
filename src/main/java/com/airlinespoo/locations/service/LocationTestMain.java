
package com.airlinespoo.locations.ui;

import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.flights.repository.FlightRepository;
import com.airlinespoo.flights.repository.IFlightRepository;
import com.airlinespoo.locations.repository.CityRepository;
import com.airlinespoo.locations.repository.AirportRepository;
import com.airlinespoo.locations.service.LocationService;

public class LocationTestMain {

    public static void main(String[] args) {

        // Crear los repositorios JDBC
        CityRepository cityRepository = new CityRepository();
        AirportRepository airportRepository = new AirportRepository();

        // Crear el servicio con las dependencias
        IFlightRepository flightRepository = new FlightRepository();
        LocationService locationService = new LocationService(cityRepository, airportRepository, flightRepository);
        // Crear la entrada de consola
        ConsoleInput consoleInput = new ConsoleInput();

        // Crear y ejecutar el menú
        LocationMenu locationMenu = new LocationMenu(
            locationService,
            consoleInput
        );

        locationMenu.run();
    }
}
