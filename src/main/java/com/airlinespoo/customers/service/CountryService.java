package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.exception.DuplicateException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio de los países.
 */
public class CountryService {

    private final CountryRepository countryRepository;
    private final CustomerRepository customerRepository;

    /**
     * Crea el servicio de países.
     *
     * @param countryRepository  repositorio de países
     * @param customerRepository repositorio de clientes, para impedir eliminar países en uso
     */
    public CountryService(CountryRepository countryRepository,
                          CustomerRepository customerRepository) {
        this.countryRepository = countryRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Registra un país nuevo.
     *
     * @param id   identificador positivo
     * @param name nombre válido (máximo 20 caracteres)
     * @return el país guardado
     * @throws ValidationException si los datos no son válidos
     * @throws DuplicateException  si ya existe un país con ese id
     */
    public Country createCountry(int id, String name) {
        Country country = new Country(id, name);
        return countryRepository.create(country);
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
        List<Country> paises = countryRepository.findAll();
        paises.sort(Comparator.comparing(Country::getName));
        return paises;
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
        return countryRepository.update(country);
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
