package com.airlinespoo.airlines.repository;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.core.repository.InMemoryRepository;

import java.util.Optional;

/**
 * Repositorio en memoria para la gestión y persistencia temporal de entidades {@link Airline}.
 */
public class AirlineRepository extends InMemoryRepository<Airline, Integer> {

    /**
     * {@inheritDoc}
     */
    @Override
    protected String entityName() {
        return "aerolínea";
    }

    /**
     * Busca una aerolínea coincidiendo exactamente con su nombre (ignorando mayúsculas/minúsculas).
     *
     * @param name nombre de la aerolínea a buscar
     * @return un {@link Optional} con la aerolínea encontrada o vacío si no existe
     */
    public Optional<Airline> findByName(String name) {
        for (Airline airline : this.values()) {
            if (airline.getName().equalsIgnoreCase(name)) {
                return Optional.of(airline);
            }
        }
        return Optional.empty();
    }

    /**
     * Verifica la existencia de una aerolínea dado su nombre.
     *
     * @param name nombre a verificar
     * @return {@code true} si ya existe, {@code false} en caso contrario
     */
    public boolean existsByName(String name) {
        return findByName(name).isPresent();
    }
}
