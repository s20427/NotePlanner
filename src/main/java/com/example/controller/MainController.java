package com.example.controller;

import com.example.model.Category;
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
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private CalendarController calendarController;
    private LocalDate currentDate;
    private ObservableList<Note> notes;
    private ObservableList<Event> events;
    private AtomicInteger noteIdCounter = new AtomicInteger(4);
    private AtomicInteger eventIdCounter = new AtomicInteger(1);
    private ResourceBundle bundle;

    private ResourceBundle loadBundle(Locale locale) {
        return ResourceBundle.getBundle("com.example.i18n.messages", locale);
    }

    @FXML
    private void initialize() {
        bundle = ResourceBundle.getBundle("com.example.i18n.messages", new Locale("pl"));

        events = FXCollections.observableArrayList();

        calendarController = new CalendarController();
        calendarController.setMainController(this);
        calendarController.setCalendarView(calendarView);
        calendarController.setCurrentDate(LocalDate.now());
        calendarController.setEvents(events);
        calendarController.setViewSelector(viewSelector, bundle);

        languageSelector.setItems(FXCollections.observableArrayList("Polski", "English"));
        languageSelector.setValue("Polski");
        languageSelector.setOnAction(event -> changeLanguage());

        filterOptions.setItems(FXCollections.observableArrayList(
                bundle.getString("filter.all"),
                bundle.getString("filter.title"),
                bundle.getString("filter.description"),
                bundle.getString("filter.category"),
                bundle.getString("filter.tags")
        ));
        filterOptions.setValue(bundle.getString("filter.all"));

        filterOptions.valueProperty().addListener((observable, oldValue, newValue) -> handleSearch());

        updateTexts();

        notes = FXCollections.observableArrayList(
                new Note(1, "Note 1", "This is the first note", "work, urgent", Category.WORK),
                new Note(2, "Shopping List", "Buy milk and bread", "home, shopping", Category.HOME)
        );

        events = FXCollections.observableArrayList(
                new Event(1, "Meeting with Bob", LocalDateTime.of(2024, 8, 14, 0, 30), LocalDateTime.of(2024, 8, 14, 2, 30), "Discuss project details", "work, important", Category.WORK),
                new Event(2, "Dentist Appointment", LocalDateTime.of(2024, 8, 15, 1, 0), LocalDateTime.of(2024, 8, 15, 1, 30), "Regular check-up", "health", Category.PRIVATE)
        );

        notesListView.setItems(notes);
        calendarController.setEvents(events);
        calendarController.updateCalendarView(CalendarView.MONTH);
        notesListView.setCellFactory(new Callback<ListView<Note>, ListCell<Note>>() {
            @Override
            public ListCell<Note> call(ListView<Note> listView) {
                return new ListCell<Note>() {
                    @Override
                    protected void updateItem(Note note, boolean empty) {
                        super.updateItem(note, empty);
                        if (empty || note == null) {
                            setText(null);
                            setGraphic(null);
                        } else {
                            String[] lines = note.getContent().split("\n", 4);
                            StringBuilder displayText = new StringBuilder(note.getTitle());
                            for (int i = 1; i < lines.length && i <= 2; i++) {
                                displayText.append("\n").append(lines[i]);
                            }

                            Label textLabel = new Label(displayText.toString());
                            textLabel.setStyle("-fx-padding: 5px;");

                            Label tagsLabel = new Label(note.getTagsAsString());
                            tagsLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");

                            VBox coloredBox = new VBox();
                            coloredBox.setStyle("-fx-background-color: " + note.getCategory().getColor() + "; -fx-min-width: 5px;");

                            VBox contentBox = new VBox(textLabel, tagsLabel);
                            contentBox.setSpacing(5);

                            HBox hBox = new HBox(coloredBox, contentBox);
                            hBox.setSpacing(10);

                            setGraphic(hBox);
                        }
                    }
                };
            }
        });

        currentDate = LocalDate.now();

        calendarController.setViewSelector(viewSelector, bundle);
        updateCalendarView(calendarController.getLastActiveView());

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
    }

    private void changeLanguage() {
        String selectedLanguage = languageSelector.getValue();
        Locale locale = selectedLanguage.equals("Polski") ? new Locale("pl") : new Locale("en");
        bundle = loadBundle(locale);

        // Debugowanie
        System.out.println("Notatki po zmianie języka: " + notes.size());
        System.out.println("Wydarzenia po zmianie języka: " + events.size());

        notesListView.setItems(notes);
        notesListView.refresh();
        updateTexts();
        updateFilterOptions();
        refreshViews();

        calendarController.updateButtonLabels(bundle);

        viewSelector.setItems(FXCollections.observableArrayList(
                bundle.getString("calendar.month"),
                bundle.getString("calendar.week"),
                bundle.getString("calendar.day")
        ));

        viewSelector.setValue(bundle.getString(calendarController.getLastActiveView().getResourceKey()));

        CalendarView selectedView = CalendarView.fromLocalizedName(viewSelector.getValue(), bundle);
        if (selectedView != null) {
            calendarController.updateCalendarView(selectedView);
        }
    }

    public void refreshViews() {
        // Odśwież ListView z notatkami
        notesListView.refresh();

        // Odśwież widok kalendarza
        updateCalendarView(calendarController.getLastActiveView());
    }

    private void updateTexts() {
        searchField.setPromptText(bundle.getString("note.searchPlaceholder"));

        addNoteButton.setText(bundle.getString("note.addButton"));

        viewSelector.setItems(FXCollections.observableArrayList(
                bundle.getString("calendar.month"),
                bundle.getString("calendar.week"),
                bundle.getString("calendar.day")
        ));
        viewSelector.setValue(bundle.getString(calendarController.getLastActiveView().getResourceKey()));

        viewSelector.setOnAction(event -> {
            CalendarView selectedView = CalendarView.fromLocalizedName(viewSelector.getValue(), bundle);
            if (selectedView != null) {
                calendarController.updateCalendarView(selectedView);
            }
        });
    }

    private void updateFilterOptions() {
        filterOptions.getItems().clear();
        // Pobierz aktualne wartości z bundle
        String allOption = bundle.getString("filter.all");
        String titleOption = bundle.getString("filter.title");
        String descriptionOption = bundle.getString("filter.description");
        String categoryOption = bundle.getString("filter.category");
        String tagsOption = bundle.getString("filter.tags");

        // Aktualizuj ComboBox z nowymi wartościami
        filterOptions.setItems(FXCollections.observableArrayList(
                allOption,
                titleOption,
                descriptionOption,
                categoryOption,
                tagsOption
        ));

        // Jeśli poprzednia opcja istnieje w nowym języku, ustaw ją; jeśli nie, ustaw domyślną
        if (filterOptions.getValue() != null && filterOptions.getItems().contains(filterOptions.getValue())) {
            filterOptions.setValue(filterOptions.getValue());
        } else {
            filterOptions.setValue(allOption); // Domyślna wartość
        }
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

    public void addEvent(Event event) {
        events.add(event);
        handleSearch();
    }

    public int generateNewEventId() {
        return eventIdCounter.getAndIncrement();
    }

    public void addNote(Note note) {
        notes.add(note);
        notesListView.setItems(notes);
        notesListView.refresh();
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
        String filterOption = filterOptions.getValue();
        ObservableList<Note> filteredNotes = FXCollections.observableArrayList();
        ObservableList<Event> filteredEvents = FXCollections.observableArrayList();

        String allOption = bundle.getString("filter.all");
        String titleOption = bundle.getString("filter.title");
        String descriptionOption = bundle.getString("filter.description");
        String categoryOption = bundle.getString("filter.category");
        String tagsOption = bundle.getString("filter.tags");

        if (filterOption == null || searchText == null) {
            // Jeśli opcja filtrowania lub tekst wyszukiwania jest null, zakończ metodę.
            return;
        }

        for (Note note : notes) {
            boolean matches = false;
            String translatedCategory = note.getCategory().getTranslatedName(bundle).toLowerCase();

            if (filterOption.equals(allOption)) {
                matches = note.getTitle().toLowerCase().contains(searchText) ||
                        note.getContent().toLowerCase().contains(searchText) ||
                        translatedCategory.contains(searchText) ||
                        note.getTagsAsString().toLowerCase().contains(searchText);
            } else if (filterOption.equals(titleOption)) {
                matches = note.getTitle().toLowerCase().contains(searchText);
            } else if (filterOption.equals(descriptionOption)) {
                matches = note.getContent().toLowerCase().contains(searchText);
            } else if (filterOption.equals(categoryOption)) {
                matches = translatedCategory.contains(searchText);
            } else if (filterOption.equals(tagsOption)) {
                matches = note.getTagsAsString().toLowerCase().contains(searchText);
            }
            if (matches) {
                filteredNotes.add(note);
            }
        }

        for (Event event : events) {
            boolean matches = false;
            String translatedCategory = event.getCategory().getTranslatedName(bundle).toLowerCase();

            if (filterOption.equals(allOption)) {
                matches = event.getTitle().toLowerCase().contains(searchText) ||
                        event.getDescription().toLowerCase().contains(searchText) ||
                        translatedCategory.contains(searchText) ||
                        event.getTagsAsString().toLowerCase().contains(searchText);
            } else if (filterOption.equals(titleOption)) {
                matches = event.getTitle().toLowerCase().contains(searchText);
            } else if (filterOption.equals(descriptionOption)) {
                matches = event.getDescription().toLowerCase().contains(searchText);
            } else if (filterOption.equals(categoryOption)) {
                matches = translatedCategory.contains(searchText);
            } else if (filterOption.equals(tagsOption)) {
                matches = event.getTagsAsString().toLowerCase().contains(searchText);
            }
            if (matches) {
                filteredEvents.add(event);
            }
        }

        // Aktualizacja listy notatek
        notesListView.setItems(filteredNotes);

        // Aktualizacja widoku kalendarza na podstawie filtrowanych wydarzeń
        calendarController.setEvents(filteredEvents);
        calendarController.updateCalendarView(calendarController.getLastActiveView());
    }

    private void updateCalendarView(CalendarView selectedView) {
        calendarController.updateCalendarView(selectedView);
    }
}