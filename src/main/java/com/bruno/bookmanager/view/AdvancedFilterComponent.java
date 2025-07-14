package com.bruno.bookmanager.view;

import com.bruno.bookmanager.filters.Filter;
import com.bruno.bookmanager.filters.GenereFilter;
import com.bruno.bookmanager.filters.StatoLetturaFilter;
import com.bruno.bookmanager.filters.ValutazioneFilter;
import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import com.bruno.bookmanager.utils.StringUtils;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Componente per filtri avanzati multi-selezione.
 */
public class AdvancedFilterComponent {

    private final MenuButton menuButton;
    private final VBox contentBox;
    private final ScrollPane scrollPane;

    // Set per tenere traccia delle selezioni
    private final Set<StatoLettura> selectedStati = new HashSet<>();
    private final Set<Integer> selectedValutazioni = new HashSet<>();
    private final Set<Genere> selectedGeneri = new HashSet<>();

    // Callback per notificare cambiamenti
    private Runnable onFilterChangeCallback;

    public AdvancedFilterComponent(MenuButton menuButton) {
        this.menuButton = menuButton;
        this.contentBox = new VBox(5);
        this.scrollPane = new ScrollPane(contentBox);

        setupScrollPane();
        setupFilterContent();
        setupCustomMenuItem();
        updateButtonText();
    }

    private void setupScrollPane() {
        scrollPane.setFitToWidth(true);
        scrollPane.setMaxHeight(400);
        scrollPane.setMinHeight(300);
        scrollPane.setPrefWidth(280);
        scrollPane.getStyleClass().add("filter-scroll-pane");
    }

    private void setupFilterContent() {
        contentBox.getStyleClass().add("filter-content");


        addSection("📚 Stato Lettura", createStatoLetturaFilters());

        addSeparator();

        addSection("⭐ Valutazione", createValutazioneFilters());

        addSeparator();

        addSection("📖 Genere", createGenereFilters());
    }

    private void addSection(String title, List<CheckBox> checkBoxes) {
        Label sectionLabel = new Label(title);
        contentBox.getChildren().add(sectionLabel);

        VBox sectionBox = new VBox(3);
        sectionBox.setStyle("-fx-padding: 5 0 10 15;");
        sectionBox.getChildren().addAll(checkBoxes);
        contentBox.getChildren().add(sectionBox);
    }

    private void addSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 5 0;");
        contentBox.getChildren().add(separator);
    }

    private List<CheckBox> createStatoLetturaFilters() {
        List<CheckBox> checkBoxes = new ArrayList<>();

        for (StatoLettura stato : StatoLettura.values()) {
            CheckBox checkBox = new CheckBox(StringUtils.formatEnumName(stato.name()));

            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    selectedStati.add(stato);
                } else {
                    selectedStati.remove(stato);
                }
                notifyFilterChange();
            });

            checkBoxes.add(checkBox);
        }

        return checkBoxes;
    }

    private List<CheckBox> createValutazioneFilters() {
        List<CheckBox> checkBoxes = new ArrayList<>();

        // Non valutato (0 stelle)
        CheckBox zeroStars = new CheckBox("❌ Non valutato");
        zeroStars.getStyleClass().add("zero-stars");
        zeroStars.setOnAction(e -> {
            if (zeroStars.isSelected()) {
                selectedValutazioni.add(0);
            } else {
                selectedValutazioni.remove(0);
            }
            notifyFilterChange();
        });
        checkBoxes.add(zeroStars);

        // 1-5 stelle
        for (int i = 1; i <= 5; i++) {
            final int rating = i;
            String stars = "⭐".repeat(i);
            CheckBox checkBox = new CheckBox(stars + " " + i + " stell" + (i > 1 ? "e" : "a"));

            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    selectedValutazioni.add(rating);
                } else {
                    selectedValutazioni.remove(rating);
                }
                notifyFilterChange();
            });

            checkBoxes.add(checkBox);
        }

        return checkBoxes;
    }

    private List<CheckBox> createGenereFilters() {
        List<CheckBox> checkBoxes = new ArrayList<>();

        for (Genere genere : Genere.values()) {
            CheckBox checkBox = new CheckBox(StringUtils.formatEnumName(genere.name()));

            checkBox.setOnAction(e -> {
                if (checkBox.isSelected()) {
                    selectedGeneri.add(genere);
                } else {
                    selectedGeneri.remove(genere);
                }
                notifyFilterChange();
            });

            checkBoxes.add(checkBox);
        }

        return checkBoxes;
    }

    private void setupCustomMenuItem() {
        CustomMenuItem customMenuItem = new CustomMenuItem(scrollPane);
        customMenuItem.setHideOnClick(false);

        menuButton.getItems().clear();
        menuButton.getItems().add(customMenuItem);
        menuButton.getStyleClass().add("filter");


    }

    public void clearAll() {
        contentBox.getChildren().forEach(node -> {
            if (node instanceof VBox vbox) {
                vbox.getChildren().forEach(child -> {
                    if (child instanceof CheckBox checkBox) {
                        checkBox.setSelected(false);
                    }
                });
            }
        });

        // Pulisci le selezioni
        selectedStati.clear();
        selectedValutazioni.clear();
        selectedGeneri.clear();

        notifyFilterChange();
    }

    private void notifyFilterChange() {
        updateButtonText();
        if (onFilterChangeCallback != null) {
            onFilterChangeCallback.run();
        }
    }

    private void updateButtonText() {
        int totalSelected = selectedStati.size() + selectedValutazioni.size() + selectedGeneri.size();

        if (totalSelected == 0) {
            menuButton.setText("");
        } else {
            menuButton.setText("Filtri (" + totalSelected + ")");
        }
    }

    /**
     * Costruisce il filtro combinato basato sulle selezioni correnti.
     */
    public Filter<Libro> buildCombinedFilter() {
        Filter<Libro> combinedFilter = null;

        // Filtro stati lettura
        if (!selectedStati.isEmpty()) {
            Filter<Libro> statoFilter = null;
            for (StatoLettura stato : selectedStati) {
                Filter<Libro> singleFilter = new StatoLetturaFilter(stato);
                statoFilter = (statoFilter == null) ? singleFilter : statoFilter.or(singleFilter);
            }
            combinedFilter = statoFilter;
        }

        // Filtro valutazioni
        if (!selectedValutazioni.isEmpty()) {
            Filter<Libro> valutazioneFilter = null;
            for (Integer valutazione : selectedValutazioni) {
                Filter<Libro> singleFilter = new ValutazioneFilter(valutazione);
                valutazioneFilter = (valutazioneFilter == null) ? singleFilter : valutazioneFilter.or(singleFilter);
            }
            combinedFilter = (combinedFilter == null) ? valutazioneFilter : combinedFilter.and(valutazioneFilter);
        }

        // Filtro generi
        if (!selectedGeneri.isEmpty()) {
            Filter<Libro> genereFilter = null;
            for (Genere genere : selectedGeneri) {
                Filter<Libro> singleFilter = new GenereFilter(genere);
                genereFilter = (genereFilter == null) ? singleFilter : genereFilter.or(singleFilter);
            }
            combinedFilter = (combinedFilter == null) ? genereFilter : combinedFilter.and(genereFilter);
        }

        return combinedFilter;
    }

    /**
     * Verifica se ci sono filtri attivi.
     */
    public boolean hasActiveFilters() {
        return !selectedStati.isEmpty() || !selectedValutazioni.isEmpty() || !selectedGeneri.isEmpty();
    }

    /**
     * Imposta il callback per notificare i cambiamenti.
     */
    public void setOnFilterChangeCallback(Runnable callback) {
        this.onFilterChangeCallback = callback;
    }
}
