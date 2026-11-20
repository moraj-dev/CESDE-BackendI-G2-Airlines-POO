package com.airlinespoo.flights.ui;

import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.flights.repository.FlightRepository;
import com.airlinespoo.flights.repository.IFlightRepository;
import com.airlinespoo.flights.repository.JdbcAirlineLookup;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.locations.repository.AirportRepository;
import com.airlinespoo.locations.repository.CityRepository;
import com.airlinespoo.locations.service.LocationService;

/**
 * Punto de entrada temporal para probar el módulo de vuelos desde IntelliJ IDEA.
 *
 * <p>Construye las dependencias JDBC y reutiliza el servicio de ubicaciones.
 * Las aerolíneas se consultan desde PostgreSQL mediante un adaptador temporal,
 * sin depender del repositorio en memoria del módulo Airlines.</p>
 *
 * <p>Esta clase es de prueba: se debe retirar cuando el menú principal de
 * Airlines-POO integre definitivamente {@link FlightMenu}.</p>
 */
public final class FlightTestMain {

    /** Impide crear instancias de esta clase de arranque. */
    private FlightTestMain() {
    }

    /**
     * Inicia el menú de vuelos con dependencias conectadas a PostgreSQL.
     *
     * @param args argumentos de consola (no se utilizan)
     */
    public static void main(String[] args) {
        CityRepository cityRepository = new CityRepository();
        AirportRepository airportRepository = new AirportRepository();

        IFlightRepository flightRepository = new FlightRepository();
        LocationService locationService = new LocationService(cityRepository, airportRepository, flightRepository);
        JdbcAirlineLookup airlineLookup = new JdbcAirlineLookup();
        FlightService flightService = new FlightService(flightRepository, locationService, airlineLookup);

        ConsoleInput consoleInput = new ConsoleInput();
        FlightMenu flightMenu = new FlightMenu(flightService, consoleInput);
        flightMenu.run();
    }
}
