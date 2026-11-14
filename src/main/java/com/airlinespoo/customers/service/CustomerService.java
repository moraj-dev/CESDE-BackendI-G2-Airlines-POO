package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.repository.ICustomerRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Reglas de negocio de los clientes: registro, consulta, actualización de contacto,
 * listados, búsquedas y eliminación.
 */
public class CustomerService {

    private static final Comparator<Customer> BY_LAST_NAME_THEN_FIRST_NAME =
        Comparator.comparing(Customer::getLastName)
            .thenComparing(Customer::getFirstName);

    private final ICustomerRepository customerRepository;
    private final CountryService countryService;
    private final DocumentTypeService documentTypeService;

    /**
     * Crea el servicio de clientes.
     *
     * @param customerRepository  repositorio de clientes
     * @param countryService      servicio de países, para validar que el país exista
     * @param documentTypeService servicio de tipos de documento, para validar que el tipo exista
     */
    public CustomerService(ICustomerRepository customerRepository,
                           CountryService countryService,
                           DocumentTypeService documentTypeService) {
        this.customerRepository = customerRepository;
        this.countryService = countryService;
        this.documentTypeService = documentTypeService;
    }

    /**
     * Busca un cliente por documento.
     *
     * @param id documento de identidad
     * @return el cliente encontrado
     * @throws NotFoundException si no existe
     */
    public Customer getCustomer(String id) {
        String cleanId = Validator.normalize(id);
        Optional<Customer> found = customerRepository.findById(cleanId);
        if (found.isEmpty()) {
            throw new NotFoundException("cliente", cleanId);
        }
        return found.get();
    }

    /**
     * Registra un cliente nuevo.
     *
     * @param id              documento de identidad
     * @param firstName       nombre
     * @param lastName        apellido
     * @param documentTypeId  código del tipo de documento, que debe existir
     * @param telephoneNumber teléfono
     * @param email           correo (opcional)
     * @param countryId       id del país, que debe existir
     * @param address         dirección (opcional)
     * @return el cliente guardado
     * @throws NotFoundException   si el país o el tipo de documento no existen
     * @throws ValidationException si algún dato no es válido
     * @throws DuplicateException  si el documento o el correo ya están registrados
     */
    public Customer createCustomer(String id, String firstName, String lastName,
                                   String documentTypeId, String telephoneNumber,
                                   String email, int countryId, String address) {
        DocumentType documentType = documentTypeService.getDocumentType(documentTypeId);
        Country country = countryService.getCountry(countryId);
        Customer customer = new Customer(id, firstName, lastName, documentType,
            telephoneNumber, email, country, address);

        if (customerRepository.existsById(customer.getId())) {
            throw new DuplicateException("cliente", customer.getId());
        }
        if (customer.getEmail() != null && customerRepository.existsByEmail(customer.getEmail())) {
            throw new DuplicateException("Ya existe un cliente con el correo '" + customer.getEmail() + "'.");
        }
        customerRepository.create(customer);
        return customer;
    }

    /**
     * @return todos los clientes, ordenados por apellido y luego por nombre
     */
    public List<Customer> listCustomers() {
        List<Customer> customers = customerRepository.findAll();
        customers.sort(BY_LAST_NAME_THEN_FIRST_NAME);
        return customers;
    }

    /**
     * Elimina un cliente.
     *
     * @param id documento de identidad
     * @throws NotFoundException si no existe
     */
    public void deleteCustomer(String id) {
        Customer customer = getCustomer(id);
        customerRepository.deleteById(customer.getId());
    }

    /**
     * Actualiza teléfono, correo y dirección de un cliente.
     *
     * @param id              documento de identidad
     * @param telephoneNumber nuevo teléfono
     * @param email           nuevo correo (opcional)
     * @param address         nueva dirección (opcional)
     * @return el cliente actualizado
     * @throws NotFoundException   si el cliente no existe
     * @throws DuplicateException  si el correo pertenece a otro cliente
     * @throws ValidationException si algún dato no es válido
     */
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
        customerRepository.update(customer);
        return customer;
    }

    // métodos complementarios

    /**
     * Busca un cliente por correo.
     *
     * @param email correo a buscar
     * @return el cliente, o {@link Optional#empty()} si no existe
     */
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
