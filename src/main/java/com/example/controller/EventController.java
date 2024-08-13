package com.example.controller;

import com.example.model.Category;
import com.example.model.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

public class EventController {

    @FXML
    private TextField titleField;
    @FXML
    private DatePicker datePicker;
    @FXML
    private ComboBox<String> startTimeComboBox;
    @FXML
    private ComboBox<String> endTimeComboBox;
    @FXML
    private TextArea descriptionArea;
    @FXML
    private TextField tagsField;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private Button saveButton;

    private MainController mainController;

    @FXML
    private ResourceBundle resources;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    private void initialize() {
        ObservableList<String> timeOptions = FXCollections.observableArrayList();
        for (int hour = 0; hour < 24; hour++) {
            timeOptions.add(String.format("%02d:00", hour));
            timeOptions.add(String.format("%02d:30", hour));
        }
        startTimeComboBox.setItems(timeOptions);
        endTimeComboBox.setItems(timeOptions);

        categoryComboBox.setItems(FXCollections.observableArrayList(Category.values()));

        startTimeComboBox.setPromptText(resources.getString("event.startTime"));
        endTimeComboBox.setPromptText(resources.getString("event.endTime"));
    }

    @FXML
    private void handleSave() {
        String title = titleField.getText();
        LocalDate date = datePicker.getValue();
        String startTime = startTimeComboBox.getValue();
        String endTime = endTimeComboBox.getValue();
        String tagsText = tagsField.getText();
        List<String> tags = Arrays.asList(tagsText.split(","));
        Category selectedCategory = categoryComboBox.getValue();

        if (title != null && date != null && startTime != null && endTime != null) {
            LocalTime startLocalTime = LocalTime.parse(startTime);
            LocalTime endLocalTime = LocalTime.parse(endTime);

            LocalDateTime startDateTime = LocalDateTime.of(date, startLocalTime);
            LocalDateTime endDateTime = LocalDateTime.of(date, endLocalTime);

            String description = descriptionArea.getText();

            Event event = new Event(mainController.generateNewEventId(), title, startDateTime, endDateTime, description, tagsText, selectedCategory);
            event.setTags(tags);
            mainController.addEvent(event);
            closeWindow();
        } else {
            showAlert(resources.getString("event.errorTitle"), resources.getString("event.errorMessage"));
        }
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}