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

/**
 * Menú de consola de clientes: registrar, consultar, actualizar contacto, listar,
 * buscar por apellido, eliminar y acceso a los menús de países y tipos de documento.
 * Solo habla con los servicios.
 */
public class CustomerMenu implements Menu {

    private static final int OPTION_REGISTER = 1;
    private static final int OPTION_GET = 2;
    private static final int OPTION_UPDATE = 3;
    private static final int OPTION_LIST = 4;
    private static final int OPTION_SEARCH = 5;
    private static final int OPTION_DELETE = 6;
    private static final int OPTION_COUNTRIES = 7;
    private static final int OPTION_DOCUMENT_TYPES = 8;
    private static final int OPTION_BACK = 9;

    private final ConsoleInput input;
    private final CustomerService customerService;
    private final CountryService countryService;
    private final DocumentTypeService documentTypeService;
    private final Menu countryMenu;
    private final Menu documentTypeMenu;

    /**
     * Crea el menú con el lector de consola, los servicios y los submenús que necesita.
     *
     * @param input               lector de consola compartido
     * @param customerService     servicio de clientes
     * @param countryService      servicio de países
     * @param documentTypeService servicio de tipos de documento
     * @param countryMenu         menú de países
     * @param documentTypeMenu    menú de tipos de documento
     */
    public CustomerMenu(ConsoleInput input, CustomerService customerService,
                        CountryService countryService, DocumentTypeService documentTypeService,
                        Menu countryMenu, Menu documentTypeMenu) {
        this.input = input;
        this.customerService = customerService;
        this.countryService = countryService;
        this.documentTypeService = documentTypeService;
        this.countryMenu = countryMenu;
        this.documentTypeMenu = documentTypeMenu;
    }

    @Override
    public String title() {
        return "Clientes";
    }

    /** Muestra el menú y atiende al usuario hasta que elige volver. */
    @Override
    public void run() {
        boolean back = false;
        while (!back) {
            printOptions();
            int option = input.readInt("Elija una opción", OPTION_REGISTER, OPTION_BACK);

            if (option == OPTION_BACK) {
                back = true;
            } else if (option == OPTION_COUNTRIES) {
                countryMenu.run();
            } else if (option == OPTION_DOCUMENT_TYPES) {
                documentTypeMenu.run();
            } else {
                execute(option);
                input.pause();
            }
        }
    }

    private void printOptions() {
        System.out.println();
        System.out.println("------ CLIENTES ------");
        System.out.println(OPTION_REGISTER + ". Registrar cliente");
        System.out.println(OPTION_GET + ". Consultar por documento");
        System.out.println(OPTION_UPDATE + ". Actualizar contacto");
        System.out.println(OPTION_LIST + ". Listar clientes");
        System.out.println(OPTION_SEARCH + ". Buscar por apellido");
        System.out.println(OPTION_DELETE + ". Eliminar cliente");
        System.out.println(OPTION_COUNTRIES + ". Gestionar países");
        System.out.println(OPTION_DOCUMENT_TYPES + ". Gestionar tipos de documento");
        System.out.println(OPTION_BACK + ". Volver");
        System.out.println("----------------------");
    }

    // Ejecuta la opción elegida; si el servicio lanza un error, lo muestra en lugar de cerrar el programa.
    private void execute(int option) {
        try {
            switch (option) {
                case OPTION_REGISTER:
                    registerCustomer();
                    break;
                case OPTION_GET:
                    getCustomer();
                    break;
                case OPTION_UPDATE:
                    updateCustomer();
                    break;
                case OPTION_LIST:
                    listCustomers();
                    break;
                case OPTION_SEARCH:
                    searchByLastName();
                    break;
                case OPTION_DELETE:
                    deleteCustomer();
                    break;
                default:
                    break;
            }
        } catch (AirlinesException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void registerCustomer() {
        System.out.println("---- Registrar cliente ----");
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
            throw new BusinessRuleException(
                "No hay tipos de documento registrados. Regístralos en 'Gestionar tipos de documento'.");
        }
        for (int i = 0; i < types.size(); i++) {
            DocumentType type = types.get(i);
            System.out.println((i + 1) + ". " + type.getId() + " - " + type.getDescription());
        }
        int choice = input.readInt("Elija el tipo de documento", 1, types.size());
        return types.get(choice - 1); // pasa del número visible a la posición de la lista
    }

    private Country chooseCountry() {
        List<Country> countries = countryService.listCountries();
        if (countries.isEmpty()) {
            throw new BusinessRuleException(
                "No hay países registrados. Regístralos en 'Gestionar países'.");
        }
        for (int i = 0; i < countries.size(); i++) {
            System.out.println((i + 1) + ". " + countries.get(i).getName());
        }
        int choice = input.readInt("Elija el país", 1, countries.size());
        return countries.get(choice - 1);
    }

    private void getCustomer() {
        System.out.println("---- Consultar cliente ----");
        String id = input.readRequiredText("Documento de identidad");
        printDetails(customerService.getCustomer(id));
    }

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

    private void listCustomers() {
        System.out.println("---- Lista de clientes ----");
        printList(customerService.listCustomers());
    }

    private void searchByLastName() {
        System.out.println("---- Buscar por apellido ----");
        String text = input.readRequiredText("Apellido (o parte de él)");
        printList(customerService.searchCustomersByLastName(text));
    }

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
        if (customers.isEmpty()) {
            System.out.println("No hay clientes para mostrar.");
            return;
        }
        for (Customer customer : customers) {
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
