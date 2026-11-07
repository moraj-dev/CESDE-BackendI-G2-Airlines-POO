package com.airlinespoo.airlines;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.core.util.IdGenerator;

/**
 * Carga datos iniciales de prueba para el módulo de aerolíneas.
 */
public class AirlineSampleData {
    private final AirlineRepository airlineRepository;
    private final IdGenerator idGenerator;

    /**
     * Construye la clase cargadora de datos de muestra.
     *
     * @param airlineRepository repositorio donde se registrarán las aerolíneas
     * @param idGenerator       generador de IDs para sincronizar la secuencia
     */
    public AirlineSampleData(AirlineRepository airlineRepository, IdGenerator idGenerator) {
        this.airlineRepository = airlineRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Carga las aerolíneas por defecto en el repositorio y sincroniza el generador de IDs.
     */
    public void load() {
        airlineRepository.create(new Airline(100, "Avianca"));
        airlineRepository.create(new Airline(110, "Latam"));
        airlineRepository.create(new Airline(120, "Clic"));
        idGenerator.syncWith(120);
    }
}
