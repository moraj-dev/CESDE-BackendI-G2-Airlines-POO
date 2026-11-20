package com.airlinespoo.customers;

import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.service.CountryService;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.customers.service.DocumentTypeService;

/**
 * Carga datos de ejemplo del módulo de clientes (5 países, 4 tipos de documento y
 * 3 clientes) a través de los servicios, solo cuando las tablas están vacías.
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
        Country colombia = null;
        Country mexico = null;

        if (countryService.listCountries().isEmpty()) {
            colombia = countryService.createCountry("Colombia");
            mexico = countryService.createCountry("México");
            countryService.createCountry("Argentina");
            countryService.createCountry("Chile");
            countryService.createCountry("Perú");
        }

        if (documentTypeService.listDocumentTypes().isEmpty()) {
            documentTypeService.createDocumentType("CC", "Cédula ciudadanía");
            documentTypeService.createDocumentType("TI", "Tarjeta de identidad");
            documentTypeService.createDocumentType("PA", "Pasaporte");
            documentTypeService.createDocumentType("CE", "Cédula extranjería");
        }

        if (customerService.countCustomers() == 0 && colombia != null && mexico != null) {
            customerService.createCustomer("1001", "Laura", "Pérez", "CC",
                "3001234567", "laura.perez@mail.com", colombia.getId(), "Calle 10 # 5-20");
            customerService.createCustomer("1002", "Carlos", "Gómez", "CC",
                "3109876543", "carlos.gomez@mail.com", colombia.getId(), null);
            customerService.createCustomer("P5003", "Ana", "Ramírez", "PA",
                "+525512345678", null, mexico.getId(), "Av. Reforma 100");
        }
    }
}
