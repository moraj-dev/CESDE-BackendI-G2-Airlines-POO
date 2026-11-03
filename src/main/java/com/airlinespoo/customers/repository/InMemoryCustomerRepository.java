package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de clientes que guarda los datos en memoria.
 */
public class InMemoryCustomerRepository extends InMemoryRepository<Customer, String>
    implements CustomerRepository {

    @Override
    protected String entityName() {
        return "cliente";
    }

    @Override
    public Optional<Customer> findByEmail(String email) {
        for (Customer customer : values()) {
            if (email != null && email.equalsIgnoreCase(customer.getEmail())) {
                return Optional.of(customer);
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean existsByEmail(String email) {
        return findByEmail(email).isPresent();
    }

    @Override
    public List<Customer> findByLastNameContaining(String text) {
        List<Customer> result = new ArrayList<>();
        String search = Validator.normalize(text);
        if (search == null || search.isEmpty()) {
            return result;
        }
        search = search.toLowerCase();
        for (Customer customer : values()) {
            if (customer.getLastName().toLowerCase().contains(search)) {
                result.add(customer);
            }
        }
        return result;
    }

    @Override
    public List<Customer> findByCountry(Country country) {
        List<Customer> result = new ArrayList<>();
        for (Customer customer : values()) {
            if (customer.getCountry().equals(country)) {
                result.add(customer);
            }
        }
        return result;
    }

    @Override
    public List<Customer> findByDocumentType(DocumentType documentType) {
        List<Customer> result = new ArrayList<>();
        for (Customer customer : values()) {
            if (customer.getDocumentType().equals(documentType)) {
                result.add(customer);
            }
        }
        return result;
    }

    @Override
    public boolean existsByCountry(Country country) {
        return !findByCountry(country).isEmpty();
    }

    @Override
    public boolean existsByDocumentType(DocumentType documentType) {
        return !findByDocumentType(documentType).isEmpty();
    }
}

