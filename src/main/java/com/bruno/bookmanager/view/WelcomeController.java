package com.bruno.bookmanager.view;

import com.bruno.bookmanager.dao.DAOType;
import com.bruno.bookmanager.service.LibroService;
import com.bruno.bookmanager.utils.StringUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class WelcomeController {

    private static final Logger logger = LoggerFactory.getLogger(WelcomeController.class);

    @FXML
    private void handleCreateNewLibrary(ActionEvent event) {
        try {
            // Mostra dialog per scegliere dove salvare la nuova libreria
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Crea Nuova Libreria");
            fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("File JSON", "*.json"),
                    new FileChooser.ExtensionFilter("Database SQLite", "*.db"));
            fileChooser.setInitialFileName("mia_libreria.json");
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            File selectedFile = fileChooser.showSaveDialog(stage);

            if (selectedFile != null) {
                // Determina il tipo di DAO in base all'estensione
                DAOType daoType;
                if (selectedFile.getName().toLowerCase().endsWith(".db")) {
                    daoType = DAOType.SQLITE;
                } else {
                    daoType = DAOType.CACHED_JSON; // Usa versione cached per prestazioni migliori
                }
                if(selectedFile.exists())selectedFile.delete();

                // Configura il service
                LibroService.getInstance().setDAO(daoType, selectedFile.getAbsolutePath());

                // Apri la libreria
                openLibraryView(stage, selectedFile.getName());
            }
        } catch (Exception e) {
            showErrorAlert("Errore", "Impossibile creare la nuova libreria: " + e.getMessage());
        }
    }

    @FXML
    private void handleLoadLibrary(ActionEvent event) {
        try {
            // Mostra dialog per scegliere il file da caricare
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Carica Libreria Esistente");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("File JSON", "*.json"),
                    new FileChooser.ExtensionFilter("Database SQLite", "*.db"),
                    new FileChooser.ExtensionFilter("Tutti i file", "*.*")
            );
            fileChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            File selectedFile = fileChooser.showOpenDialog(stage);

            if (selectedFile != null && selectedFile.exists()) {
                // Determina il tipo di DAO in base all'estensione
                DAOType daoType;
                if (selectedFile.getName().toLowerCase().endsWith(".db")) {
                    daoType = DAOType.SQLITE;
                } else {
                    daoType = DAOType.CACHED_JSON;
                }

                LibroService service = LibroService.getInstance();

                service.setDAO(daoType, selectedFile.getAbsolutePath());
                service.caricaCollezione();

                openLibraryView(stage, selectedFile.getName());
            }
        } catch (Exception e) {
            showErrorAlert("Errore", "Impossibile caricare la libreria: " + e.getMessage());
        }
    }

    /**
     * Apre la vista principale della libreria.
     */
    private void openLibraryView(Stage stage, String name) throws IOException {

        FXMLLoader loader = new FXMLLoader(getClass().getResource("LibreriaView.fxml"));
        Parent root = loader.load();

        stage.setScene(new Scene(root, 1200, 800));
        stage.setTitle("Book Manager - "+name);
        stage.setMinWidth(800);
        stage.setMinHeight(600);
        stage.show();
    }

    /**
     * Mostra un dialog di errore.
     */
    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
