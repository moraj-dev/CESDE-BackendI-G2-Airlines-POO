package com.airlinespoo.customers;

import com.airlinespoo.customers.service.CountryService;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.customers.service.DocumentTypeService;

/**
 * Carga datos de ejemplo del módulo de clientes: 5 países, 4 tipos de documento
 * y 3 clientes, para poder probar sin escribir todo cada vez.
 */
public class CustomerSampleData {

    private final CountryService countryService;
    private final DocumentTypeService documentTypeService;
    private final CustomerService customerService;

    /**
     * Crea el cargador con los servicios que necesita.
     *
     * @param countryService      servicio de países
     * @param documentTypeService servicio de tipos de documento
     * @param customerService     servicio de clientes
     */
    public CustomerSampleData(CountryService countryService,
                              DocumentTypeService documentTypeService,
                              CustomerService customerService) {
        this.countryService = countryService;
        this.documentTypeService = documentTypeService;
        this.customerService = customerService;
    }

    /**
     * Carga los catálogos y luego los clientes. El orden importa: createCustomer
     * verifica que el país y el tipo de documento ya existan.
     */
    public void load() {
        loadCountries();
        loadDocumentTypes();
        loadCustomers();
    }

    private void loadCountries() {
        countryService.createCountry(1, "Colombia");
        countryService.createCountry(2, "México");
        countryService.createCountry(3, "Argentina");
        countryService.createCountry(4, "Chile");
        countryService.createCountry(5, "Perú");
    }

    private void loadDocumentTypes() {
        documentTypeService.createDocumentType("CC", "Cédula ciudadanía");
        documentTypeService.createDocumentType("TI", "Tarjeta de identidad");
        documentTypeService.createDocumentType("PA", "Pasaporte");
        documentTypeService.createDocumentType("CE", "Cédula extranjería");
    }

    private void loadCustomers() {
        customerService.createCustomer("1001", "Laura", "Pérez", "CC",
            "3001234567", "laura.perez@mail.com", 1, "Calle 10 # 5-20");
        customerService.createCustomer("1002", "Carlos", "Gómez", "CC",
            "3109876543", "carlos.gomez@mail.com", 1, null);
        customerService.createCustomer("P5003", "Ana", "Ramírez", "PA",
            "+525512345678", null, 2, "Av. Reforma 100");
    }
}
