package com.airlinespoo.customers;

import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.customers.repository.DocumentTypeRepository;
import com.airlinespoo.customers.repository.ICountryRepository;
import com.airlinespoo.customers.repository.ICustomerRepository;
import com.airlinespoo.customers.repository.IDocumentTypeRepository;
import com.airlinespoo.customers.service.CountryService;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.customers.service.DocumentTypeService;
import com.airlinespoo.customers.ui.CountryMenu;
import com.airlinespoo.customers.ui.CustomerMenu;
import com.airlinespoo.customers.ui.DocumentTypeMenu;

/** Clase provisional para probar el módulo de clientes con la base de datos. No se sube. */
public class CustomerDemo {

    public static void main(String[] args) {
        ConsoleInput input = new ConsoleInput();

        // Repositorios (JDBC)
        ICountryRepository countryRepository = new CountryRepository();
        IDocumentTypeRepository documentTypeRepository = new DocumentTypeRepository();
        ICustomerRepository customerRepository = new CustomerRepository();

        // Servicios
        CountryService countryService = new CountryService(countryRepository, customerRepository);
        DocumentTypeService documentTypeService =
            new DocumentTypeService(documentTypeRepository, customerRepository);
        CustomerService customerService =
            new CustomerService(customerRepository, countryService, documentTypeService);

        // Menús
        Menu countryMenu = new CountryMenu(input, countryService);
        Menu documentTypeMenu = new DocumentTypeMenu(input, documentTypeService);
        Menu customerMenu = new CustomerMenu(input, customerService, countryService,
            documentTypeService, countryMenu, documentTypeMenu);

        customerMenu.run();
    }
}
