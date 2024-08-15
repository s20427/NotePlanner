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

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class MainController {

    @FXML
    private TextField searchField;
    @FXML
    private ComboBox<String> filterOptions;
    @FXML
    private Button saveButton;
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
    @FXML
    private Button moveUpButton;
    @FXML
    private Button moveDownButton;

    private CalendarController calendarController;
    private ObservableList<Note> notes;
    private ObservableList<Event> events;
    private ResourceBundle bundle;
    private List<CheckBox> categoryCheckboxes;
    private ScheduledExecutorService autosaveScheduler;

    /**
     * Loads the appropriate resource bundle based on the locale.
     */
    private ResourceBundle loadBundle(Locale locale) {
        return ResourceBundle.getBundle("com.example.i18n.messages", locale);
    }

    /**
     * Initializes the controller after the root element has been completely processed.
     */
    @FXML
    private void initialize() {
        bundle = ResourceBundle.getBundle("com.example.i18n.messages", new Locale("pl"));

        // Load notes and events from storage
        notes = FXCollections.observableArrayList(DataStorage.loadNotes());
        events = FXCollections.observableArrayList(DataStorage.loadEvents());

        // Start the autosave functionality
        startAutosave();

        // Add category display for filtering
        addCategoryDisplay();

        // Initialize the calendar controller and configure its settings
        initializeCalendarController();

        // Set up language selector and its event handler
        setupLanguageSelector();

        // Set up filter options for the search functionality
        setupFilterOptions();

        // Set up notes list view
        setupNotesListView();

        // Set up move buttons and their initial state
        setupMoveButtons();

        // Update texts in the UI
        updateTexts();
        updateCategoryDisplay();
    }

    /**
     * Initializes the calendar controller and configures its settings.
     */
    private void initializeCalendarController() {
        calendarController = new CalendarController();
        calendarController.setMainController(this);
        calendarController.setCalendarView(calendarView);
        calendarController.setCurrentDate(LocalDate.now());
        calendarController.setEvents(events);
        calendarController.setViewSelector(viewSelector, bundle);
        calendarController.updateCalendarView(CalendarView.MONTH);
    }

    /**
     * Sets up the language selector and its event handler.
     */
    private void setupLanguageSelector() {
        languageSelector.setItems(FXCollections.observableArrayList("Polski", "English"));
        languageSelector.setValue("Polski");
        languageSelector.setOnAction(event -> changeLanguage());
    }

    /**
     * Sets up filter options for the search functionality.
     */
    private void setupFilterOptions() {
        filterOptions.setItems(FXCollections.observableArrayList(
                bundle.getString("filter.all"),
                bundle.getString("filter.title"),
                bundle.getString("filter.description"),
                bundle.getString("filter.category"),
                bundle.getString("filter.tags")
        ));
        filterOptions.setValue(bundle.getString("filter.all"));
        filterOptions.valueProperty().addListener((observable, oldValue, newValue) -> handleSearch());
    }

    /**
     * Sets up the notes list view, including its cell factory and event handlers.
     */
    private void setupNotesListView() {
        notesListView.setItems(notes);
        notesListView.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Note note, boolean empty) {
                super.updateItem(note, empty);
                if (empty || note == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    String[] lines = note.getContent().split("\n", 4);
                    Label titleLabel = new Label(note.getTitle());
                    titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

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
        });

        // Handle double-click to open a note for editing
        notesListView.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                Note selectedNote = notesListView.getSelectionModel().getSelectedItem();
                if (selectedNote != null) {
                    openNoteWindow(selectedNote, true);
                }
            }
        });

        VBox.setVgrow(notesListView, Priority.ALWAYS);
        VBox.setVgrow(calendarView, Priority.ALWAYS);
    }

    /**
     * Sets up the move up and move down buttons, including their initial states and event handlers.
     */
    private void setupMoveButtons() {
        moveUpButton.setOnAction(event -> moveSelectedNoteUp());
        moveDownButton.setOnAction(event -> moveSelectedNoteDown());

        moveUpButton.setDisable(true);
        moveDownButton.setDisable(true);

        // Update move buttons state based on note selection
        notesListView.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            boolean noSelection = newSelection == null;
            updateMoveButtonsState(noSelection);
        });
    }

    /**
     * Moves the selected note down in the list.
     */
    public void moveSelectedNoteDown() {
        int selectedIndex = notesListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1 || selectedIndex == notes.size() - 1) {
            return; // No valid selection or already at the bottom
        }

        Note note = notes.remove(selectedIndex);
        notes.add(selectedIndex + 1, note);
        notesListView.getSelectionModel().select(selectedIndex + 1);
    }

    /**
     * Moves the selected note up in the list.
     */
    public void moveSelectedNoteUp() {
        int selectedIndex = notesListView.getSelectionModel().getSelectedIndex();
        if (selectedIndex == -1 || selectedIndex == 0) {
            return; // No valid selection or already at the top
        }

        Note note = notes.remove(selectedIndex);
        notes.add(selectedIndex - 1, note);
        notesListView.getSelectionModel().select(selectedIndex - 1);
    }

    /**
     * Updates the state of the move buttons based on selection and search status.
     */
    private void updateMoveButtonsState(boolean noSelection) {
        boolean isSearching = !searchField.getText().trim().isEmpty();

        moveUpButton.setDisable(noSelection || isSearching || notesListView.getSelectionModel().getSelectedIndex() <= 0);
        moveDownButton.setDisable(noSelection || isSearching || notesListView.getSelectionModel().getSelectedIndex() >= notesListView.getItems().size() - 1);
    }

    /**
     * Adds category display at the bottom of the main layout for filtering.
     */
    private void addCategoryDisplay() {
        HBox categoryBox = new HBox(20); // Create an HBox to hold all categories
        categoryBox.setStyle("-fx-padding: 10px; -fx-background-color: #f4f4f4;");
        categoryCheckboxes = new ArrayList<>();

        for (Category category : Category.values()) {
            addCategoryToDisplay(category, categoryBox);
        }

        mainLayout.setBottom(categoryBox);
    }

    /**
     * Adds a single category to the display at the bottom of the main layout.
     */
    private void addCategoryToDisplay(Category category, HBox categoryBox) {
        setCategoryDisplay(categoryBox, category);
    }

    /**
     * Filters notes and events based on the selected categories.
     */
    private void filterNotesAndEvents() {
        List<Category> selectedCategories = categoryCheckboxes.stream()
                .filter(CheckBox::isSelected)
                .map(cb -> Category.values()[categoryCheckboxes.indexOf(cb)])
                .toList();

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

    /**
     * Handles manual save action.
     */
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

    /**
     * Starts the autosave functionality.
     */
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
        }, 5, 5, TimeUnit.MINUTES); // Start after 5 minutes, repeat every 5 minutes
    }

    /**
     * Stops the autosave functionality and saves data before application shutdown.
     */
    public void stop() {
        if (autosaveScheduler != null && !autosaveScheduler.isShutdown()) {
            autosaveScheduler.shutdown();
            try {
                autosaveScheduler.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            DataStorage.saveNotes(notes);
            DataStorage.saveEvents(events);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Displays a confirmation alert after a successful save.
     */
    private void showSaveConfirmation() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(bundle.getString("save.successTitle"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString("save.successText"));
        alert.showAndWait();
    }

    /**
     * Displays an error alert if saving data fails.
     */
    private void showSaveError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(bundle.getString("save.failedTitle"));
        alert.setHeaderText(null);
        alert.setContentText(bundle.getString("save.failedText"));
        alert.showAndWait();
    }

    /**
     * Handles language change.
     */
    private void changeLanguage() {
        CalendarView selectedView = CalendarView.fromLocalizedName(viewSelector.getValue(), bundle);
        String selectedLanguage = languageSelector.getValue();
        Locale locale = selectedLanguage.equals("Polski") ? new Locale("pl") : new Locale("en");
        bundle = loadBundle(locale);

        notesListView.refresh();


        calendarController.setResources(bundle);
        calendarController.updateButtonLabels(bundle);
        calendarController.setEvents(events); // Rebind events to ensure translation is applied
        calendarController.updateDateInfoLabel(bundle);
        refreshViews();

        viewSelector.setItems(FXCollections.observableArrayList(
                bundle.getString("calendar.month"),
                bundle.getString("calendar.week"),
                bundle.getString("calendar.day")
        ));

        calendarController.updateCalendarView(selectedView);
        if (selectedView != null) {
            calendarController.updateCalendarView(calendarController.getLastActiveView());
        }

        updateTexts();
        updateFilterOptions();
        updateCategoryDisplay();
    }

    /**
     * Updates the category display with the new language
     */
    private void updateCategoryDisplay() {
        HBox categoryBox = new HBox(20); // Create an HBox to hold all categories
        categoryBox.setStyle("-fx-padding: 10px; -fx-background-color: #f4f4f4;");
        categoryCheckboxes = new ArrayList<>(); // Reset the checkboxes list

        for (Category category : Category.values()) {
            setCategoryDisplay(categoryBox, category);
        }

        // Replace the current category display with the updated one
        mainLayout.setBottom(categoryBox);
    }

    private void setCategoryDisplay(HBox categoryBox, Category category) {
        Circle colorCircle = new Circle(8);
        colorCircle.setFill(Color.web(category.getColor()));
        colorCircle.setStroke(Color.web(category.getColor())); // Initially, no outline

        CheckBox categoryCheckBox = new CheckBox();
        categoryCheckBox.setStyle("-fx-opacity: 0;"); // Hide the default checkbox
        categoryCheckBox.setSelected(true); // Default to selected

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

        Label categoryLabel = new Label(category.getTranslatedName(bundle));
        categoryLabel.setStyle("-fx-font-size: 12px;");

        HBox itemBox = new HBox(5, colorCircle, categoryLabel);
        itemBox.setAlignment(Pos.CENTER_LEFT); // Align elements to the left
        HBox.setHgrow(itemBox, Priority.ALWAYS);
        itemBox.setMaxWidth(Double.MAX_VALUE); // Ensure it takes up all available space
        itemBox.setStyle("-fx-alignment: center;");

        categoryBox.getChildren().add(itemBox);
        categoryCheckboxes.add(categoryCheckBox);
        itemBox.setOnMouseClicked(event -> categoryCheckBox.setSelected(!categoryCheckBox.isSelected()));
    }

    /**
     * Refreshes the views after any update.
     */
    public void refreshViews() {
        notesListView.refresh();
        calendarController.updateCalendarView(calendarController.getLastActiveView());
    }

    /**
     * Updates the text fields and labels based on the selected language.
     */
    private void updateTexts() {
        searchField.setPromptText(bundle.getString("note.searchPlaceholder"));
        addNoteButton.setText(bundle.getString("note.addButton"));
        saveButton.setText(bundle.getString("save.buttonText"));

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

    /**
     * Updates the filter options based on the selected language.
     */
    private void updateFilterOptions() {
        filterOptions.getItems().clear();
        filterOptions.setItems(FXCollections.observableArrayList(
                bundle.getString("filter.all"),
                bundle.getString("filter.title"),
                bundle.getString("filter.description"),
                bundle.getString("filter.category"),
                bundle.getString("filter.tags")
        ));
        if (filterOptions.getValue() != null && filterOptions.getItems().contains(filterOptions.getValue())) {
            filterOptions.setValue(filterOptions.getValue());
        } else {
            filterOptions.setValue(bundle.getString("filter.all")); // Default value
        }
    }

    /**
     * Opens a window for adding a new note.
     */
    @FXML
    private void openAddNoteWindow() {
        Note newNote = new Note();
        openNoteWindow(newNote, false);
    }

    /**
     * Opens a window for editing an existing note or creating a new one.
     */
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

    /**
     * Adds a new event to the list and applies the search filter if active.
     */
    public void addEvent(Event event) {
        events.add(event);
        handleSearch();
    }

    /**
     * Adds a new note to the list and refreshes the list view.
     */
    public void addNote(Note note) {
        notes.add(note);
        notesListView.setItems(notes);
        notesListView.refresh();
    }

    /**
     * Updates the note in the list and refreshes the list view.
     */
    public void updateNote() {
        notesListView.refresh();
    }

    /**
     * Updates the calendar view.
     */
    public void updateEvent() {
        calendarController.updateCalendarView(calendarController.getLastActiveView());
    }

    /**
     * Handles search functionality, filtering notes and events based on user input.
     */
    @FXML
    private void handleSearch() {
        String searchText = searchField.getText().toLowerCase();
        boolean isSearching = !searchText.trim().isEmpty();
        String filterOption = filterOptions.getValue();
        ObservableList<Note> filteredNotes = FXCollections.observableArrayList();
        ObservableList<Event> filteredEvents = FXCollections.observableArrayList();

        if (filterOption == null) {
            return; // Exit if filter option or search text is null
        }

        applySearchFilterToNotes(searchText, filterOption, filteredNotes);
        applySearchFilterToEvents(searchText, filterOption, filteredEvents);

        // Update the ListView with filtered or original notes based on the search status
        notesListView.setItems(isSearching ? filteredNotes : notes);
        notesListView.refresh();

        // Update the CalendarView with filtered or original events based on the search status
        calendarController.setEvents(filteredEvents);
        calendarController.updateCalendarView(calendarController.getLastActiveView());

        // Update move buttons state based on search status and selected index
        updateMoveButtonsState(isSearching);
    }

    /**
     * Applies search filtering to notes.
     */
    private void applySearchFilterToNotes(String searchText, String filterOption, ObservableList<Note> filteredNotes) {
        String allOption = bundle.getString("filter.all");
        String titleOption = bundle.getString("filter.title");
        String descriptionOption = bundle.getString("filter.description");
        String categoryOption = bundle.getString("filter.category");
        String tagsOption = bundle.getString("filter.tags");

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
    }

    /**
     * Applies search filtering to events.
     */
    private void applySearchFilterToEvents(String searchText, String filterOption, ObservableList<Event> filteredEvents) {
        String allOption = bundle.getString("filter.all");
        String titleOption = bundle.getString("filter.title");
        String descriptionOption = bundle.getString("filter.description");
        String categoryOption = bundle.getString("filter.category");
        String tagsOption = bundle.getString("filter.tags");

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
    }

    /**
     * Updates the calendar view with the selected view.
     */
    private void updateCalendarView(CalendarView selectedView) {
        calendarController.updateCalendarView(selectedView);
    }

    /**
     * Deletes a note from the list and refreshes the ListView.
     */
    public void deleteNote(Note note) {
        if (notes.contains(note)) {
            notes.remove(note);
            notesListView.setItems(notes);
            notesListView.refresh();  // Ensure the ListView is updated after deletion
        }
    }

    /**
     * Deletes an event and refreshes the views.
     */
    public void deleteEvent(Event event) {
        if (events.contains(event)) {
            events.remove(event);
            calendarController.setEvents(events);  // Update the events list in the CalendarController
            calendarController.updateCalendarView(calendarController.getLastActiveView()); // Refresh the calendar view
            refreshViews();  // Ensure other views are updated after deletion
        }
    }
}