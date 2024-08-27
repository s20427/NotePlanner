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
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

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
    private Label categoryLabel;
    @FXML
    private ComboBox<Category> categoryComboBox;
    @FXML
    private Button saveButton;
    @FXML
    private Button deleteButton;

    @FXML
    private ResourceBundle resources;

    private MainController mainController;
    private Event event;
    private boolean isEditMode = false;

    /**
     * Sets the main controller to link to the rest of the application.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Initializes the controller, setting up combo boxes, category dropdown, and text prompts.
     */
    @FXML
    private void initialize() {
        setupTimeComboBoxes();
        setupCategoryComboBox();
        configureFieldPrompts();
    }

    /**
     * Sets the event content when editing an existing event.
     */
    public void setEventContent(Event event) {
        this.event = event;
        titleField.setText(event.getTitle());
        datePicker.setValue(event.getDateTime().toLocalDate());
        startTimeComboBox.setValue(event.getDateTime().toLocalTime().toString());
        endTimeComboBox.setValue(event.getEndDateTime().toLocalTime().toString());
        descriptionArea.setText(event.getDescription());
        tagsField.setText(event.getTagsAsString());
        categoryComboBox.setValue(event.getCategory());
        isEditMode = true;
        deleteButton.setVisible(true);
    }

    /**
     * Sets the edit mode, determining if the form is for creating a new event or editing an existing one.
     */
    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        if (isEditMode) {
            saveButton.setText(resources.getString("event.saveButton"));
            deleteButton.setText(resources.getString("button.delete"));
        } else {
            saveButton.setText(resources.getString("event.addButton"));
            deleteButton.setVisible(false);
        }
    }

    /**
     * Handles saving the event (either creating a new one or editing an existing one).
     */
    @FXML
    private void handleSave() {
        if (validateInput()) {
            saveOrUpdateEvent();
            closeWindow();
            mainController.refreshViews();
        }
    }

    /**
     * Handles deleting the event.
     */
    @FXML
    private void handleDelete() {
        confirmAndDeleteEvent();
    }

    /**
     * Closes the event editing window.
     */
    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }

    /**
     * Sets up time options for start and end time combo boxes.
     */
    private void setupTimeComboBoxes() {
        ObservableList<String> timeOptions = FXCollections.observableArrayList();
        for (int hour = 0; hour < 24; hour++) {
            timeOptions.add(String.format("%02d:00", hour));
            timeOptions.add(String.format("%02d:30", hour));
        }
        startTimeComboBox.setItems(timeOptions);
        endTimeComboBox.setItems(timeOptions);
    }

    /**
     * Sets up the category combo box with translated category names.
     */
    private void setupCategoryComboBox() {
        categoryComboBox.setItems(FXCollections.observableArrayList(Category.values()));
        categoryComboBox.setCellFactory(comboBox -> new ListCell<>() {
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

        categoryComboBox.setButtonCell(new ListCell<>() {
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
    }

    /**
     * Configures field prompts for user input fields.
     */
    private void configureFieldPrompts() {
        titleField.setPromptText(resources.getString("event.titlePlaceholder"));
        descriptionArea.setPromptText(resources.getString("event.descriptionPlaceholder"));
        tagsField.setPromptText(resources.getString("event.tagsPlaceholder"));
        startTimeComboBox.setPromptText(resources.getString("event.startTime"));
        endTimeComboBox.setPromptText(resources.getString("event.endTime"));
        categoryLabel.setText(resources.getString("event.categoryLabel"));
        saveButton.setText(resources.getString("event.addButton"));
    }

    /**
     * Saves or updates the event based on the mode (edit or new).
     */
    private void saveOrUpdateEvent() {
        String title = titleField.getText();
        LocalDate date = datePicker.getValue();
        LocalTime startLocalTime = LocalTime.parse(startTimeComboBox.getValue());
        LocalTime endLocalTime = LocalTime.parse(endTimeComboBox.getValue());
        String description = descriptionArea.getText();
        List<String> tags = Arrays.stream(tagsField.getText().split(","))
                .map(String::trim)
                .collect(Collectors.toList());
        Category selectedCategory = categoryComboBox.getValue();
        String translatedCategory = selectedCategory.getTranslatedName(resources);

        // Ensure the translated category is included in the tags
        if (!tags.contains(translatedCategory)) {
            tags.add(0, translatedCategory.toLowerCase());
        }

        // Update the event if in edit mode, otherwise create a new event
        if (isEditMode && event != null) {
            event.setTitle(title);
            event.setDateTime(date.atTime(startLocalTime));
            event.setEndDateTime(date.atTime(endLocalTime));
            event.setDescription(description);
            event.setTags(tags);
            event.setCategory(selectedCategory);
            mainController.updateEvent();
        } else {
            Event newEvent = new Event(title, date.atTime(startLocalTime), date.atTime(endLocalTime), description, String.join(", ", tags), selectedCategory);
            mainController.addEvent(newEvent);
        }
    }

    /**
     * Validates input fields before saving.
     */
    private boolean validateInput() {
        String title = titleField.getText();
        LocalDate date = datePicker.getValue();
        String startTime = startTimeComboBox.getValue();
        String endTime = endTimeComboBox.getValue();
        Category selectedCategory = categoryComboBox.getValue();

        if (isInputInvalid(title, date, startTime, endTime, selectedCategory)) {
            return false;
        }

        LocalTime startLocalTime = LocalTime.parse(startTime);
        LocalTime endLocalTime = LocalTime.parse(endTime);

        if (endLocalTime.isBefore(startLocalTime) || endLocalTime.equals(startLocalTime)) {
            showValidationError(resources.getString("validation.endTimeBeforeStartTime"));
            return false;
        }

        return true;
    }

    /**
     * Checks if any input field is invalid.
     */
    private boolean isInputInvalid(String title, LocalDate date, String startTime, String endTime, Category selectedCategory) {
        if (title == null || title.trim().isEmpty()) {
            showValidationError(resources.getString("validation.titleRequired"));
            return true;
        }

        if (date == null) {
            showValidationError(resources.getString("validation.dateRequired"));
            return true;
        }

        if (startTime == null || endTime == null) {
            showValidationError(resources.getString("validation.timeRequired"));
            return true;
        }

        if (selectedCategory == null) {
            showValidationError(resources.getString("validation.categoryRequired"));
            return true;
        }

        return false;
    }

    /**
     * Shows a validation error alert with a specified message.
     */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(resources.getString("validation.errorTitle"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Confirms and deletes the event if confirmed.
     */
    private void confirmAndDeleteEvent() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resources.getString("event.deleteConfirmationTitle"));
        alert.setHeaderText(null);
        alert.setContentText(resources.getString("event.deleteConfirmationText"));

        ButtonType deleteButtonType = new ButtonType(resources.getString("button.delete"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(resources.getString("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        alert.showAndWait().ifPresent(type -> {
            if (type == deleteButtonType) {
                mainController.deleteEvent(event);
                closeWindow();
                mainController.refreshViews();
            }
        });
    }
}