package com.airlinespoo.airlines.model;

import com.airlinespoo.core.util.Validator;

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
}
