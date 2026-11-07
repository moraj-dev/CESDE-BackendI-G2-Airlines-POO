package com.airlinespoo.customers.repository;

import com.airlinespoo.customers.model.DocumentType;

import java.util.List;
import java.util.Optional;

public interface IDocumentTypeRepository {
    void create(DocumentType documentType);
    Optional<DocumentType> findById(String id);
    List<DocumentType> findAll();
    void update(DocumentType documentType);
    void deleteById(String id);
    boolean existsById(String id);
    long count();
}
