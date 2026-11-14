package com.airlinespoo.customers.repository;

import com.airlinespoo.customers.model.Country;

import java.util.List;
import java.util.Optional;

public interface ICountryRepository {
    void create(Country country);
    Optional<Country> findById(Integer id);
    List<Country> findAll();
    void update(Country country);
    void deleteById(Integer id);
    boolean existsById(Integer id);
    long count();
}
