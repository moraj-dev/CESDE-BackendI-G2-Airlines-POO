package com.airlinespoo.locations.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.locations.model.City;


import java.util.List;
import java.util.stream.Collectors;

public class CityRepository extends InMemoryRepository<City, Integer> {

    @Override
    protected String entityName() {
        return "ciudad";
    }

    public List<City> searchCitiesByName(String name) {
        String validName = Validator.requireText(name, "nombre de ciudad");
        return values().stream()
            .filter(city -> city.getName().toLowerCase().contains(validName.toLowerCase()))
            .collect(Collectors.toList());
    }
}
