package com.airlinespoo.customers.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;

/**
 * Cliente de la aerolínea. Se identifica por su documento de identidad.
 */
public class Customer {

    public static final int MAX_ID_LENGTH = 20;
    public static final int MAX_FIRST_NAME_LENGTH = 30;
    public static final int MAX_LAST_NAME_LENGTH = 30;
    public static final int MAX_TELEPHONE_LENGTH = 15;
    public static final int MAX_EMAIL_LENGTH = 80;
    public static final int MAX_ADDRESS_LENGTH = 40;
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+$";
    private static final String TELEPHONE_PATTERN = "^\\+?\\d+$";

    private final String id;
    private String firstName;
    private String lastName;
    private DocumentType documentType;
    private String telephoneNumber;
    private String email;
    private Country country;
    private String address;

    /**
     * Crea un cliente válido.
     *
     * @param id              documento de identidad (obligatorio, máximo 20, no cambia)
     * @param firstName       nombre (obligatorio, máximo 30)
     * @param lastName        apellido (obligatorio, máximo 30)
     * @param documentType    tipo de documento (obligatorio)
     * @param telephoneNumber teléfono (obligatorio, máximo 15, solo dígitos con + opcional al inicio)
     * @param email           correo (opcional; si existe, máximo 80 y formato válido)
     * @param country         país (obligatorio)
     * @param address         dirección (opcional, máximo 40)
     * @throws ValidationException si algún dato no cumple las reglas
     */
    public Customer(String id, String firstName, String lastName, DocumentType documentType,
                    String telephoneNumber, String email, Country country, String address) {
        this.id = validateId(id);
        this.firstName = validateFirstName(firstName);
        this.lastName = validateLastName(lastName);
        this.documentType = validateDocumentType(documentType);
        this.telephoneNumber = validateTelephoneNumber(telephoneNumber);
        this.email = validateEmail(email);
        this.country = validateCountry(country);
        this.address = validateAddress(address);
    }

    private static String validateId(String id) {
        return Validator.requireText(id, "documento de identidad", MAX_ID_LENGTH);
    }

    private static String validateFirstName(String firstName) {
        return Validator.requireText(firstName, "nombre", MAX_FIRST_NAME_LENGTH);
    }

    private static String validateLastName(String lastName) {
        return Validator.requireText(lastName, "apellido", MAX_LAST_NAME_LENGTH);
    }

    private static DocumentType validateDocumentType(DocumentType documentType) {
        return Validator.requireNotNull(documentType, "tipo de documento");
    }

    private static String validateTelephoneNumber(String telephoneNumber) {
        String cleaned = Validator.requireText(telephoneNumber, "teléfono", MAX_TELEPHONE_LENGTH);
        if (!cleaned.matches(TELEPHONE_PATTERN)) {
            throw new ValidationException(
                "El campo 'teléfono' solo puede tener dígitos, con un + opcional al inicio.");
        }
        return cleaned;
    }

    private static String validateEmail(String email) {
        String cleaned = normalizeOptional(email);
        if (cleaned == null) {
            return null;
        }
        Validator.requireMaxLength(cleaned, MAX_EMAIL_LENGTH, "correo");
        if (!cleaned.matches(EMAIL_PATTERN)) {
            throw new ValidationException("El campo 'correo' no tiene un formato válido.");
        }
        return cleaned;
    }

    private static Country validateCountry(Country country) {
        return Validator.requireNotNull(country, "país");
    }

    private static String validateAddress(String address) {
        String cleaned = normalizeOptional(address);
        if (cleaned == null) {
            return null;
        }
        return Validator.requireMaxLength(cleaned, MAX_ADDRESS_LENGTH, "dirección");
    }

    private static String normalizeOptional(String value) {
        String cleaned = Validator.normalize(value);
        return (cleaned == null || cleaned.isEmpty()) ? null : cleaned;
    }

    /** @return el documento de identidad */
    public String getId() {
        return id;
    }

    /** @return nombre y apellido separados por un espacio */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /** @return el nombre */
    public String getFirstName() {
        return firstName;
    }

    /** @return el apellido */
    public String getLastName() {
        return lastName;
    }

    /** @return el tipo de documento */
    public DocumentType getDocumentType() {
        return documentType;
    }

    /** @return el teléfono */
    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    /** @return el correo, o {@code null} si no tiene */
    public String getEmail() {
        return email;
    }

    /** @return el país */
    public Country getCountry() {
        return country;
    }

    /** @return la dirección, o {@code null} si no tiene */
    public String getAddress() {
        return address;
    }

    /**
     * Cambia el nombre.
     *
     * @param firstName nuevo nombre (obligatorio, máximo 30)
     * @throws ValidationException si no es válido
     */
    public void setFirstName(String firstName) {
        this.firstName = validateFirstName(firstName);
    }

    /**
     * Cambia el apellido.
     *
     * @param lastName nuevo apellido (obligatorio, máximo 30)
     * @throws ValidationException si no es válido
     */
    public void setLastName(String lastName) {
        this.lastName = validateLastName(lastName);
    }

    /**
     * Cambia el tipo de documento.
     *
     * @param documentType nuevo tipo (obligatorio)
     * @throws ValidationException si es {@code null}
     */
    public void setDocumentType(DocumentType documentType) {
        this.documentType = validateDocumentType(documentType);
    }

    /**
     * Cambia el teléfono.
     *
     * @param telephoneNumber nuevo teléfono (obligatorio, máximo 15, solo dígitos con + opcional)
     * @throws ValidationException si no es válido
     */
    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = validateTelephoneNumber(telephoneNumber);
    }

    /**
     * Cambia el correo.
     *
     * @param email nuevo correo, o vacío/{@code null} para quitarlo
     * @throws ValidationException si supera 80 caracteres o no tiene formato válido
     */
    public void setEmail(String email) {
        this.email = validateEmail(email);
    }

    /**
     * Cambia el país.
     *
     * @param country nuevo país (obligatorio)
     * @throws ValidationException si es {@code null}
     */
    public void setCountry(Country country) {
        this.country = validateCountry(country);
    }

    /**
     * Cambia la dirección.
     *
     * @param address nueva dirección, o vacío/{@code null} para quitarla (máximo 40)
     * @throws ValidationException si supera 40 caracteres
     */
    public void setAddress(String address) {
        this.address = validateAddress(address);
    }

    /** Dos clientes son iguales si tienen el mismo documento. */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer other)) return false;
        return id.equals(other.id);
    }

    /** Código hash basado solo en el documento. */
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    /** @return texto legible con el documento y el nombre */
    @Override
    public String toString() {
        return "Customer{id='" + id + "', name='" + getFullName() + "'}";
    }
}
