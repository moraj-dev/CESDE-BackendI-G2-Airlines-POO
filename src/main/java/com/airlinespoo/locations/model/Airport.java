package com.airlinespoo.locations.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;
import java.util.UUID;

public class Airport implements Identifiable<UUID> {
    private UUID id;
    private String name;
    private City city;

    public Airport(UUID id, String name, City city){
        this.id = Validator.requireNotNull(id, "id de aeropuerto");
        this.name = Validator.requireText(name, "nombre de aeropuerto", 50);
        this.city = Validator.requireNotNull(city, "ciudad");
    }

    public Airport(String name, City city) {
        this.id = UUID.randomUUID();
        this.name = Validator.requireText(name, "nombre de aeropuerto", 50);
        this.city = Validator.requireNotNull(city, "ciudad");
    }

    public void setName(String name) {
        this.name = Validator.requireText(name, "nombre de aereopuerto", 50);
    }

    public void setCity(City city) {
        this.city = Validator.requireNotNull(city, "ciudad");
    }

    public String getName() {
        return name;
    }

    public City getCity() {
        return city;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Airport airport = (Airport) o;
        return Objects.equals(id, airport.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "Airport{" +
            "name='" + name + '\'' +
            ", city=" + city +
            '}';
    }
}
