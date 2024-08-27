package com.example.controller;

import com.example.model.CalendarView;
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
import javafx.scene.control.*;
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
    private Label dateInfoLabel;
    private Button previousButton;
    private Button nextButton;
    private Button addEventButton;
    private LocalDate currentDate;
    private ResourceBundle resources;
    private ObservableList<Event> events;
    private CalendarView lastActiveView = CalendarView.MONTH;
    private MainController mainController;

    /**
     * Initialize the navigation buttons and their actions
     */
    private void initializeButtons() {
        previousButton = new Button("\u2190");  // Left arrow button
        nextButton = new Button("\u2192");      // Right arrow button
        dateInfoLabel = new Label();
        addEventButton = new Button(resources.getString("event.addButton"));

        previousButton.setOnAction(event -> handlePrevious());
        nextButton.setOnAction(event -> handleNext());
        addEventButton.setOnAction(event -> handleAddEvent());
    }

    /**
     * Set up the view selector and bind actions to it
     */
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

        // Layout setup: buttons on the left, date in the center, and ComboBox on the right
        HBox leftControls = new HBox(10);
        leftControls.setAlignment(Pos.CENTER_LEFT);
        leftControls.getChildren().addAll(addEventButton, previousButton, nextButton);

        HBox rightControls = new HBox();
        rightControls.setAlignment(Pos.CENTER_RIGHT);
        rightControls.getChildren().add(viewSelector);

        HBox dateLabelContainer = new HBox();
        dateLabelContainer.setAlignment(Pos.CENTER);
        dateLabelContainer.getChildren().add(dateInfoLabel);
        dateLabelContainer.setPadding(new Insets(0, 10, 0, 10));

        HBox mainControls = new HBox();
        mainControls.getChildren().addAll(leftControls, dateLabelContainer, rightControls);
        HBox.setHgrow(dateLabelContainer, Priority.ALWAYS);
        mainControls.setPadding(new Insets(0, 10, 20, 10));

        StackPane stackPane = new StackPane();
        stackPane.getChildren().addAll(mainControls);

        // Add the stackPane to the top of the BorderPane
        calendarView.setTop(stackPane);
        StackPane.setMargin(dateInfoLabel, new Insets(0, 0, 10, 0));
    }

    /**
     * Update the button labels with the correct translations
     */
    public void updateButtonLabels(ResourceBundle resources) {
        addEventButton.setText(resources.getString("event.addButton"));
        updateDateInfoLabel(resources);
    }

    /**
     * Update the date information label based on the current date
     */
    public void updateDateInfoLabel(ResourceBundle resources) {
        if (currentDate != null) {
            dateInfoLabel.setText(currentDate.getMonth().getDisplayName(TextStyle.FULL, resources.getLocale()) + " " + currentDate.getYear());
        }
    }

    /**
     * Set the calendar view container
     */
    public void setCalendarView(BorderPane calendarView) {
        this.calendarView = calendarView;
    }

    /**
     * Set the current date displayed in the calendar
     */
    public void setCurrentDate(LocalDate currentDate) {
        this.currentDate = currentDate;
    }

    /**
     * Set the list of events to be displayed in the calendar
     */
    public void setEvents(ObservableList<Event> events) {
        this.events = events;
    }

    /**
     * Set the main controller for the application
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Get the last active view (month, week, day)
     */
    public CalendarView getLastActiveView() {
        return lastActiveView;
    }

    /**
     * Handle switching to the previous month/week/day based on the active view
     */
    public void handlePrevious() {
        switch (lastActiveView) {
            case MONTH -> currentDate = currentDate.minusMonths(1);
            case WEEK -> currentDate = currentDate.minusWeeks(1);
            case DAY -> currentDate = currentDate.minusDays(1);
        }
        updateCalendarView(lastActiveView);
    }

    /**
     * Handle switching to the next month/week/day based on the active view
     */
    public void handleNext() {
        switch (lastActiveView) {
            case MONTH -> currentDate = currentDate.plusMonths(1);
            case WEEK -> currentDate = currentDate.plusWeeks(1);
            case DAY -> currentDate = currentDate.plusDays(1);
        }
        updateCalendarView(lastActiveView);
    }

    /**
     * Handle adding a new event by opening the event creation window
     */
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

    /**
     * Update the calendar view (month/week/day)
     */
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

    /**
     * Set the resource bundle for localization
     */
    public void setResources(ResourceBundle resources) {
        this.resources = resources;
    }

    /**
     * Display the month view with days and events
     */
    private void displayMonthView() {
        if (currentDate != null) {
            GridPane gridPane = new GridPane();
            ScrollPane scrollPane = new ScrollPane(gridPane);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setPadding(new Insets(5, 5, 5, 5));

            // Set day of the week headers (starting with Monday)
            for (int i = 0; i < 7; i++) {
                DayOfWeek dayOfWeek = DayOfWeek.of((i) % 7 + 1); // Start with Monday
                Label dayOfWeekLabel = new Label(dayOfWeek.getDisplayName(TextStyle.SHORT, resources.getLocale()));
                dayOfWeekLabel.setStyle("-fx-font-size: 12px; -fx-fill: #333333;");
                dayOfWeekLabel.setMaxWidth(Double.MAX_VALUE);
                dayOfWeekLabel.setAlignment(Pos.CENTER);
                GridPane.setHalignment(dayOfWeekLabel, HPos.CENTER);
                dayOfWeekLabel.setPrefHeight(30);
                gridPane.add(dayOfWeekLabel, i, 0);
            }

            // Set minimum column widths for the days
            double minCellWidth = 100;

            for (int i = 0; i < 7; i++) {
                ColumnConstraints dayColumnConstraints = new ColumnConstraints();
                dayColumnConstraints.setMinWidth(minCellWidth);
                dayColumnConstraints.setHgrow(Priority.ALWAYS);
                gridPane.getColumnConstraints().add(dayColumnConstraints);
            }

            // Set the row height for the day of the week headers
            RowConstraints headerRowConstraints = new RowConstraints();
            headerRowConstraints.setPrefHeight(30);
            headerRowConstraints.setMinHeight(30);
            gridPane.getRowConstraints().add(headerRowConstraints);

            // Reserve space for up to 6 rows of days in the month
            for (int i = 0; i < 6; i++) {
                RowConstraints rowConstraints = new RowConstraints();
                rowConstraints.setVgrow(Priority.ALWAYS); // Allow vertical growth
                gridPane.getRowConstraints().add(rowConstraints);
            }

            // Calculate the starting point and total days in the current month
            LocalDate firstDayOfMonth = currentDate.withDayOfMonth(1);
            int startDayIndex = (firstDayOfMonth.getDayOfWeek().getValue() % 7) - 1; // Adjust index for Monday start
            if (startDayIndex < 0) startDayIndex = 6; // Adjust for Sunday
            int daysInMonth = currentDate.lengthOfMonth();

            int rowIndex = 1;
            int colIndex = startDayIndex;

            // Handle previous month days to fill in the first week
            LocalDate previousMonth = firstDayOfMonth.minusMonths(1);
            int daysInPreviousMonth = previousMonth.lengthOfMonth();

            // Handle next month days to fill in the last week
            LocalDate nextMonth = firstDayOfMonth.plusMonths(1);

            // Fill in the previous month's days
            for (int i = startDayIndex - 1; i >= 0; i--) {
                VBox dayBox = createDayBox(previousMonth.withDayOfMonth(daysInPreviousMonth - (startDayIndex - 1 - i)), true);
                gridPane.add(dayBox, i, rowIndex);
            }

            // Fill in the current month's days
            for (int day = 1; day <= daysInMonth; day++) {
                VBox dayBox = createDayBox(firstDayOfMonth.withDayOfMonth(day), false);
                gridPane.add(dayBox, colIndex, rowIndex);

                colIndex++;
                if (colIndex > 6) { // Move to the next row after filling a week
                    colIndex = 0;
                    rowIndex++;
                }
            }

            // Fill in the next month's days
            int dayOfNextMonth = 1;
            while (colIndex <= 6) {
                VBox dayBox = createDayBox(nextMonth.withDayOfMonth(dayOfNextMonth), true);
                gridPane.add(dayBox, colIndex, rowIndex);
                colIndex++;
                dayOfNextMonth++;
            }

            // Ensure all rows fill the vertical space
            for (int i = gridPane.getRowConstraints().size(); i < 7; i++) {
                RowConstraints rowConstraints = new RowConstraints();
                rowConstraints.setVgrow(Priority.ALWAYS);
                gridPane.getRowConstraints().add(rowConstraints);
            }

            calendarView.setCenter(scrollPane);

            // Update the date info label
            dateInfoLabel.setText(currentDate.getMonth().getDisplayName(TextStyle.FULL, resources.getLocale()) + " " + currentDate.getYear());
        }
    }

    /**
     * Create a VBox for a day, including events
     */
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

    /**
     * Helper method to open the event editing window
     */
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

    /**
     * Format event labels with start time and title
     */
    private String formatEventLabel(Event event) {
        LocalTime startTime = event.getDateTime().toLocalTime();
        String title = event.getTitle();
        return startTime + " " + title;
    }

    /**
     * Display the week or day view
     */
    private void displayView(boolean isDayView) {
        if (currentDate != null) {
            GridPane gridPane = new GridPane();
            ScrollPane scrollPane = new ScrollPane(gridPane);
            scrollPane.setFitToWidth(true);
            scrollPane.setPadding(new Insets(5, 5, 5, 5));

            int columnCount = isDayView ? 1 : 7;

            // Add time column with fixed width
            ColumnConstraints timeColumnConstraints = new ColumnConstraints();
            timeColumnConstraints.setPrefWidth(50);
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
                    dayPane.setStyle("-fx-border-color: #cccccc; -fx-border-width: 0 1 1 0;");
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
                eventLabel.setPrefHeight(eventDuration - 4);
                eventLabel.setAlignment(Pos.TOP_LEFT);
                eventLabel.setPadding(new Insets(2));
                eventLabel.setLayoutY(startMinutes + 2);

                // Add event editing functionality on double-click
                eventLabel.setOnMouseClicked(eventMouseEvent -> {
                    if (eventMouseEvent.getClickCount() == 2 && eventMouseEvent.getButton() == MouseButton.PRIMARY) {
                        openEventWindow(event);
                    }
                });

                // Add event label to the map for this time slot
                List<Label> eventList = eventMap.getOrDefault(key, new ArrayList<>());
                eventList.add(eventLabel);
                eventMap.put(key, eventList);
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

                        for (int i = 0; i < eventList.size(); i++) {
                            Label eventLabel = eventList.get(i);
                            eventLabel.setPrefWidth(eventWidth);
                            eventLabel.setMaxWidth(eventWidth);
                            eventLabel.setMinWidth(eventWidth);
                            eventLabel.setLayoutX(i * (eventWidth + 4) + 2); // Offset event by 2px
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

    /**
     * Helper method to get a specific node from the GridPane
     */
    private Node getNodeFromGridPane(GridPane gridPane, int col, int row) {
        for (Node node : gridPane.getChildren()) {
            if (GridPane.getColumnIndex(node) == col && GridPane.getRowIndex(node) == row) {
                return node;
            }
        }
        return null;
    }

    /**
     * Display the week view
     */
    private void displayWeekView() {
        displayView(false);
    }

    /**
     * Display the day view
     */
    private void displayDayView() {
        displayView(true);
    }
}