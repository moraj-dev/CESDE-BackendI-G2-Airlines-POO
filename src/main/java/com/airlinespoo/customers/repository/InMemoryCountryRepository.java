package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.customers.model.Country;

public class InMemoryCountryRepository extends InMemoryRepository<Country, Integer>
    implements CountryRepository {

    @Override
    protected String entityName() {
        return "país";
    }
}
