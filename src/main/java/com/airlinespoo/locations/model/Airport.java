package com.airlinespoo.locations.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;
import java.util.UUID;
/**
 * Representa un aeropuerto asociado a una ciudad dentro del sistema.
 */
public class Airport implements Identifiable<UUID> {
    private UUID id;
    private String name;
    private City city;
    /**
     * Crea un aeropuerto con un identificador, nombre y ciudad determinados.
     *
     * @param id identificador único del aeropuerto.
     * @param name nombre del aeropuerto.
     * @param city ciudad a la que pertenece el aeropuerto.
     * @throws ValidationException si alguno de los valores no cumple las validaciones.
     */
    public Airport(UUID id, String name, City city){
        this.id = Validator.requireNotNull(id, "id de aeropuerto");
        this.name = Validator.requireText(name, "nombre de aeropuerto", 50);
        this.city = Validator.requireNotNull(city, "ciudad");
    }
    /**
     * Crea un aeropuerto nuevo y genera automáticamente su identificador UUID.
     *
     * @param name nombre del aeropuerto.
     * @param city ciudad a la que pertenece el aeropuerto.
     * @throws ValidationException si el nombre o la ciudad no son válidos.
     */
    public Airport(String name, City city) {
        this.id = UUID.randomUUID();
        this.name = Validator.requireText(name, "nombre de aeropuerto", 50);
        this.city = Validator.requireNotNull(city, "ciudad");
    }
    /**
     * Actualiza el nombre del aeropuerto.
     *
     * @param name nuevo nombre del aeropuerto.
     * @throws ValidationException si el nombre no es válido.
     */
    public void setName(String name) {
        this.name = Validator.requireText(name, "nombre de aereopuerto", 50);
    }
    /**
     * Actualiza la ciudad asociada al aeropuerto.
     *
     * @param city nueva ciudad del aeropuerto.
     * @throws ValidationException si la ciudad es null.
     */
    public void setCity(City city) {
        this.city = Validator.requireNotNull(city, "ciudad");
    }
    /** @return nombre del aeropuerto. */
    public String getName() {
        return name;
    }
    /** @return ciudad asociada al aeropuerto. */
    public City getCity() {
        return city;
    }
    /** @return identificador único del aeropuerto. */
    @Override
    public UUID getId() {
        return id;
    }
    /**
     * Compara dos aeropuertos según su identificador.
     *
     * @param o objeto que se desea comparar.
     * @return true si ambos objetos representan el mismo aeropuerto.
     */
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Airport airport = (Airport) o;
        return Objects.equals(id, airport.id);
    }
    /** @return código hash basado en el identificador del aeropuerto. */
    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
    /** @return representación textual del aeropuerto. */
    @Override
    public String toString() {
        return "Airport{" +
            "name='" + name + '\'' +
            ", city=" + city +
            '}';
    }
}
