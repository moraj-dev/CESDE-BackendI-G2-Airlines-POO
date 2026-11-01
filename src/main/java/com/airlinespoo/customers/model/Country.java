package com.airlinespoo.customers.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;

public class Country implements Identifiable<Integer> {

    public static final int MAX_NAME_LENGTH = 20; // declaramos en rango como constante, se usa como lo pide la guia(seccion 5)

    private final int id; // atributo privado y no cambiente(final)
    private String name;

    public Country(int id, String name) {
        this.id = (int) Validator.requirePositive(id, "id del país"); // llamamos al metodo de validacion de validator
        this.name = validateName(name); // llamamos al metodo de validacion de validator
    }

    @Override
    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = validateName(name);
    }

    private static String validateName(String name) {
        return Validator.requireText(name, "nombre del país", MAX_NAME_LENGTH);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Country other)) return false;
        return id == other.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Country{id=" + id + ", name='" + name + "'}";
    }
}
