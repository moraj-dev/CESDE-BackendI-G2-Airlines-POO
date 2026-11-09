package com.airlinespoo.reservations.model;

public class Seat {

    private final String rowNumber;
    private final int seatNumber;

    public Seat(String rowNumber, int seatNumber) {

        // Validaciones
        if (rowNumber == null || rowNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("El número de fila de la silla no puede ser nulo o vacío.");
        }

        String normalizedRow = rowNumber.trim().toUpperCase();// limpiar espacios - trasformar mayusculas

        if (normalizedRow.length() > 2) {
            throw new IllegalArgumentException("El número de fila no puede tener más de 2 caracteres.");
        }

        if (seatNumber < 1 || seatNumber > 99) {
            throw new IllegalArgumentException("El número de silla debe estar entre 1 y 99.");
        }

        this.rowNumber = normalizedRow;
        this.seatNumber = seatNumber;
    }

    // getters sin setters - silla es inmutable
    public String getRowNumber() {
        return rowNumber;
    }

    public int getSeatNumber() {
        return seatNumber;
    }

    // equals - comparar sillas y hashCode - trasformar listas conjuntos o mapas
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Seat seat = (Seat) o;
        return seatNumber == seat.seatNumber && rowNumber.equals(seat.rowNumber);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(rowNumber, seatNumber);
    }

    // toString - mostrar numeros en consola
    @Override
    public String toString() {
        return rowNumber + seatNumber;
    }
}
