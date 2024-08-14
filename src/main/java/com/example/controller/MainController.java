package com.example.controller;

import com.example.model.Category;
import com.example.service.DataStorage;
import com.example.model.Event;
import com.example.model.Note;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

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
    private BorderPane mainLayout;
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
    private List<CheckBox> categoryCheckboxes;
    private ScheduledExecutorService autosaveScheduler;


    private ResourceBundle loadBundle(Locale locale) {
        return ResourceBundle.getBundle("com.example.i18n.messages", locale);
    }

    @FXML
    private void initialize() {
        bundle = ResourceBundle.getBundle("com.example.i18n.messages", new Locale("pl"));

        notes = FXCollections.observableArrayList(DataStorage.loadNotes());
        events = FXCollections.observableArrayList(DataStorage.loadEvents());

        // Debug: Print out loaded data
        System.out.println("Loaded notes: " + notes.size());
        System.out.println("Loaded events: " + events.size());

        // Inicjalizacja funkcji Autosave
        startAutosave();
        addCategoryDisplay();

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
                            // The first line of the content is treated as the title
                            String[] lines = note.getContent().split("\n", 4);

                            // Create a Label for the title and apply bold styling
                            Label titleLabel = new Label(note.getTitle());
                            titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

                            // Build the remaining content after the title
                            StringBuilder remainingContent = new StringBuilder();
                            for (int i = 1; i < lines.length && i <= 2; i++) {
                                remainingContent.append(lines[i]).append("\n");
                            }

                            Label contentLabel = new Label(remainingContent.toString());
                            contentLabel.setStyle("-fx-font-size: 12px;");

                            Label tagsLabel = new Label(note.getTagsAsString());
                            tagsLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 10px;");

                            VBox coloredBox = new VBox();
                            coloredBox.setStyle("-fx-background-color: " + note.getCategory().getColor() + "; -fx-min-width: 5px;");

                            VBox contentBox = new VBox(titleLabel, contentLabel, tagsLabel);
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

    private void addCategoryDisplay() {
        HBox categoryBox = new HBox(20); // Create an HBox to hold all categories
        categoryBox.setStyle("-fx-padding: 10px; -fx-background-color: #f4f4f4;");
        categoryCheckboxes = new ArrayList<>();

        for (Category category : Category.values()) {
            // Create a custom circle for the category
            Circle colorCircle = new Circle(8);
            colorCircle.setFill(Color.web(category.getColor()));
            colorCircle.setStroke(Color.web(category.getColor())); // Initially, no outline

            // Create a checkbox for the category but without the default box
            CheckBox categoryCheckBox = new CheckBox();
            categoryCheckBox.setStyle("-fx-opacity: 0;"); // Hide the default checkbox
            categoryCheckBox.setSelected(true); // Default to selected

            // Bind the circle's fill and stroke based on the checkbox's selected state
            categoryCheckBox.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    colorCircle.setFill(Color.web(category.getColor())); // Filled when selected
                    colorCircle.setStroke(Color.web(category.getColor())); // No outline when selected
                } else {
                    colorCircle.setFill(Color.TRANSPARENT); // Transparent when not selected
                    colorCircle.setStroke(Color.web(category.getColor())); // Outline when not selected
                }
                filterNotesAndEvents(); // Apply the filter whenever a checkbox is toggled
            });

            // Create a label with the translated category name
            Label categoryLabel = new Label(category.getTranslatedName(bundle));
            categoryLabel.setStyle("-fx-font-size: 12px;");

            // Combine the circle and label into an HBox
            HBox itemBox = new HBox(5, colorCircle, categoryLabel);
            itemBox.setAlignment(Pos.CENTER_LEFT); // Align elements to the left

            // Ensure the itemBox grows to fill available space
            HBox.setHgrow(itemBox, Priority.ALWAYS);
            itemBox.setMaxWidth(Double.MAX_VALUE); // Ensure it takes up all available space

            // Center content within the itemBox
            itemBox.setStyle("-fx-alignment: center;");

            // Add the itemBox to the main category box
            categoryBox.getChildren().add(itemBox);

            // Add the invisible checkbox to the list for later reference
            categoryCheckboxes.add(categoryCheckBox);

            // Ensure clicking on the circle or label toggles the checkbox
            itemBox.setOnMouseClicked(event -> categoryCheckBox.setSelected(!categoryCheckBox.isSelected()));
        }

        // Add the HBox to the bottom of the BorderPane
        mainLayout.setBottom(categoryBox);
    }

    private void filterNotesAndEvents() {
        // Collect selected categories
        List<Category> selectedCategories = categoryCheckboxes.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> Category.values()[categoryCheckboxes.indexOf(cb)])
                .collect(Collectors.toList());

        // Filter notes by selected categories
        ObservableList<Note> filteredNotes = notes.filtered(note -> selectedCategories.contains(note.getCategory()));

        // Filter events by selected categories
        ObservableList<Event> filteredEvents = events.filtered(event -> selectedCategories.contains(event.getCategory()));

        // Display the filtered notes and events
        notesListView.setItems(filteredNotes);
        notesListView.refresh();

        // Update the calendar view with filtered events
        calendarController.setEvents(filteredEvents);
        calendarController.updateCalendarView(calendarController.getLastActiveView());
    }


    @FXML
    private void handleManualSave() {
        try {
            DataStorage.saveNotes(notes);
            DataStorage.saveEvents(events);
            showSaveConfirmation();
        } catch (IOException e) {
            e.printStackTrace();
            showSaveError();
        }
    }

    private void startAutosave() {
        autosaveScheduler = Executors.newScheduledThreadPool(1);
        autosaveScheduler.scheduleAtFixedRate(() -> {
            try {
                DataStorage.saveNotes(notes);
                DataStorage.saveEvents(events);
                System.out.println("Autosave executed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }, 5, 5, TimeUnit.MINUTES); // pierwsze uruchomienie po 5 minutach, a potem co 5 minut
    }

    // Usunięto @Override
    public void stop() throws Exception {
        if (autosaveScheduler != null && !autosaveScheduler.isShutdown()) {
            autosaveScheduler.shutdown();
            try {
                autosaveScheduler.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Zapisanie danych przy zamykaniu aplikacji
        try {
            DataStorage.saveNotes(notes);
            DataStorage.saveEvents(events);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void showSaveConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Zapisano");
        alert.setHeaderText(null);
        alert.setContentText("Dane zostały pomyślnie zapisane.");
        alert.showAndWait();
    }

    private void showSaveError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Błąd zapisu");
        alert.setHeaderText(null);
        alert.setContentText("Wystąpił błąd podczas zapisywania danych.");
        alert.showAndWait();
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