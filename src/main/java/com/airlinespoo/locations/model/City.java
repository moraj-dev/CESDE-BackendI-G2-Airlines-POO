package com.airlinespoo.locations.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;

public class City implements Identifiable<Integer> {
    private int id;
    private String name;

    public City(int id, String name){
        this.id = Math.toIntExact(Validator.requirePositive(id, "id de ciudad"));
        this.name = Validator.requireText(name, "nombre de la ciudad", 20);
    }

    public String getName() {
        return name;
    }

    public void setName(String name){
        this.name = Validator.requireText(name, "nombre de la ciudad", 20);
    }

    @Override
    public Integer getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof City city)) return false;
        return id == city.id;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public String toString() {
        return "City{" +
            "name='" + name + '\'' +
            '}';
    }
}
