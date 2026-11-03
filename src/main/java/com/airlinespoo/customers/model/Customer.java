package com.airlinespoo.customers.model;

import com.airlinespoo.core.exception.ValidationException;
import com.airlinespoo.core.model.Identifiable;
import com.airlinespoo.core.util.Validator;

import java.util.Objects;

/**
 * Cliente de la aerolínea. Se identifica por su documento de identidad.
 */
public class Customer implements Identifiable<String> {

    // Constantes
    public static final int MAX_ID_LENGTH = 20;
    public static final int MAX_FIRST_NAME_LENGTH = 30;
    public static final int MAX_LAST_NAME_LENGTH = 30;
    public static final int MAX_TELEPHONE_LENGTH = 15;
    public static final int MAX_EMAIL_LENGTH = 80;
    public static final int MAX_ADDRESS_LENGTH = 40;
    private static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+$";
    private static final String TELEPHONE_PATTERN = "^\\+?\\d+$";

    // Atributos
    private final String id;
    private String firstName;
    private String lastName;
    private DocumentType documentType;
    private String telephoneNumber;
    private String email;
    private Country country;
    private String address;

    public Customer(String id, String firstName, String lastName, DocumentType documentType,
                    String telephoneNumber, String email, Country country, String address) {
        /**
         * Crea un cliente válido.
         *
         * @param id              documento de identidad (obligatorio, no cambia)
         * @param firstName       nombre (obligatorio)
         * @param lastName        apellido (obligatorio)
         * @param documentType    tipo de documento (obligatorio)
         * @param telephoneNumber teléfono, solo dígitos con un + opcional al inicio
         * @param email           correo (opcional, puede ser null)
         * @param country         país (obligatorio)
         * @param address         dirección (opcional, puede ser null)
         */

        this.id = validateId(id);
        this.firstName = validateFirstName(firstName);
        this.lastName = validateLastName(lastName);
        this.documentType = validateDocumentType(documentType);
        this.telephoneNumber = validateTelephoneNumber(telephoneNumber);
        this.email = validateEmail(email);
        this.country = validateCountry(country);
        this.address = validateAddress(address);
    }

    // Validaciones
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
        String limpio = Validator.requireText(telephoneNumber, "teléfono", MAX_TELEPHONE_LENGTH);
        if (!limpio.matches(TELEPHONE_PATTERN)) {
            throw new ValidationException(
                "El campo 'teléfono' solo puede tener dígitos, con un + opcional al inicio.");
        }
        return limpio;
    }

    private static String validateEmail(String email) {
        String limpio = normalizeOptional(email);
        if (limpio == null) {
            return null;
        }
        Validator.requireMaxLength(limpio, MAX_EMAIL_LENGTH, "correo");
        if (!limpio.matches(EMAIL_PATTERN)) {
            throw new ValidationException("El campo 'correo' no tiene un formato válido.");
        }
        return limpio;
    }

    private static Country validateCountry(Country country) {
        return Validator.requireNotNull(country, "país");
    }

    private static String validateAddress(String address) {
        String limpio = normalizeOptional(address);
        if (limpio == null) {
            return null;
        }
        return Validator.requireMaxLength(limpio, MAX_ADDRESS_LENGTH, "dirección");
    }

    private static String normalizeOptional(String value) {
        String limpio = Validator.normalize(value);
        return (limpio == null || limpio.isEmpty()) ? null : limpio;
    }

    // Getters
    @Override
    public String getId() {
        return id;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public Country getCountry() {
        return country;
    }

    public String getAddress() {
        return address;
    }

    // Setters (el id no tiene setter)

    public void setFirstName(String firstName) {
        this.firstName = validateFirstName(firstName);
    }

    public void setLastName(String lastName) {
        this.lastName = validateLastName(lastName);
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = validateDocumentType(documentType);
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = validateTelephoneNumber(telephoneNumber);
    }

    public void setEmail(String email) {
        this.email = validateEmail(email);
    }

    public void setCountry(Country country) {
        this.country = validateCountry(country);
    }

    public void setAddress(String address) {
        this.address = validateAddress(address);
    }

    // equals, hashCode y toString

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer other)) return false;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Customer{id='" + id + "', name='" + getFullName() + "'}";
    }
}
