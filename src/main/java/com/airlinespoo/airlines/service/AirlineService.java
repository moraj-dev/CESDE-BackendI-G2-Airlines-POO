package com.airlinespoo.airlines.service;

import com.airlinespoo.airlines.model.Airline;
import com.airlinespoo.airlines.repository.AirlineRepository;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.core.util.Validator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Proporciona la lógica de negocio para la administración y operaciones sobre aerolíneas.
 */
public class AirlineService {

    private final AirlineRepository airlineRepository;
    private final IdGenerator idGenerator;

    /**
     * Construye el servicio de aerolíneas con sus dependencias requeridas.
     *
     * @param airlineRepository repositorio de aerolíneas
     * @param idGenerator       generador de identificadores únicos
     */
    public AirlineService(AirlineRepository airlineRepository, IdGenerator idGenerator) {
        this.airlineRepository = airlineRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Crea y registra una nueva aerolínea tras validar que su nombre no esté duplicado.
     *
     * @param name nombre de la aerolínea
     * @return la {@link Airline} creada y persistida
     * @throws DuplicateException si ya existe una aerolínea con el mismo nombre
     */
    public Airline createAirline(String name) {
        String cleanName = Validator.normalize(name);
        if (airlineRepository.existsByName(cleanName)) {
            throw new DuplicateException("Ya existe una aerolínea con el nombre '" +  cleanName + "'.");
        }

        int id = idGenerator.next();
        Airline airline = new Airline(id, cleanName);
        return airlineRepository.create(airline);
    }

    /**
     * Obtiene una aerolínea específica a partir de su identificador.
     *
     * @param id identificador de la aerolínea
     * @return la {@link Airline} encontrada
     * @throws NotFoundException si el identificador no existe
     */
    public Airline getAirline(Integer id) {
        Optional<Airline> found = airlineRepository.findById(id);
        if (found.isEmpty()) {
            throw new NotFoundException("aerolínea", id);
        }
        return found.get();
    }

    /**
     * Lista todas las aerolíneas registradas ordenadas alfabéticamente por su nombre.
     *
     * @return lista ordenada de aerolíneas
     */
    public List<Airline> listAirlines() {
        List<Airline> airlines = airlineRepository.findAll();
        airlines.sort(Comparator.comparing(Airline::getName, String.CASE_INSENSITIVE_ORDER));
        return airlines;
    }

    /**
     * Modifica el nombre de una aerolínea existente validando duplicados.
     *
     * @param id   identificador de la aerolínea
     * @param name nuevo nombre a establecer
     * @return la {@link Airline} actualizada
     * @throws DuplicateException si el nuevo nombre ya pertenece a otra aerolínea
     */
    public Airline renameAirline(Integer id, String name) {
        Airline airline = getAirline(id);
        String cleanName = Validator.normalize(name);

        Optional<Airline> found = airlineRepository.findByName(cleanName);
        if (found.isPresent() && !found.get().getId().equals(airline.getId())) {
            throw new DuplicateException("Ya existe una aerolínea con el nombre '" +  cleanName + "'.");
        }

        airline.setName(cleanName);
        return airlineRepository.update(airline);
    }

    /**
     * Elimina una aerolínea del sistema según su identificador.
     *
     * @param id identificador de la aerolínea a eliminar
     */
    public void deleteAirline(Integer id) {
        getAirline(id);
        airlineRepository.deleteById(id);
    }

    /**
     * Cuenta el total de aerolíneas registradas actualmente.
     *
     * @return cantidad total de aerolíneas
     */
    public long countAirlines() {
        return airlineRepository.count();
    }

    /**
     * Busca aerolíneas cuyo nombre contenga el texto de búsqueda proporcionado.
     *
     * @param text texto o fragmento a buscar
     * @return lista de aerolíneas que coinciden con el criterio
     */
    public List<Airline> searchAirlinesByName(String text) {
        String cleanSearch = Validator.requireText(text, "texto de búsqueda").toLowerCase();
        List<Airline> result = new ArrayList<>();
        for (Airline airline : airlineRepository.findAll()) {
            if (airline.getName().toLowerCase().contains(cleanSearch)) {
                result.add(airline);
            }
        }
        return result;
    }
}
