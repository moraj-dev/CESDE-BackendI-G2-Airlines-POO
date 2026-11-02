package com.airlinespoo.customers.service;

import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;

/**
 * Reglas de negocio de los países.
 */
public class CountryService {

    private final CountryRepository countryRepository;
    private final CustomerRepository customerRepository;

    public CountryService(CountryRepository countryRepository,
                          CustomerRepository customerRepository) {
        this.countryRepository = countryRepository;
        this.customerRepository = customerRepository;
    }


}
