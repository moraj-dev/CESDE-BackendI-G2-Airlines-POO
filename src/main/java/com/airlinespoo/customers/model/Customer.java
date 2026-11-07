package com.airlinespoo.customers.model;

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
    public static final String EMAIL_PATTERN = "^[^@\\s]+@[^@\\s]+$";
    public static final String TELEPHONE_PATTERN = "^\\+?\\d+$";

    private String id;
    private String firstName;
    private String lastName;
    private DocumentType documentType;
    private String telephoneNumber;
    private String email;
    private Country country;
    private String address;

    public Customer(String id, String firstName, String lastName, DocumentType documentType,
                    String telephoneNumber, String email, Country country, String address) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.documentType = documentType;
        this.telephoneNumber = telephoneNumber;
        this.email = email;
        this.country = country;
        this.address = address;
    }

    public Customer() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Customer other)) return false;
        return Objects.equals(id, other.id);
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
