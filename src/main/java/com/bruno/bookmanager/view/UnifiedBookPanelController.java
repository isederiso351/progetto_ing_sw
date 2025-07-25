package com.bruno.bookmanager.view;

import com.bruno.bookmanager.command.AddLibroCommand;
import com.bruno.bookmanager.command.CommandHistory;
import com.bruno.bookmanager.command.RemoveLibroCommand;
import com.bruno.bookmanager.command.UpdateLibroCommand;
import com.bruno.bookmanager.exception.BookManagerException;
import com.bruno.bookmanager.exception.ValidationException;
import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import com.bruno.bookmanager.service.LibroService;
import com.bruno.bookmanager.service.Validator;
import com.bruno.bookmanager.utils.StringUtils;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Optional;


public class UnifiedBookPanelController {

    private final LibroService libroService = LibroService.getInstance();
    private final CommandHistory commandHistory = CommandHistory.getInstance();
    // Elementi della vista dettagli
    @FXML
    private VBox detailsView;
    @FXML
    private Label titoloLabel;
    @FXML
    private Label autoreText, isbnText, genereText, valutazioneText, statoText;
    // Elementi della vista form
    @FXML
    private VBox formView;
    @FXML
    private Label formTitleLabel;
    @FXML
    private TextField titoloField, autoreField, isbnField;
    @FXML
    private ComboBox<Genere> genereComboBox;
    @FXML
    private Slider valutazioneSlider;
    @FXML
    private Label valutazioneLabel;
    @FXML
    private ComboBox<StatoLettura> statoComboBox;
    @FXML
    private Button editButton, deleteButton;
    @FXML
    private Button salvaButton;
    // Stato e servizi
    private Libro currentLibro;
    private boolean isEditMode = false;
    private boolean isAddMode = false;
    // Callback
    private Runnable onCloseCallback;
    private Runnable onDeleteCallback;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        setupFormControls();
        showDetailsView();
    }

    private void setupFormControls() {
        // Setup genere combo box
        genereComboBox.getItems().addAll(Genere.values());
        genereComboBox.setCellFactory(listView -> new ListCell<Genere>() {
            @Override
            protected void updateItem(Genere item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(StringUtils.formatEnumName(item.name()));
                }
            }
        });
        genereComboBox.setButtonCell(new ListCell<Genere>() {
            @Override
            protected void updateItem(Genere item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(StringUtils.formatEnumName(item.name()));
                }
            }
        });

        // Setup stato combo box
        statoComboBox.getItems().addAll(StatoLettura.values());
        statoComboBox.setCellFactory(listView -> new ListCell<StatoLettura>() {
            @Override
            protected void updateItem(StatoLettura item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(StringUtils.formatEnumName(item.name()));
                }
            }
        });
        statoComboBox.setButtonCell(new ListCell<StatoLettura>() {
            @Override
            protected void updateItem(StatoLettura item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(StringUtils.formatEnumName(item.name()));
                }
            }
        });

        // Setup valutazione slider
        valutazioneSlider.setMin(0);
        valutazioneSlider.setMax(5);
        valutazioneSlider.setValue(0);
        valutazioneSlider.setMajorTickUnit(1);
        valutazioneSlider.setMinorTickCount(0);
        valutazioneSlider.setSnapToTicks(true);
        valutazioneSlider.setShowTickLabels(true);
        valutazioneSlider.setShowTickMarks(true);

        // Aggiorna la label quando cambia il valore
        valutazioneSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            int rating = newVal.intValue();
            if (rating == 0) {
                valutazioneLabel.setText("✘ No valutazione");
                return;
            }
            String stars = "★".repeat(rating) + "☆".repeat(5 - rating);
            valutazioneLabel.setText(rating + "/5 " + stars);
        });


        titoloField.textProperty().addListener((obs, oldText, newText) -> updateSaveButtonState());
        isbnField.textProperty().addListener((obs, oldText, newText) -> updateSaveButtonState());
        genereComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());
        statoComboBox.valueProperty().addListener((obs, oldVal, newVal) -> updateSaveButtonState());
    }

    private void updateSaveButtonState() {
        if (salvaButton != null) {
            try {
                Validator.validateLibro(createLibroFromForm());
                salvaButton.setOnAction(this::onSalvaClicked);
                salvaButton.setOpacity(1);
            } catch (ValidationException e) {
                salvaButton.setOnAction(ae -> {
                    Label messageLabel = new Label(e.getMessage());
                    messageLabel.setStyle("-fx-text-fill: red; -fx-padding: 5 0 0 0;");

                    // Aggiungi sotto il pulsante
                    int index = formView.getChildren().indexOf(salvaButton.getParent());
                    formView.getChildren().add(index + 1, messageLabel);

                    // Rimuovi dopo 2 secondi
                    PauseTransition pause = new PauseTransition(Duration.seconds(2));
                    pause.setOnFinished(ev -> formView.getChildren().remove(messageLabel));
                    pause.play();
                });
                salvaButton.setOpacity(0.5);
                salvaButton.setTooltip(new Tooltip(e.getMessage()));
            }
        }
    }

    // ============= MODALITÀ VISUALIZZAZIONE =============

    public void showDetailsView() {
        detailsView.setVisible(true);
        detailsView.setManaged(true);
        formView.setVisible(false);
        formView.setManaged(false);

        editButton.setVisible(true);
        deleteButton.setVisible(true);
        salvaButton.setVisible(false);

        isEditMode = false;
        isAddMode = false;
    }

    public void setLibro(Libro libro) {
        this.currentLibro = libro;
        showDetailsView();

        titoloLabel.setText(libro.getTitolo());
        autoreText.setText(libro.getAutore());
        isbnText.setText(libro.getIsbn());
        genereText.setText(StringUtils.formatEnumName(libro.getGenere() != null ? libro.getGenere().name() : ""));
        valutazioneText.setText("★".repeat(libro.getValutazione()));
        valutazioneText.setTextFill(Color.GOLD);
        statoText.setText(StringUtils.formatEnumName(libro.getStatoLettura().name()));
    }

    // ============= MODALITÀ EDIT =============

    public void showEditView() {
        showFormView();
        isEditMode = true;
        isAddMode = false;

        formTitleLabel.setText("Modifica Libro");

        // Popola il form con i dati esistenti
        if (currentLibro != null) {
            titoloField.setText(currentLibro.getTitolo());
            autoreField.setText(currentLibro.getAutore());
            isbnField.setText(currentLibro.getIsbn());
            isbnField.setDisable(true); // Non permettere modifica ISBN
            genereComboBox.setValue(currentLibro.getGenere());
            valutazioneSlider.setValue(currentLibro.getValutazione());
            statoComboBox.setValue(currentLibro.getStatoLettura());
        }

        editButton.setVisible(false);
        deleteButton.setVisible(false);
        salvaButton.setVisible(true);
    }

    // ============= MODALITÀ AGGIUNGI =============

    public void showAddView() {
        showFormView();
        isEditMode = false;
        isAddMode = true;
        currentLibro = null;

        formTitleLabel.setText("Nuovo Libro");

        // Pulisci il form
        titoloField.clear();
        autoreField.clear();
        isbnField.clear();
        isbnField.setDisable(false);
        valutazioneSlider.setValue(0);
        statoComboBox.setValue(StatoLettura.DA_LEGGERE); // Default

        editButton.setVisible(false);
        deleteButton.setVisible(false);
        salvaButton.setVisible(true);
    }

    private void showFormView() {
        detailsView.setVisible(false);
        detailsView.setManaged(false);
        formView.setVisible(true);
        formView.setManaged(true);

        updateSaveButtonState();
    }

    // ============= EVENT HANDLERS =============

    @FXML
    public void onEditClicked(ActionEvent actionEvent) {
        showEditView();
    }

    @FXML
    public void onDeleteClicked(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conferma eliminazione");
        alert.setHeaderText("Eliminare il libro?");
        alert.setContentText("Sei sicuro di voler eliminare \"" + currentLibro.getTitolo() + "\"?");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                commandHistory.executeCommand(new RemoveLibroCommand(libroService, currentLibro.getIsbn()));
                currentLibro = null;
            } catch (BookManagerException e) {
                showErrorAlert("Errore", "Impossibile eliminare il libro: " + e.getMessage());
            }
        }
        if (onDeleteCallback != null) {
            onDeleteCallback.run();
        }
    }

    @FXML
    public void onCloseClicked(ActionEvent actionEvent) {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    @FXML
    public void onSalvaClicked(ActionEvent actionEvent) {
        try {
            Libro libro = createLibroFromForm();

            if (isEditMode) {
                commandHistory.executeCommand(new UpdateLibroCommand(libroService, libro));
                currentLibro = libro;
                setLibro(libro);
            } else if (isAddMode) {
                commandHistory.executeCommand(new AddLibroCommand(libroService, libro));
                currentLibro = libro;
                setLibro(libro);
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }

        } catch (BookManagerException e) {
            showErrorAlert("Errore", e.getMessage());
        } catch (Exception e) {
            showErrorAlert("Errore", "Si è verificato un errore imprevisto: " + e.getMessage());
        }
    }


    // ============= CALLBACK SETTERS =============

    public void setOnCloseCallback(Runnable onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
    }

    public void setOnDeleteCallback(Runnable onDeleteCallback) {
        this.onDeleteCallback = onDeleteCallback;
    }

    public void setOnSaveCallback(Runnable onSaveCallback) {
        this.onSaveCallback = onSaveCallback;
    }

    // ============= UTILITY METHODS =============

    private Libro createLibroFromForm() {
        String titolo = titoloField.getText().trim();
        String autore = autoreField.getText() != null ? autoreField.getText().trim() : "";
        String isbn = isbnField.getText().trim();
        Genere genere = genereComboBox.getValue();
        int valutazione = (int) valutazioneSlider.getValue();
        StatoLettura stato = statoComboBox.getValue();

        return new Libro(titolo, autore, isbn, genere, valutazione, stato);
    }

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }


}
