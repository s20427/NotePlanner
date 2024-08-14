package com.example.controller;

import com.example.model.Category;
import com.example.model.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.Arrays;

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

        categoryComboBox.setCellFactory(comboBox -> new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTranslatedName(resources));
                }
            }
        });

        categoryComboBox.setButtonCell(new ListCell<Category>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getTranslatedName(resources));
                }
            }
        });

        startTimeComboBox.setPromptText(resources.getString("event.startTime"));
        endTimeComboBox.setPromptText(resources.getString("event.endTime"));
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            String title = titleField.getText();
            LocalDate date = datePicker.getValue();
            String startTime = startTimeComboBox.getValue();
            String endTime = endTimeComboBox.getValue();
            String tagsText = tagsField.getText();
            List<String> tags = Arrays.asList(tagsText.split(",")).stream().map(String::trim).collect(Collectors.toList());
            Category selectedCategory = categoryComboBox.getValue();

            String translatedCategory = selectedCategory.getTranslatedName(resources);
            if (!tags.contains(translatedCategory)) {
                tags.add(0, translatedCategory.toLowerCase());
            }

            LocalTime startLocalTime = LocalTime.parse(startTime);
            LocalTime endLocalTime = LocalTime.parse(endTime);

            if (title != null && date != null && startTime != null && endTime != null) {
                Event event = new Event(mainController.generateNewEventId(), title, date.atTime(startLocalTime), date.atTime(endLocalTime), descriptionArea.getText(), String.join(", ", tags), selectedCategory);
                event.setTags(tags);
                mainController.addEvent(event);
                closeWindow();
                mainController.refreshViews();
            }
        }
    }

    private boolean validateInput() {
        String title = titleField.getText();
        LocalDate date = datePicker.getValue();
        String startTime = startTimeComboBox.getValue();
        String endTime = endTimeComboBox.getValue();
        Category selectedCategory = categoryComboBox.getValue();

        if (title == null || title.trim().isEmpty()) {
            showValidationError("Title is required.");
            return false;
        }

        if (date == null) {
            showValidationError("Date must be selected.");
            return false;
        }

        if (startTime == null || endTime == null) {
            showValidationError("Both start and end time must be selected.");
            return false;
        }

        LocalTime startLocalTime = LocalTime.parse(startTime);
        LocalTime endLocalTime = LocalTime.parse(endTime);

        if (endLocalTime.isBefore(startLocalTime) || endLocalTime.equals(startLocalTime)) {
            showValidationError("End time must be after start time.");
            return false;
        }

        if (selectedCategory == null) {
            showValidationError("Category must be selected.");
            return false;
        }

        return true;
    }

    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Validation Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}