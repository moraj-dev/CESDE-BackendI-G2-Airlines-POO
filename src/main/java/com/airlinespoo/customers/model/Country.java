package com.airlinespoo.customers.model;

import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.core.exception.ValidationException;

import java.util.Objects;

/**
 * Representa un país del catálogo de clientes.
 */
public class Country implements Identifiable<Integer> {

    public static final int MAX_NAME_LENGTH = 20; // declaramos en rango como constante, se usa como lo pide la guia(seccion 5)

    private final int id; // atributo privado y no cambiente(final)
    private String name;

    /**
     * Crea un país válido.
     *
     * @param id   identificador positivo del país; no cambia después
     * @param name nombre del país (obligatorio, máximo 20 caracteres)
     * @throws ValidationException si algún dato no cumple las reglas
     */
    public Country(int id, String name) {
        this.id = (int) Validator.requirePositive(id, "id del país"); // llamamos al metodo de validacion de validator
        this.name = validateName(name); // llamamos al metodo de validacion de validator
    }

    @Override
    public Integer getId() {
        return id;
    }

    /**
     * @return el nombre del país
     */
    public String getName() {
        return name;
    }

    /**
     * Cambia el nombre del país.
     *
     * @param name nuevo nombre (obligatorio, máximo 20 caracteres)
     * @throws ValidationException si el nombre no es válido
     */
    public void setName(String name) {
        this.name = validateName(name);
    }

    private static String validateName(String name) {
        return Validator.requireText(name, "nombre del país", MAX_NAME_LENGTH);
    }

    /** Dos países son iguales si tienen el mismo id. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Country other)) return false;
        return id == other.id;
    }

    /** Código hash basado solo en el id. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** @return texto legible con el id y el nombre */
    @Override
    public String toString() {
        return "Country{id=" + id + ", name='" + name + "'}";
    }
}
