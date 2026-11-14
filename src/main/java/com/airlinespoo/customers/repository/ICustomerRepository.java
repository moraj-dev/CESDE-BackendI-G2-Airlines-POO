package com.airlinespoo.customers.repository;

import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;

import java.util.List;
import java.util.Optional;

public interface ICustomerRepository {
    void create(Customer customer);
    Optional<Customer> findById(String id);
    List<Customer> findAll();
    void update(Customer customer);
    void deleteById(String id);
    boolean existsById(String id);
    long count();

    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
    List<Customer> findByLastNameContaining(String text);
    List<Customer> findByCountry(Country country);
    List<Customer> findByDocumentType(DocumentType documentType);
    boolean existsByCountry(Country country);
    boolean existsByDocumentType(DocumentType documentType);
}
