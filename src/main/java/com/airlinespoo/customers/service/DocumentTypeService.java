package com.airlinespoo.customers.service;

import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.exception.NotFoundException;
import com.airlinespoo.core.util.Validator;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.repository.ICustomerRepository;
import com.airlinespoo.customers.repository.IDocumentTypeRepository;
import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.exception.DuplicateException;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class DocumentTypeService {
    private final IDocumentTypeRepository iDocumentTypeRepository;
    private final ICustomerRepository iCustomerRepository;

    /**
     * Crea el servicio de tipos de documento.
     *
     * @param iDocumentTypeRepository repositorio de tipos de documento
     * @param iCustomerRepository     repositorio de clientes, para impedir eliminar tipos en uso
     */
    public DocumentTypeService(IDocumentTypeRepository iDocumentTypeRepository,
                               ICustomerRepository iCustomerRepository) {
        this.iDocumentTypeRepository = iDocumentTypeRepository;
        this.iCustomerRepository = iCustomerRepository;
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
        return iDocumentTypeRepository.create(documentType);
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
        Optional<DocumentType> found = iDocumentTypeRepository.findById(cleanId);
        if (found.isEmpty()) {
            throw new NotFoundException("tipo de documento", cleanId);
        }
        return found.get();
    }

    /**
     * @return todos los tipos de documento, ordenados por descripción
     */
    public List<DocumentType> listDocumentTypes() {
        List<DocumentType> documentTypes = iDocumentTypeRepository.findAll();
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
        return iDocumentTypeRepository.update(documentType);
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
        if (iCustomerRepository.existsByDocumentType(documentType)) {
            throw new BusinessRuleException("No se puede eliminar el tipo de documento porque tiene clientes.");
        }
        iDocumentTypeRepository.deleteById(documentType.getId());
    }

    private String cleanId(String id) {
        String clean = Validator.normalize(id);
        if (clean == null) {
            return null;
        }
        return clean.toUpperCase();
    }

}
