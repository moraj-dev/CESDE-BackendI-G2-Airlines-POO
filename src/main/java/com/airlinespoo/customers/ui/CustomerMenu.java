package com.airlinespoo.customers.ui;

import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.customers.service.CountryService;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.customers.service.DocumentTypeService;

public class CountryConsole implements Menu {

    private final ConsoleInput input;
    private final CustomerService customerService;
    private final CountryService countryService;
    private final DocumentTypeService documentTypeService;

    public CountryConsole(ConsoleInput input, CustomerService customerService, CountryService countryService, DocumentTypeService documentTypeService) {
        this.input = input;
        this.customerService = customerService;
        this.countryService = countryService;
        this.documentTypeService = documentTypeService;
    }

    @Override
    public String title() {
        return "Clientes";
    }

    @Override
    public void run() {

    }
}
