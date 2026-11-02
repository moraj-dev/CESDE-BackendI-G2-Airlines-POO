package com.airlinespoo.customers.repository;

import com.airlinespoo.core.repository.InMemoryRepository;
import com.airlinespoo.customers.model.DocumentType;

/**
 * Repositorio de tipos de documento que guarda los datos en memoria.
 */

public class InMemoryDocumentTypeRepository extends InMemoryRepository<DocumentType, String>
    implements DocumentTypeRepository {

    @Override
    protected String entityName() {
        return "tipo de documento";
    }
}
