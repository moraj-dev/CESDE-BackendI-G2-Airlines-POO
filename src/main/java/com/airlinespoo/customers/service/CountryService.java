package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

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

    public Country createCountry(int id, String name) {
        Country country = new Country(id, name);
        return countryRepository.create(country);
    }

    public Country getCountry(int id) {
        Optional<Country> found = countryRepository.findById(id);
        if (found.isEmpty()) {
            throw new NotFoundException("país", id);
        }
        return found.get();
    }

    public List<Country> listCountries() {
        List<Country> paises = countryRepository.findAll();
        paises.sort(Comparator.comparing(Country::getName));
        return paises;
    }

    public Country updateCountry(int id, String name) {
        Country country = getCountry(id);
        country.setName(name);
        return countryRepository.update(country);
    }

    public void deleteCountry(int id) {
        Country country = getCountry(id);
        if (customerRepository.existsByCountry(country)) {
            throw new BusinessRuleException("No se puede eliminar el país porque tiene clientes.");
        }
        countryRepository.deleteById(id);
    }

}
