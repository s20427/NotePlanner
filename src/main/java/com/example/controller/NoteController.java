package com.example.controller;

import com.example.model.Category;
import com.example.model.Note;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NoteController {

    @FXML
    private TextArea noteContentArea; // Text area for note content

    @FXML
    private ComboBox<Category> categoryComboBox; // ComboBox for selecting note category

    @FXML
    private TextField tagsField; // TextField for entering tags

    @FXML
    private Button saveButton; // Button to save the note

    @FXML
    private Button deleteButton; // Button to delete the note

    @FXML
    private Label categoryLabel; // Label for category ComboBox

    @FXML
    private ResourceBundle resources; // ResourceBundle for localized strings

    private MainController mainController; // Reference to the MainController
    private Note note; // The note being edited or created
    private boolean isEditMode = false; // Flag to indicate if we are in edit mode

    /**
     * Sets the MainController instance.
     * @param mainController The main controller.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Sets the content of the note in the UI fields for editing.
     * @param note The note to be edited.
     */
    public void setNoteContent(Note note) {
        this.note = note;
        this.noteContentArea.setText(note.getContent());
        this.tagsField.setText(note.getTagsAsString());
        this.categoryComboBox.setValue(note.getCategory());
        this.isEditMode = true;
        deleteButton.setVisible(true); // Show the delete button when editing an existing note
    }

    /**
     * Configures the UI for either edit mode or add mode.
     * @param isEditMode True if editing an existing note, false if adding a new note.
     */
    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        if (isEditMode) {
            saveButton.setText(resources.getString("note.saveButton"));
        } else {
            saveButton.setText(resources.getString("note.addButton"));
            deleteButton.setVisible(false); // Hide the delete button when adding a new note
        }
    }

    /**
     * Initializes the UI components and sets up localization.
     */
    @FXML
    private void initialize() {
        // Set placeholders and labels based on the resource bundle
        noteContentArea.setPromptText(resources.getString("note.contentPlaceholder"));
        tagsField.setPromptText(resources.getString("note.tagsPlaceholder"));
        categoryLabel.setText(resources.getString("note.categoryLabel"));

        // Populate the category comboBox with categories
        categoryComboBox.setItems(FXCollections.observableArrayList(Category.values()));

        // Configure the ComboBox cells to display localized category names
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
     * Handles saving the note. Validates the input and either updates an existing note or creates a new one.
     */
    @FXML
    private void handleSave() {
        if (validateInput()) {
            String content = noteContentArea.getText();
            String tagsText = tagsField.getText();
            List<String> tags = Arrays.stream(tagsText.split(","))
                    .map(String::trim)
                    .collect(Collectors.toList());
            Category selectedCategory = categoryComboBox.getValue();

            String translatedCategory = selectedCategory.getTranslatedName(resources);
            if (!tags.contains(translatedCategory)) {
                tags.add(0, translatedCategory.toLowerCase());
            }

            if (isEditMode) {
                note.setContent(content);
                note.setTags(tags);
                note.setCategory(selectedCategory);
                mainController.updateNote();
            } else {
                Note newNote = new Note(content, content, String.join(", ", tags), selectedCategory);
                mainController.addNote(newNote);
            }
            mainController.refreshViews();
            closeWindow();
        }
    }

    /**
     * Validates the note input to ensure that necessary fields are filled.
     * @return True if validation passes, false otherwise.
     */
    private boolean validateInput() {
        String content = noteContentArea.getText();
        Category selectedCategory = categoryComboBox.getValue();

        if (content == null || content.trim().isEmpty()) {
            showValidationError(resources.getString("validation.contentRequired"));
            return false;
        }

        if (selectedCategory == null) {
            showValidationError(resources.getString("validation.categoryRequired"));
            return false;
        }

        return true;
    }

    /**
     * Handles deleting the note after user confirmation.
     */
    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(resources.getString("note.deleteConfirmationTitle"));
        alert.setHeaderText(null);
        alert.setContentText(resources.getString("note.deleteConfirmationText"));

        ButtonType deleteButtonType = new ButtonType(resources.getString("button.delete"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(resources.getString("button.cancel"), ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(deleteButtonType, cancelButtonType);

        alert.showAndWait().ifPresent(type -> {
            if (type == deleteButtonType) {
                mainController.deleteNote(note);
                closeWindow();
                mainController.refreshViews();
            }
        });
    }

    /**
     * Displays a validation error message in an alert dialog.
     * @param message The validation error message.
     */
    private void showValidationError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(resources.getString("validation.errorTitle"));
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Closes the note editing window.
     */
    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}