package com.airlinespoo.airlines;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.repository.InMemoryAirlineRepository;
import com.airlinespoo.core.util.IdGenerator;

/**
 * Carga datos iniciales de prueba para el módulo de aerolíneas.
 */
public class AirlineSampleData {
    private final InMemoryAirlineRepository inMemoryAirlineRepository;
    private final IdGenerator idGenerator;

    /**
     * Construye la clase cargadora de datos de muestra.
     *
     * @param inMemoryAirlineRepository repositorio donde se registrarán las aerolíneas
     * @param idGenerator       generador de IDs para sincronizar la secuencia
     */
    public AirlineSampleData(InMemoryAirlineRepository inMemoryAirlineRepository, IdGenerator idGenerator) {
        this.inMemoryAirlineRepository = inMemoryAirlineRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Carga las aerolíneas por defecto en el repositorio y sincroniza el generador de IDs.
     */
    public void load() {
        inMemoryAirlineRepository.create(new Airline(100, "Avianca"));
        inMemoryAirlineRepository.create(new Airline(110, "Latam"));
        inMemoryAirlineRepository.create(new Airline(120, "Clic"));
        idGenerator.syncWith(120);
    }
}
