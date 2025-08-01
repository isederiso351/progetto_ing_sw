package com.bruno.bookmanager.view;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("WelcomeView.fxml"));
        Scene scene = new Scene(loader.load());
        primaryStage.setTitle("Book Manager");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
