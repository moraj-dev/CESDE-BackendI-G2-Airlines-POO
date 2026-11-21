package com.airlinespoo.airlines.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;

/**
 * Representa una aerolínea en el sistema, implementando la interfaz {@link Identifiable}.
 */
public class Airline implements Identifiable<Integer> {

    private static final int MAX_NAME_LENGTH = 50;
    private final int id;
    private String name;

    /**
     * Construye una nueva instancia de {@code Airline}.
     *
     * @param id   identificador único de la aerolínea
     * @param name nombre de la aerolínea
     */
    public Airline(int id, String name) {
        this.id = (int) Validator.requirePositive(id, "id de la aerolínea");
        this.name = validateName(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Integer getId() {
        return id;
    }

    /**
     * Obtiene el nombre de la aerolínea.
     *
     * @return nombre actual
     */
    public String getName() {
        return name;
    }

    /**
     * Establece o modifica el nombre de la aerolínea previa validación.
     *
     * @param name nuevo nombre a asignar
     */
    public void setName(String name) {
        this.name = validateName(name);
    }

    /**
     * Valida que el nombre cumpla con los requisitos establecidos.
     *
     * @param name texto del nombre
     * @return nombre validado y depurado
     */
    private String validateName(String name) {
        return Validator.requireText(name, "nombre de la aerolínea", MAX_NAME_LENGTH);
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
