package com.airlinespoo.customers.model;

import java.util.Objects;

/**
 * Tipo de documento de identidad (CC, TI, PA...). Su id es un código de 2 caracteres.
 */
public class DocumentType {

    public static final int ID_LENGTH = 2;
    public static final int MAX_DESCRIPTION_LENGTH = 20;

    private String id;
    private String description;

    public DocumentType(String id, String description) {
        this.id = id;
        this.description = description;
    }

    public DocumentType() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DocumentType other)) return false;
        return Objects.equals(id, other.id);
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
