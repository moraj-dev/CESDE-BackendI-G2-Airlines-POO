package com.airlinespoo.customers.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;

import java.util.Locale;
import java.util.Objects;

/**
 * Tipo de documento de identidad (CC, TI, PA...). Su id es un código de 2 caracteres.
 */
public class DocumentType {

    public static final int ID_LENGTH = 2;
    public static final int MAX_DESCRIPTION_LENGTH = 20;

    private final String id;
    private String description;

    /**
     * Crea un tipo de documento válido.
     *
     * @param id          código de exactamente 2 caracteres; se guarda en mayúsculas
     * @param description descripción (obligatoria, máximo 20 caracteres)
     * @throws ValidationException si algún dato no cumple las reglas
     */
    public DocumentType(String id, String description) {
        this.id = validateId(id);
        this.description = validateDescription(description);
    }

    /**
     * @return el código del tipo de documento
     */
    public String getId() {
        return id;
    }

    /**
     * @return la descripción del tipo de documento
     */
    public String getDescription() {
        return description;
    }

    /**
     * Cambia la descripción del tipo de documento.
     *
     * @param description nueva descripción (obligatoria, máximo 20 caracteres)
     * @throws ValidationException si la descripción no es válida
     */
    public void setDescription(String description) {
        this.description = validateDescription(description);
    }

    private static String validateId(String id) {
        String cleaned = Validator.requireText(id, "id del tipo de documento", ID_LENGTH);
        if (cleaned.length() != ID_LENGTH) {
            throw new ValidationException(
                "El campo 'id del tipo de documento' debe tener exactamente "
                    + ID_LENGTH + " caracteres.");
        }
        return cleaned.toUpperCase(Locale.ROOT);
    }

    private static String validateDescription(String description) {
        return Validator.requireText(description, "descripción", MAX_DESCRIPTION_LENGTH);
    }

    /** Dos tipos son iguales si tienen el mismo id. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentType other)) return false;
        return id.equals(other.id);
    }

    /** Código hash basado solo en el id. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** @return texto legible con el id y la descripción */
    @Override
    public String toString() {
        return "DocumentType{id='" + id + "', description='" + description + "'}";
    }
}
