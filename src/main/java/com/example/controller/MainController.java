package com.example.controller;

import com.example.model.Event;
import com.example.model.Note;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;

public class MainController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterOptions;
    @FXML
    private ListView<Note> notesListView;
    @FXML
    private ComboBox<String> languageSelector;
    @FXML
    private ComboBox<String> viewSelector;
    @FXML
    private BorderPane calendarView;
    @FXML
    private VBox notesPanel;
    @FXML
    private Button addNoteButton;
    @FXML
    private Button addEventButton; // Nowy przycisk do dodawania wydarzeń
    @FXML
    private Button previousButton;
    @FXML
    private Button nextButton;

    private CalendarController calendarController;
    private LocalDate currentDate;
    private ObservableList<Note> notes;
    private ObservableList<Event> events; // Lista wydarzeń
    private AtomicInteger noteIdCounter = new AtomicInteger(4); // Assuming there are 3 sample notes initially
    private AtomicInteger eventIdCounter = new AtomicInteger(1); // Inicjalizujemy dla wydarzeń
    private ResourceBundle bundle;

    private ResourceBundle loadBundle(Locale locale) {
        return ResourceBundle.getBundle("com.example.i18n.messages", locale);
    }

    @FXML
    private void initialize() {
        bundle = loadBundle(new Locale("pl"));

        languageSelector.setItems(FXCollections.observableArrayList("Polski", "English"));
        languageSelector.setValue("Polski");
        languageSelector.setOnAction(event -> changeLanguage());

        updateTexts();

        notes = FXCollections.observableArrayList(
                new Note(1, "Note 1", "Note 1\nContent 1\nLine 2\nLine 3", "", ""),
                new Note(2, "Note 2", "Note 2\nContent 2\nLine 2\nLine 3", "", ""),
                new Note(3, "Note 3", "Note 3\nContent 3\nLine 2\nLine 3", "", "")
        );
        events = FXCollections.observableArrayList(); // Inicjalizujemy listę wydarzeń
        notesListView.setItems(notes);
        notesListView.setCellFactory(new Callback<ListView<Note>, ListCell<Note>>() {
            @Override
            public ListCell<Note> call(ListView<Note> listView) {
                return new ListCell<Note>() {
                    @Override
                    protected void updateItem(Note note, boolean empty) {
                        super.updateItem(note, empty);
                        if (empty || note == null) {
                            setText(null);
                        } else {
                            String[] lines = note.getContent().split("\n", 4);
                            StringBuilder displayText = new StringBuilder(note.getTitle());
                            for (int i = 1; i < lines.length && i <= 2; i++) {
                                displayText.append("\n").append(lines[i]);
                            }
                            setText(displayText.toString());
                        }
                    }
                };
            }
        });

        currentDate = LocalDate.now();

        calendarController = new CalendarController();
        calendarController.setCalendarView(calendarView);
        calendarController.setCurrentDate(currentDate);
        calendarController.setViewSelector(viewSelector, bundle);
        calendarController.setEvents(events); // Przekazujemy listę wydarzeń do kontrolera kalendarza
        updateCalendarView(viewSelector.getValue());

        VBox.setVgrow(notesListView, Priority.ALWAYS);
        VBox.setVgrow(calendarView, Priority.ALWAYS);

        notesListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Note selectedNote = notesListView.getSelectionModel().getSelectedItem();
                if (selectedNote != null) {
                    openNoteWindow(selectedNote, true);
                }
            }
        });

        addEventButton.setOnAction(event -> openAddEventWindow()); // Obsługa dodawania wydarzeń
    }

    private void changeLanguage() {
        String selectedLanguage = languageSelector.getValue();
        Locale locale = selectedLanguage.equals("Polski") ? new Locale("pl") : new Locale("en");
        bundle = loadBundle(locale);
        updateTexts();
        updateCalendarView(viewSelector.getValue());
    }

    private void updateTexts() {
        searchField.setPromptText(bundle.getString("note.searchPlaceholder"));
        filterOptions.setItems(FXCollections.observableArrayList(bundle.getString("note.filterOptions").split(",")));
        filterOptions.setValue(bundle.getString("note.filterOptions").split(",")[0]);

        addNoteButton.setText(bundle.getString("note.addButton"));
        previousButton.setText(bundle.getString("note.previousButton"));
        nextButton.setText(bundle.getString("note.nextButton"));
        addEventButton.setText(bundle.getString("event.addButton")); // Dodajemy tekst do przycisku wydarzeń

        viewSelector.setItems(FXCollections.observableArrayList(
                bundle.getString("calendar.month"),
                bundle.getString("calendar.week"),
                bundle.getString("calendar.day")
        ));
        viewSelector.setValue(bundle.getString("calendar.month"));
    }

    @FXML
    private void openAddNoteWindow() {
        Note newNote = new Note();
        openNoteWindow(newNote, false);
    }

    private void openNoteWindow(Note note, boolean isEditMode) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fxml/note.fxml"), bundle);
            Parent root = loader.load();

            NoteController noteController = loader.getController();
            noteController.setMainController(this);
            noteController.setEditMode(isEditMode);
            if (isEditMode) {
                noteController.setNoteContent(note);
            }

            Stage stage = new Stage();
            stage.setTitle(isEditMode ? bundle.getString("note.editTitle") : bundle.getString("note.title"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(notesPanel.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void openAddEventWindow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fxml/event.fxml"), bundle);
            Parent root = loader.load();

            EventController eventController = loader.getController();
            eventController.setMainController(this);

            Stage stage = new Stage();
            stage.setTitle(bundle.getString("event.title"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(notesPanel.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addEvent(Event event) {
        events.add(event);
        updateCalendarView(viewSelector.getValue());
    }

    public int generateNewEventId() {
        return eventIdCounter.getAndIncrement();
    }

    public void addNote(Note note) {
        notes.add(note);
    }

    public void updateNote() {
        notesListView.refresh();
    }

    public int generateNewNoteId() {
        return noteIdCounter.getAndIncrement();
    }

    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        ObservableList<Note> filteredNotes = FXCollections.observableArrayList();
        ObservableList<Event> filteredEvents = FXCollections.observableArrayList();

        for (Note note : notes) {
            if (note.getTitle().toLowerCase().contains(searchText) || note.getContent().toLowerCase().contains(searchText)) {
                filteredNotes.add(note);
            }
        }

        for (Event event : events) {
            if (event.getTitle().toLowerCase().contains(searchText) || event.getDescription().toLowerCase().contains(searchText)) {
                filteredEvents.add(event);
            }
        }

        notesListView.setItems(filteredNotes);
        // Możesz również zaktualizować widok kalendarza, aby wyświetlał tylko znalezione wydarzenia
        calendarController.updateCalendarView(viewSelector.getValue());
    }

    @FXML
    private void handlePrevious() {
        calendarController.handlePrevious();
    }

    @FXML
    private void handleNext() {
        calendarController.handleNext();
    }

    private void updateCalendarView(String selectedView) {
        calendarController.updateCalendarView(selectedView);
    }
}
