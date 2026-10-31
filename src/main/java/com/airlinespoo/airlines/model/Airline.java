package com.airlinespoo.airlines.model;

import com.airlinespoo.core.util.Validator;

import java.util.Objects;

public class Airline {

    private static final int MAX_NAME_LENGTH = 50;
    private final int id;
    private String name;

    private String validateName(String name) {
        return Validator.requireText(name, "nombre de la aerolínea", MAX_NAME_LENGTH);
    }

    public Airline(int id, String name) {
        this.id = id;
        this.name = validateName(name);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = validateName(name);
    }

    @Override
    public String toString() {
        return "Id: " + id + ", Name: " + name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Airline airline = (Airline) o;
        return id == airline.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
