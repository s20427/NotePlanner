package com.example;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.example.controller.MainController;

import java.io.IOException;

public class Main extends Application {

    private MainController mainController;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fxml/main.fxml"));
        BorderPane root = loader.load();

        primaryStage.setMinWidth(100);
        primaryStage.setMinHeight(50);

        primaryStage.setTitle("Note Planner");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();

        mainController = loader.getController();
    }

    @Override
    public void stop() throws Exception {
        if (mainController != null) {
            mainController.stop();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}