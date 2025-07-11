package com.bruno.bookmanager.view;

import com.bruno.bookmanager.model.Genere;
import com.bruno.bookmanager.model.Libro;
import com.bruno.bookmanager.model.StatoLettura;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.io.IOException;
import java.util.List;

public class LibreriaController {

    @FXML
    private FlowPane booksFlowPane;

    @FXML
    private StackPane detailsPlaceholder;

    private BoxDetailsController boxController;

    public void initialize() {
        List<Libro> libri = List.of(new Libro("Titolo Molto lungo per vedere come reagisce", "Autore 1", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO), new Libro("Titolo 2", "Autore 2", "123456789", Genere.ROMANZO, 1, StatoLettura.LETTO), new Libro("Titolo 1", "Autore 1", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO), new Libro("Titolo 2", "Autore 2", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO), new Libro("Titolo 1", "Autore 1", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO), new Libro("Titolo 2", "Autore 2", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO), new Libro("Titolo 1", "Autore 1", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO), new Libro("Titolo 2", "Autore 2", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO), new Libro("Titolo 1", "Autore 1", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO), new Libro("Titolo 2", "Autore 2", "123456789", Genere.ROMANZO, 5, StatoLettura.LETTO));

        for (Libro libro : libri) {
            Node bookCard = createBookCard(libro);
            bookCard.setOnMouseClicked(e -> showBoxDetails(libro));
            booksFlowPane.getChildren().add(bookCard);
        }
    }

    private VBox createBookCard(Libro libro) {
        VBox card = new VBox(10); // spacing tra titolo e autore
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefSize(150, 120);
        card.setStyle("-fx-background-color: white;" + "-fx-background-radius: 10;" + "-fx-border-radius: 10;" + "-fx-border-color: #000000;" + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 10, 0.8, 2, 2);");

        Label titleLabel = new Label(libro.getTitolo());
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        titleLabel.setWrapText(true);
        titleLabel.setTextAlignment(TextAlignment.CENTER);
        titleLabel.setTooltip(new Tooltip(libro.getTitolo()));

        Label authorLabel = new Label("di " + libro.getAutore());
        authorLabel.setFont(Font.font("Arial", 12));
        authorLabel.setTextFill(Color.GRAY);
        authorLabel.setWrapText(true);
        authorLabel.setTextAlignment(TextAlignment.CENTER);

        HBox starsBox = new HBox(2);
        starsBox.setAlignment(Pos.CENTER);
        for (int i = 0; i < libro.getValutazione(); i++) {
            Label star = new Label("★");
            star.setTextFill(Color.GOLD);
            star.setFont(Font.font(14));
            starsBox.getChildren().add(star);
        }

        card.getChildren().addAll(titleLabel, authorLabel, starsBox);
        return card;
    }

    private void showBoxDetails(Libro libro){
        if (detailsPlaceholder.getChildren().isEmpty()) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("BoxDetailsView.fxml"));

            Parent libroDetails = null;
            try {
                libroDetails = loader.load();
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            boxController = loader.getController();
            detailsPlaceholder.getChildren().add(libroDetails);
        }
        detailsPlaceholder.setVisible(true);
        detailsPlaceholder.setManaged(true);

        boxController.setLibro(libro);
        boxController.setOnCloseCallback(this::hideBoxDetails);
    }

    public void hideBoxDetails() {
        detailsPlaceholder.setVisible(false);
        detailsPlaceholder.setManaged(false);
    }

}
