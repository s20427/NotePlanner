package com.example.controller;

import com.example.model.Category;
import com.example.model.Note;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;

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
        }
    }

    @FXML
    private void handleSave() {
        String content = noteContentArea.getText();
        String tagsText = tagsField.getText();
        List<String> tags = Arrays.asList(tagsText.split(","));
        Category selectedCategory = categoryComboBox.getValue();

        if (content != null && !content.trim().isEmpty()) {
            String title = content.split("\n", 2)[0];
            if (isEditMode) {
                note.setTitle(title);
                note.setContent(content);
                note.setTags(tags);
                note.setCategory(selectedCategory);
                mainController.updateNote();
            } else {
                int newId = mainController.generateNewNoteId();
                Note newNote = new Note(newId, title, content, tagsText, selectedCategory);
                mainController.addNote(newNote);
            }
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}