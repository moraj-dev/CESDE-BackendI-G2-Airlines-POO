package com.airlinespoo.app;

import com.airlinespoo.airlines.AirlineSampleData;
import com.airlinespoo.airlines.repository.*;
import com.airlinespoo.airlines.service.AirlineService;
import com.airlinespoo.airlines.ui.AirlineMenu;
import com.airlinespoo.core.ui.*;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.customers.CustomerSampleData;
import com.airlinespoo.customers.repository.*;
import com.airlinespoo.customers.service.*;
import com.airlinespoo.customers.ui.CustomerMenu;
import com.airlinespoo.locations.repository.*;
import com.airlinespoo.locations.service.*;
import com.airlinespoo.locations.ui.LocationMenu;
import com.airlinespoo.flights.repository.*;
import com.airlinespoo.flights.service.FlightService;
import com.airlinespoo.flights.service.FlightDataSeeder;
import com.airlinespoo.flights.ui.FlightMenu;
import com.airlinespoo.reservations.repository.ReservationRepository;
import com.airlinespoo.reservations.service.ReservationService;
import com.airlinespoo.reservations.service.ReservationDataSeeder;
import com.airlinespoo.reservations.ui.ReservationMenu;
import java.util.List;

/** Punto de entrada del sistema: monta servicios, datos de muestra y menús. */
public final class Main {
    private Main() { }

    public static void main(String[] args) {
        ConsoleInput input = new ConsoleInput();

        ICountryRepository countryRepository = new CountryRepository();
        IDocumentTypeRepository documentTypeRepository = new DocumentTypeRepository();
        ICustomerRepository customerRepository = new CustomerRepository();
        CountryService countryService = new CountryService(countryRepository, customerRepository, new IdGenerator(1, 1));
        DocumentTypeService documentService = new DocumentTypeService(documentTypeRepository, customerRepository);
        CustomerService customerService = new CustomerService(customerRepository, countryService, documentService);

        ICityRepository cityRepository = new CityRepository();
        IAirportRepository airportRepository = new AirportRepository();
        IFlightRepository flightRepository = new FlightRepository();
        LocationService locationService = new LocationService(cityRepository, airportRepository, flightRepository);

        AirlineRepository airlineRepository = new JdbcAirlineRepository();
        IdGenerator airlineIds = new IdGenerator(100, 10);
        AirlineService airlineService = new AirlineService(airlineRepository, airlineIds);
        FlightService flightService = new FlightService(
            flightRepository, locationService, airlineService);
        airlineService.setAirlineHasFlights(id -> flightService.airlineHasFlights(id));

        ReservationRepository reservationRepository = new ReservationRepository(customerService, flightService);
        ReservationService reservationService = new ReservationService(reservationRepository);

        // Cada carga se ejecuta por separado para que un error en un módulo
        // no impida inicializar los demás.
        loadSample("Clientes", () -> {
            countryService.syncIdGenerator();
            new CustomerSampleData(countryService, documentService, customerService).load();
        });
        loadSample("Ubicaciones", () -> new LocationDataSeeder(locationService).loadSampleData());
        loadSample("Aerolíneas", () -> new AirlineSampleData(airlineRepository, airlineIds).load());
        loadSample("Vuelos", () -> new FlightDataSeeder(flightService, flightRepository).loadSampleData());
        loadSample("Reservas", () -> new ReservationDataSeeder(
            reservationService, reservationRepository, customerService, flightService).loadSampleData());

        List<Menu> menus = List.of(
            new CustomerMenu(input, customerService, countryService, documentService),
            new LocationMenu(locationService, input),
            new AirlineMenu(airlineService, input),
            new FlightMenu(flightService, input),
            new ReservationMenu(reservationService, customerService, flightService, input)
        );
        new MainMenu(input, menus).run();
    }

    private static void loadSample(String module, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException error) {
            System.out.println("Aviso: no se pudieron cargar ejemplos de " + module + ": " + error.getMessage());
        }
    }
}
