package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.Repository;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;

import java.util.List;
import java.util.Optional;

/**
 * Contrato de almacenamiento de clientes, con búsquedas por correo, apellido,
 * país y tipo de documento.
 */

public interface CustomerRepository extends Repository<Customer, String> {

    /** Busca un cliente por correo, sin distinguir mayúsculas. */
    Optional<Customer> findByEmail(String email);

    /** Indica si ya existe un cliente con ese correo. */
    boolean existsByEmail(String email);

    /** Busca clientes cuyo apellido contenga el texto, sin distinguir mayúsculas. */
    List<Customer> findByLastNameContaining(String text);

    /** Lista los clientes de un país. */
    List<Customer> findByCountry(Country country);

    /** Lista los clientes que usan un tipo de documento. */
    List<Customer> findByDocumentType(DocumentType documentType);

    /** Indica si algún cliente pertenece a ese país. */
    boolean existsByCountry(Country country);

    /** Indica si algún cliente usa ese tipo de documento. */
    boolean existsByDocumentType(DocumentType documentType);
}
