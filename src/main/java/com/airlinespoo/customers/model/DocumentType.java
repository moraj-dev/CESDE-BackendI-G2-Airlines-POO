package com.airlinespoo.customers.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;

/**
 * Tipo de documento de identidad (CC, TI, PA...). Su id es un código de 2 caracteres.
 */
public class DocumentType implements Identifiable<String> {

    public static final int ID_LENGTH = 2; // constante de largo de el tipo de documento
    public static final int MAX_DESCRIPTION_LENGTH = 20; // constante de largo de la descripcion

    private final String id;
    private String description;

    public DocumentType(String id, String description) {
        this.id = validateId(id); // validamos el id con validator del kit base
        this.description = validateDescription(description); // igual aca
    }

    @Override
    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = validateDescription(description);
    }

    private static String validateId(String id) {
        //valida que el texto no mida mas de 2 caracteres
        String limpio = Validator.requireText(id, "id del tipo de documento", ID_LENGTH);
        if (limpio.length() != ID_LENGTH) { // si no tiene 2 caracteres lanza error
            throw new ValidationException(
                "El campo 'id del tipo de documento' debe tener exactamente "
                    + ID_LENGTH + " caracteres.");
        }
        return limpio.toUpperCase(); // combierte a mayusculas y asi se guarda "cc" ---> "CC"
    }

    private static String validateDescription(String description) {
        return Validator.requireText(description, "descripción", MAX_DESCRIPTION_LENGTH);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentType other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DocumentType{id='" + id + "', description='" + description + "'}";
    }
}
