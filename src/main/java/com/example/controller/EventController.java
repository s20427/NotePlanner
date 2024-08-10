package com.example.controller;

import com.example.model.Event;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class EventController {

    @FXML
    private TextField titleField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private TextField timeField;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField tagsField;
    @FXML
    private Button saveButton;

    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText();
        LocalDate date = datePicker.getValue();
        String time = timeField.getText();
        LocalDateTime dateTime = LocalDateTime.of(date, LocalTime.parse(time));
        String description = descriptionArea.getText();
        String tags = tagsField.getText();

        Event event = new Event(mainController.generateNewEventId(), title, dateTime, description, tags, "");
        mainController.addEvent(event);
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}

