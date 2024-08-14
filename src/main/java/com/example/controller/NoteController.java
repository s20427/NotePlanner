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
    private TextArea noteContentArea;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private TextField tagsField;

    @FXML
    private Button saveButton;

    private MainController mainController;
    private Note note;
    private boolean isEditMode = false;

    @FXML
    private ResourceBundle resources;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public void setNoteContent(Note note) {
        this.note = note;
        this.noteContentArea.setText(note.getContent());
        this.tagsField.setText(note.getTagsAsString());
        this.categoryComboBox.setValue(note.getCategory());
        this.isEditMode = true;
    }

    public void setEditMode(boolean isEditMode) {
        this.isEditMode = isEditMode;
        if (isEditMode) {
            saveButton.setText(resources.getString("note.saveButton"));
        } else {
            saveButton.setText(resources.getString("note.addButton"));
        }
    }

    @FXML
    private void initialize() {
        if (resources != null) {
            noteContentArea.setPromptText(resources.getString("note.contentPlaceholder"));
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
        }
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            String content = noteContentArea.getText();
            String tagsText = tagsField.getText();
            List<String> tags = Arrays.asList(tagsText.split(",")).stream().map(String::trim).collect(Collectors.toList());
            Category selectedCategory = categoryComboBox.getValue();

            String translatedCategory = selectedCategory.getTranslatedName(resources);
            if (selectedCategory != null && !tags.contains(translatedCategory)) {
                tags.add(0, translatedCategory.toLowerCase());
            }

            String title = content.split("\n", 2)[0];
            if (isEditMode) {
                note.setTitle(title);
                note.setContent(content);
                note.setTags(tags);
                note.setCategory(selectedCategory);
                mainController.updateNote();
            } else {
                int newId = mainController.generateNewNoteId();
                Note newNote = new Note(newId, title, content, String.join(", ", tags), selectedCategory);
                mainController.addNote(newNote);
            }
            mainController.refreshViews();
            closeWindow();
        }
    }

    private boolean validateInput() {
        String content = noteContentArea.getText();
        Category selectedCategory = categoryComboBox.getValue();

        if (content == null || content.trim().isEmpty()) {
            showValidationError("Content is required.");
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