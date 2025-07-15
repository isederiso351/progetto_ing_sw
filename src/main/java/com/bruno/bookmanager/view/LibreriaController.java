package com.bruno.bookmanager.view;

import com.bruno.bookmanager.command.CommandHistory;
import com.bruno.bookmanager.exception.BookManagerException;
import com.bruno.bookmanager.filters.*;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import com.bruno.bookmanager.service.LibroService;
import com.bruno.bookmanager.utils.StringUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.io.IOException;
import java.util.List;

public class LibreriaController {

    // Services and controllers
    private final LibroService libroService = LibroService.getInstance();
    private final CommandHistory commandHistory = CommandHistory.getInstance();
    // FXML elements
    @FXML
    private FlowPane booksFlowPane;
    @FXML
    private StackPane detailsPlaceholder;
    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> searchTypeComboBox;
    @FXML
    private MenuButton filtersMenuButton;
    @FXML
    private ComboBox<String> sortComboBox;
    @FXML
    private CheckBox sortOrderCheckBox;
    @FXML
    private Button undoButton;
    @FXML
    private Button redoButton;
    @FXML
    private Button addBookButton;
    private UnifiedBookPanelController panelController;
    private AdvancedFilterComponent advancedFilter;
    private List<Libro> currentBooks;

    @FXML
    public void initialize() {
        booksFlowPane.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            }
        });
        setupControls();
        setupEventListeners();
        loadBooks();
        updateUndoRedoButtons();
    }

    private void setupControls() {
        // Setup search type combo
        searchTypeComboBox.getItems().addAll("Titolo", "Autore", "ISBN");
        searchTypeComboBox.setValue("Titolo");

        // Setup advanced filter component
        advancedFilter = new AdvancedFilterComponent(filtersMenuButton);
        advancedFilter.setOnFilterChangeCallback(this::applyFiltersAndSearch);

        // Setup sort combo
        sortComboBox.getItems().addAll("Titolo", "Autore", "Valutazione", "Genere", "Stato");
        sortComboBox.setValue("Titolo");
        sortOrderCheckBox.setText("Decrescente");
        sortOrderCheckBox.setSelected(false); // Default: crescente
    }

    private void setupEventListeners() {

        searchField.textProperty().addListener((obs, oldText, newText) -> applyFiltersAndSearch());
        searchTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSearch());
        sortComboBox.valueProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSearch());
        sortOrderCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> applyFiltersAndSearch());
    }

    private void loadBooks() {
        try {
            currentBooks = libroService.getAllLibri();
            applyFiltersAndSearch();
        } catch (BookManagerException e) {
            showErrorAlert("Errore", "Impossibile caricare i libri: " + e.getMessage());
            currentBooks = List.of(); // Lista vuota come fallback
            displayBooks(currentBooks);
        }
    }

    private void applyFiltersAndSearch() {
        try {
            SearchCriteria.Builder criteriaBuilder = new SearchCriteria.Builder();

            // Costruisci il filtro combinato
            Filter<Libro> combinedFilter = buildCombinedFilter();
            if (combinedFilter != null) {
                criteriaBuilder.filter(combinedFilter);
            }

            // Applica ordinamento
            SearchCriteria.SortField sortField = getSortFieldFromCombo();
            boolean ascending = !sortOrderCheckBox.isSelected(); // Checkbox = decrescente
            criteriaBuilder.sortBy(sortField, ascending);

            SearchCriteria criteria = criteriaBuilder.build();
            List<Libro> books = libroService.search(criteria);

            currentBooks = books;
            displayBooks(books);

        } catch (BookManagerException e) {
            showErrorAlert("Errore", "Errore durante il filtro: " + e.getMessage());
        }
    }

    private Filter<Libro> buildCombinedFilter() {
        Filter<Libro> filter = null;

        // Filtro di ricerca testuale
        String searchText = searchField.getText();
        if (searchText != null && !searchText.trim().isEmpty()) {
            String searchType = searchTypeComboBox.getValue();
            Filter<Libro> textFilter = switch (searchType) {
                case "Titolo" -> new TitoloFilter(searchText);
                case "Autore" -> new AutoreFilter(searchText);
                case "ISBN" -> new ISBNFilter(searchText);
                default -> new TitoloFilter(searchText);
            };
            filter = textFilter;
        }

        // Filtro avanzato dal componente
        Filter<Libro> advancedFilterResult = advancedFilter.buildCombinedFilter();
        if (advancedFilterResult != null) {
            filter = (filter == null) ? advancedFilterResult : filter.and(advancedFilterResult);
        }

        return filter;
    }

    private SearchCriteria.SortField getSortFieldFromCombo() {
        String sortBy = sortComboBox.getValue();
        return switch (sortBy) {
            case "Autore" -> SearchCriteria.SortField.AUTORE;
            case "Valutazione" -> SearchCriteria.SortField.VALUTAZIONE;
            case "Genere" -> SearchCriteria.SortField.GENERE;
            case "Stato" -> SearchCriteria.SortField.STATO;
            default -> SearchCriteria.SortField.TITOLO;
        };
    }

    private void displayBooks(List<Libro> libri) {
        booksFlowPane.getChildren().clear();

        if (libri.isEmpty()) {
            Label noBooks = new Label("Nessun libro presente nella libreria");
            noBooks.getStyleClass().add("no-books");
            booksFlowPane.getChildren().add(noBooks);
            return;
        }

        for (Libro libro : libri) {
            Node bookCard = createBookCard(libro);
            bookCard.setOnMouseClicked(e -> showBookDetails(libro));
            booksFlowPane.getChildren().add(bookCard);
        }
    }

    private VBox createBookCard(Libro libro) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefSize(180, 150);
        card.getStyleClass().add("book-card");

        Label titleLabel = new Label(libro.getTitolo());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setTooltip(new Tooltip(libro.getTitolo()));
        titleLabel.setMaxWidth(150);

        Label authorLabel = new Label("");
        if (libro.getAutore() != null && !libro.getAutore().isEmpty()) {
            authorLabel.setText("di " + libro.getAutore());
            authorLabel.setFont(Font.font("Arial", 11));
            authorLabel.setTextFill(Color.GRAY);
            authorLabel.setWrapText(true);
            authorLabel.setTextAlignment(TextAlignment.CENTER);
            authorLabel.setMaxWidth(150);
        }

        HBox starsBox = new HBox(2);
        starsBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < libro.getValutazione(); i++) {
            Label star = new Label("★");
            star.setTextFill(Color.GOLD);
            star.setFont(Font.font(12));
            starsBox.getChildren().add(star);
        }

        // Indicatore stato lettura
        Label statoLabel = new Label(getStatusEmoji(libro.getStatoLettura()));
        statoLabel.setFont(Font.font(14));
        statoLabel.setTooltip(new Tooltip(StringUtils.formatEnumName(libro.getStatoLettura().name())));

        card.getChildren().addAll(titleLabel, authorLabel, starsBox, statoLabel);
        return card;
    }

    private String getStatusEmoji(StatoLettura stato) {
        return switch (stato) {
            case DA_LEGGERE -> "📚";
            case IN_LETTURA -> "📖";
            case LETTO -> "✅";
        };
    }

    private boolean loadBookPanel() {
        if (detailsPlaceholder.getChildren().isEmpty()) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("UnifiedBookPanelView.fxml"));
                Parent bookPanel = loader.load();
                panelController = loader.getController();
                panelController.setOnDeleteCallback(this::handleDeleteFromPanel);
                panelController.setOnCloseCallback(this::hideBoxDetails);
                panelController.setOnSaveCallback(this::handleSaveFromPanel);
                detailsPlaceholder.getChildren().add(bookPanel);
            } catch (IOException e) {
                showErrorAlert("Errore", "Impossibile caricare il pannello del libro " + e);
                return false;
            }
        }
        return true;
    }


    private void showBookDetails(Libro libro) {
        if (!loadBookPanel()) return;
        detailsPlaceholder.setVisible(true);
        detailsPlaceholder.setManaged(true);

        panelController.setLibro(libro);
        panelController.setOnCloseCallback(this::hideBoxDetails);
    }

    public void hideBoxDetails() {
        detailsPlaceholder.setVisible(false);
        detailsPlaceholder.setManaged(false);
    }

    private void updateUndoRedoButtons() {
        undoButton.setDisable(!commandHistory.canUndo());
        redoButton.setDisable(!commandHistory.canRedo());

        // Aggiorna tooltip con descrizione operazione
        if (commandHistory.canUndo()) {
            undoButton.setTooltip(new Tooltip("Annulla: " + commandHistory.getLastCommandDescription()));
        } else {
            undoButton.setTooltip(new Tooltip("Nessuna operazione da annullare"));
        }

        if (commandHistory.canRedo()) {
            redoButton.setTooltip(new Tooltip("Ripeti: " + commandHistory.getNextRedoCommandDescription()));
        } else {
            redoButton.setTooltip(new Tooltip("Nessuna operazione da ripetere"));
        }
    }

    //=======================Handlers===============================

    @FXML
    public void handleClearFilters(ActionEvent actionEvent) {
        searchField.clear();
        searchTypeComboBox.setValue("Titolo");
        advancedFilter.clearAll();
        sortComboBox.setValue("Titolo");
        sortOrderCheckBox.setSelected(false);
        // applyFiltersAndSearch() verrà chiamato automaticamente dai listener
    }

    @FXML
    public void handleUndo(ActionEvent actionEvent) {
        try {
            hideBoxDetails();
            commandHistory.undo();
            applyFiltersAndSearch();
            updateUndoRedoButtons();

        } catch (BookManagerException e) {
            showErrorAlert("Errore", "Impossibile annullare: " + e.getMessage());
        }
    }

    @FXML
    public void handleRedo(ActionEvent actionEvent) {
        try {
            hideBoxDetails();
            commandHistory.redo();
            applyFiltersAndSearch();
            updateUndoRedoButtons();
        } catch (BookManagerException e) {
            showErrorAlert("Errore", "Impossibile ripetere: " + e.getMessage());
        }
    }

    private void handleDeleteFromPanel() {
        hideBoxDetails();
        applyFiltersAndSearch();
        updateUndoRedoButtons();
    }

    private void handleSaveFromPanel() {
        applyFiltersAndSearch();
        updateUndoRedoButtons();
    }

    public void handleAddBook(ActionEvent actionEvent) {
        if (!loadBookPanel()) return;
        detailsPlaceholder.setVisible(true);
        detailsPlaceholder.setManaged(true);

        panelController.showAddView();
    }

    // ============= UTILITY METHODS =============

    private void showErrorAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
