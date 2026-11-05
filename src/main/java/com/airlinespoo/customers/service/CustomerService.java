package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.repository.CustomerRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class CustomerService {
    private final CustomerRepository customerRepository;
    private final CountryService countryService;
    private final DocumentTypeService documentTypeService;

    private static final Comparator<Customer> BY_LAST_NAME_THEN_FIRST_NAME =
        Comparator.comparing(Customer::getLastName)
            .thenComparing(Customer::getFirstName);

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
        customers.sort(BY_LAST_NAME_THEN_FIRST_NAME);
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

    //metodos complemetarios

    public Optional<Customer> getCustomerByEmail(String email) {
        String cleanEmail = Validator.normalize(email);
        return customerRepository.findByEmail(cleanEmail);
    }
    /**
     * Busca clientes cuyo apellido contenga el texto, sin distinguir mayúsculas.
     *
     * @param text texto a buscar (obligatorio)
     * @return clientes que coinciden, ordenados por apellido y nombre
     * @throws ValidationException si el texto está vacío
     */
    public List<Customer> searchCustomersByLastName(String text) {
        String cleanText = Validator.normalize(text);
        if (cleanText == null || cleanText.isEmpty()) {
            throw new ValidationException("El campo 'apellido a buscar' no puede estar vacío.");
        }
        List<Customer> customers = customerRepository.findByLastNameContaining(cleanText);
        customers.sort(BY_LAST_NAME_THEN_FIRST_NAME);
        return customers;
    }

    /**
     * Lista los clientes de un país.
     *
     * @param countryId id del país, que debe existir
     * @return clientes del país, ordenados por apellido y nombre
     * @throws NotFoundException si el país no existe
     */
    public List<Customer> listCustomersByCountry(int countryId) {
        Country country = countryService.getCountry(countryId);
        List<Customer> customers = customerRepository.findByCountry(country);
        customers.sort(BY_LAST_NAME_THEN_FIRST_NAME);
        return customers;
    }

    /**
     * Lista los clientes que usan un tipo de documento.
     *
     * @param documentTypeId id del tipo de documento, que debe existir
     * @return clientes con ese tipo, ordenados por apellido y nombre
     * @throws NotFoundException si el tipo de documento no existe
     */
    public List<Customer> listCustomersByDocumentType(String documentTypeId) {
        DocumentType documentType = documentTypeService.getDocumentType(documentTypeId);
        List<Customer> customers = customerRepository.findByDocumentType(documentType);
        customers.sort(BY_LAST_NAME_THEN_FIRST_NAME);
        return customers;
    }

    /**
     * Cuenta el total de clientes registrados.
     *
     * @return la cantidad de clientes
     */
    public long countCustomers() {
        return customerRepository.count();
    }
}
