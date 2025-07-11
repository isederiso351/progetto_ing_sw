package com.bruno.bookmanager.view;

import com.bruno.bookmanager.model.Libro;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

import static com.bruno.bookmanager.utils.StringUtils.formatEnumName;


public class BoxDetailsController {

    @FXML
    private Label titoloLabel;
    @FXML
    private Text autoreText, isbnText, genereText, valutazioneText, statoText;
    @FXML
    private Button editButton, deleteButton, closeButton;

    private Runnable onCloseCallback;

    public void setLibro(Libro libro) {
        titoloLabel.setText(libro.getTitolo());
        autoreText.setText(libro.getAutore());
        isbnText.setText(libro.getIsbn());
        genereText.setText(formatEnumName(libro.getGenere().name()));
        valutazioneText.setText(""+libro.getValutazione());
        statoText.setText(formatEnumName(libro.getStatoLettura().name()));
    }

    public void onEditClicked(ActionEvent actionEvent) {
    }

    public void onDeleteClicked(ActionEvent actionEvent) {
    }

    public void setOnCloseCallback(Runnable onCloseCallback) {
        this.onCloseCallback = onCloseCallback;
    }

    public void onCloseClicked(ActionEvent actionEvent) {
        if(onCloseCallback != null) {
            onCloseCallback.run();
        }
    }
}
