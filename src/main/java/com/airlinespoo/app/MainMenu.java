package com.airlinespoo.app;

import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.core.ui.ConsoleInput;
import java.util.List;
import java.util.Objects;

/** Menú principal polimórfico para acceder a todos los módulos. */
public class MainMenu implements Menu {
    private final ConsoleInput input;
    private final List<Menu> modules;
    public MainMenu(ConsoleInput input, List<Menu> modules) {
        this.input = Objects.requireNonNull(input);
        this.modules = List.copyOf(modules);
    }
    @Override public String title() { return "Airlines POO"; }
    @Override public void run() {
        int option;
        do {
            System.out.println("\n=== AIRLINES POO - MENÚ PRINCIPAL ===");
            for (int i = 0; i < modules.size(); i++) {
                System.out.println((i + 1) + ". " + modules.get(i).title());
            }
            System.out.println("0. Salir");
            option = input.readInt("Seleccione una opción", 0, modules.size());
            if (option != 0) {
                try { modules.get(option - 1).run(); }
                catch (RuntimeException ex) { System.out.println("Error del módulo: " + ex.getMessage()); }
            }
        } while (option != 0);
        System.out.println("Hasta pronto.");
    }
}
