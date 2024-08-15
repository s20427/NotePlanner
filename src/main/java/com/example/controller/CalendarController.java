package com.example.controller;

import com.example.model.Event;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.time.*;
import java.time.format.TextStyle;
import java.util.*;

public class CalendarController {

    private ComboBox<String> viewSelector;
    private BorderPane calendarView;
    private LocalDate currentDate;
    private ResourceBundle resources;
    private ObservableList<Event> events;
    private CalendarView lastActiveView = CalendarView.MONTH;
    private Label dateInfoLabel;
    private Button previousButton;
    private Button nextButton;
    private Button addEventButton;
    private MainController mainController;

    private void initializeButtons() {
        previousButton = new Button("\u2190");  // Change text to left arrow
        nextButton = new Button("\u2192");      // Change text to right arrow
        dateInfoLabel = new Label(); // Upewnij się, że etykieta jest zainicjalizowana
        addEventButton = new Button(resources.getString("event.addButton"));

        previousButton.setOnAction(event -> handlePrevious());
        nextButton.setOnAction(event -> handleNext());
        addEventButton.setOnAction(event -> handleAddEvent());
    }

    public void setViewSelector(ComboBox<String> viewSelector, ResourceBundle resources) {
        this.viewSelector = viewSelector;
        this.resources = resources;

        initializeButtons();

        this.viewSelector.setItems(FXCollections.observableArrayList(
                resources.getString(CalendarView.MONTH.getResourceKey()),
                resources.getString(CalendarView.WEEK.getResourceKey()),
                resources.getString(CalendarView.DAY.getResourceKey())
        ));

        this.viewSelector.setValue(resources.getString(lastActiveView.getResourceKey()));
        this.viewSelector.setOnAction(event -> {
            CalendarView selectedView = CalendarView.fromLocalizedName(this.viewSelector.getValue(), resources);
            if (selectedView != null) {
                lastActiveView = selectedView;
                updateCalendarView(selectedView);
            }
        });

        // Set up the layout with buttons on the left, date in the center, and ComboBox on the right
        HBox leftControls = new HBox(10);
        leftControls.setAlignment(Pos.CENTER_LEFT);
        leftControls.getChildren().addAll(addEventButton, previousButton, nextButton);

        HBox rightControls = new HBox();
        rightControls.setAlignment(Pos.CENTER_RIGHT);
        rightControls.getChildren().add(viewSelector);

        // Main layout for the controls
        HBox mainControls = new HBox();
        mainControls.getChildren().addAll(leftControls, new Region(), rightControls);
        HBox.setHgrow(mainControls.getChildren().get(1), Priority.ALWAYS); // The Region acts as a spacer
        mainControls.setPadding(new Insets(0, 10, 20, 10)); // Add padding around the HBox

        // StackPane to center the dateInfoLabel over the main controls
        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(mainControls, dateInfoLabel);
        StackPane.setAlignment(dateInfoLabel, Pos.CENTER);

        // Add the stackPane to the top of the BorderPane
        calendarView.setTop(stackPane);
        StackPane.setMargin(dateInfoLabel, new Insets(0, 0, 10, 0)); // Add bottom padding to the dateInfoLabel
    }

    public void updateButtonLabels(ResourceBundle resources) {
        addEventButton.setText(resources.getString("event.addButton"));
        updateDateInfoLabel(resources);
    }

    public void updateDateInfoLabel(ResourceBundle resources) {
        if (currentDate != null) {
            dateInfoLabel.setText(currentDate.getMonth().getDisplayName(TextStyle.FULL, resources.getLocale()) + " " + currentDate.getYear());
        }
    }

    public void setCalendarView(BorderPane calendarView) {
        this.calendarView = calendarView;
    }

    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    public void setEvents(ObservableList<Event> events) {
        this.events = events;
    }

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    public CalendarView getLastActiveView() {
        return lastActiveView;
    }

    public void handlePrevious() {
        switch (lastActiveView) {
            case MONTH -> currentDate = currentDate.minusMonths(1);
            case WEEK -> currentDate = currentDate.minusWeeks(1);
            case DAY -> currentDate = currentDate.minusDays(1);
        }
        updateCalendarView(lastActiveView);
    }

    public void handleNext() {
        switch (lastActiveView) {
            case MONTH -> currentDate = currentDate.plusMonths(1);
            case WEEK -> currentDate = currentDate.plusWeeks(1);
            case DAY -> currentDate = currentDate.plusDays(1);
        }
        updateCalendarView(lastActiveView);
    }

    private void handleAddEvent() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fxml/event.fxml"), resources);
            Parent root = loader.load();

            EventController eventController = loader.getController();
            eventController.setMainController(mainController);

            Stage stage = new Stage();
            stage.setTitle(resources.getString("event.title"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(calendarView.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateCalendarView(CalendarView selectedView) {
        if (selectedView == null) {
            throw new IllegalArgumentException("Selected view cannot be null");
        }
        this.lastActiveView = selectedView;
        switch (selectedView) {
            case MONTH -> displayMonthView();
            case WEEK -> displayWeekView();
            case DAY -> displayDayView();
        }
    }

    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    private void displayMonthView() {
        if (currentDate != null) {
            GridPane gridPane = new GridPane();
            ScrollPane scrollPane = new ScrollPane(gridPane);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setPadding(new Insets(5, 5, 5, 5));

            // Ustawienie nagłówków dni tygodnia (od poniedziałku)
            for (int i = 0; i < 7; i++) {
                DayOfWeek dayOfWeek = DayOfWeek.of((i) % 7 + 1); // Start with Monday
                Label dayOfWeekLabel = new Label(dayOfWeek.getDisplayName(TextStyle.SHORT, resources.getLocale()));
                dayOfWeekLabel.setStyle("-fx-font-size: 12px; -fx-fill: #333333;");
                dayOfWeekLabel.setMaxWidth(Double.MAX_VALUE);
                dayOfWeekLabel.setAlignment(Pos.CENTER);
                GridPane.setHalignment(dayOfWeekLabel, HPos.CENTER);
                dayOfWeekLabel.setPrefHeight(30); // Set the height of the day name row
                gridPane.add(dayOfWeekLabel, i, 0);
            }

            // Ustawienie minimalnych rozmiarów kolumn
            double minCellWidth = 100;  // Minimalna szerokość komórki

            for (int i = 0; i < 7; i++) {
                ColumnConstraints dayColumnConstraints = new ColumnConstraints();
                dayColumnConstraints.setMinWidth(minCellWidth);
                dayColumnConstraints.setHgrow(Priority.ALWAYS);
                gridPane.getColumnConstraints().add(dayColumnConstraints);
            }

            // Ograniczenie wysokości wiersza z nagłówkami dni tygodnia
            RowConstraints headerRowConstraints = new RowConstraints();
            headerRowConstraints.setPrefHeight(30); // Ustawienie wysokości wiersza z nazwami dni
            headerRowConstraints.setMinHeight(30);
            gridPane.getRowConstraints().add(headerRowConstraints);

            // Ustawienie rozmiarów dla reszty wierszy (dni miesiąca)
            for (int i = 0; i < 6; i++) { // Zarezerwowanie miejsca na maksymalnie 6 wierszy na miesiąc
                RowConstraints rowConstraints = new RowConstraints();
                rowConstraints.setVgrow(Priority.ALWAYS); // Ustawienie dynamicznego rozciągania w pionie
                gridPane.getRowConstraints().add(rowConstraints);
            }

            // Tworzenie dni miesiąca
            LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
            int startDayIndex = (firstDayOfMonth.getDayOfWeek().getValue() % 7) - 1; // Poniedziałek to 0, Niedziela to 6
            if (startDayIndex < 0) startDayIndex = 6; // Jeśli wynik jest ujemny, ustawiamy na 6 (niedziela)
            int daysInMonth = currentDate.lengthOfMonth();

            int rowIndex = 1;
            int colIndex = startDayIndex;

            // Poprzedni miesiąc
            LocalDate previousMonth = firstDayOfMonth.minusMonths(1);
            int daysInPreviousMonth = previousMonth.lengthOfMonth();

            // Następny miesiąc
            LocalDate nextMonth = firstDayOfMonth.plusMonths(1);

            // Dodaj dni poprzedniego miesiąca, aby wypełnić pierwszy tydzień
            for (int i = startDayIndex - 1; i >= 0; i--) {
                VBox dayBox = createDayBox(previousMonth.withDayOfMonth(daysInPreviousMonth - (startDayIndex - 1 - i)), true);
                gridPane.add(dayBox, i, rowIndex);
            }

            // Dodaj dni bieżącego miesiąca
            for (int day = 1; day <= daysInMonth; day++) {
                VBox dayBox = createDayBox(firstDayOfMonth.withDayOfMonth(day), false);
                gridPane.add(dayBox, colIndex, rowIndex);

                colIndex++;
                if (colIndex > 6) { // Jeśli dojdziemy do końca tygodnia, przechodzimy do następnego wiersza
                    colIndex = 0;
                    rowIndex++;
                }
            }

            // Dodaj dni następnego miesiąca, aby wypełnić ostatni tydzień
            int dayOfNextMonth = 1;
            while (colIndex <= 6) {
                VBox dayBox = createDayBox(nextMonth.withDayOfMonth(dayOfNextMonth), true);
                gridPane.add(dayBox, colIndex, rowIndex);
                colIndex++;
                dayOfNextMonth++;
            }

            // Upewnij się, że wszystkie wiersze wypełniają pionową przestrzeń
            for (int i = gridPane.getRowConstraints().size(); i < 7; i++) {
                RowConstraints rowConstraints = new RowConstraints();
                rowConstraints.setVgrow(Priority.ALWAYS); // Dynamiczne rozciąganie w pionie
                gridPane.getRowConstraints().add(rowConstraints);
            }

            calendarView.setCenter(scrollPane);

            // Przywracanie dateInfoLabel
            dateInfoLabel.setText(currentDate.getMonth().getDisplayName(TextStyle.FULL, resources.getLocale()) + " " + currentDate.getYear());
        }
    }

    private VBox createDayBox(LocalDate date, boolean isAdjacentMonth) {
        VBox dayBox = new VBox();
        dayBox.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 1 1 0;");
        dayBox.setPadding(new Insets(2));
        dayBox.setFillWidth(true);

        Label dayLabel = new Label(String.valueOf(date.getDayOfMonth()));
        dayLabel.setStyle(isAdjacentMonth ? "-fx-font-size: 12px; -fx-text-fill: #aaaaaa;" : "-fx-font-size: 12px; -fx-text-fill: #333333;");
        dayLabel.setMaxWidth(Double.MAX_VALUE);
        dayLabel.setAlignment(Pos.TOP_RIGHT);

        dayBox.getChildren().add(dayLabel);

        // Add events to the corresponding days
        List<Event> eventsForDay = events.stream()
                .filter(event -> event.getDateTime().toLocalDate().equals(date))
                .sorted(Comparator.comparing(Event::getDateTime))
                .toList();

        for (Event event : eventsForDay) {
            Label eventLabel = new Label(formatEventLabel(event));
            eventLabel.setStyle(
                    "-fx-background-color: " + event.getCategory().getColor() + "; " +
                            "-fx-text-fill: white; " +
                            "-fx-padding: 2px; " +
                            "-fx-border-radius: 4px; " +
                            "-fx-background-radius: 4px; " +
                            "-fx-border-color: #000000; " +
                            "-fx-border-width: 1px;"
            );
            eventLabel.setMaxWidth(Double.MAX_VALUE);
            eventLabel.setWrapText(false);
            eventLabel.setEllipsisString("...");

            // Add double-click event listener to open the event for editing
            eventLabel.setOnMouseClicked(e -> {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    openEventWindow(event);
                }
            });

            dayBox.getChildren().add(eventLabel);
        }

        return dayBox;
    }

    // Helper method to open the event editing window
    private void openEventWindow(Event event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/fxml/event.fxml"), resources);
            Parent root = loader.load();

            EventController eventController = loader.getController();
            eventController.setMainController(mainController);
            eventController.setEventContent(event);
            eventController.setEditMode(true);

            Stage stage = new Stage();
            stage.setTitle(resources.getString("event.editTitle"));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(calendarView.getScene().getWindow());
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private String formatEventLabel(Event event) {
        LocalTime startTime = event.getDateTime().toLocalTime();
        String title = event.getTitle();
        return startTime + " " + title;
    }

    private void displayView(boolean isDayView) {
        if (currentDate != null) {
            GridPane gridPane = new GridPane();
            ScrollPane scrollPane = new ScrollPane(gridPane);
            scrollPane.setFitToWidth(true);
            scrollPane.setPadding(new Insets(5, 5, 5, 5));

            int columnCount = isDayView ? 1 : 7;

            // Add time column with fixed width
            ColumnConstraints timeColumnConstraints = new ColumnConstraints();
            timeColumnConstraints.setPrefWidth(50); // Width of the time column
            timeColumnConstraints.setMinWidth(50);
            gridPane.getColumnConstraints().add(timeColumnConstraints);

            // Add day columns with evenly distributed width
            for (int i = 0; i < columnCount; i++) {
                ColumnConstraints dayColumnConstraints = new ColumnConstraints();
                dayColumnConstraints.setHgrow(Priority.ALWAYS);
                dayColumnConstraints.setFillWidth(true);
                dayColumnConstraints.setMinWidth(100);
                gridPane.getColumnConstraints().add(dayColumnConstraints);
            }

            // Create header with day names or just the day (for day view)
            for (int i = 0; i < columnCount; i++) {
                LocalDate dayDate = isDayView ? currentDate : currentDate.with(DayOfWeek.MONDAY).plusDays(i);
                Label dayLabel = new Label(dayDate.getDayOfWeek().getDisplayName(TextStyle.SHORT, resources.getLocale()) + " " + dayDate.getDayOfMonth());
                dayLabel.setStyle("-fx-font-size: 14px; -fx-fill: #333333;");
                dayLabel.setMaxWidth(Double.MAX_VALUE);
                dayLabel.setAlignment(Pos.CENTER);
                GridPane.setHalignment(dayLabel, HPos.CENTER);
                gridPane.add(dayLabel, i + 1, 0); // +1 because column 0 is the time column
            }

            // Create the time column and the grid for the week or day
            for (int hour = 0; hour < 24; hour++) {
                Label hourLabel = new Label(hour + ":00");
                hourLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #333333;");
                hourLabel.setPrefHeight(60);  // Each hour equals 60 minutes
                hourLabel.setMinHeight(60);
                hourLabel.setMaxWidth(Double.MAX_VALUE);
                hourLabel.setAlignment(Pos.TOP_CENTER);
                GridPane.setHalignment(hourLabel, HPos.CENTER);
                gridPane.add(hourLabel, 0, hour + 1); // Column 0, rows 1-24

                // Create day cells for the grid
                for (int i = 0; i < columnCount; i++) {
                    Pane dayPane = new Pane(); // Pane for manually positioning events
                    dayPane.setPrefHeight(60); // Each cell is 60 minutes
                    dayPane.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 1 1 0;"); // Bottom and right borders
                    gridPane.add(dayPane, i + 1, hour + 1); // Columns 1-7 (or 1 for day view), rows 1-24
                }
            }

            Map<String, List<Label>> eventMap = new HashMap<>();

            // Add events to the grid
            for (Event event : events) {
                LocalDateTime eventStartDateTime = event.getDateTime();
                LocalDateTime eventEndDateTime = event.getEndDateTime();
                LocalDate eventDate = eventStartDateTime.toLocalDate();

                // Filter out events not within the current week or day
                if (!isDayView && (eventDate.isBefore(currentDate.with(DayOfWeek.MONDAY)) || eventDate.isAfter(currentDate.with(DayOfWeek.SUNDAY)))) {
                    continue; // Skip events outside the current week
                } else if (isDayView && !eventDate.equals(currentDate)) {
                    continue; // Skip events outside the current day
                }

                int dayOfWeekIndex = isDayView ? 0 : eventDate.getDayOfWeek().getValue() - 1;
                int startHour = eventStartDateTime.getHour();
                int startMinutes = eventStartDateTime.getMinute();

                int endHour = eventEndDateTime.getHour();
                int endMinutes = eventEndDateTime.getMinute();
                int eventDuration = (endHour * 60 + endMinutes) - (startHour * 60 + startMinutes);

                String key = dayOfWeekIndex + "_" + startHour;

                // Create a label for the event and style it
                Label eventLabel = new Label(event.getTitle());
                eventLabel.setStyle(
                        "-fx-background-color: " + event.getCategory().getColor() + "; " +
                                "-fx-text-fill: white; " +
                                "-fx-padding: 2px; " +
                                "-fx-border-radius: 4px; " +
                                "-fx-background-radius: 4px; " +
                                "-fx-border-color: #000000; " +
                                "-fx-border-width: 1px;"
                );
                eventLabel.setPrefHeight(eventDuration - 4);  // Subtract 4px for padding
                eventLabel.setAlignment(Pos.TOP_LEFT);
                eventLabel.setPadding(new Insets(2)); // Add 2px padding
                eventLabel.setLayoutY(startMinutes + 2); // Offset event label by 2px

                // Add event editing functionality on double-click
                eventLabel.setOnMouseClicked(eventMouseEvent -> {
                    if (eventMouseEvent.getClickCount() == 2 && eventMouseEvent.getButton() == MouseButton.PRIMARY) {
                        openEventWindow(event); // Open event editing window
                    }
                });

                // Add event label to the map for this time slot
                List<Label> eventList = eventMap.getOrDefault(key, new ArrayList<>());
                eventList.add(eventLabel);
                eventMap.put(key, eventList);

                System.out.println("Added event: " + event.getTitle() + " to key: " + key + " with duration: " + eventDuration);
            }

            // Add events to the grid and dynamically adjust their width
            for (Map.Entry<String, List<Label>> entry : eventMap.entrySet()) {
                String[] parts = entry.getKey().split("_");
                int dayOfWeekIndex = Integer.parseInt(parts[0]);
                int hour = Integer.parseInt(parts[1]);
                List<Label> eventList = entry.getValue();

                Pane dayPane = (Pane) getNodeFromGridPane(gridPane, dayOfWeekIndex + 1, hour + 1);
                if (dayPane != null) {
                    dayPane.widthProperty().addListener((obs, oldWidth, newWidth) -> {
                        double totalWidth = newWidth.doubleValue();

                        if (totalWidth <= 0) {
                            totalWidth = 100; // Set default width if necessary
                        }

                        double eventWidth = totalWidth / eventList.size() - 4; // Subtract 4px for padding
                        System.out.println("Total column width: " + totalWidth + ", Event Width: " + eventWidth); // Debugging

                        for (int i = 0; i < eventList.size(); i++) {
                            Label eventLabel = eventList.get(i);
                            eventLabel.setPrefWidth(eventWidth);
                            eventLabel.setMaxWidth(eventWidth);
                            eventLabel.setMinWidth(eventWidth);
                            eventLabel.setLayoutX(i * (eventWidth + 4) + 2); // Offset event by 2px

                            System.out.println("Event " + eventLabel.getText() + " positioned at X: " + i * (eventWidth + 4) + 2);
                        }
                    });

                    // Add event labels to the day pane
                    for (Label eventLabel : eventList) {
                        dayPane.getChildren().add(eventLabel);
                    }
                }
            }

            // Set the grid inside the calendar view
            calendarView.setCenter(scrollPane);
            dateInfoLabel.setText(currentDate.getMonth().getDisplayName(TextStyle.FULL, resources.getLocale()) + " " + currentDate.getYear());
        }
    }

    // Helper method to get a specific node from the GridPane
    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    private void displayWeekView() {
        displayView(false);
    }

    private void displayDayView() {
        displayView(true);
    }
}