
package com.airlinespoo.locations.repository;

import com.airlinespoo.locations.model.City;

import java.util.List;
import java.util.Optional;

public interface ICityRepository {

    City create(City city);

    Optional<City> findById(Integer id);

    List<City> findAll();

    City update(City city);

    boolean deleteById(Integer id);

    boolean existsById(Integer id);

    long count();

    List<City> searchCitiesByName(String name);
}

