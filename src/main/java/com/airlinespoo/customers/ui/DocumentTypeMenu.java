package com.airlinespoo.customers.ui;

import com.airlinespoo.core.exception.AirlinesException;
import com.airlinespoo.core.ui.ConsoleInput;
import com.airlinespoo.core.ui.Menu;
import com.airlinespoo.customers.model.DocumentType;
import com.airlinespoo.customers.service.DocumentTypeService;

import java.util.List;

/**
 * Menú de consola de tipos de documento: registrar, listar, cambiar descripción y eliminar.
 */
public class DocumentTypeMenu implements Menu {

    private static final int OPTION_REGISTER = 1;
    private static final int OPTION_LIST = 2;
    private static final int OPTION_UPDATE = 3;
    private static final int OPTION_DELETE = 4;
    private static final int OPTION_BACK = 5;

    private final ConsoleInput input;
    private final DocumentTypeService documentTypeService;

    /**
     * Crea el menú con el lector de consola y el servicio de tipos de documento.
     *
     * @param input               lector de consola compartido
     * @param documentTypeService servicio de tipos de documento
     */
    public DocumentTypeMenu(ConsoleInput input, DocumentTypeService documentTypeService) {
        this.input = input;
        this.documentTypeService = documentTypeService;
    }

    @Override
    public String title() {
        return "Tipos de documento";
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
            } else {
                execute(option);
                input.pause();
            }
        }
    }

    private void printOptions() {
        System.out.println();
        System.out.println("------ TIPOS DE DOCUMENTO ------");
        System.out.println(OPTION_REGISTER + ". Registrar tipo de documento");
        System.out.println(OPTION_LIST + ". Listar tipos de documento");
        System.out.println(OPTION_UPDATE + ". Cambiar descripción");
        System.out.println(OPTION_DELETE + ". Eliminar tipo de documento");
        System.out.println(OPTION_BACK + ". Volver");
        System.out.println("--------------------------------");
    }

    private void execute(int option) {
        try {
            switch (option) {
                case OPTION_REGISTER:
                    registerDocumentType();
                    break;
                case OPTION_LIST:
                    listDocumentTypes();
                    break;
                case OPTION_UPDATE:
                    updateDocumentType();
                    break;
                case OPTION_DELETE:
                    deleteDocumentType();
                    break;
                default:
                    break;
            }
        } catch (AirlinesException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private void registerDocumentType() {
        System.out.println("---- Registrar tipo de documento ----");
        String id = input.readRequiredText("Código de 2 letras (por ejemplo CC)");
        String description = input.readRequiredText("Descripción");
        DocumentType type = documentTypeService.createDocumentType(id, description);
        System.out.println("Tipo registrado: " + type);
    }

    private void listDocumentTypes() {
        System.out.println("---- Lista de tipos de documento ----");
        List<DocumentType> types = documentTypeService.listDocumentTypes();
        if (types.isEmpty()) {
            System.out.println("No hay tipos de documento registrados.");
            return;
        }
        for (DocumentType type : types) {
            System.out.println(type.getId() + " - " + type.getDescription());
        }
    }

    private void updateDocumentType() {
        System.out.println("---- Cambiar descripción ----");
        String id = input.readRequiredText("Código del tipo");
        String description = input.readRequiredText("Nueva descripción");
        DocumentType type = documentTypeService.updateDocumentType(id, description);
        System.out.println("Tipo actualizado: " + type);
    }

    private void deleteDocumentType() {
        System.out.println("---- Eliminar tipo de documento ----");
        String id = input.readRequiredText("Código del tipo");
        DocumentType type = documentTypeService.getDocumentType(id);
        if (input.confirm("¿Seguro que quieres eliminar " + type.getDescription() + "?")) {
            documentTypeService.deleteDocumentType(type.getId());
            System.out.println("Tipo eliminado.");
        } else {
            System.out.println("Operación cancelada.");
        }
    }
}
