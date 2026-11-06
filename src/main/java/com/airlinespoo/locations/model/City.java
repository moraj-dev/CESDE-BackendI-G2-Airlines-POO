package com.airlinespoo.locations.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;
/**
 * Representa una ciudad dentro del sistema de gestión de aeropuertos.
 */
public class City implements Identifiable<Integer> {
    private int id;
    private String name;
    /**
     * Crea una ciudad con su identificador y nombre.
     *
     * @param id identificador único de la ciudad.
     * @param name nombre de la ciudad.
     * @throws IllegalArgumentException si alguno de los valores no cumple las validaciones.
     */
    public City(int id, String name){
        this.id = Math.toIntExact(Validator.requirePositive(id, "id de ciudad"));
        this.name = Validator.requireText(name, "nombre de la ciudad", 20);
    }
    /** @return nombre de la ciudad. */
    public String getName() {
        return name;
    }
    /**
     * Actualiza el nombre de la ciudad.
     *
     * @param name nuevo nombre de la ciudad.
     * @throws ValidationException si el nombre no cumple las condiciones de validación.
     */
    public void setName(String name){
        this.name = Validator.requireText(name, "nombre de la ciudad", 20);
    }
    /** @return identificador único de la ciudad. */
    @Override
    public Integer getId() {
        return id;
    }
    /**
     * Compara dos ciudades según su identificador.
     *
     * @param o objeto que se desea comparar.
     * @return true si ambos objetos representan la misma ciudad.
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof City city)) return false;
        return id == city.id;
    }
    /** @return código hash basado en el identificador de la ciudad. */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    /** @return representación textual de la ciudad. */
    @Override
    public String toString() {
        return "City{" +
            "name='" + name + '\'' +
            '}';
    }
}
