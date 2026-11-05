package com.airlinespoo.customers.ui;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.exception.BusinessRuleException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.customers.model.Country;
import com.airlinespoo.customers.model.Customer;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.service.CountryService;
import com.airlinespoo.customers.service.CustomerService;
import com.airlinespoo.customers.service.DocumentTypeService;

import java.util.List;

public class CustomerMenu implements Menu {

    private static final int MAX_OPTION = 7;

    private final ConsoleInput input;
    private final CustomerService customerService;
    private final CountryService countryService;
    private final DocumentTypeService documentTypeService;

    public CustomerMenu(ConsoleInput input, CustomerService customerService, CountryService countryService, DocumentTypeService documentTypeService) {
        this.input = input;
        this.customerService = customerService;
        this.countryService = countryService;
        this.documentTypeService = documentTypeService;
    }

    @Override
    public String title() {
        return "Clientes";
    }

    @Override
    public void run() {
        boolean back = false;
        while (!back) {
            System.out.println("------ MENU ------");
            System.out.println("1. Registrar");
            System.out.println("2. Consultar");
            System.out.println("3. actualizar contacto");
            System.out.println("4. Listar");
            System.out.println("5. buscar");
            System.out.println("6. Eliminar");
            System.out.println("7. Volver");
            System.out.println("------------------");

            int option = input.readInt("Elija una opción", 1, MAX_OPTION);

            if (option == MAX_OPTION) {
                back = true;
            } else {
                execute(option);
                input.pause();
            }
        }
    }

    // ejecuta la opción elegida; si el servicio lanza un error, lo muestra en lugar de cerrar el programa
    private void execute(int option) {
        try {
            switch (option) {
                case 1:
                    registerCustomer();
                    break;
                case 2:
                    getCustomer();
                    break;
                case 3:
                    updateCustomer();
                    break;
                case 4:
                    listCustomers();
                    break;
                case 5:
                    searchByLastName();
                    break;
                case 6:
                    deleteCustomer();
                    break;
            }
        } catch (AirlinesException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 1
    private void registerCustomer() {
        System.out.println("Necesitamos esta informacion");
        String id = input.readRequiredText("Documento");
        String firstName = input.readRequiredText("Nombre");
        String lastName = input.readRequiredText("Apellido");
        String telephone = input.readRequiredText("Teléfono");
        String email = input.readOptionalText("Correo");
        String address = input.readOptionalText("Dirección");
        DocumentType documentType = chooseDocumentType();
        Country country = chooseCountry();

        Customer customer = customerService.createCustomer(
            id,
            firstName,
            lastName,
            documentType.getId(),
            telephone,
            email,
            country.getId(),
            address);
        System.out.println("Cliente registrado con éxito: " + customer);
    }
    private DocumentType chooseDocumentType() {
        List<DocumentType> types = documentTypeService.listDocumentTypes();
        if (types.isEmpty()) {
            throw new BusinessRuleException("No hay tipos de documento registrados.");
        }
        for (int i = 0; i < types.size(); i++) {
            DocumentType type = types.get(i);
            System.out.println((i + 1) + ". " + type.getId() + " - " + type.getDescription());
        }
        int choice = input.readInt("Elija el tipo de documento", 1, types.size());
        return types.get(choice - 1); // devuelve de número visible a posición de la lista
    }

    private Country chooseCountry() {
        List<Country> countries = countryService.listCountries();
        if (countries.isEmpty()) {
            throw new BusinessRuleException("No hay países registrados.");
        }
        for (int i = 0; i < countries.size(); i++) {
            System.out.println((i + 1) + ". " + countries.get(i).getName());
        }
        int choice = input.readInt("Elija el país", 1, countries.size());
        return countries.get(choice - 1);
    }

    // 2
    private void getCustomer() {
        System.out.println("---- Consultar cliente ----");
        String id = input.readRequiredText("Documento de identidad");
        printDetails(customerService.getCustomer(id));
    }

    // 3
    private void updateCustomer() {
        System.out.println("---- Actualizar contacto ----");
        String id = input.readRequiredText("Documento de identidad");
        Customer current = customerService.getCustomer(id);
        printDetails(current);
        System.out.println("Deja en blanco lo que no quieras cambiar.");

        String telephone = input.readOptionalText("Nuevo teléfono");
        String email = input.readOptionalText("Nuevo correo");
        String address = input.readOptionalText("Nueva dirección");

        if (telephone.isEmpty()) {
            telephone = current.getTelephoneNumber();
        }
        if (email.isEmpty()) {
            email = current.getEmail();
        }
        if (address.isEmpty()) {
            address = current.getAddress();
        }

        Customer updated = customerService.updateCustomer(id, telephone, email, address);
        System.out.println("Contacto actualizado: " + updated);
    }

    // 4
    private void listCustomers() {
        System.out.println("---- Lista de clientes ----");
        printList(customerService.listCustomers());
    }

    // 5
    private void searchByLastName() {
        System.out.println("---- Buscar por apellido ----");
        String text = input.readRequiredText("Apellido (o parte de él)");
        printList(customerService.searchCustomersByLastName(text));
    }

    // 6
    private void deleteCustomer() {
        System.out.println("---- Eliminar cliente ----");
        String id = input.readRequiredText("Documento de identidad");
        Customer customer = customerService.getCustomer(id);
        if (input.confirm("¿Seguro que quieres eliminar a " + customer.getFullName() + "?")) {
            customerService.deleteCustomer(id);
            System.out.println("Cliente eliminado.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }

    private void printList(List<Customer> customers) {
        if (customers.isEmpty()) { // comprueva que no este vacia la lista
            System.out.println("No hay clientes para mostrar.");
            return;
        }
        for (Customer customer : customers) { // imprime la la lista de clientes
            System.out.println(customer);
        }
    }

    private void printDetails(Customer customer) {
        System.out.println("Documento: " + customer.getId()
            + " (" + customer.getDocumentType().getId() + ")");
        System.out.println("Nombre: " + customer.getFullName());
        System.out.println("Teléfono: " + customer.getTelephoneNumber());
        System.out.println("Correo: " + (customer.getEmail() == null ? "-" : customer.getEmail()));
        System.out.println("País: " + customer.getCountry().getName());
        System.out.println("Dirección: " + (customer.getAddress() == null ? "-" : customer.getAddress()));
    }
}
