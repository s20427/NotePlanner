package com.example.controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;

public class MainController {

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> filterOptions;

    @FXML
    private ListView<String> notesListView;

    @FXML
    private ComboBox<String> viewSelector;

    @FXML
    private BorderPane calendarView;

    @FXML
    private VBox notesPanel;

    @FXML
    private VBox calendarPanel;

    private CalendarController calendarController;

    private LocalDate currentDate;

    @FXML
    private void initialize() {
        filterOptions.setItems(FXCollections.observableArrayList("All", "Notes", "Events"));
        filterOptions.setValue("All");

        notesListView.setItems(FXCollections.observableArrayList(
                "Note 1", "Note 2", "Note 3" // Sample notes
        ));

        currentDate = LocalDate.now(); // Inicjalizacja currentDate
        calendarController = new CalendarController();
        calendarController.setViewSelector(viewSelector);
        calendarController.setCalendarView(calendarView);  // Set calendar view here
        calendarController.setCurrentDate(currentDate); // Ustawienie currentDate w CalendarController
        calendarController.updateCalendarView(viewSelector.getValue());

        // Ustaw proporcje dla notesPanel i calendarPanel
        HBox.setHgrow(notesPanel, Priority.ALWAYS);
        HBox.setHgrow(calendarPanel, Priority.ALWAYS);

        // Ustaw szerokość notesPanel na 30% i calendarPanel na 70%
        notesPanel.setPrefWidth(300); // Ustawienie domyślnej szerokości
        calendarPanel.setPrefWidth(700); // Ustawienie domyślnej szerokości

        // Ustaw marginesy dla notesPanel i calendarPanel
        notesPanel.setPadding(new Insets(10));
        calendarPanel.setPadding(new Insets(10));

        // Ustawienia VBox.vgrow programowo
        VBox.setVgrow(notesListView, Priority.ALWAYS);
        VBox.setVgrow(calendarView, Priority.ALWAYS);
    }

    @FXML
    private void handleSearch() {
        // Implement search functionality
    }

    @FXML
    private void handlePrevious() {
        calendarController.handlePrevious();
    }

    @FXML
    private void handleNext() {
        calendarController.handleNext();
    }
}
