package com.example;

import com.example.presenter.MainPresenter;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {

    private MainPresenter mainPresenter;

    @Override
    public void start(Stage primaryStage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fxml/main.fxml"));
        BorderPane root = loader.load();

        primaryStage.setMinWidth(100);
        primaryStage.setMinHeight(50);

        primaryStage.setTitle("Note Planner");
        primaryStage.setScene(new Scene(root, 1200, 800));
        primaryStage.show();

        mainPresenter = loader.getController();
    }

    @Override
    public void stop() throws Exception {
        if (mainPresenter != null) {
            mainPresenter.stop();
        }
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}