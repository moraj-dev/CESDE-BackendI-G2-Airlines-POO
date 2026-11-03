package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.repository.CustomerRepository;

import java.util.List;
import java.util.Optional;

public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CountryService countryService;
    private final DocumentTypeService documentTypeService;

    public CustomerService(CustomerRepository customerRepository,
                           CountryService countryService,
                           DocumentTypeService documentTypeService) {
        this.customerRepository = customerRepository;
        this.countryService = countryService;
        this.documentTypeService = documentTypeService;
    }

    public Customer getCustomer(String id) {
        String cleanId = Validator.normalize(id);
        Optional<Customer> found = customerRepository.findById(cleanId);
        if (found.isEmpty()) {
            throw new NotFoundException("cliente", cleanId);
        }
        return found.get();
    }

    public Customer createCustomer(String id, String firstName, String lastName,
                                   String documentTypeId, String telephoneNumber,
                                   String email, int countryId, String address) {
        DocumentType documentType = documentTypeService.getDocumentType(documentTypeId);
        Country country = countryService.getCountry(countryId);
        Customer customer = new Customer(id, firstName, lastName, documentType,
            telephoneNumber, email, country, address);

        if (customer.getEmail() != null && customerRepository.existsByEmail(customer.getEmail())) {
            throw new DuplicateException("Ya existe un cliente con el correo '" + customer.getEmail() + "'.");
        }
        return customerRepository.create(customer);
    }

    public List<Customer> listCustomers() {
        List<Customer> customers = customerRepository.findAll();
        customers.sort((a, b) -> {
            int result = a.getLastName().compareTo(b.getLastName());
            if (result != 0) {
                return result;
            }
            return a.getFirstName().compareTo(b.getFirstName());
        });
        return customers;
    }

    public void deleteCustomer(String id) {
        Customer customer = getCustomer(id);
        customerRepository.deleteById(customer.getId());
    }

    public Customer updateCustomer(String id, String telephoneNumber,
                                   String email, String address) {
        Customer customer = getCustomer(id);

        String cleanEmail = Validator.normalize(email);
        if (cleanEmail != null && cleanEmail.isEmpty()) {
            cleanEmail = null;
        }

        if (cleanEmail != null) {
            Optional<Customer> owner = customerRepository.findByEmail(cleanEmail);
            if (owner.isPresent() && !owner.get().getId().equals(customer.getId())) {
                throw new DuplicateException("Ya existe un cliente con el correo '" + cleanEmail + "'.");
            }
        }

        customer.setTelephoneNumber(telephoneNumber);
        customer.setEmail(cleanEmail);
        customer.setAddress(address);
        return customerRepository.update(customer);
    }
}
