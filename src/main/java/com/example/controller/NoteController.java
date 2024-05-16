package com.example.controller;

import com.example.model.Note;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.util.ResourceBundle;

public class NoteController {

    @FXML
    private TextArea noteContentArea;

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
        noteContentArea.setPromptText(resources.getString("note.contentPlaceholder"));
    }

    @FXML
    private void handleSave() {
        String content = noteContentArea.getText();
        if (content != null && !content.trim().isEmpty()) {
            String title = content.split("\n", 2)[0];
            if (isEditMode) {
                note.setTitle(title);
                note.setContent(content);
                mainController.updateNote();
            } else {
                int newId = mainController.generateNewNoteId();
                mainController.addNote(new Note(newId, title, content, "", ""));
            }
            closeWindow();
        }
    }

    private void closeWindow() {
        Stage stage = (Stage) saveButton.getScene().getWindow();
        stage.close();
    }
}