package com.airlinespoo.airlines.model;

import com.airlinespoo.core.util.Validator;

public class Airline {

    private static final int MAX_NAME_LENGTH = 50;
    private final int id;
    private String name;

    public Airline(int id, String name) {
        this.id = id;
        this.name = Validator.requireText(name, "nombre de la aerolínea", MAX_NAME_LENGTH);
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
