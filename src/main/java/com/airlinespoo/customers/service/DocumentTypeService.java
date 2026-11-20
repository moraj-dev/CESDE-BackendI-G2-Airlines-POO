package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.DuplicateException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.repository.ICustomerRepository;
import com.airlinespoo.customers.repository.IDocumentTypeRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Reglas de negocio de los tipos de documento.
 */
public class DocumentTypeService {

    private final IDocumentTypeRepository documentTypeRepository;
    private final ICustomerRepository customerRepository;

    /**
     * Crea el servicio de tipos de documento.
     *
     * @param documentTypeRepository repositorio de tipos de documento
     * @param customerRepository     repositorio de clientes, para impedir eliminar tipos en uso
     */
    public DocumentTypeService(IDocumentTypeRepository documentTypeRepository,
                               ICustomerRepository customerRepository) {
        this.documentTypeRepository = documentTypeRepository;
        this.customerRepository = customerRepository;
    }

    /**
     * Registra un tipo de documento nuevo; el id se normaliza a mayúsculas.
     *
     * @param id          código de 2 caracteres
     * @param description descripción válida (máximo 20 caracteres)
     * @return el tipo guardado
     * @throws ValidationException si los datos no son válidos
     * @throws DuplicateException  si ya existe un tipo con ese id
     */
    public DocumentType createDocumentType(String id, String description) {
        DocumentType documentType = new DocumentType(id, description);
        if (documentTypeRepository.existsById(documentType.getId())) {
            throw new DuplicateException("tipo de documento", documentType.getId());
        }
        documentTypeRepository.create(documentType);
        return documentType;
    }

    /**
     * Busca un tipo de documento por id, sin distinguir mayúsculas.
     *
     * @param id código del tipo
     * @return el tipo encontrado
     * @throws NotFoundException si no existe
     */
    public DocumentType getDocumentType(String id) {
        String cleanId = cleanId(id);
        Optional<DocumentType> found = documentTypeRepository.findById(cleanId);
        if (found.isEmpty()) {
            throw new NotFoundException("tipo de documento", cleanId);
        }
        return found.get();
    }

    /**
     * @return todos los tipos de documento, ordenados por descripción
     */
    public List<DocumentType> listDocumentTypes() {
        List<DocumentType> documentTypes = documentTypeRepository.findAll();
        documentTypes.sort(Comparator.comparing(DocumentType::getDescription));
        return documentTypes;
    }

    /**
     * Cambia la descripción de un tipo de documento.
     *
     * @param id          código del tipo
     * @param description nueva descripción válida
     * @return el tipo actualizado
     * @throws NotFoundException   si no existe
     * @throws ValidationException si la descripción no es válida
     */
    public DocumentType updateDocumentType(String id, String description) {
        DocumentType documentType = getDocumentType(id);
        documentType.setDescription(description);
        documentTypeRepository.update(documentType);
        return documentType;
    }

    /**
     * Elimina un tipo de documento que no esté en uso.
     *
     * @param id código del tipo
     * @throws NotFoundException     si no existe
     * @throws BusinessRuleException si algún cliente usa ese tipo
     */
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
        return clean.toUpperCase(Locale.ROOT);
    }
}
