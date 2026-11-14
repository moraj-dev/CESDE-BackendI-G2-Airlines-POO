package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.IdGenerator;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.repository.ICountryRepository;
import com.airlinespoo.customers.repository.ICustomerRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio de los países.
 */
public class CountryService {

    private final ICountryRepository countryRepository;
    private final ICustomerRepository customerRepository;
    private final IdGenerator idGenerator;

    /**
     * Crea el servicio de países.
     *
     * @param countryRepository  repositorio de países
     * @param customerRepository repositorio de clientes, para impedir eliminar países en uso
     * @param idGenerator        generador de ids consecutivos para los países nuevos
     */
    public CountryService(ICountryRepository countryRepository,
                          ICustomerRepository customerRepository,
                          IdGenerator idGenerator) {
        this.countryRepository = countryRepository;
        this.customerRepository = customerRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Sincroniza el generador de ids con los países que ya existen en la base de datos,
     * para no repetir ids. Debe llamarse una vez, al arrancar el programa.
     */
    public void syncIdGenerator() {
        int maxId = 0;
        for (Country country : countryRepository.findAll()) {
            maxId = Math.max(maxId, country.getId());
        }
        idGenerator.syncWith(maxId);
    }

    /**
     * Registra un país nuevo; el id lo asigna el generador.
     *
     * @param name nombre válido (máximo 20 caracteres)
     * @return el país guardado
     * @throws ValidationException si el nombre no es válido
     */
    public Country createCountry(String name) {
        Country country = new Country(idGenerator.next(), name);
        countryRepository.create(country);
        return country;
    }

    /**
     * Busca un país por id.
     *
     * @param id identificador del país
     * @return el país encontrado
     * @throws NotFoundException si no existe
     */
    public Country getCountry(int id) {
        Optional<Country> found = countryRepository.findById(id);
        if (found.isEmpty()) {
            throw new NotFoundException("país", id);
        }
        return found.get();
    }

    /**
     * @return todos los países, ordenados por nombre
     */
    public List<Country> listCountries() {
        List<Country> countries = countryRepository.findAll();
        countries.sort(Comparator.comparing(Country::getName));
        return countries;
    }

    /**
     * Cambia el nombre de un país.
     *
     * @param id   identificador del país
     * @param name nuevo nombre válido
     * @return el país actualizado
     * @throws NotFoundException   si el país no existe
     * @throws ValidationException si el nombre no es válido
     */
    public Country updateCountry(int id, String name) {
        Country country = getCountry(id);
        country.setName(name);
        countryRepository.update(country);
        return country;
    }

    /**
     * Elimina un país que no tenga clientes.
     *
     * @param id identificador del país
     * @throws NotFoundException     si el país no existe
     * @throws BusinessRuleException si algún cliente pertenece a ese país
     */
    public void deleteCountry(int id) {
        Country country = getCountry(id);
        if (customerRepository.existsByCountry(country)) {
            throw new BusinessRuleException("No se puede eliminar el país porque tiene clientes.");
        }
        countryRepository.deleteById(id);
    }
}
