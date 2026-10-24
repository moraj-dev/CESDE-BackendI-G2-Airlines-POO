package com.airlinespoo.customers.model;

public class Country {
    private Integer id;
    private String name;

    public Country(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    // Metodos


    //Getter para el id
    public Integer getId() {
        if (id <0) {
            System.out.println("El ID ingresado no puede ser menor a 0");
        }
        return id;
    }

    //Getters y Setters para el name

    public String getName() {

        return name;
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty()) { // botrra espacios y verifica que tenga texto
            throw new IllegalArgumentException("El nombre del país no puede estar vacío.");
        }

        if (name.length() > 20) { // verifica que no supera los 20 caracteres
            throw new IllegalArgumentException("El nombre no puede tener mas de 20 carateres."); // esto captura detiene el procesod de que se guarde
        }
        this.name = name;
    }
}
