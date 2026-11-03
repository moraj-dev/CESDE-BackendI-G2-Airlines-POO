package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.repository.CountryRepository;
import com.airlinespoo.customers.repository.CustomerRepository;
import com.airlinespoo.customers.repository.DocumentTypeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DocumentTypeService {
    private final DocumentTypeRepository documentTypeRepository;
    private final CustomerRepository customerRepository;

    public DocumentTypeService(DocumentTypeRepository documentTypeRepository,
                               CustomerRepository customerRepository) {
        this.documentTypeRepository = documentTypeRepository;
        this.customerRepository = customerRepository;
    }

    public DocumentType createDocumentType(String id, String description) {
        DocumentType documentType = new DocumentType(id, description);
        return documentTypeRepository.create(documentType);
    }

    public DocumentType getDocumentType(String id) {
        String cleanId = cleanId(id);
        Optional<DocumentType> found = documentTypeRepository.findById(cleanId);
        if (found.isEmpty()) {
            throw new NotFoundException("tipo de documento", cleanId);
        }
        return found.get();
    }

    public List<DocumentType> listDocumentTypes() {
        List<DocumentType> documentTypes = documentTypeRepository.findAll();
        documentTypes.sort(Comparator.comparing(DocumentType::getDescription));
        return documentTypes;
    }

    public DocumentType updateDocumentType(String id, String description) {
        DocumentType documentType = getDocumentType(id);
        documentType.setDescription(description);
        return documentTypeRepository.update(documentType);
    }

    public void deleteDocumentType(String id) {
        DocumentType documentType = getDocumentType(id);
        if (customerRepository.existsByDocumentType(documentType)) {
            throw new BusinessRuleException("No se puede eliminar el tipo de documento porque tiene clientes.");
        }
        documentTypeRepository.deleteById(documentType.getId());
    }

    private String cleanId(String id) {
        String clean = Validator.normalize(id);
        if (clean == null) {
            return null;
        }
        return clean.toUpperCase();
    }

}
